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


package org.apache.isis.webapp;

import org.apache.log4j.Logger;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.webapp.context.RequestContext;


public class UserManager {

    private static final Logger LOG = Logger.getLogger(UserManager.class);
    private static UserManager instance;

    private static AuthenticationManager getAuthenticationManager() {
        if (instance == null) {
            throw new IllegalStateException("Server initialisation failed, or not defined as a context listener");
        }
        return instance.authenticationManager;
    }

    public static void startRequest(RequestContext context) {
        AuthenticationSession session = context.getSession();
        if (session == null) {
            session = new UserlessSession();
        }
        LOG.info("start request: " + session);
        IsisContext.closeSession();
        IsisContext.openSession(session);
    }

    public static AuthenticationSession authenticate(AuthenticationRequestPassword passwordAuthenticationRequest) {
        AuthenticationSession session = getAuthenticationManager().authenticate(passwordAuthenticationRequest);
        if (session != null)  {
            LOG.info("log on user " + session.getUserName());
            IsisContext.closeSession();
            IsisContext.openSession(session);
        }
        return session;
    }

    public static void endRequest(final AuthenticationSession session) {
        LOG.info("end request: " + session);
        IsisContext.closeSession();
    }

    public static void logoffUser(final AuthenticationSession session) {
        LOG.info("log off user " + session.getUserName());
        IsisContext.closeSession();
        getAuthenticationManager().closeSession(session);
        
        UserlessSession replacementSession = new UserlessSession();
        IsisContext.openSession(replacementSession);
    }

    private final AuthenticationManager authenticationManager;

    public UserManager(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        UserManager.instance = this;
    }
}

