<%@ page import="org.jahia.services.render.Resource,
                 org.jahia.utils.LanguageCodeConverters" %>
<%@ page import="org.jahia.services.content.decorator.JCRSiteNode" %>
<%@ page import="java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="site" value="${currentNode.resolveSite}"/>
<c:set var="siteKey" value="${site.name}"/>
<c:set var="installedModules" value="${site.installedModules}"/>
<c:set var="templatePackageName" value="${site.templatePackageName}"/>

<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="managesites.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>

<script type="text/javascript">

    var defaultLang;
    var mandatoryLanguages;
    var inactiveLanguages;
    var inactiveLiveLanguages;

    function removeAll(src, remove) {
      for (var i = 0; i < remove.length; i++) {
          if (src.indexOf(remove[i]) >= 0) {
              src.splice(src.indexOf(remove[i]), 1);
          }
      }
      return src;
    }

    function updateSite() {
        showLoading();
        inactiveLiveLanguages = inactiveLiveLanguages.concat($("#updateSiteForm #language_list").fieldValue());
        var data = {
            'j:languages': $("#updateSiteForm [name='activeLanguages']").fieldValue().concat([defaultLang,'${currentResource.locale}']).concat($("#updateSiteForm #language_list").fieldValue()),
            'j:mandatoryLanguages': (mandatoryLanguages.length == 0) ? ['jcrClearAllValues'] : mandatoryLanguages,
            'j:inactiveLanguages': (inactiveLanguages.length == 0) ? ['jcrClearAllValues'] : inactiveLanguages,
            'j:inactiveLiveLanguages': (inactiveLiveLanguages.length == 0) ? ['jcrClearAllValues'] : inactiveLiveLanguages,
            'j:mixLanguage': $("#mixLanguages").attr('checked') != null,
            'j:allowsUnlistedLanguages': $("#allowsUnlistedLanguages").attr('checked') != null
        };
        $('#updateSiteForm').ajaxSubmit({
            data: data,
            dataType: "json",
            success: function(response) {
                if (response.warn != undefined) {
                    alert(response.warn);
                    hideLoading();
                } else {
                    if ($("#updateSiteForm #language_list").fieldValue().length == 0) {
                        hideLoading();
                    } else {
                        window.location.reload();
                    }
                }
            },
            error: function(response) {
                hideLoading();
            }
        });
        return true;
    }

    function updateBoxes() {
        $("#updateSiteForm input").enable(true);

        defaultLang = $("#updateSiteForm [name='j:defaultLanguage']").fieldValue()[0]
        $("#updateSiteForm [name='activeLanguages'][value='"+defaultLang+"']").enable(false);
        $("#updateSiteForm [name='activeLiveLanguages'][value='"+defaultLang+"']").enable(false);

        inactiveLanguages = removeAll($("#updateSiteForm [name='allLanguages']").fieldValue(), $("#updateSiteForm [name='activeLanguages']").fieldValue());
        inactiveLiveLanguages = removeAll($("#updateSiteForm [name='allLanguages']").fieldValue(), $("#updateSiteForm [name='activeLiveLanguages']").fieldValue());
        inactiveLanguages = removeAll(inactiveLanguages, [defaultLang]);
        inactiveLiveLanguages = removeAll(inactiveLiveLanguages, [defaultLang]);

        $.each(inactiveLanguages, function(i,v) {
            $("#updateSiteForm [type='checkbox'][value='"+v+"']").enable(false);
            $("#updateSiteForm [name='activeLanguages'][value='"+v+"']").enable(true);
        })
        $.each(inactiveLiveLanguages, function(i,v) {
            $("#updateSiteForm [name='j:defaultLanguage'][value='"+v+"']").enable(false);
        })

        mandatoryLanguages = $("#updateSiteForm [name='mandatoryLanguages']").fieldValue();
//

        $("#updateSiteForm [name='activeLanguages'][value='${currentResource.locale}']").enable(false);
        mix = $("#updateSiteForm [name='mixLanguage']").attr("checked") != null
        $("#updateSiteForm [name='allowsUnlistedLanguages']").enable(mix);
    }

    $(document).ready(function() {
        updateBoxes();
    })

</script>

<h2>${fn:escapeXml(currentNode.displayableName)} - ${fn:escapeXml(site.displayableName)}</h2>

<%
    JCRSiteNode site = (JCRSiteNode) pageContext.getAttribute("site");
    Resource r = (Resource) request.getAttribute("currentResource");
    final Locale currentLocale = r.getLocale();
    List<Locale> siteLocales = new ArrayList<Locale>(site.getLanguagesAsLocales());
    siteLocales.addAll(site.getInactiveLanguagesAsLocales());

    Collections.sort(siteLocales, new Comparator<Locale>() {
            public int compare(Locale o1, Locale o2) {
                return o1.getDisplayName(currentLocale).compareTo(o2.getDisplayName(currentLocale));
            }
        });

    request.setAttribute("siteLocales", siteLocales);

    request.setAttribute("availableLocales", LanguageCodeConverters.getSortedLocaleList(r.getLocale()));
%>


<form id="updateSiteForm" action="${url.base}${renderContext.mainResource.node.resolveSite.path}" method="post">
    <input type="hidden" name="jcrMethodToCall" value="put"/>
    <input type="hidden" name="jcrRedirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
    <table style="width: 100%;" cellpadding="0" cellspacing="0" border="1">
        <thead>
        <tr>
            <th>Language</th>
            <th>Default language</th>
            <th>Mandatory</th>
            <th>Active (Edit)</th>
            <th>Active (Live)</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="locale" items="${siteLocales}" varStatus="status">
            <c:set var="langAsString">${locale}</c:set>
            <input type="hidden" name="allLanguages" value="${locale}" class="language"/>
            <tr>
                <td><%= ((Locale) pageContext.getAttribute("locale")).getDisplayName(currentLocale)%> (${locale})</td>
                <td><input type="radio" name="j:defaultLanguage" value="${locale}" onchange="updateBoxes()"
                           <c:if test="${site.defaultLanguage eq locale}">checked="checked"</c:if> /></td>
                <td><input type="checkbox" name="mandatoryLanguages" value="${locale}"  onchange="updateBoxes()"
                           <c:if test="${functions:contains(site.mandatoryLanguages, langAsString)}">checked="checked"</c:if>/></td>
                <td><input type="checkbox" name="activeLanguages" value="${locale}"  onchange="updateBoxes()"
                           <c:if test="${functions:contains(site.languages, langAsString)}">checked="checked"</c:if>/></td>
                <td><input type="checkbox" name="activeLiveLanguages" value="${locale}" onchange="updateBoxes()"
                           <c:if test="${functions:contains(site.activeLiveLanguages, langAsString)}">checked="checked"</c:if>/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <input type="checkbox" name="mixLanguage" id="mixLanguages" value="true"${site.mixLanguagesActive ? ' checked="checked"' : ''} onchange="updateBoxes()"/>
    <label for="mixLanguages">&nbsp;<fmt:message
            key="org.jahia.admin.languages.ManageSiteLanguages.mixLanguages.label"/></label>
    <br>
    <input type="checkbox" name="allowsUnlistedLanguages" id="allowsUnlistedLanguages" value="true"${site.allowsUnlistedLanguages ? ' checked="checked"' : ''} />
    <label for="allowsUnlistedLanguages">&nbsp;<fmt:message
            key="org.jahia.admin.languages.ManageSiteLanguages.allowsUnlistedLanguages.label"/></label>
    <br>


    <div>
        <b><fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.addLanguages.label"/></b><br/>

        <b><fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.availableLanguages.label"/></b><br/>
        <select name="language_list" id="language_list" multiple="multiple" size="10">
            <c:forEach var="locale" items="${availableLocales}">
                <c:set var="langAsString">${locale}</c:set>
                <c:if test="${not functions:contains(siteLocales, langAsString)}">
                <option value="${locale}"><%= ((Locale) pageContext.getAttribute("locale")).getDisplayName(currentLocale)%> (${locale})</option>
                </c:if>
            </c:forEach>
        </select>
    </div>

    <input type="button" class="button" id="updateSite_button" value="Submit" onclick="updateSite()" />

</form>

<div style="display:none;" class="loading">
    <h1><fmt:message key="org.jahia.admin.workInProgressTitle"/></h1>
</div>
