package com.pms.servlet;

import com.pms.dao.ProductDAO;
import com.pms.model.Product;
import com.pms.util.ValidationUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Module 5: Edit Product. GET loads the product into the form, POST saves the changes. */
@WebServlet("/editProduct")
public class EditProductServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer pid = parsePid(req.getParameter("pid"));
        if (pid == null) {
            fail(req, resp, "Invalid product ID.");
            return;
        }

        try {
            Product p = productDAO.getProductById(pid);
            if (p == null) {
                fail(req, resp, "Product with ID " + pid + " does not exist.");
                return;
            }
            // Put the current values in request attributes for EditProduct.jsp
            req.setAttribute("pid", p.getPid());
            req.setAttribute("pname", p.getPname());
            req.setAttribute("pprice", p.getPprice().toPlainString());
            req.setAttribute("pqty", p.getPqty());
            req.getRequestDispatcher("/EditProduct.jsp").forward(req, resp);

        } catch (SQLException e) {
            log("Could not load product", e);
            fail(req, resp, "Could not load the product because of a database problem.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        Integer pid = parsePid(req.getParameter("pid"));
        String pname = req.getParameter("pname");
        String pprice = req.getParameter("pprice");
        String pqty = req.getParameter("pqty");

        if (pid == null) {
            fail(req, resp, "Invalid product ID.");
            return;
        }

        // Validate. On a mistake, show the edit form again with the typed values.
        String error = ValidationUtil.validateProduct(pname, pprice, pqty);
        if (error != null) {
            req.setAttribute("errorMessage", error);
            req.setAttribute("pid", pid);
            req.setAttribute("pname", pname);
            req.setAttribute("pprice", pprice);
            req.setAttribute("pqty", pqty);
            req.getRequestDispatcher("/EditProduct.jsp").forward(req, resp);
            return;
        }

        try {
            if (productDAO.getProductById(pid) == null) {
                fail(req, resp, "Product with ID " + pid + " does not exist.");
                return;
            }

            Product p = new Product(pid, pname.trim(),
                    new BigDecimal(pprice.trim()), Integer.parseInt(pqty.trim()));

            if (productDAO.updateProduct(p)) {
                req.getSession().setAttribute("successMsg", "Product updated successfully.");
                resp.sendRedirect(req.getContextPath() + "/dashboard");
            } else {
                fail(req, resp, "Product update failed. Please try again.");
            }
        } catch (SQLException e) {
            log("Update failed", e);
            fail(req, resp, "Product update failed because of a database problem.");
        }
    }

    /** Converts text to an int, or returns null if it is not a valid number. */
    private Integer parsePid(String text) {
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String message)
            throws ServletException, IOException {
        req.setAttribute("errorMessage", message);
        req.getRequestDispatcher("/producterror.jsp").forward(req, resp);
    }
}
