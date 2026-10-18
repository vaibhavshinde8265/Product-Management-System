# Project Report: Product Management System

## 1. Introduction

The Product Management System is a web application built with Java Servlets, JSP, JSTL, JDBC and MySQL.
It lets a user register, log in securely, and manage products through a dashboard that supports the four
CRUD operations: Create, Read, Update and Delete.

## 2. Technologies

| Layer | Technology |
|---|---|
| View (pages) | JSP + JSTL + HTML/CSS |
| Controller | Java Servlets and one Servlet Filter |
| Data access | JDBC with the DAO pattern |
| Database | MySQL (`product_management`) |
| Server | Apache Tomcat 9 |
| Build | Maven (WAR packaging) |

## 3. Architecture (MVC with DAO)

```
Browser  ->  Servlet (controller)  ->  DAO (SQL)  ->  MySQL
   ^              |
   |              v
   +----------  JSP (view)
```

* **Model:** `User`, `Product` (plain Java classes holding one row of data).
* **View:** the JSP pages. They contain no database code, only display logic.
* **Controller:** the servlets. They read the request, validate it, call the DAO, then choose the next page.
* **DAO:** `UserDAO`, `ProductDAO`. Only these classes talk to the database.

## 4. Database design

**users:** `uid` (PK, auto increment), `name`, `email` (unique), `mobile`, `password` (hash).
**products:** `pid` (PK, auto increment), `pname`, `pprice` DECIMAL(10,2), `pqty`.

## 5. Application flow

| Step | Page / URL | Servlet | Success goes to | Failure goes to |
|---|---|---|---|---|
| 1 | `register.jsp` | `/register` | `login.jsp` (message) | `Usererror.jsp` |
| 2 | `login.jsp` | `/login` | `/dashboard` | `Usererror.jsp` |
| 3 | `productDashboard.jsp` | `/dashboard` | shows the product table | `producterror.jsp` |
| 4 | `AddProduct.jsp` | `/addProduct` | `/dashboard` (message) | form again (validation) or `producterror.jsp` (database) |
| 5 | `EditProduct.jsp` | `/editProduct` | `/dashboard` (message) | form again (validation) or `producterror.jsp` (not found / database) |
| 6 | delete button | `/deleteProduct` | `/dashboard` (message) | `producterror.jsp` |
| 7 | Logout link | `/logout` | `login.jsp` (message) | none |

### Detailed description

1. **Registration.** The user fills in name, email, mobile and password. `RegisterServlet` validates the data,
   checks that the email is not already used, hashes the password and inserts the row. On success the user is
   redirected to the login page; on any failure `Usererror.jsp` shows the reason.
2. **Login.** `LoginServlet` finds the user by email and verifies the password against the stored hash. If it
   matches, a new session is created, the user object is stored in it, and the user is redirected to the
   dashboard. Wrong credentials show "Invalid email or password." on `Usererror.jsp`.
3. **Dashboard.** `DashboardServlet` loads all products with `ProductDAO.getAllProducts()` and forwards to
   `productDashboard.jsp`, which shows them in a table with Edit and Delete buttons and an Add New Product button.
4. **Add.** The form data is validated (name required, price a non-negative number with at most 2 decimals,
   quantity a non-negative whole number). A valid product is inserted, a success message is stored in the session,
   and the user is redirected to the dashboard.
5. **Edit.** `EditProductServlet` loads the product by PID. If it does not exist, `producterror.jsp` is shown.
   The PID is read-only on the form. After validation the row is updated and the user returns to the dashboard.
6. **Delete.** The Delete button submits a small POST form after a JavaScript confirmation popup. The servlet
   deletes the row by PID; if no such product exists, `producterror.jsp` is shown.
7. **Logout.** The session is invalidated and the user is sent to the login page.

## 6. Session management

`AuthFilter` runs before every product page and servlet. If the session has no logged-in user, the request is
redirected to the login page. The filter also sends no-cache headers, so the Back button cannot show product
pages after logout. Sessions expire after 30 minutes of inactivity.

## 7. Validation and error handling

* **Client side:** HTML5 attributes (`required`, `min`, `step`, `pattern`, `minlength`) give instant feedback.
* **Server side:** `ValidationUtil` repeats every check, because browser checks can be bypassed.
* **Database errors** (`SQLException`) are caught in the servlets, logged on the server, and shown to the user as a
  friendly message, never as a stack trace.

## 8. Security measures

* Salted PBKDF2 password hashing (65,536 iterations, SHA-256).
* PreparedStatement everywhere (no SQL injection).
* `<c:out>` for every value shown on a page (no XSS).
* Login filter, POST-only delete, session-fixation protection, session timeout.

## 9. Conclusion

The application meets all the functional requirements of the assignment: registration, login, product dashboard,
add / edit / delete with validation, error pages, session management and logout. Possible future improvements:
CSRF tokens, pagination and search on the dashboard, product ownership per user, and password reset by email.
