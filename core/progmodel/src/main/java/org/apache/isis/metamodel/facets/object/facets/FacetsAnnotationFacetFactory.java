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


package org.apache.isis.metamodel.facets.object.facets;

import org.apache.isis.applib.annotation.Facets;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.FacetUtil;
import org.apache.isis.metamodel.facets.MethodRemover;
import org.apache.isis.metamodel.java5.AnnotationBasedFacetFactoryAbstract;


public class FacetsAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public FacetsAnnotationFacetFactory() {
        super(ObjectFeatureType.OBJECTS_ONLY);
    }

    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        final Facets annotation = (Facets) getAnnotation(cls, Facets.class);
        return FacetUtil.addFacet(create(annotation, holder));
    }

    /**
     * Returns a {@link FacetsFacet} impl provided that at least one valid
     * {@link FacetsFacet#facetFactories() factory} was specified.
     */
    private FacetsFacet create(final Facets annotation, final FacetHolder holder) {
        if (annotation == null) {
            return null;
        }
        final FacetsFacetAnnotation facetsFacetAnnotation = new FacetsFacetAnnotation(annotation, holder);
        return facetsFacetAnnotation.facetFactories().length > 0 ? facetsFacetAnnotation : null;
    }

}
