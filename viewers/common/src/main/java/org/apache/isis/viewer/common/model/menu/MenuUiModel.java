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
package org.apache.isis.viewer.common.model.menu;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBar;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.viewer.common.model.action.ActionFactory;
import org.apache.isis.viewer.common.model.menuitem.MenuItemUiModel;
import org.apache.isis.viewer.common.model.userprofile.UserProfileUiModelProvider;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class MenuUiModel implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @NonNull private final DomainServiceLayout.MenuBar menuBarSelect;
    @NonNull private final List<String> menuContributingServiceIds;
    
    public String getCssClass() {
        return menuBarSelect.name().toLowerCase(Locale.ENGLISH);
    }
    
    public <T, M extends MenuItemUiModel<T, M>> 
    void buildMenuItems(
            final IsisWebAppCommonContext commonContext,
            final ActionFactory<T> menuActionFactory,
            final Function<String, M> menuItemFactory,
            final Consumer<M> onNewMenuItem) {
        
        val menuBars = commonContext.getMenuBarsService().menuBars();

        // TODO: remove hard-coded dependency on BS3
        final BS3MenuBar menuBar = (BS3MenuBar) menuBars.menuBarFor(getMenuBarSelect());
        
        MenuUiModel_buildMenuItems.buildMenuItems(
                commonContext, 
                menuBar,
                menuActionFactory,
                menuItemFactoryHonoringUserProfile(commonContext, menuItemFactory),
                onNewMenuItem);
        
    }
    
    // -- HELPER IN SUPPORT OF THE CURRENT USER PROFILE

    /**
     * Wraps a (simple) menu item factory.
     * @implNote when ever the top level MenuItem name is {@code null} we set the name
     * to the current user's profile name 
     */
    private <M> Function<String, M> menuItemFactoryHonoringUserProfile(
            final IsisWebAppCommonContext commonContext,
            final Function<String, M> menuItemFactory) {
        return (String topLevelMenuItemName)->
            menuItemFactory.apply(
                replaceNullWithUserProfileName(commonContext, topLevelMenuItemName));
    }
    
    private String replaceNullWithUserProfileName(
            final IsisWebAppCommonContext commonContext, 
            final String x) {
        if(x==null) {
            val userProfile = commonContext
                    .lookupServiceElseFail(UserProfileUiModelProvider.class)
                    .getUserProfile();
            return userProfile.getUserProfileName();
        }
        return x; 
    }
    

}