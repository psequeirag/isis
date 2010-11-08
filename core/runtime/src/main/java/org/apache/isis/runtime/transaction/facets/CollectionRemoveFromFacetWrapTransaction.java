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


package org.apache.isis.runtime.transaction.facets;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.DecoratingFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionRemoveFromFacetAbstract;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.transaction.IsisTransactionManager;
import org.apache.isis.runtime.transaction.TransactionalClosureAbstract;


public class CollectionRemoveFromFacetWrapTransaction extends CollectionRemoveFromFacetAbstract implements
        DecoratingFacet<CollectionRemoveFromFacet> {

    private final CollectionRemoveFromFacet underlyingFacet;

    public CollectionRemoveFromFacet getDecoratedFacet() {
        return underlyingFacet;
    }

    public CollectionRemoveFromFacetWrapTransaction(final CollectionRemoveFromFacet underlyingFacet) {
        super(underlyingFacet.getFacetHolder());
        this.underlyingFacet = underlyingFacet;
    }

    public void remove(final ObjectAdapter adapter, final ObjectAdapter referencedAdapter) {
        if (adapter.isTransient()) {
        	// NOT !adapter.isPersistent();
        	// (value adapters are neither persistent or transient) 
            underlyingFacet.remove(adapter, referencedAdapter);
        } else {
        	getTransactionManager().executeWithinTransaction(
    			new TransactionalClosureAbstract(){
    				public void execute() {
    					underlyingFacet.remove(adapter, referencedAdapter);
    				}});
        }
    }

    @Override
    public String toString() {
        return super.toString() + " --> " + underlyingFacet.toString();
    }

    
    /////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    /////////////////////////////////////////////////////////////////
    
    private static IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }



}

