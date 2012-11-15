/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.google.gwt.user.client.Event;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.contentengine.ButtonItem;
import org.jahia.ajax.gwt.client.widget.contentengine.EditContentEngine;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 12 nov. 2009
 */
public class ContentTypeWindow extends Window {
    private GWTJahiaNode parentNode;
    private final Linker linker;
    private ButtonBar buttonBar;
    private Button ok;
    private Button cancel;
    private ContentTypeTree contentTypeTree;

    public ContentTypeWindow(final Linker linker, GWTJahiaNode parent, List<GWTJahiaNode> components, final Map<String, GWTJahiaNodeProperty> props, final String nodeName, final boolean createInParentAndMoveBefore) {
        this.linker = linker;
        this.parentNode = parent;
        setLayout(new FitLayout());
        setBodyBorder(false);
        setSize(400, 650);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        contentTypeTree = new ContentTypeTree();
        contentTypeTree.fillStore(components);
        TreeGrid treeGrid = contentTypeTree.getTreeGrid();
        treeGrid.sinkEvents(Event.ONDBLCLICK + Event.ONCLICK);
        treeGrid.addListener(Events.OnDoubleClick, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent baseEvent) {
                GWTJahiaNodeType gwtJahiaNodeType = (GWTJahiaNodeType) (((TreeGridEvent) baseEvent).getModel()).get("componentNodeType");
                if (gwtJahiaNodeType != null && linker != null && !gwtJahiaNodeType.isMixin()) {
                    EngineLoader.showCreateEngine(linker, parentNode, gwtJahiaNodeType, props, nodeName, createInParentAndMoveBefore);
                    hide();
                }
            }
        });

        add(contentTypeTree);
        setFocusWidget(contentTypeTree.getNameFilterField());
        contentTypeTree.layout(true);
        layout();

        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);
        final Window window = this;
        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        ok = new Button(Messages.get("label.ok"));
        ok.setHeight(ButtonItem.BUTTON_HEIGHT);
        ok.setEnabled(false);
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        ok.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                GWTJahiaNode selectedItem = contentTypeTree.getTreeGrid().getSelectionModel().getSelectedItem();
                GWTJahiaNodeType contentTypeModelData = null;
                if (selectedItem != null) {
                    contentTypeModelData = (GWTJahiaNodeType) selectedItem.get("componentNodeType");
                }
                if (contentTypeModelData != null && !contentTypeModelData.isMixin()) {
                    final GWTJahiaNodeType gwtJahiaNodeType = contentTypeModelData;
                    if (gwtJahiaNodeType != null) {
                        EngineLoader.showCreateEngine(ContentTypeWindow.this.linker, parentNode, gwtJahiaNodeType, props, nodeName, createInParentAndMoveBefore);
                        window.hide();
                    }
                }
            }
        });


        buttonBar.add(ok);

        cancel = new Button(Messages.get("label.cancel"));
        cancel.setHeight(ButtonItem.BUTTON_HEIGHT);
        cancel.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                window.hide();
            }
        });
        buttonBar.add(cancel);
        buttonsPanel.add(buttonBar);

        setBottomComponent(buttonsPanel);


        setFooter(true);
        ok.setEnabled(true);
    }

    public static void createContent(final Linker linker, final String name, final List<String> nodeTypes, final Map<String, GWTJahiaNodeProperty> props, final GWTJahiaNode targetNode, boolean includeSubTypes, final boolean createInParentAndMoveBefore) {
        createContent(linker, name, nodeTypes, props, targetNode, includeSubTypes, createInParentAndMoveBefore, null);
    }
    
    public static void createContent(final Linker linker, final String name, final List<String> nodeTypes, final Map<String, GWTJahiaNodeProperty> props, final GWTJahiaNode targetNode, boolean includeSubTypes, final boolean createInParentAndMoveBefore, final Set<String> displayedNodeTypes) {
        String contentPath = "$site/components/*";
        if ("studiomode".equals(linker.getConfig().getName())) {
            contentPath = "/modules/*";
        }
        linker.loading(Messages.get("label.loading", "Loading"));
        JahiaContentManagementService.App.getInstance().getContentTypesAsTree(Arrays.asList(contentPath), nodeTypes, Arrays.asList("name"), includeSubTypes, false, new BaseAsyncCallback<List<GWTJahiaNode>>() {
            public void onSuccess(List<GWTJahiaNode> result) {
                linker.loaded();
                if (result.size() == 1 && result.get(0).getChildren().isEmpty()) {
                    EngineLoader.showCreateEngine(linker, targetNode, (GWTJahiaNodeType) result.get(0).get("componentNodeType"), props,
                            name, createInParentAndMoveBefore);

                } else {
                    if (nodeTypes != null && nodeTypes.size() == 1 && displayedNodeTypes != null) {
                        GWTJahiaNodeType targetNodeType = getTargetNodeType(nodeTypes.get(0), result, displayedNodeTypes);
                        if (targetNodeType != null) {
                            EngineLoader.showCreateEngine(linker, targetNode, targetNodeType, props, name, createInParentAndMoveBefore);
                            return;
                        }
                    }
                    new ContentTypeWindow(linker, targetNode, result, props, name, createInParentAndMoveBefore).show();
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                linker.loaded();
                super.onFailure(caught);
            }

            private GWTJahiaNodeType getTargetNodeType(String nodeTypeName, List<GWTJahiaNode> result,
                    Set<String> displayedNodeTypes) {
                GWTJahiaNodeType targetNodeType = null;
                for (GWTJahiaNode nd : result) {
                    Object[] target = getTargetNodeType(nodeTypeName, nd, displayedNodeTypes);
                    if (!((Boolean) target[0])) {
                        return null;
                    } else {
                        if (targetNodeType == null && target[1] != null) {
                            targetNodeType = (GWTJahiaNodeType) target[1];
                        }
                    }
                }

                return targetNodeType;
            }
            
            private Object[] getTargetNodeType(String nodeTypeName, GWTJahiaNode startNode, Set<String> displayedNodeTypes) {
                boolean sinlgeTarget = true;
                GWTJahiaNodeType targetNodeType = null;
                if (startNode.getChildren().size() > 0) {
                    for (ModelData child : startNode.getChildren()) {
                        Object[] result = getTargetNodeType(nodeTypeName, (GWTJahiaNode) child, displayedNodeTypes);
                        if (!((Boolean) result[0])) {
                            return result;
                        }
                        if (targetNodeType == null && result[1] != null) {
                            targetNodeType = (GWTJahiaNodeType) result[1];
                        }
                    }
                } else  if (!startNode.isNodeType("jnt:componentFolder")) {
                    GWTJahiaNodeType nodeType = (GWTJahiaNodeType) startNode.get("componentNodeType");
                    if (nodeType != null) {
                        if (nodeTypeName.equals(nodeType.getName())) {
                            targetNodeType  = nodeType;
                        }
                        if (!displayedNodeTypes.contains(nodeType.getName())) {
                            sinlgeTarget = false;
                        }
                    }
                }
                return new Object[] {sinlgeTarget, targetNodeType};
            }
        });
    }
}
