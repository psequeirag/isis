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

package org.apache.isis.metamodel.facets.properties.modify;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.Facet;

/**
 * Mechanism for clearing a property of an object (that is, setting it to <tt>null</tt>).
 * 
 * <p>
 * In the standard Apache Isis Programming Model, typically corresponds to a method named <tt>clearXxx</tt> (for a
 * property <tt>getXxx</tt>). As a fallback the standard model also supports invoking the <tt>setXxx</tt> method with
 * <tt>null</tt>.
 */
public interface PropertyClearFacet extends Facet {

    public void clearProperty(ObjectAdapter inObject);
}
