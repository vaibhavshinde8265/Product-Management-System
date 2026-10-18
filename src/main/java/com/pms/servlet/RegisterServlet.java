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

/** Module 1: User Registration. Handles the form posted from register.jsp. */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String mobile = req.getParameter("mobile");
        String password = req.getParameter("password");

        // 1. Validate the input
        String error = ValidationUtil.validateUser(name, email, mobile, password);
        if (error != null) {
            fail(req, resp, error);
            return;
        }

        name = name.trim();
        email = email.trim().toLowerCase();
        mobile = mobile.trim();

        try {
            // 2. Email must be unique
            if (userDAO.emailExists(email)) {
                fail(req, resp, "This email is already registered. Please login or use another email.");
                return;
            }

            // 3. Save the user
            if (userDAO.registerUser(new User(name, email, mobile, password))) {
                resp.sendRedirect(req.getContextPath() + "/login.jsp?registered=1");
            } else {
                fail(req, resp, "Registration failed. Please try again.");
            }
        } catch (SQLException e) {
            log("Registration failed", e);
            fail(req, resp, "Registration failed because of a database problem. Please try again later.");
        }
    }

    /** Shows Usererror.jsp with a message and a link back to the registration page. */
    private void fail(HttpServletRequest req, HttpServletResponse resp, String message)
            throws ServletException, IOException {
        req.setAttribute("errorMessage", message);
        req.setAttribute("backUrl", "register.jsp");
        req.setAttribute("backText", "Back to Registration");
        req.getRequestDispatcher("/Usererror.jsp").forward(req, resp);
    }
}
