<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>User Error</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="card">
        <h2>Something went wrong</h2>

        <div class="alert error">
            <c:choose>
                <c:when test="${not empty errorMessage}"><c:out value="${errorMessage}"/></c:when>
                <c:otherwise>The page you asked for is not available or an unexpected error occurred.</c:otherwise>
            </c:choose>
        </div>

        <%-- backUrl is set by the servlet (register.jsp or login.jsp) --%>
        <c:choose>
            <c:when test="${not empty backUrl}">
                <a class="btn" href="${pageContext.request.contextPath}/<c:out value='${backUrl}'/>"><c:out value="${backText}"/></a>
            </c:when>
            <c:otherwise>
                <a class="btn" href="${pageContext.request.contextPath}/login.jsp">Go to Login</a>
            </c:otherwise>
        </c:choose>
    </div>
</div>
</body>
</html>
