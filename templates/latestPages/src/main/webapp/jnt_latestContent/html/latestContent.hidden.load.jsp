<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>


<%-- Get parameters of the module --%>
<jcr:nodeProperty node="${currentNode}" name='j:nbOfResult' var="nbOfResult"/>
<c:set var="startNode" value="${currentNode.properties.startNode.node}"/>
<jcr:nodeProperty node="${currentNode}" name='j:criteria' var="criteria"/>
<jcr:nodeProperty node="${currentNode}" name='j:type' var="type"/>

<%-- Execute the query, depending on the selected mode --%>
<jcr:sql var="result"
         sql="SELECT * FROM [${type.string}] AS page WHERE ISDESCENDANTNODE(page,'${startNode.path}') ORDER BY [jcr:${criteria.string}] DESC"
         limit="${nbOfResult.long}"/>

<%-- Debug message --%>
<%-- <p>Debug > Nb of result from query (Criteria : ${criteria.string} - Nb of result : ${nbOfResult.long} - Mode : ${mode.string}) : ${fn:length(result.nodes)}</p>  --%>

<%-- Set variables to store the result --%>
<c:set var="renderOptionsOnChild" value="none" scope="request"/>
<c:set var="currentList" value="${result.nodes}" scope="request"/>
<c:set var="end" value="${functions:length(result.nodes)}" scope="request"/>
<c:set var="listTotalSize" value="${end}" scope="request"/>