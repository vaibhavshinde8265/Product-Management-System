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

/** Module 4: Add New Product. GET shows the form, POST saves the product. */
@WebServlet("/addProduct")
public class AddProductServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/AddProduct.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String pname = req.getParameter("pname");
        String pprice = req.getParameter("pprice");
        String pqty = req.getParameter("pqty");

        // 1. Validate. On a mistake, show the form again with the message and the typed values.
        String error = ValidationUtil.validateProduct(pname, pprice, pqty);
        if (error != null) {
            req.setAttribute("errorMessage", error);
            req.setAttribute("pname", pname);
            req.setAttribute("pprice", pprice);
            req.setAttribute("pqty", pqty);
            req.getRequestDispatcher("/AddProduct.jsp").forward(req, resp);
            return;
        }

        // 2. Insert into the database
        try {
            Product p = new Product(0, pname.trim(),
                    new BigDecimal(pprice.trim()), Integer.parseInt(pqty.trim()));

            if (productDAO.addProduct(p)) {
                req.getSession().setAttribute("successMsg", "Product added successfully.");
                resp.sendRedirect(req.getContextPath() + "/dashboard");   // redirect after POST
            } else {
                fail(req, resp, "Product could not be added. Please try again.");
            }
        } catch (SQLException e) {
            log("Insert failed", e);
            fail(req, resp, "Product insertion failed because of a database problem.");
        }
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String message)
            throws ServletException, IOException {
        req.setAttribute("errorMessage", message);
        req.getRequestDispatcher("/producterror.jsp").forward(req, resp);
    }
}
