/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

 package org.jahia.params;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.exceptions.JahiaSiteNotFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Mar 5, 2005
 * Time: 3:46:19 PM
 * 
 */
public class ProcessingContextFactoryImpl implements ProcessingContextFactory {

    public ParamBean getContext(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final ServletContext servletContext)
            throws JahiaException, JahiaSiteNotFoundException, JahiaPageNotFoundException {
        final URLGenerator urlGenerator = new ServletURLGeneratorImpl(request, response);
        final long startTime = System.currentTimeMillis();
        // get the main http method...
        final String requestMethod = request.getMethod();
        int intRequestMethod = 0;

        if (requestMethod.equals("GET")) {
            intRequestMethod = ProcessingContext.GET_METHOD;
        } else if (requestMethod.equals("POST")) {
            intRequestMethod = ProcessingContext.POST_METHOD;
        }
        final ParamBean paramBean = new ParamBean(request, response, servletContext,
                org.jahia.settings.SettingsBean.getInstance(), startTime,
                intRequestMethod);
        paramBean.setUrlGenerator(urlGenerator);
        return paramBean;
    }

    public ProcessingContext getContext(SessionState sessionState) {
        if (sessionState == null) {
            // todo Generate a new session ID in a meaningful way.
            String id = "internal_session_id";
            sessionState = new BasicSessionState(id);
        }
        final URLGenerator urlGenerator = new BasicURLGeneratorImpl();
        final ProcessingContext processingContext = new ProcessingContext();
        processingContext.setUrlGenerator(urlGenerator);
        processingContext.setSessionState(sessionState);
        return processingContext;
    }
}
