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

package org.jahia.bin.filters.jcr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.params.valves.AuthValveContext;
import org.jahia.pipelines.Pipeline;
import org.jahia.pipelines.PipelineException;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUserManagerService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet filter that performs user authentication and the initialization of
 * the JCR session for current user.
 * 
 * @author Thomas Draier
 */
public class JcrSessionFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(JcrSessionFilter.class);
    
    private Pipeline authPipeline;

    private JCRSessionFactory sessionFactory;
    
    private JahiaUserManagerService userManagerService;
    
    public void destroy() {
    	// do nothing
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        try {
            if (Jahia.isInitiated()) {
                try {
                    sessionFactory.setCurrentUser(null);
                    authPipeline.invoke(new AuthValveContext((HttpServletRequest) servletRequest,
                            (HttpServletResponse) servletResponse, sessionFactory));
                } catch (PipelineException pe) {
                    logger.error("Error while authorizing user", pe);
                }
            }

            if (sessionFactory.getCurrentUser() == null) {
                sessionFactory
                        .setCurrentUser(userManagerService.lookupUser(JahiaUserManagerService.GUEST_USERNAME));
            } else {
                ((HttpServletRequest)servletRequest).getSession().setAttribute(ProcessingContext.SESSION_USER, sessionFactory.getCurrentUser());
            }

            filterChain.doFilter (servletRequest, servletResponse );
        } finally {
            if (Jahia.isInitiated()) {
                sessionFactory.setCurrentUser(null);
                sessionFactory.setCurrentLocale(null);
                sessionFactory.setCurrentAliasedUser(null);
                sessionFactory.setCurrentServletPath(null);
                /*sessionFactory.setVersionDate(null);
                sessionFactory.setVersionLabel(null);*/
                sessionFactory.closeAllSessions();
            }
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    	// do nothing;
    }

	public void setAuthPipeline(Pipeline authPipeline) {
    	this.authPipeline = authPipeline;
    }

	public void setSessionFactory(JCRSessionFactory sessionFactory) {
    	this.sessionFactory = sessionFactory;
    }

	public void setUserManagerService(JahiaUserManagerService userManagerService) {
    	this.userManagerService = userManagerService;
    }
}
