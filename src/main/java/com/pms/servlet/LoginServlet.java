package com.pms.servlet;

import com.pms.dao.UserDAO;
import com.pms.model.User;
import com.pms.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Module 2: User Login. Creates the session when the credentials are correct. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (ValidationUtil.isBlank(email) || ValidationUtil.isBlank(password)) {
            fail(req, resp, "Email and password are required.");
            return;
        }

        try {
            User user = userDAO.login(email.trim().toLowerCase(), password);

            if (user == null) {
                // Same message for "no such email" and "wrong password" (does not reveal which one)
                fail(req, resp, "Invalid email or password.");
                return;
            }

            // Start a fresh session (prevents session fixation) and remember the user
            HttpSession old = req.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(30 * 60);     // 30 minutes of inactivity

            resp.sendRedirect(req.getContextPath() + "/dashboard");

        } catch (SQLException e) {
            log("Login failed", e);
            fail(req, resp, "Login failed because of a database problem. Please try again later.");
        }
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String message)
            throws ServletException, IOException {
        req.setAttribute("errorMessage", message);
        req.setAttribute("backUrl", "login.jsp");
        req.setAttribute("backText", "Back to Login");
        req.getRequestDispatcher("/Usererror.jsp").forward(req, resp);
    }
}
