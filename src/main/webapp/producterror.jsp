<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Product Error</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="card">
        <h2>Product Error</h2>

        <div class="alert error">
            <c:choose>
                <c:when test="${not empty errorMessage}"><c:out value="${errorMessage}"/></c:when>
                <c:otherwise>An unexpected product error occurred.</c:otherwise>
            </c:choose>
        </div>

        <a class="btn" href="${pageContext.request.contextPath}/dashboard">Back to Dashboard</a>
    </div>
</div>
</body>
</html>
