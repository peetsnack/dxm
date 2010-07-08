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
package org.jahia.services.content.rules;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.drools.spi.KnowledgeHelper;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.notification.CamelNotificationService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
import javax.script.*;
import java.io.*;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 29 juin 2010
 */
public class RulesNotificationService {
    private transient static Logger logger = Logger.getLogger(RulesNotificationService.class);

    private static RulesNotificationService instance;

    public static synchronized RulesNotificationService getInstance() {
        if (instance == null) {
            instance = new RulesNotificationService();
        }
        return instance;
    }

    private CamelNotificationService notificationService;

    public void setNotificationService(CamelNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void notifyNewUser(AddedNodeFact node, final String template, KnowledgeHelper drools)
            throws RepositoryException, ScriptException, IOException {
        JCRNodeWrapper userNode = node.getNode();
        if (userNode.hasProperty("j:email") && !userNode.getProperty("j:external").getBoolean()) {
            String toMail = userNode.getProperty("j:email").getString();
            String fromMail = SettingsBean.getInstance().getMail_from();
            String ccList = null;
            String bcclist = null;
            Locale locale;
            try {
                locale = LanguageCodeConverters.languageCodeToLocale(userNode.getProperty(
                        "preferredLanguage").getString());
            } catch (RepositoryException e) {
                locale = LanguageCodeConverters.languageCodeToLocale(
                        SettingsBean.getInstance().getDefaultLanguageCode());
            }
            sendMail(template, userNode, toMail, fromMail, ccList, bcclist, locale);

        }
    }

    public void notifyCurrentUser(User user, final String template, final String fromMail, final String ccList,
                                  final String bccList, KnowledgeHelper drools)
            throws RepositoryException, ScriptException, IOException {
        JahiaUser userNode = user.getJahiaUser();
        if (userNode.getProperty("j:email") != null) {
            String toMail = userNode.getProperty("j:email");
            Locale locale = getLocale(userNode);

            sendMail(template, userNode, toMail, fromMail, ccList, bccList, locale);
        }
    }

    public void notifyCurrentUser(User user, final String template, final String fromMail, KnowledgeHelper drools)
            throws RepositoryException, ScriptException, IOException {
        JahiaUser userNode = user.getJahiaUser();
        if (userNode.getProperty("j:email") != null) {
            String toMail = userNode.getProperty("j:email");
            Locale locale = getLocale(userNode);
            sendMail(template, userNode, toMail, fromMail, null, null, locale);
        }
    }

    private Locale getLocale(JahiaUser userNode) {
        Locale locale;
        String property = userNode.getProperty("preferredLanguage");
        if (property != null) {
            locale = LanguageCodeConverters.languageCodeToLocale(property);
        } else {
            locale = LanguageCodeConverters.languageCodeToLocale(
                    SettingsBean.getInstance().getDefaultLanguageCode());
        }
        return locale;
    }

    public void notifyUser(String user, final String template, final String fromMail, KnowledgeHelper drools)
            throws RepositoryException, ScriptException, IOException {
        JahiaUser userNode = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(user);
        if (userNode.getProperty("j:email") != null) {
            String toMail = userNode.getProperty("j:email");
            sendMail(template, userNode, toMail, fromMail, null, null, getLocale(userNode));
        }
    }

    public void notifyUser(String user, final String template, final String fromMail, final String ccList,
                           final String bccList, KnowledgeHelper drools)
            throws RepositoryException, ScriptException, IOException {
        JahiaUser userNode = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(user);
        if (userNode.getProperty("j:email") != null) {
            String toMail = userNode.getProperty("j:email");
            sendMail(template, userNode, toMail, fromMail, ccList, bccList, getLocale(userNode));
        }
    }

    private void sendMail(String template, Object user, String toMail, String fromMail, String ccList, String bcclist,
                          Locale locale) throws RepositoryException, ScriptException {
        // Resolve template :
        ScriptEngineManager scriptManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptManager.getEngineByExtension(StringUtils.substringAfterLast(template, "."));
        ScriptContext scriptContext = scriptEngine.getContext();
        final Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("currentUser", user);
        InputStream scriptInputStream = JahiaContextLoaderListener.getServletContext().getResourceAsStream(template);
        if (scriptInputStream != null) {
            String resourceBundleName = StringUtils.substringBeforeLast(StringUtils.substringAfter(template,
                                                                                                   "/").replaceAll("/",
                                                                                                                   "."),
                                                                        ".");
            String subject = "";
            try {
                ResourceBundle resourceBundle = ResourceBundle.getBundle(resourceBundleName, locale);
                bindings.put("bundle", resourceBundle);
                subject = resourceBundle.getString("subject");
            } catch (MissingResourceException e) {
                // No RB
            }
            Reader scriptContent = null;
            try {
                scriptContent = new InputStreamReader(scriptInputStream);
                scriptContext.setWriter(new StringWriter());
                // The following binding is necessary for Javascript, which doesn't offer a console by default.
                bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                Object result = scriptEngine.eval(scriptContent, bindings);
                StringWriter writer = (StringWriter) scriptContext.getWriter();
                String body = writer.toString();
                notificationService.sendMail("seda:users?multipleConsumers=true", subject,
                                             body, null, fromMail, toMail, ccList, bcclist);
            } finally {
                if (scriptContent != null) {
                    IOUtils.closeQuietly(scriptContent);
                }
            }
        }
    }
}
