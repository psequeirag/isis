/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.remoting.client.persistence;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.remoting.client.transaction.ClientSideTransaction;
import org.apache.isis.remoting.client.transaction.ClientTransactionEvent;
import org.apache.isis.remoting.data.common.ObjectData;
import org.apache.isis.remoting.data.common.ReferenceData;
import org.apache.isis.remoting.exchange.ExecuteClientActionRequest;
import org.apache.isis.remoting.exchange.ExecuteClientActionResponse;
import org.apache.isis.remoting.exchange.KnownObjectsRequest;
import org.apache.isis.remoting.facade.ServerFacade;
import org.apache.isis.remoting.protocol.ObjectEncoderDecoder;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.ConcurrencyException;
import org.apache.isis.runtime.persistence.PersistenceSessionTransactionManagement;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManagerProxy;
import org.apache.isis.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtime.transaction.IsisTransactionManagerAbstract;
import org.apache.isis.runtime.transaction.messagebroker.MessageBroker;
import org.apache.isis.runtime.transaction.updatenotifier.UpdateNotifier;
import org.apache.log4j.Logger;

public class ClientSideTransactionManager extends IsisTransactionManagerAbstract<ClientSideTransaction> {

    final static Logger LOG = Logger.getLogger(ClientSideTransactionManager.class);

    private final AdapterManagerProxy adapterManager;
    private final PersistenceSessionTransactionManagement transactionManagement;
    private final ServerFacade connection;
    private final ObjectEncoderDecoder encoder;
    
    ////////////////////////////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////////////////////////////

    public ClientSideTransactionManager(
            final AdapterManagerProxy adapterManager, 
            final PersistenceSessionTransactionManagement transactionManagement, 
            final ServerFacade connection, 
            final ObjectEncoderDecoder encoder) {
        this.adapterManager = adapterManager;
        this.transactionManagement = transactionManagement;
        this.connection = connection;
        this.encoder = encoder;
    }

    

    ////////////////////////////////////////////////////////////////
    // start, addCommand, flush, end
    ////////////////////////////////////////////////////////////////
    
    public void startTransaction() {
        ensureTransactionNotInProgress();
        if (LOG.isDebugEnabled()) {
            LOG.debug("startTransaction");
        }
        
        // just in case...?
        transactionManagement.clearAllDirty();
        
        createTransaction();
    }

    
    /**
     * Overridable hook.
     * 
     * <p>
     * The provided {@link MessageBroker} and {@link UpdateNotifier} are obtained from
     * the hook methods ({@link #createMessageBroker()} and {@link #createUpdateNotifier()}).
     * 
     * @see #createMessageBroker()
     * @see #createUpdateNotifier()
     */
    protected ClientSideTransaction createTransaction(MessageBroker messageBroker, UpdateNotifier updateNotifier) {
        return new ClientSideTransaction(this, messageBroker, updateNotifier);
    }

    public void addCommand(final PersistenceCommand command) {
        // does nothing
    }

    public boolean flushTransaction() {
        return false;
    }


    public void endTransaction() {
        ensureTransactionInProgress();
        if (LOG.isDebugEnabled()) {
            LOG.debug("endTransaction");
        }

        if (getTransaction().isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("  no transaction commands to process");
            }
        } else {
            endNonEmptyTransaction();
        }

        getTransaction().commit();
    }


    private void endNonEmptyTransaction() {
        final KnownObjectsRequest knownObjects = new KnownObjectsRequest();

        final ClientTransactionEvent[] transactionEntries = getTransaction().getEntries();
        final ReferenceData[] referenceData = asData(transactionEntries, knownObjects);
        
        final int[] eventTypes = asEventTypes(transactionEntries);

        ExecuteClientActionResponse results;
        try {
            ExecuteClientActionRequest request = new ExecuteClientActionRequest(getAuthenticationSession(), referenceData, eventTypes);
			results = connection.executeClientAction(request);
        } catch (final ConcurrencyException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("concurrency conflict: " + e.getMessage());
            }
            final Oid oid = e.getSource();
            if (oid == null) {
                throw e;
            } else {
                final ObjectAdapter failedObject = transactionManagement.reload(oid);
                throw new ConcurrencyException("Object automatically reloaded: "
                        + failedObject.getSpecification().getTitle(failedObject), e);
            }
        }

        if (results != null) {
            handleResults(transactionEntries, results);
        }
    }

    private int[] asEventTypes(
            final ClientTransactionEvent[] entries ) {
        
        final int numberOfEvents = entries.length;
        final int[] types = new int[numberOfEvents];

        for (int i = 0; i < numberOfEvents; i++) {
            types[i] = entries[i].getType();
        }
        return types;
    }

    private ReferenceData[] asData(
            final ClientTransactionEvent[] entries,
            final KnownObjectsRequest knownObjects ) {
        
        final int numberOfEvents = entries.length;
        final ReferenceData[] data = new ReferenceData[numberOfEvents];

        for (int i = 0; i < numberOfEvents; i++) {
            switch (entries[i].getType()) {
            case ClientTransactionEvent.ADD:
                data[i] = encoder.encodeMakePersistentGraph(entries[i].getObject(), knownObjects);
                break;
            case ClientTransactionEvent.CHANGE:
                data[i] = encoder.encodeGraphForChangedObject(entries[i].getObject(), knownObjects);
                break;
            case ClientTransactionEvent.DELETE:
                data[i] = encoder.encodeIdentityData(entries[i].getObject());
                break;
            }
        }
        return data;
    }

    
    private void handleResults(
            final ClientTransactionEvent[] entries,
            final ExecuteClientActionResponse results) {
        
        final int numberOfEvents = entries.length;

        final int[] eventTypes = asEventTypes(entries);

        final ReferenceData[] persistedUpdates = results.getPersisted();
        final Version[] changedVersions = results.getChanged();
        
        for (int i = 0; i < numberOfEvents; i++) {
            switch (eventTypes[i]) {
                case ClientTransactionEvent.ADD:
                    // causes OID to be updated
                    final ReferenceData update = persistedUpdates[i];
                    
                    Oid updatedOid = update.getOid();
                    adapterManager.remapUpdated(updatedOid);
                    final ObjectAdapter adapter = adapterManager.getAdapterFor(updatedOid);
                    
                    adapter.changeState(ResolveState.RESOLVED);
                    entries[i].getObject().setOptimisticLock(update.getVersion());
    
                    break;
                case ClientTransactionEvent.CHANGE:
                    entries[i].getObject().setOptimisticLock(changedVersions[i]);
                    getUpdateNotifier().addChangedObject(entries[i].getObject());
                    break;
                }
        }

        final ObjectData[] updates = results.getUpdates();
        for (int i = 0; i < updates.length; i++) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("update " + updates[i].getOid());
            }
            encoder.decode(updates[i]);
        }

        for (int i = 0; i < numberOfEvents; i++) {
            switch (eventTypes[i]) {
            case ClientTransactionEvent.DELETE:
                getUpdateNotifier().addDisposedObject(entries[i].getObject());
                break;
            }
        }
    }


    public void abortTransaction() {
        ensureTransactionInProgress();
        if (LOG.isDebugEnabled()) {
            LOG.debug("abortTransaction");
        }
        getTransaction().abort();
    }


    
    //////////////////////////////////////////////////////////////////
    // Not public API 
    //////////////////////////////////////////////////////////////////

    public void addMakePersistent(ObjectAdapter object) {
        ensureTransactionInProgress();

        getTransaction().addMakePersistent(object);
    }

    public void addObjectChanged(ObjectAdapter object) {
        ensureTransactionInProgress();

        getTransaction().addObjectChanged(object);
    }

    public void addDestroyObject(ObjectAdapter object) {
        ensureTransactionInProgress();

        getTransaction().addDestroyObject(object);    
    }



    //////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    //////////////////////////////////////////////////////////////////
    
    private AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }


}


