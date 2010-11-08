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


package org.apache.isis.extensions.dnd;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.extensions.dnd.view.Placement;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.Workspace;


public class DummyWorkspaceView extends DummyView implements Workspace {

    public DummyWorkspaceView() {
        setupAllowSubviewsToBeAdded(true);
    }

    public View addIconFor(final ObjectAdapter adapter, Placement placement) {
        return createAndAddView();
    }

    private DummyView createAndAddView() {
        final DummyView view = new DummyView();
        addView(view);
        return view;
    }

    public View addWindowFor(final ObjectAdapter object, Placement placement) {
        return createAndAddView();
    }

    public View createSubviewFor(final ObjectAdapter object, final boolean asIcon) {
        return createAndAddView();
    }

    public void lower(final View view) {}

    public void raise(final View view) {}

    public void removeViewsFor(final ObjectAdapter object) {}

    @Override
    public Workspace getWorkspace() {
        return this;
    }

    public void removeObject(final ObjectAdapter object) {}

    public void addDialog(View dialog, Placement placement) {}

    public void addWindow(View window, Placement placement) {}

}
