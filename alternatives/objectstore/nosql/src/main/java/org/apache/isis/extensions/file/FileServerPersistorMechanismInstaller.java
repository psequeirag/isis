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


package org.apache.isis.extensions.file;

import org.apache.isis.core.metamodel.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.extensions.nosql.NoSqlDataDatabase;
import org.apache.isis.extensions.nosql.NoSqlPersistorMechanismInstaller;

public class FileServerPersistorMechanismInstaller extends NoSqlPersistorMechanismInstaller {
    
    private static final String ROOT = ConfigurationConstants.ROOT + "nosql.fileserver.";
    private static final String DB_HOST = ROOT + "host";
    private static final String DB_PORT = ROOT + "port";
    private static final String DB_TIMEMOUT = ROOT + "timeout";

    public FileServerPersistorMechanismInstaller() {
        super("fileserver");
    }

    protected NoSqlDataDatabase createNoSqlDatabase(IsisConfiguration configuration) {
        NoSqlDataDatabase db;
        String host = configuration.getString(DB_HOST, "localhost");
        int port = configuration.getInteger(DB_PORT, 0);
        int timeout = configuration.getInteger(DB_TIMEMOUT, 5000);
        db = new FileServerDb(host, port, timeout);
        return db;
    }

}


