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
package org.apache.isis.jdo.datanucleus.persistence.queries;

import java.util.List;

import javax.jdo.listener.InstanceLifecycleEvent;

import org.datanucleus.enhancement.Persistable;

import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.jdo.jdosupport.IsisJdoSupport_v3_2;
import org.apache.isis.jdo.persistence.IsisLifecycleListener;
import org.apache.isis.jdo.persistence.PersistenceSession5;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.persistence.PersistenceQuery;

public abstract class PersistenceQueryProcessorAbstract<T extends PersistenceQuery>
implements PersistenceQueryProcessor<T> {


    final PersistenceSession5 persistenceSession;

    protected PersistenceQueryProcessorAbstract(final PersistenceSession5 persistenceSession) {
        this.persistenceSession = persistenceSession;
    }


    /**
     * Traversing the provided list causes (or should cause) the
     * {@link IsisLifecycleListener#postLoad(InstanceLifecycleEvent) {
     * to be called.
     */
    protected List<ObjectAdapter> loadAdapters(final List<?> pojos) {
        final List<ObjectAdapter> adapters = _Lists.newArrayList();
        for (final Object pojo : pojos) {
            // ought not to be necessary, however for some queries it seems that the
            // lifecycle listener is not called
            ObjectAdapter adapter;
            if(pojo instanceof Persistable) {
                // an entity
                adapter = persistenceSession.initializeMapAndCheckConcurrency((Persistable) pojo);
                _Assert.assertNotNull(adapter);
            } else {
                // a value type
                adapter = persistenceSession.adapterFor(pojo);
                _Assert.assertNotNull(adapter);
            }
            adapters.add(adapter);
        }
        return adapters;
    }

    // -- HELPER

    protected static IsisJdoSupport_v3_2 isisJdoSupport() { 
        return IsisContext.getServiceRegistry().lookupServiceElseFail(IsisJdoSupport_v3_2.class);
    }


}