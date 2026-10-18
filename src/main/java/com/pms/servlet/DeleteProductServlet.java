package com.pms.servlet;

import com.pms.dao.ProductDAO;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Module 6: Delete Product.
 * Only POST is supported: a delete changes data, so it must not be triggered by a plain link.
 * The confirmation prompt is shown by JavaScript in productDashboard.jsp before the form is sent.
 */
@WebServlet("/deleteProduct")
public class DeleteProductServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int pid;
        try {
            pid = Integer.parseInt(req.getParameter("pid").trim());
        } catch (NumberFormatException | NullPointerException e) {
            fail(req, resp, "Invalid product ID.");
            return;
        }

        try {
            if (productDAO.deleteProduct(pid)) {
                req.getSession().setAttribute("successMsg", "Product deleted successfully.");
                resp.sendRedirect(req.getContextPath() + "/dashboard");   // refreshes the dashboard
            } else {
                fail(req, resp, "Product with ID " + pid + " does not exist, so it could not be deleted.");
            }
        } catch (SQLException e) {
            log("Delete failed", e);
            fail(req, resp, "Product deletion failed because of a database problem.");
        }
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String message)
            throws ServletException, IOException {
        req.setAttribute("errorMessage", message);
        req.getRequestDispatcher("/producterror.jsp").forward(req, resp);
    }
}
