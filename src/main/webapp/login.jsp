<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>User Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="card">
        <h2>User Login</h2>

        <%-- Messages chosen by a fixed query parameter (the text itself is not taken from the URL) --%>
        <c:if test="${param.registered == '1'}">
            <div class="alert success">Registration successful. Please login.</div>
        </c:if>
        <c:if test="${param.loggedout == '1'}">
            <div class="alert info">You have been logged out successfully.</div>
        </c:if>
        <c:if test="${param.login == 'required'}">
            <div class="alert error">Please login to continue.</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="post">

            <label for="email">Email</label>
            <input type="email" id="email" name="email" required>

            <label for="password">Password</label>
            <input type="password" id="password" name="password" required>

            <button type="submit" class="btn full">Login</button>
        </form>

        <div class="link-row">
            New user? <a href="${pageContext.request.contextPath}/register.jsp">Register here</a>
        </div>
    </div>
</div>
</body>
</html>
