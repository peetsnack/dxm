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
package org.jahia.services.textextraction;

import java.io.Writer;

import javax.jcr.RepositoryException;

import org.drools.core.util.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.tools.OutWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Text extraction utility class for (re-)extracting text on existing document nodes.
 * 
 * @author Benjamin Papez
 */
public final class TextExtractionHelper {

    private static boolean checkingExtractions;

    static final Logger logger = LoggerFactory.getLogger(TextExtractionHelper.class);
    
    private static TextExtractionChecker extractionChecker;

    /**
     * Triggers the process of checking for missing text extractions check. If the <code>fixMissingExtraction</code> is set to <code>true</code> also 
     * tries to extract the text now.
     * 
     * This method ensures that only one check process runs at a time.
     * 
     * @param fixMissingExtraction
     *            if set to <code>true</code> performs the text extraction now; in case of <code>false</code> only the missing extraction count is
     *            reported, but no fix is done
     * @param statusOut
     *            a writer to log current processing status into
     * @return the status object to indicate the result of the check
     * @throws RepositoryException
     *             in case of JCR errors
     */
    public static synchronized ExtractionCheckStatus checkMissingExtraction(
            final boolean fixMissingExtraction, final Writer statusOut) throws RepositoryException {
        if (checkingExtractions) {
            throw new IllegalStateException("The process fpr checking extractions is currently running."
                    + " Cannot start the second process.");
        }
        checkingExtractions = true;
        long timer = System.currentTimeMillis();
        final ExtractionCheckStatus status = new ExtractionCheckStatus();
        
        final OutWrapper out = new OutWrapper(logger, statusOut);

        out.echo("Start {} missing extraction ", fixMissingExtraction ? "fixing" : "checking");
        
        extractionChecker = new TextExtractionChecker(status, fixMissingExtraction, out);

        try {
            JCRCallback<Object> callback = new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    extractionChecker.perform(session);
                    return null;
                }
            };
            out.echo("Missing extractions in DEFAULT workspace for: ");
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE, callback);
            if (status.extractable == 0) {
                out.echo("none");
            }
            long extractableInDefault = status.extractable; 
            out.echo("\nMissing extractions in LIVE workspace for: ");            
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, callback);
            if (status.extractable == extractableInDefault) {
                out.echo("none");
            }
        } finally {
            checkingExtractions = false;
            logger.info("\nDone {} text extractions in {} ms. Status: {}",
                    new Object[] {
                            fixMissingExtraction ? "fixing" : "checking",
                            (System.currentTimeMillis() - timer),
                            status.toString() });
        }

        return status;
    }

    /**
     * Triggers the process of checking for files (by filter), where extraction is possible. If the <code>redoExtraction</code> is set to <code>true</code> also 
     * tries to extract the text now.
     * 
     * This method ensures that only one check process runs at a time.
     * 
     * @param redoExtraction
     *            if set to <code>true</code> performs the text extraction now; in case of <code>false</code> only the extraction count is
     *            reported, but not redone
     * @param statusOut
     *            a writer to log current processing status into
     * @return the status object to indicate the result of the check
     * @throws RepositoryException
     *             in case of JCR errors
     */
    public static synchronized ExtractionCheckStatus checkExtractionByFilter(
            final boolean redoExtraction, RepositoryFileFilter filter, final Writer statusOut) throws RepositoryException {
        if (checkingExtractions) {
            throw new IllegalStateException("The process for checking extractions is currently running."
                    + " Cannot start the second process.");
        }
        checkingExtractions = true;
        long timer = System.currentTimeMillis();
        final ExtractionCheckStatus status = new ExtractionCheckStatus();
        
        final OutWrapper out = new OutWrapper(logger, statusOut);

        out.echo("Start {} extraction by filter", redoExtraction ? "redoing" : "checking");
        
        extractionChecker = new TextExtractionChecker(status, redoExtraction, filter, out);

        try {
            JCRCallback<Object> callback = new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    extractionChecker.perform(session);
                    return null;
                }
            };
            if (StringUtils.isEmpty(filter.getWorkspace())
                    || "default".equals(filter.getWorkspace())) {
                out.echo("Extractions in DEFAULT workspace for: ");
                JCRTemplate.getInstance().doExecuteWithSystemSession(null,
                        Constants.EDIT_WORKSPACE, callback);
                if (status.extractable == 0) {
                    out.echo("none");
                }
            }
            long extractableInDefault = status.extractable;
            if (StringUtils.isEmpty(filter.getWorkspace())
                    || "live".equals(filter.getWorkspace())) {
                out.echo("\nExtractions in LIVE workspace for: ");
                JCRTemplate.getInstance().doExecuteWithSystemSession(null,
                        Constants.LIVE_WORKSPACE, callback);
                if (status.extractable == extractableInDefault) {
                    out.echo("none");
                }
            }
        } finally {
            checkingExtractions = false;
            logger.info(
                    "\nDone {} text extractions in {} ms. Status: {}",
                    new Object[] { redoExtraction ? "redoing" : "checking",
                            (System.currentTimeMillis() - timer),
                            status.toString() });
        }

        return status;
    }
    
    
    /**
     * Forces stop of the extraction check process if it is currently running.
     */
    public static void forceStopExtractionCheck() {
        if (extractionChecker != null) {
            extractionChecker.stop();
        }
    }
    
    /**
     * Returns <code>true</code> if the process for checking extractions is currently running.
     * 
     * @return <code>true</code> if the process for checking extractions is currently running; <code>false</code> otherwise
     */
    public static boolean isCheckingExtractions() {
        return checkingExtractions;
    }    

    private TextExtractionHelper() {
        super();
    }
}