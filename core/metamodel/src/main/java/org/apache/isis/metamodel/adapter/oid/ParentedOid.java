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

package org.apache.isis.metamodel.adapter.oid;

import java.io.Serializable;

import org.apache.isis.commons.internal.url.UrlDecoderUtil;

/**
 * Used as the {@link Oid} for collections.
 */
public interface ParentedOid extends Serializable, Oid {

    RootOid getParentOid();

    String getName();

    // -- DECODE FROM STRING

    public static ParentedOid deStringEncoded(final String urlEncodedOidStr) {
        final String oidStr = UrlDecoderUtil.urlDecode(urlEncodedOidStr);
        return deString(oidStr);
    }

    public static ParentedOid deString(String enString) {
        return Oid.unmarshaller().unmarshal(enString, ParentedOid.class);
    }

}
