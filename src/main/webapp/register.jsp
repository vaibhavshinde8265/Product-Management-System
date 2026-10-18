<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>User Registration</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="card">
        <h2>User Registration</h2>

        <form action="${pageContext.request.contextPath}/register" method="post">

            <label for="uid">User ID</label>
            <!-- The database generates the ID (AUTO_INCREMENT), so this field is display only -->
            <input type="text" id="uid" value="Auto-generated" disabled>

            <label for="name">Name</label>
            <input type="text" id="name" name="name" maxlength="100" required>

            <label for="email">Email</label>
            <input type="email" id="email" name="email" maxlength="150" required>

            <label for="mobile">Mobile Number</label>
            <input type="text" id="mobile" name="mobile" maxlength="15"
                   pattern="\+?[0-9]{10,14}" title="Enter 10 to 14 digits" required>

            <label for="password">Password</label>
            <input type="password" id="password" name="password" minlength="6" required>

            <button type="submit" class="btn full">New User</button>
        </form>

        <div class="link-row">
            Already registered? <a href="${pageContext.request.contextPath}/login.jsp">Login here</a>
        </div>
    </div>
</div>
</body>
</html>
