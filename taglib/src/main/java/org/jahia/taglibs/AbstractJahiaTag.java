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

package org.jahia.taglibs;

import org.slf4j.Logger;
import org.apache.taglibs.standard.tag.common.fmt.BundleSupport;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.taglibs.utility.Utils;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.util.*;

/**
 * This abstract Tag is the starting point for implementing any knew tags. In contains common attributes that should be
 * used in the implementation of the derived tags. For instance, the 'xhtmlCompliantHtml' is used to know if the tag
 * should render XHTML compliant HTML or simple basic HTML.</br>
 * The same is true regarding the 'resourceBundle' attribute. Instead of having to set the name of the resource bundle
 * file for all Jahia tags, it is much more convenient to set it once, at the beginning of the template, and then simply
 * fetching this set values.
 *
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class AbstractJahiaTag extends BodyTagSupport {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractJahiaTag.class);

    /**
     * Name of the resourceBundle all tags derived from this class will use.
     */
    private String resourceBundle;


    /**
     * If set to 'true' the output generated by the tag will be XHTML compliant, otherwise it will be
     * HTML compliant
     */
    protected boolean xhtmlCompliantHtml;

    /**
     * The languageCode attribute keeps track of the current language
     */
    protected String languageCode;

    /**
     * The CSS class the surrounding div or span element will have
     */
    protected String cssClassName;

    public String getResourceBundle() {
        if (resourceBundle == null || "".equals(resourceBundle)) {
            try {
                resourceBundle = ServicesRegistry.getInstance()
                        .getJahiaTemplateManagerService().getTemplatePackage(
                                getRenderContext().getSite()
                                        .getTemplatePackageName())
                        .getResourceBundleName();
            } catch (Exception e) {
                logger.warn(
                        "Unable to retrieve resource bundle name for current template set. Cause: "
                                + e.getMessage(), e);
            }
        }
        return resourceBundle;
    }

    public void setResourceBundle(String resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public boolean isXhtmlCompliantHtml() {
        return xhtmlCompliantHtml;
    }

    public void setXhtmlCompliantHtml(boolean xhtmlCompliantHtml) {
        this.xhtmlCompliantHtml = xhtmlCompliantHtml;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getCssClassName() {
        return cssClassName;
    }

    public void setCssClassName(String cssClassName) {
        this.cssClassName = cssClassName;
    }

    protected String getMessage(final String key, final String defaultValue) {
        String message = defaultValue;
        if (key != null) {
            try {
                message = retrieveResourceBundle().getString(key);
            } catch (MissingResourceException e) {
                // use default value
            }
        }
        return message;
    }

    protected String getMessage(final String key) {
        return getMessage(key, "???" + key + "???");
    }

    /**
     * Retrieve the parent resource bundle if any and if the current one is null.
     * This has to be called in subtags of TemplateTag (any tag within a template should do actually).
     */
    protected ResourceBundle retrieveResourceBundle() {
        ResourceBundle bundle = null;
        final LocalizationContext localizationCtx = BundleSupport.getLocalizationContext(pageContext);
        if (localizationCtx != null) {
            bundle = localizationCtx.getResourceBundle();
        }
        if (bundle == null) {
            bundle = new JahiaResourceBundle(resourceBundle,
                    getRenderContext().getMainResourceLocale(), getRenderContext()
                            .getSite().getTemplatePackageName());
        }
        return bundle;
    }

    /**
     * Returns an instance of the current {@link RenderContext}.
     *
     * @return an instance of the current {@link RenderContext}
     */
    protected final RenderContext getRenderContext() {
        return Utils.getRenderContext(pageContext);
    }

    /**
     * Returns the {@link Resource}, currently being rendered.
     *
     * @return the {@link Resource}, currently being rendered
     */
    protected final Resource getCurrentResource() {
        return (Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);
    }

    /**
     * Returns current {@link JahiaData} instance.
     *
     * @return current {@link JahiaData} instance
     */

    /*
    protected JahiaData getJahiaData() {
        return (JahiaData) pageContext.getAttribute("org.jahia.data.JahiaData",
                PageContext.REQUEST_SCOPE);
    }
    */

    /**
     * Generate jahia_gwt_dictionary JavaScript include
     *
     * @return jahia_gwt_dictionary JavaScript include
     */
    protected String getGwtDictionaryInclude() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        Locale locale = getUILocale();

        return getGwtDictionnaryInclude(request, locale);
    }

    public static String getGwtDictionnaryInclude(HttpServletRequest request, Locale locale) {
        StringBuilder s = new StringBuilder();
        s.append("<script type=\"text/javascript\" src=\"").append(
                request.getContextPath())
                .append("/gwt/resources/i18n/messages");
        if (LanguageCodeConverters.getAvailableBundleLocales().contains(locale)) {
            s.append("_").append(locale.toString());
        }
        s.append(".js\"></script>");
        return s.toString();
    }

    protected void resetState() {
        cssClassName = null;
        languageCode = null;
        resourceBundle = null;
        xhtmlCompliantHtml = false;
    }

    protected boolean isLogged() {
        return getRenderContext().isLoggedIn();
    }

    @Override
    public void release() {
        resetState();
        super.release();
    }

    /**
     * Generates the language switching link for the specified language.
     *
     * @param langCode the language to generate a link for
     * @return the language switching link for the specified language
     */
    protected final String generateCurrentNodeLangSwitchLink(String langCode) {
        RenderContext ctx = getRenderContext();
        if (ctx != null) {
            return ctx.getURLGenerator().getLanguages().get(langCode);
        } else {
            logger.error("Unable to get lang[" + langCode + "] link for current resource");
            return "";
        }
    }

    /**
     * Generates the language switching link for the specified node and language.
     *
     * @param node     the node to generate the link for
     * @param langCode the language to generate a link for
     * @return the language switching link for the specified language
     */
    protected final String generateNodeLangSwitchLink(JCRNodeWrapper node, String langCode) {
        if (node == null) {
            logger.warn("Node not specified. Language link will be generated for current node.");
            return generateCurrentNodeLangSwitchLink(langCode);
        }
        RenderContext ctx = getRenderContext();
        if (ctx != null) {
            Resource resource = new Resource(node, "html", null, Resource.CONFIGURATION_PAGE);
            URLGenerator url = new URLGenerator(ctx, resource);
            return url.getLanguages().get(langCode);
        } else {
            logger.error("Unable to get lang[" + langCode + "] link for home page");
            return "";
        }

    }

    /**
     * Returns the current user.
     *
     * @return the current user
     */
    protected final JahiaUser getUser() {
        RenderContext ctx = getRenderContext();
        return ctx != null ? ctx.getUser() : null;
    }
    
    protected Locale getUILocale() {
        RenderContext renderContext = getRenderContext();
        HttpSession session = pageContext.getSession();
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        return getUILocale(renderContext, session, request);
    }

    public static Locale getUILocale(RenderContext renderContext, HttpSession session, HttpServletRequest request) {
        Locale currentLocale = renderContext != null ? renderContext.getUILocale() : null;
        if (session != null) {
            if (session.getAttribute(ProcessingContext.SESSION_UI_LOCALE) != null) {
                currentLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_UI_LOCALE);
            }
        }

        if (currentLocale == null) {
            currentLocale = renderContext != null ? renderContext.getFallbackLocale() : null;
        }
        if (currentLocale == null) {
            currentLocale = request.getLocale();
        }

        return currentLocale;
    }
}
