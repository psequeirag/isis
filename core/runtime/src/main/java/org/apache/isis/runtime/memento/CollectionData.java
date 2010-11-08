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


package org.apache.isis.runtime.memento;

import java.io.IOException;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.encoding.DataInputExtended;
import org.apache.isis.core.metamodel.encoding.DataInputStreamExtended;
import org.apache.isis.core.metamodel.encoding.DataOutputExtended;


class CollectionData extends Data {
	
    private final static long serialVersionUID = 1L;
    final Data[] elements;

    public CollectionData(final Oid oid, ResolveState resolveState, final String className, final Data[] elements) {
        super(oid, resolveState.name(), className);
        this.elements = elements;
        initialized();
    }

    public CollectionData(final DataInputExtended input) throws IOException {
        super(input);
        this.elements = input.readEncodables(Data.class);
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        super.encode(output);
        output.writeEncodables(elements);
    }

	public void initialized() {
		// nothing to do
	}

    @Override
    public void debug(final DebugString debug) {
        super.debug(debug);
        for (int i = 0; i < elements.length; i++) {
            debug.appendln("" + i + 1, elements[i]);

            // TODO recurse!
        }
    }

    @Override
    public String toString() {
        final StringBuffer str = new StringBuffer("(");
        for (int i = 0; i < elements.length; i++) {
            str.append((i > 0) ? "," : "");
            str.append(elements[i]);
        }
        str.append(")");
        return str.toString();
    }

}
