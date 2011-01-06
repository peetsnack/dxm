/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.uicomponents.bean.editmode;

import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem;
import org.jahia.services.uicomponents.bean.Visibility;
import org.jahia.services.uicomponents.bean.contentmanager.Column;
import org.jahia.services.uicomponents.bean.toolbar.Toolbar;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 14, 2010
 * Time: 12:30:01 PM
 * 
 */
public class SidePanelTab implements Serializable, BeanNameAware {
    private String name;
    private String key;
    private Toolbar treeContextMenu;
    private Toolbar tableContextMenu;
    private Map<String, String> params;
    private Visibility visibility;
    private List<Column> treeColumns;
    private List<Column> tableColumns;
    private String icon;
    private SidePanelTabItem tabItem;
    private String requiredPermission;

    public SidePanelTab() {
        tableColumns = new ArrayList<Column>();
        treeColumns = new ArrayList<Column>();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Toolbar getTreeContextMenu() {
        return treeContextMenu;
    }

    public void setTreeContextMenu(Toolbar treeContextMenu) {
        this.treeContextMenu = treeContextMenu;
    }

    public Toolbar getTableContextMenu() {
        return tableContextMenu;
    }

    public void setTableContextMenu(Toolbar tableContextMenu) {
        this.tableContextMenu = tableContextMenu;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void setBeanName(String name) {
        this.name = name;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public List<Column> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(List<Column> tableColumns) {
        this.tableColumns = tableColumns;
    }

    public List<Column> getTreeColumns() {
        return treeColumns;
    }

    public void setTreeColumns(List<Column> treeColumns) {
        this.treeColumns = treeColumns;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public SidePanelTabItem getTabItem() {
        return tabItem;
    }

    public void setTabItem(SidePanelTabItem tabItem) {
        this.tabItem = tabItem;
    }
}

