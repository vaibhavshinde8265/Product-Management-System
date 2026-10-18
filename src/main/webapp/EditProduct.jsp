<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Opened directly (not through /editProduct?pid=..)? Send the user back to the dashboard --%>
<c:if test="${requestScope['javax.servlet.forward.request_uri'] == null}">
    <c:redirect url="/dashboard"/>
</c:if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit Product</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="card">
        <h2>Edit Product</h2>

        <c:if test="${not empty errorMessage}">
            <div class="alert error"><c:out value="${errorMessage}"/></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/editProduct" method="post">

            <label for="pid">Product ID (PID)</label>
            <!-- read-only: the user may not change the ID, but it is still submitted -->
            <input type="text" id="pid" name="pid" value="<c:out value='${pid}'/>" readonly>

            <label for="pname">Product Name</label>
            <input type="text" id="pname" name="pname" maxlength="100"
                   value="<c:out value='${pname}'/>" required>

            <label for="pprice">Product Price</label>
            <input type="number" id="pprice" name="pprice" min="0" step="0.01"
                   value="<c:out value='${pprice}'/>" required>

            <label for="pqty">Product Quantity</label>
            <input type="number" id="pqty" name="pqty" min="0" step="1"
                   value="<c:out value='${pqty}'/>" required>

            <button type="submit" class="btn full">Edit Product</button>
        </form>

        <div class="link-row">
            <a href="${pageContext.request.contextPath}/dashboard">Cancel and go back to Dashboard</a>
        </div>
    </div>
</div>
</body>
</html>
