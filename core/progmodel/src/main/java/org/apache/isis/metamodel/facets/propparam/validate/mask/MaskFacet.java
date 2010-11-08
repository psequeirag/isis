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

package org.apache.isis.metamodel.facets.propparam.validate.mask;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.SingleStringValueFacet;
import org.apache.isis.metamodel.facets.propparam.validate.regex.RegExFacet;
import org.apache.isis.metamodel.interactions.ValidatingInteractionAdvisor;

/**
 * Whether the (string) property or a parameter must correspond to a specific mask.
 * 
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to the <tt>@Mask</tt> annotation.
 * 
 * <p>
 * TODO: not yet implemented by the framework or any viewer.
 * 
 * @see RegExFacet
 */
public interface MaskFacet extends SingleStringValueFacet, ValidatingInteractionAdvisor {

    /**
     * Whether the provided string matches the mask.
     */
    public boolean doesNotMatch(ObjectAdapter adapter);
}
