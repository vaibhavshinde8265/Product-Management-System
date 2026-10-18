# Code Explanation: Product Management System

This document explains every file in the project, in the order a request travels through the application.

---

## 0. How one request travels (big picture)

Example: the user clicks **Login**.

1. The browser sends `POST /login` with email and password.
2. Tomcat finds `LoginServlet` (because of `@WebServlet("/login")`).
3. The servlet checks the input with `ValidationUtil`, then asks `UserDAO.login(...)`.
4. `UserDAO` gets a connection from `DBConnection`, runs a SELECT, and checks the password with `PasswordUtil`.
5. The servlet stores the user in the **session** and redirects to `/dashboard`.
6. `AuthFilter` sees the session has a user, lets the request pass, and `DashboardServlet` loads products and forwards to `productDashboard.jsp`.

Every feature follows the same path: **JSP form -> Servlet -> DAO -> Database -> back to a JSP**.

---

## 1. `pom.xml` (Maven build file)

| Part | Meaning |
|---|---|
| `<packaging>war</packaging>` | Build a web archive that can be deployed on Tomcat. |
| `maven.compiler.source/target = 11` | Compile for Java 11. |
| `javax.servlet-api` (`provided`) | Needed to compile servlets. `provided` means Tomcat supplies it, so it is **not** packed into the WAR. |
| `javax.servlet.jsp-api` (`provided`) | Same idea for JSP. |
| `jstl 1.2` | The `<c:...>` tags used in JSP pages. It is packed into the WAR. |
| `mysql-connector-j` | The JDBC driver that lets Java talk to MySQL. |
| `<finalName>` | The WAR is named `ProductManagementSystem.war`, which also becomes the URL context path. |

---

## 2. `sql/product_management.sql`

* `CREATE DATABASE IF NOT EXISTS product_management;` creates the database only if it is missing.
* **users** table: `uid` is the primary key with `AUTO_INCREMENT` (MySQL generates 1, 2, 3...). `email` is `UNIQUE` so two users cannot share an email. `password` is `VARCHAR(255)` because it holds a hash, which is much longer than the original password.
* **products** table: `pprice` is `DECIMAL(10,2)`, meaning up to 10 digits with 2 after the decimal point. This is the correct type for money (never use float/double for prices).
* The final `INSERT` adds the sample row "Mobile, 15000, 50" from the assignment.

---

## 3. `src/main/resources/db.properties`

Holds `db.url`, `db.user`, `db.password`. Keeping them in a file means you change the password without touching Java code.
In the URL: `useSSL=false` avoids SSL warnings on a local database, `allowPublicKeyRetrieval=true` lets MySQL 8 log in with its default authentication, and `serverTimezone=UTC` avoids a timezone error with some driver versions.

---

## 4. Package `com.pms.util` (helper classes)

### 4.1 `DBConnection.java`

* `private DBConnection()`: a private constructor, so nobody can create an object. All methods are `static`.
* `loadProperties()`: reads `db.properties` from the classpath **once** and keeps it in a static field. `synchronized` makes this safe when two requests arrive at the same time.
* `getConnection()`: loads the MySQL driver with `Class.forName(...)`, then calls `DriverManager.getConnection(url, user, password)`. It returns a **new** connection every time, and the caller must close it.

### 4.2 `PasswordUtil.java`

Storing plain passwords is dangerous, so we store a **hash**.

* `hash(password)`: makes 16 random bytes (the **salt**) with `SecureRandom`, runs **PBKDF2 with HMAC-SHA256** (65,536 rounds, deliberately slow so guessing is hard), and returns `salt:hash` in Base64 text.
* The salt means two users with the same password get different hashes.
* `verify(password, stored)`: splits the stored text into salt and hash, hashes the typed password with the **same salt**, and compares. `MessageDigest.isEqual` compares in constant time, which avoids timing attacks.
* A hash cannot be reversed, so passwords cannot be "decrypted" back.

### 4.3 `ValidationUtil.java`

One place for all input rules. Each method returns an **error message** or `null` when everything is valid.

* `EMAIL` pattern: letters/digits/`._%+-`, then `@`, domain, dot, 2+ letters.
* `MOBILE` pattern: optional `+` then 10 to 14 digits (fits `VARCHAR(15)`).
* `validateUser(...)`: checks name, email, mobile and that the password has at least 6 characters.
* `validateProduct(...)`:
  * Name is required, at most 100 characters.
  * Price is parsed with `BigDecimal`. It must be a number, not negative (`signum() < 0`), have at most 2 decimals (`scale() > 2`), and not exceed `99999999.99` (the `DECIMAL(10,2)` limit).
  * Quantity must match `\d{1,9}`, which means digits only (no minus sign, no decimal point) and small enough to fit in an `int`.

Why validate again on the server when HTML already has `required`/`min`? Because users can bypass browser checks (for example with developer tools), so the server must never trust the browser.

---

## 5. Package `com.pms.model` (data classes)

### 5.1 `User.java` and 5.2 `Product.java`

Plain Java classes (POJOs/beans): private fields, a constructor, getters and setters. One object = one table row.

* `Product.pprice` is `BigDecimal`, matching `DECIMAL`, so there are no rounding errors like `0.1 + 0.2`.
* JSP's Expression Language reads these through getters: `${p.pname}` calls `p.getPname()`. That is why the getter names matter.

---

## 6. Package `com.pms.dao` (database access)

The **DAO pattern** keeps all SQL in one layer. Servlets never write SQL.

Every method uses **try-with-resources**:

```java
try (Connection con = DBConnection.getConnection();
     PreparedStatement ps = con.prepareStatement(sql)) { ... }
```

Connection, statement and result set are closed automatically, even when an exception happens, so there are no connection leaks.

Every query uses `?` placeholders with `ps.setString(...)` / `ps.setInt(...)`. This is a **PreparedStatement**, and it prevents SQL injection because the values are never glued into the SQL text.

### 6.1 `UserDAO.java`

| Method | What it does |
|---|---|
| `emailExists(email)` | `SELECT 1 FROM users WHERE email = ?`. Returns true if any row comes back (`rs.next()`). |
| `registerUser(user)` | `INSERT` into `users`. The password is hashed with `PasswordUtil.hash` first. Returns true if exactly 1 row was inserted (`executeUpdate() == 1`). |
| `login(email, password)` | Selects the row for this email, then calls `PasswordUtil.verify`. If both pass, builds a `User` **without the password hash** and returns it. Otherwise returns `null`. |

### 6.2 `ProductDAO.java`

| Method | SQL | Notes |
|---|---|---|
| `getAllProducts()` | `SELECT ... ORDER BY pid` | Loops over the `ResultSet` and builds a `List<Product>`. |
| `getProductById(pid)` | `SELECT ... WHERE pid = ?` | Returns `null` when not found, which the servlets use to show "product does not exist". |
| `addProduct(p)` | `INSERT` | `pid` is not inserted; MySQL generates it. |
| `updateProduct(p)` | `UPDATE ... WHERE pid = ?` | Changes name, price and quantity. |
| `deleteProduct(pid)` | `DELETE ... WHERE pid = ?` | Returns false when no row matched. |
| `mapRow(rs)` | (helper) | Converts the current result row to a `Product`, so the code is not repeated. |

---

## 7. Package `com.pms.servlet` (controllers)

A servlet is a Java class that handles web requests. `@WebServlet("/login")` maps a URL to the class, so no XML entry is needed. `doGet` handles GET requests (opening a page or link) and `doPost` handles POST requests (submitting a form).

Common pattern in the servlets:

* `req.setCharacterEncoding("UTF-8")` before reading parameters, so non-English text is read correctly.
* `req.setAttribute(...)` stores data for the JSP in the **request**, and `forward(...)` passes control to a JSP on the server (the URL in the browser does not change).
* `resp.sendRedirect(...)` tells the browser to make a **new** request to another URL.
* **Post/Redirect/Get:** after a successful POST we redirect. Without this, pressing F5 would re-submit the form and add the product twice.

### 7.1 `RegisterServlet` (`/register`)

1. Reads `name`, `email`, `mobile`, `password`.
2. `ValidationUtil.validateUser(...)`. If an error message comes back, `fail(...)` forwards to `Usererror.jsp`.
3. Trims the values and lowercases the email so `A@x.com` and `a@x.com` are treated as the same.
4. `emailExists(...)`: a duplicate email shows an error.
5. `registerUser(...)`: on success redirect to `login.jsp?registered=1`; otherwise show an error.
6. `catch (SQLException e)`: logs the real error on the server with `log(...)` and shows a simple message to the user.
7. `fail(...)` sets `errorMessage`, `backUrl` and `backText` so `Usererror.jsp` can show the message and a correct "Back" button.

### 7.2 `LoginServlet` (`/login`)

1. Checks that email and password are not blank.
2. Calls `userDAO.login(...)`. If it returns `null`, the message is **"Invalid email or password."** The same text is used for "no such email" and "wrong password", so an attacker cannot find out which emails exist.
3. On success:
   * `old.invalidate()` destroys any existing session, then `getSession(true)` makes a **new** one. This protects against **session fixation** (an attacker planting a known session ID before login).
   * `session.setAttribute("user", user)` is what marks the user as logged in.
   * `setMaxInactiveInterval(30 * 60)` expires the session after 30 minutes without activity.
4. Redirects to `/dashboard`.

### 7.3 `LogoutServlet` (`/logout`)

Gets the session with `getSession(false)` (`false` = do not create a new one), calls `invalidate()` to delete all session data, and redirects to `login.jsp?loggedout=1`.

### 7.4 `DashboardServlet` (`/dashboard`)

1. `getAllProducts()` and `req.setAttribute("products", products)`.
2. **Flash message:** the add/edit/delete servlets save a message in the *session* (`successMsg`) just before redirecting. Here it is copied into the request and **removed** from the session, so the message appears once and disappears on refresh.
3. Forwards to `productDashboard.jsp`. On `SQLException` it forwards to `producterror.jsp`.

This is why the JSP is never opened directly: the data must be loaded by the servlet first.

### 7.5 `AddProductServlet` (`/addProduct`)

* `doGet`: just shows `AddProduct.jsp`.
* `doPost`:
  1. Validate with `ValidationUtil.validateProduct`. If invalid, put the message **and the typed values** back into the request and show the form again, so the user does not have to retype.
  2. Build a `Product` (`new BigDecimal(...)` and `Integer.parseInt(...)` are safe here because validation already passed).
  3. `addProduct(p)` then success message + redirect to the dashboard. If the insert fails or throws `SQLException`, forward to `producterror.jsp`.

### 7.6 `EditProductServlet` (`/editProduct`)

* `parsePid(text)`: converts text to an `Integer` or returns `null`. It catches `NumberFormatException` (text like "abc") and `NullPointerException` (no `pid` parameter).
* `doGet` (open the edit page for `?pid=5`):
  1. Invalid PID gives an error page.
  2. `getProductById` returns `null` gives "Product with ID x does not exist." (requirement for Module 5).
  3. Otherwise put `pid`, `pname`, `pprice`, `pqty` in the request and forward to `EditProduct.jsp`. `toPlainString()` prevents scientific notation like `1E+4`.
* `doPost` (save changes): validates; if invalid shows the form again with the message; checks the product still exists (someone may have deleted it meanwhile); updates; then success message + redirect.

### 7.7 `DeleteProductServlet` (`/deleteProduct`)

* **Only `doPost` deletes.** `doGet` just redirects to the dashboard. If delete worked through a normal link, a browser prefetching links or a search crawler could delete data.
* `deleteProduct(pid)` returns `false` when no row matched, which gives "does not exist" (requirement for Module 6).
* On success: flash message and redirect, which refreshes the dashboard.

---

## 8. `AuthFilter` (session management)

A **filter** runs *before* the servlet/JSP it protects. `@WebFilter(urlPatterns = {...})` lists the protected URLs.

1. Sets headers `Cache-Control: no-store` etc., so the browser does not keep a copy of these pages. After logout, the Back button then cannot display the dashboard.
2. `req.getSession(false)` gets the session without creating one. If there is no session or no `user` attribute, it redirects to `login.jsp?login=required` and returns (the request stops here).
3. Otherwise `chain.doFilter(...)` lets the request continue.

Login, registration and the error pages are not listed, so they stay public.

---

## 9. JSP pages (views)

Common things used in the pages:

* `${pageContext.request.contextPath}` gives `/ProductManagementSystem`, so links work wherever the app is deployed.
* `<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>` enables JSTL tags.
* `<c:out value="${...}"/>` prints a value **with HTML escaping**, so a product named `<script>alert(1)</script>` is shown as text and not executed (prevents XSS). Printing `${...}` directly would not escape in all places.
* `<c:if>`, `<c:forEach>`, `<c:choose>` are the JSTL "if", "for" and "if/else".

### 9.1 `register.jsp`
A form posting to `/register`. User ID is a **disabled** field showing "Auto-generated", because the database creates the ID (the assignment lists the field, but the table uses `AUTO_INCREMENT`). The HTML5 attributes `required`, `type="email"`, `pattern`, `minlength` give instant browser-side validation. The submit button says "New User" as in the assignment diagram.

### 9.2 `login.jsp`
A form posting to `/login`. At the top, `<c:if test="${param.registered == '1'}">` shows a message based on a query parameter. The message text itself is written in the page, not taken from the URL, so nobody can inject text through the URL.

### 9.3 `productDashboard.jsp`
* The first `<c:if>` checks `requestScope['javax.servlet.forward.request_uri']`. That attribute exists only when the page was reached by a **forward** from a servlet. If the page was opened directly, there is no product list, so it redirects to `/dashboard`.
* The top bar shows `sessionScope.user.name` (from the session) and the Logout link.
* The success message (flash) is shown if present.
* `<c:forEach var="p" items="${products}">` builds one table row per product with columns PID, Pname, Price, Pqty, Operations.
* **Edit** is a link to `/editProduct?pid=...`.
* **Delete** is a small form that POSTs the PID. `onsubmit="return confirm('...')"` shows the confirmation popup; if the user clicks Cancel, `confirm` returns `false` and the form is **not** sent.
* If the list is empty, a "No products found" row is shown.
* The **+ Add New Product** button links to `/addProduct`.

### 9.4 `AddProduct.jsp`
Form posting to `/addProduct`. `type="number" min="0" step="0.01"` for price and `step="1"` for quantity give browser validation. Values are refilled from request attributes (`value="<c:out value='${pname}'/>"`) after a server-side validation failure. PID is display-only (auto-generated).

### 9.5 `EditProduct.jsp`
Form posting to `/editProduct`, pre-filled with the current values. The PID input has `readonly`, so it is visible and submitted but cannot be edited. Like the dashboard, it redirects to `/dashboard` if opened directly.

### 9.6 `Usererror.jsp`
Shows `errorMessage` and a button back to `backUrl` (registration or login). If no message was given (for example a 404 page), it shows a default message and a link to the login page. It is also registered in `web.xml` as the 404/500 error page.

### 9.7 `producterror.jsp`
Shows the product-related error (insert/update/delete failure, product not found, database problem) and a "Back to Dashboard" button. It is protected by `AuthFilter`.

---

## 10. `WEB-INF/web.xml` (deployment descriptor)

* `<welcome-file>register.jsp</welcome-file>`: opening the app URL shows the registration page, which is step 1 of the application flow.
* `<session-timeout>30</session-timeout>`: default session lifetime in minutes.
* `<error-page>`: shows `Usererror.jsp` for 404 and 500 errors instead of the server's raw error page.
* Servlets and the filter are registered with annotations, so no `<servlet>` entries are needed.

---

## 11. `css/style.css`

Styling only: centred white "cards", form fields, coloured buttons (`.btn`, `.green`, `.red`, `.gray`), message boxes (`.alert.success`, `.alert.error`, `.alert.info`), and the dashboard table. It has no effect on how the program works.

---

## 12. Summary of the CRUD operations

| Operation | Page | Servlet | DAO method | SQL |
|---|---|---|---|---|
| Create | `AddProduct.jsp` | `AddProductServlet` | `addProduct` | `INSERT` |
| Read | `productDashboard.jsp` | `DashboardServlet` | `getAllProducts`, `getProductById` | `SELECT` |
| Update | `EditProduct.jsp` | `EditProductServlet` | `updateProduct` | `UPDATE` |
| Delete | dashboard Delete button | `DeleteProductServlet` | `deleteProduct` | `DELETE` |

---

## 13. Viva questions you may be asked

1. **Why PreparedStatement instead of Statement?** It prevents SQL injection and is precompiled.
2. **Difference between forward and redirect?** Forward happens inside the server (same request, URL unchanged). Redirect makes the browser send a new request (URL changes).
3. **Why redirect after a successful POST?** To avoid duplicate submissions when the user refreshes (Post/Redirect/Get).
4. **How is the session used?** The logged-in `User` is stored in the `HttpSession`; `AuthFilter` checks it on each protected request.
5. **Why hash passwords?** So that a database leak does not reveal them; the salt makes each hash unique.
6. **What is a DAO?** A class that contains all database access code for one entity, separating it from servlets and JSPs.
7. **Why is delete a POST?** GET must be safe (no data change); links can be prefetched or opened by crawlers.
8. **What does `<c:out>` do?** Prints a value with HTML escaping to stop XSS.
9. **What is MVC here?** Model = `User`/`Product`, View = JSP, Controller = servlets.
