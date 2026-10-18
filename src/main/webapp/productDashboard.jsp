<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- If this page is opened directly (no product list loaded), go through the servlet first --%>
<c:if test="${requestScope['javax.servlet.forward.request_uri'] == null}">
    <c:redirect url="/dashboard"/>
</c:if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Product Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container wide">
    <div class="card">

        <div class="topbar">
            <h2 style="margin:0">Product Dashboard</h2>
            <div class="who">
                Welcome, <strong><c:out value="${sessionScope.user.name}"/></strong> &nbsp;
                <a class="btn gray small" href="${pageContext.request.contextPath}/logout">Logout</a>
            </div>
        </div>

        <c:if test="${not empty successMsg}">
            <div class="alert success"><c:out value="${successMsg}"/></div>
        </c:if>

        <a class="btn green" href="${pageContext.request.contextPath}/addProduct">+ Add New Product</a>

        <table>
            <thead>
                <tr>
                    <th>PID</th>
                    <th>Pname</th>
                    <th>Price</th>
                    <th>Pqty</th>
                    <th>Operations</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="p" items="${products}">
                    <tr>
                        <td><c:out value="${p.pid}"/></td>
                        <td><c:out value="${p.pname}"/></td>
                        <td><c:out value="${p.pprice}"/></td>
                        <td><c:out value="${p.pqty}"/></td>
                        <td class="actions">
                            <a class="btn small"
                               href="${pageContext.request.contextPath}/editProduct?pid=${p.pid}">Edit</a>

                            <%-- Delete is a POST form; the confirm() popup asks before it is sent --%>
                            <form action="${pageContext.request.contextPath}/deleteProduct" method="post"
                                  onsubmit="return confirm('Are you sure you want to delete this product?');">
                                <input type="hidden" name="pid" value="${p.pid}">
                                <button type="submit" class="btn red small">Delete</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty products}">
                    <tr><td colspan="5" class="empty">No products found. Click "Add New Product" to create one.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
