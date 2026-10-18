# Product Management System (Advanced Java)

A web application built with **Servlets, JSP, JSTL, JDBC and MySQL**.
Users can register, log in, and manage products (add, view, edit, delete) from a dashboard.

## 1. What you need installed

| Software | Version | Notes |
|---|---|---|
| JDK | 11 or newer | `java -version` to check |
| Apache Tomcat | **9.x** | Must be Tomcat **9**. Tomcat 10+ uses `jakarta.*` packages and this project uses `javax.*` |
| MySQL Server | 8.x | Workbench is optional |
| Maven | 3.6+ | Only if you build from the command line (Eclipse/IntelliJ include Maven) |

## 2. Project structure

```
ProductManagementSystem/
├── pom.xml                              Maven build file (dependencies)
├── sql/product_management.sql           Database + tables script
├── README.md                            This file
├── docs/
│   ├── CODE_EXPLANATION.md              Line-by-line style explanation of every file
│   └── PROJECT_REPORT.md                Short report on the application flow
└── src/main
    ├── java/com/pms
    │   ├── util/    DBConnection, PasswordUtil, ValidationUtil
    │   ├── model/   User, Product
    │   ├── dao/     UserDAO, ProductDAO
    │   ├── servlet/ Register, Login, Logout, Dashboard, AddProduct, EditProduct, DeleteProduct
    │   └── filter/  AuthFilter
    ├── resources/db.properties          Database URL / user / password
    └── webapp
        ├── register.jsp, login.jsp
        ├── productDashboard.jsp, AddProduct.jsp, EditProduct.jsp
        ├── Usererror.jsp, producterror.jsp
        ├── css/style.css
        └── WEB-INF/web.xml
```

## 3. Setup and run

### Step 1: Create the database
Open a terminal (or MySQL Workbench) and run the script:

```
mysql -u root -p < sql/product_management.sql
```

This creates the `product_management` database with the `users` and `products` tables
(and one sample product, "Mobile").

### Step 2: Set your MySQL password
Open `src/main/resources/db.properties` and change the user and password if yours are different:

```
db.url=jdbc:mysql://localhost:3306/product_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=root
```

### Step 3A: Run from Eclipse (easiest)
1. **File → Import → Maven → Existing Maven Projects** and select the project folder.
2. Add a **Tomcat 9** server (Window → Preferences → Server → Runtime Environments).
3. Right-click the project → **Run As → Run on Server** → choose Tomcat 9.
4. Open: `http://localhost:8080/ProductManagementSystem/`

### Step 3B: Run from the command line
```
mvn clean package
```
Copy `target/ProductManagementSystem.war` into Tomcat's `webapps` folder, start Tomcat
(`bin/startup.bat` on Windows, `bin/startup.sh` on Linux/macOS), then open:

`http://localhost:8080/ProductManagementSystem/`

### Step 3C: IntelliJ IDEA
Open the folder as a Maven project, add a **Tomcat 9** run configuration, set the deployment artifact to
`ProductManagementSystem:war exploded`, and set the application context to `/ProductManagementSystem`.

## 4. Application flow

1. `register.jsp` opens first and the user creates an account. Success goes to `login.jsp`, failure goes to `Usererror.jsp`.
2. The user logs in with email and password. Success opens the **Product Dashboard**, failure goes to `Usererror.jsp`.
3. On the dashboard the user can **Add**, **Edit** or **Delete** products. Delete asks for confirmation first.
4. Every operation shows a success message (on the dashboard) or an error (`producterror.jsp`).
5. **Logout** ends the session. After that, the product pages cannot be opened until the user logs in again.

## 5. Test checklist (and screenshots to take for submission)

Take a screenshot at each point; the assignment asks for them.

| # | What to do | Expected result |
|---|---|---|
| 1 | Open the registration page | Form with User ID (auto), Name, Email, Mobile, Password |
| 2 | Register with valid details | Redirect to login with "Registration successful" |
| 3 | Register again with the same email | `Usererror.jsp`: "already registered" |
| 4 | Register with mobile `123` | `Usererror.jsp` (or browser validation message) |
| 5 | Log in with a wrong password | `Usererror.jsp`: "Invalid email or password." |
| 6 | Log in correctly | Product Dashboard with the table |
| 7 | Add a product | Redirect to dashboard, "Product added successfully." |
| 8 | Add with price `-5` or quantity `2.5` | Form shown again with an error message |
| 9 | Click Edit | Edit page with PID read-only and current values filled in |
| 10 | Save an edit | Dashboard, "Product updated successfully." |
| 11 | Open `.../editProduct?pid=9999` | `producterror.jsp`: product does not exist |
| 12 | Click Delete, then Cancel | Nothing happens |
| 13 | Click Delete, then OK | Product removed, "Product deleted successfully." |
| 14 | Click Logout | Login page with "logged out" message |
| 15 | After logout open `.../dashboard` | Redirected to login ("Please login to continue.") |

## 6. Troubleshooting

| Problem | Fix |
|---|---|
| `HTTP 404` for every page | Wrong URL. The context path is `/ProductManagementSystem` (the WAR name). |
| `Access denied for user 'root'` | Fix `db.user` / `db.password` in `db.properties`, rebuild and redeploy. |
| `Unknown database 'product_management'` | Run `sql/product_management.sql` first. |
| `ClassNotFoundException: javax.servlet...` or JSP errors | You are using Tomcat 10/11. Use **Tomcat 9**. |
| `Communications link failure` | MySQL is not running or the port in `db.url` is wrong. |
| Changes not visible | Rebuild the project and restart/redeploy on Tomcat. |

## 7. Security features included

* Passwords are stored as **salted PBKDF2 hashes**, never as plain text.
* All SQL uses **PreparedStatement** (prevents SQL injection).
* All output uses `<c:out>` (prevents cross-site scripting).
* Delete works only via **POST**; the product pages are guarded by a **login filter**.
* A new session is created at login (prevents session fixation); sessions expire after 30 minutes.
