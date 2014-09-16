/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Module where a content can be created by clicking on a button or drag'n'dropping on it a content type
 *
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:03:48 PM
 */
public class PlaceholderModule extends Module {
    private LayoutContainer panel;
    private LayoutContainer pasteButton;
    private LayoutContainer pasteAsReferenceButton;

    private static int MIN_WIDTH = 100;

    public PlaceholderModule(String id, String path, Element divElement, final MainModule mainModule) {
        super(id, path, divElement, mainModule, new FlowLayout());

        if (path.endsWith("*")) {
            setBorders(false);
        } else {
            setBorders(true);
        }

        html = new HTML("");
    }

    @Override
    public void onParsed() {
        panel = new LayoutContainer(new RowLayout(Style.Orientation.VERTICAL));
        add(panel);
    }

    @Override
    public void onNodeTypesLoaded() {
        if (mainModule.getConfig().isEnableDragAndDrop()) {
            DropTarget target = new ModuleDropTarget(this, EditModeDNDListener.PLACEHOLDER_TYPE);
            target.setOperation(DND.Operation.COPY);
            target.setFeedback(DND.Feedback.INSERT);

            target.addDNDListener(mainModule.getEditLinker().getDndListener());
        }

        if (getParentModule().getChildCount() >= getParentModule().getListLimit() && getParentModule().getListLimit() != -1) {
            return;
        }

        if (getParentModule() instanceof AreaModule && getParentModule().getChildCount() == 0 && ((AreaModule) getParentModule()).editable) {
            ((AreaModule) getParentModule()).setEnabledEmptyArea();
        }


        final String resolvedNodeTypes = resolveNodeTypes();
        String[] nodeTypesArray = resolvedNodeTypes != null ? resolvedNodeTypes.split(" ") : null;

        // if we have node type restrictions
        if (nodeTypesArray != null) {
            final Set<String> displayedNodeTypes = new HashSet<String>(Arrays.asList(nodeTypesArray));
            for (final String s : displayedNodeTypes) {

                GWTJahiaNodeType nodeType = ModuleHelper.getNodeType(s);
                if (nodeType != null) {
                    Boolean canUseComponentForCreate = (Boolean) nodeType.get("canUseComponentForCreate");
                    if (canUseComponentForCreate != null && !canUseComponentForCreate) {
                        continue;
                    }
                }
                Image icon = ContentModelIconProvider.getInstance().getIcon(nodeType).createImage();
                icon.setTitle(nodeType != null ? nodeType.getLabel() : s);
                LayoutContainer p = new HorizontalPanel();
                p.add(icon);

                Text label = new Text(nodeType != null ? nodeType.getLabel() : s);
                if (getWidth() >= MIN_WIDTH) {
                    p.add(label);
                } else {
                    p.setTitle(label.getText());
                }
                p.sinkEvents(Event.ONCLICK);
                p.addStyleName("button-placeholder");
                p.addListener(Events.OnClick, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent be) {
                        final GWTJahiaNode parentNode = getParentModule().getNode();
                        if (parentNode != null && PermissionsUtils.isPermitted("jcr:addChildNodes", parentNode) && !parentNode.isLocked()) {
                            String nodeName = null;
                            if ((path != null) && !"*".equals(path) && !path.startsWith("/")) {
                                nodeName = path;
                            }
                            ContentActions.showContentWizard(mainModule.getEditLinker(), s, parentNode, nodeName, true, displayedNodeTypes);
                        }
                    }
                });
                panel.add(p, new RowData());
            }

            Image icon = ToolbarIconProvider.getInstance().getIcon("paste").createImage();
            icon.setTitle(Messages.get("label.paste", "Paste"));
            pasteButton = new HorizontalPanel();
            pasteButton.add(icon);

            Text pasteLabel = new Text(Messages.get("label.paste", "Paste"));
            if (getWidth() >= MIN_WIDTH) {
                pasteButton.add(pasteLabel);
            } else {
                pasteButton.setTitle(pasteLabel.getTitle());
            }
            pasteButton.sinkEvents(Event.ONCLICK);
            pasteButton.addStyleName("button-placeholder");

            pasteButton.addListener(Events.OnClick, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                    GWTJahiaNode parentNode = getParentModule().getNode();
                    if (parentNode != null && PermissionsUtils.isPermitted("jcr:addChildNodes", parentNode) && !parentNode.isLocked()) {
                        CopyPasteEngine.getInstance().paste(parentNode, mainModule.getEditLinker(), null);
                    }
                }
            });
            AbstractImagePrototype pasteAsReferenceIcon = ToolbarIconProvider.getInstance().getIcon("pasteReference");
            pasteAsReferenceButton = new HorizontalPanel();
            pasteAsReferenceButton.add(pasteAsReferenceIcon.createImage());

            Text pasteReferenceLabel = new Text(Messages.get("label.pasteReference", "Paste Reference"));
            if (getWidth() >= MIN_WIDTH) {
                pasteAsReferenceButton.add(pasteReferenceLabel);
            } else {
                pasteAsReferenceButton.setTitle(pasteReferenceLabel.getTitle());
            }
            pasteAsReferenceButton.sinkEvents(Event.ONCLICK);
            pasteAsReferenceButton.addStyleName("button-placeholder");

            pasteAsReferenceButton.addListener(Events.OnClick, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                    GWTJahiaNode parentNode = getParentModule().getNode();
                    if (parentNode != null && PermissionsUtils.isPermitted("jcr:addChildNodes", parentNode) && !parentNode.isLocked()) {
                        CopyPasteEngine.getInstance().pasteReference(parentNode, mainModule.getEditLinker());
                    }
                }
            });

            CopyPasteEngine.getInstance().addPlaceholder(this);
            updatePasteButton();

            panel.add(pasteButton, new RowData());
            panel.add(pasteAsReferenceButton, new RowData());
            panel.layout();
        }
    }

    public boolean isDraggable() {
        return false;
    }

    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
    }

    public void updatePasteButton() {
        if (!CopyPasteEngine.getInstance().getCopiedNodes().isEmpty() && /*CopyPasteEngine.getInstance().canCopyTo(parentModule.getNode()) &&*/ CopyPasteEngine.getInstance()
                .checkNodeType(resolveNodeTypes())) {
            pasteButton.setVisible(true);
            if (CopyPasteEngine.getInstance().canPasteAsReference()) {
                pasteAsReferenceButton.setVisible(true);
            } else {
                pasteAsReferenceButton.setVisible(false);
            }
        } else {
            pasteButton.setVisible(false);
            pasteAsReferenceButton.setVisible(false);
        }
    }

    private String resolveNodeTypes() {
        // if we defined node types here, return them
        if (nodeTypes != null && !nodeTypes.isEmpty()) {
            return nodeTypes;
        } else {
            // if we didn't define node types here, check parent
            final String parentNodeTypes = getParentModule().getNodeTypes();
            if (parentNodeTypes != null && !parentNodeTypes.isEmpty()) {
                return parentNodeTypes;
            }
        }

        return null;
    }
}
