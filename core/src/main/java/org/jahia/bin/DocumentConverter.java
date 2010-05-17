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

package org.jahia.bin;

import java.io.InputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import org.jahia.services.transform.DocumentConverterService;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.files.FileUpload;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Performs conversion of the submitted document into specified format.
 * 
 * @author Fabrice Cantegrel
 * @author Sergiy Shyrkov
 */
public class DocumentConverter extends HttpServlet implements Controller {

    private static Logger logger = Logger.getLogger(DocumentConverter.class);
    
    private DocumentConverterService converterService;
    
    private SettingsBean settingsBean;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet
     * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!converterService.isEnabled()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Conversion service is not enabled.");
        }

        FileUpload fu = new FileUpload(request, settingsBean.getTmpContentDiskPath(), Integer.MAX_VALUE);
        if (fu.getFileItems().size() == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file was submitted");
            return null;
        }
        
        // take the first one
        DiskFileItem inputFile = fu.getFileItems().values().iterator().next();
        InputStream stream = null;
        String returnedMimeType = fu.getParameterValues("mimeType") != null ? fu.getParameterValues("mimeType")[0] : null;
        if (returnedMimeType == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter mimeType");
        }
        
        
        try {
            ServletOutputStream outputStream = response.getOutputStream();
    
            stream = inputFile.getInputStream();
            // return a file
            response.setContentType(returnedMimeType);
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + FilenameUtils.getBaseName(inputFile.getName())
                            + "." + converterService.getExtension(returnedMimeType) + "\"");
    
            converterService.convert(stream,
                                    inputFile.getContentType(),
                                    outputStream,
                                    returnedMimeType);
    
            try {
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        } catch (Exception e) {
            logger.error("Error converting uploaded file " + inputFile.getFieldName() + ". Cause: "
                    + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Exception occurred: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(stream);
            for (DiskFileItem file : fu.getFileItems().values()) {
                file.delete();
            }
        }

        return null;
    }

    /**
     * @param converterService the converterService to set
     */
    public void setConverterService(DocumentConverterService converterService) {
        this.converterService = converterService;
    }

    /**
     * @param settingsBean the settingsBean to set
     */
    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

}
