<%--
  ~ Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
  ~ Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
  --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<spring:eval expression="@environment.getProperty('application.version')" var="applicationVersion"/>
<c:set var="staticPath" value="static-${applicationVersion}"/>
<html>
<head>
    <title>Find</title>
    <link rel="icon" type="image/ico" href="${staticPath}/favicon.ico">
    <script type="application/json" id="config-json"><c:out value="${configJson}" escapeXml="false"/></script>
    <script src="${staticPath}/bower_components/hp-autonomy-hod-sso-js/src/js/authenticate-combined.js"></script>
    <script src="${staticPath}/bower_components/hp-autonomy-hod-sso-js/src/js/logout-sso.js"></script>
</head>
<body></body>
</html>
