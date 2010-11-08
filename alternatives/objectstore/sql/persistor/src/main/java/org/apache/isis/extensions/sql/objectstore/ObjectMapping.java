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


package org.apache.isis.extensions.sql.objectstore;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtime.persistence.query.PersistenceQueryFindByPattern;


public interface ObjectMapping {
    void createObject(DatabaseConnector connector, ObjectAdapter object);

    void destroyObject(DatabaseConnector connector, ObjectAdapter object);

    ObjectAdapter[] getInstances(DatabaseConnector connector, ObjectSpecification spec);

    ObjectAdapter[] getInstances(DatabaseConnector connector, ObjectSpecification spec, String title);

    ObjectAdapter[] getInstances(DatabaseConnector connector, ObjectSpecification spec, PersistenceQueryFindByPattern query);

    ObjectAdapter getObject(DatabaseConnector connector, Oid oid, ObjectSpecification hint);

    boolean hasInstances(DatabaseConnector connector, ObjectSpecification cls);

    void resolve(DatabaseConnector connector, ObjectAdapter object);

    void resolveCollection(DatabaseConnector connector, ObjectAdapter object, ObjectAssociation field);

    void save(DatabaseConnector connector, ObjectAdapter object);

    void shutdown();
    
    void startup(DatabaseConnector connection, ObjectMappingLookup objectMapperLookup);

}
