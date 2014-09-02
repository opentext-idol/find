<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<spring:eval expression="@dispatcherProperties['application.version']" var="applicationVersion"/>

<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="content-type" content="text/html;charset=UTF-8">

  <title>Find - Login</title>
  <link rel="icon" type="image/ico" href="static-${applicationVersion}/favicon.ico">
  <link rel="stylesheet" type="text/css" href="static-${applicationVersion}/lib/frontend-bootstrap-theme/css/bootstrap-custom.css">
  <link rel="stylesheet" href="static-${applicationVersion}/lib/fontawesome/css/font-awesome.css">
  <link rel="stylesheet" type="text/css" href="static-${applicationVersion}/lib/login-page/css/login-page.css">
  <script type="text/javascript" src="static-${applicationVersion}/lib/require/require.js" data-main="static-${applicationVersion}/js/login.js"></script>
</head>
<body>
</body>
</html>
