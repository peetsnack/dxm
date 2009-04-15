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

 package org.jahia.data.applications;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import java.util.List;

/**
 * <p>Title: EntryPointDefinition for a web application</p>
 * <p>Description: A web application may contain multiple entry point
 * definitions, depending on the type of web application we are dealing with.
 * Servlet-based web application will for example have multiple servlet
 * mappings that will all be possible entry points into the application.
 * Portlet-based web applications will have multiple portlets in an application.
 * Note that the data contain in this object is generated by the
 * ApplicationManagerProvider, and it not meant to be stored in Jahia's
 * database, but provided by the implementation of each provider (in the case
 * of servlet-based web apps we parse the web.xml and for portlets we use
 * Jetspeed 2's registry sub-system).</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */
public interface EntryPointDefinition {

    public String getContext();

    public String getName ();

    public String getDisplayName();

    public String getDescription();

    public int getApplicationID();

    /**
     * Get the supported PortletMode for this entry point definition.
     * @return List a list of PortletMode objects
     */
    public List<PortletMode> getPortletModes();

    /**
     * Get the supported WindowState for this entry point definition.
     * @return List a list of WindowState objects.
     */
    public List<WindowState> getWindowStates();
}
