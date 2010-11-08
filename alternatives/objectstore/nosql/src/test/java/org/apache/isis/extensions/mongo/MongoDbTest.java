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


package org.apache.isis.extensions.mongo;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.extensions.nosql.ExampleValuePojo;
import org.apache.isis.extensions.nosql.SerialKeyCreator;
import org.apache.isis.extensions.nosql.TrialObjects;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SerialOid;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class MongoDbTest {
    private MongoDb db;
    private TrialObjects testObjects;
    private ObjectSpecification specification;
    private ObjectAdapter object;
    private SerialOid oid;
    private DB testDb;
    
    @Before
    public void setup() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        testObjects = new TrialObjects();
        

        Mongo m = new Mongo();
        m.dropDatabase("testdb");
        testDb = m.getDB("testdb");
        
        db = new MongoDb("localhost", 0, "testdb", new SerialKeyCreator());
        db.open();
        
        ExampleValuePojo pojo = new ExampleValuePojo();
        pojo.setName("Fred Smith");
        oid = SerialOid.createTransient(3);
        object = testObjects.createAdapter(pojo, oid);
        specification = object.getSpecification();

    }

    @Test
    public void newDatabaseContainsNothing() throws Exception {
        assertFalse(db.containsData());
    }
    
    @Test
    public void serialNumberSaved() throws Exception {
        assertEquals(0, db.readSerialNumber());
       db.writeSerialNumber(1023);
       assertEquals(1023, db.readSerialNumber());
    }

    @Test
    public void hasInstances() throws Exception {
        String specificationName = specification.getFullName();
        assertFalse(db.hasInstances(specificationName));
        db.close();
        
        
        DBCollection instances = testDb.getCollection(specificationName);
        instances.insert(new BasicDBObject().append("test", "test"));
        
        db.open();
        assertTrue(db.hasInstances(specificationName));
        assertFalse(db.hasInstances("org.xxx.unknown"));
    }

    @Test
    public void destroyInstance() throws Exception {
        db.close();

        String specificationName = specification.getFullName();      
        DBCollection instances = testDb.getCollection(specificationName);
        BasicDBObject dbObject = new BasicDBObject().append("test", "test");
        instances.insert(dbObject);
        
        db.open();
        db.delete(specificationName, dbObject.getString("_id"));
        assertFalse(db.hasInstances(specificationName));
    }
    
    @Test
    public void serviceIds() throws Exception {
        db.addService("one", "123");
        assertEquals("123", db.getService("one"));
    }
    
    @Test
    public void unknownServiceIds() throws Exception {
        assertNull(db.getService("two"));
    }
}


