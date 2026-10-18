package com.pms.servlet;

import com.pms.dao.ProductDAO;
import com.pms.model.Product;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Module 3: Product Dashboard. Loads all products and forwards to productDashboard.jsp. */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<Product> products = productDAO.getAllProducts();
            req.setAttribute("products", products);

            // "Flash" message: set by add/edit/delete servlets, shown once, then removed
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("successMsg") != null) {
                req.setAttribute("successMsg", session.getAttribute("successMsg"));
                session.removeAttribute("successMsg");
            }

            req.getRequestDispatcher("/productDashboard.jsp").forward(req, resp);

        } catch (SQLException e) {
            log("Could not load products", e);
            req.setAttribute("errorMessage", "Could not load products because of a database problem.");
            req.getRequestDispatcher("/producterror.jsp").forward(req, resp);
        }
    }
}
