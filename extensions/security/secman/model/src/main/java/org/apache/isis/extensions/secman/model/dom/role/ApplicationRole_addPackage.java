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
package org.apache.isis.extensions.secman.model.dom.role;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureType;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRole.AddPackageDomainEvent;

import lombok.RequiredArgsConstructor;

@Action(domainEvent = AddPackageDomainEvent.class, associateWith = "permissions")
@RequiredArgsConstructor
public class ApplicationRole_addPackage {
    
    @Inject private ApplicationFeatureRepository applicationFeatureRepository;
    @Inject private ApplicationPermissionRepository<? extends ApplicationPermission> applicationPermissionRepository;
    
    private final ApplicationRole holder;
    
    /**
     * Adds a {@link org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission permission} for this role to a
     * {@link ApplicationFeatureType#PACKAGE package}
     * {@link ApplicationFeature feature}.
     */
    @MemberOrder(sequence = "1")
    public ApplicationRole act(
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            @ParameterLayout(named="Mode")
            final ApplicationPermissionMode mode,
            @ParameterLayout(named="Package", typicalLength= ApplicationFeature.TYPICAL_LENGTH_PKG_FQN)
            final String packageFqn) {
        
        applicationPermissionRepository
            .newPermission(
                    holder, rule, mode, ApplicationFeatureType.PACKAGE, packageFqn);
        return holder;
    }

    @Model
    public ApplicationPermissionRule default0Act() {
        return ApplicationPermissionRule.ALLOW;
    }

    @Model
    public ApplicationPermissionMode default1Act() {
        return ApplicationPermissionMode.CHANGING;
    }

    @Model
    public java.util.Collection<String> choices2Act() {
        return applicationFeatureRepository.packageNames();
    }

}
