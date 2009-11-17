/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes.initializers;

import org.apache.log4j.Logger;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Template;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 17 nov. 2009
 */
public class TemplatesChoiceListInitializerImpl implements ChoiceListInitializer {
    private transient static Logger logger = Logger.getLogger(TemplatesChoiceListInitializerImpl.class);
    
    public List<ChoiceListValue> getChoiceListValues(ProcessingContext jParams, ExtendedPropertyDefinition declaringPropertyDefinition,
                                                     String param, String realNodeType, List<ChoiceListValue> values) {
        if (jParams == null) {
            return new ArrayList<ChoiceListValue>();
        }

        JCRNodeWrapper node = (JCRNodeWrapper) jParams.getAttribute("contextNode");
        final List<String> nodeTypeList;

        if (node == null && realNodeType==null) {
            return new ArrayList<ChoiceListValue>();
        } else if(node!=null) {
            try {
                nodeTypeList = node.getNodeTypes();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                return new ArrayList<ChoiceListValue>();
            }
        } else {
            try {
                final ExtendedNodeType nodeType = NodeTypeRegistry.getInstance().getNodeType(realNodeType);
                nodeTypeList = new ArrayList<String>();
                nodeTypeList.add(realNodeType);
                final ExtendedNodeType[] subtypes = nodeType.getMixinSubtypes();
                for (ExtendedNodeType subtype : subtypes) {
                    nodeTypeList.add(subtype.getName());
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                return new ArrayList<ChoiceListValue>();
            }
        }

        SortedSet<Template> templates;
        if ("j:template".equals(declaringPropertyDefinition.getName())) {
            templates = new TreeSet<Template>();
            try {
                for (String s : nodeTypeList) {
                    templates.addAll(RenderService.getInstance().getTemplatesSet(NodeTypeRegistry.getInstance().getNodeType(s)));
                }
            } catch (RepositoryException e) {

            }
        } else if ("j:referenceTemplate".equals(declaringPropertyDefinition.getName())) {
            templates = new TreeSet<Template>();
            try {
                if (node.hasProperty("j:node")) {
                    JCRNodeWrapper ref = (JCRNodeWrapper) node.getProperty("j:node").getNode();

                    for (String s : ref.getNodeTypes()) {
                        templates.addAll(RenderService.getInstance().getTemplatesSet(NodeTypeRegistry.getInstance().getNodeType(s)));
                    }
                }
            } catch (RepositoryException e) {

            }
        } else {
            templates = RenderService.getInstance().getAllTemplatesSet();
        }

        List<ChoiceListValue> vs = new ArrayList<ChoiceListValue>();
        for (Template template : templates) {
            if (!template.getKey().startsWith("wrapper.") && !template.getKey().startsWith("skins.") && !template.getKey().startsWith("debug.")) {
                vs.add(new ChoiceListValue(template.getKey(),new HashMap<String, Object>(), new ValueImpl(template.getKey(), PropertyType.STRING, false)));
            }
        }
        return vs;
    }
}
