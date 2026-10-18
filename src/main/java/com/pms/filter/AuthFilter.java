package com.pms.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Session management: protects every product page.
 * If there is no logged-in user in the session, the visitor is sent to the login page.
 * It also stops the browser from caching these pages, so after logout the
 * Back button cannot show the dashboard again.
 */
@WebFilter(urlPatterns = {
        "/dashboard", "/addProduct", "/editProduct", "/deleteProduct",
        "/productDashboard.jsp", "/AddProduct.jsp", "/EditProduct.jsp", "/producterror.jsp"
})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("user") != null;

        if (!loggedIn) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?login=required");
            return;
        }
        chain.doFilter(request, response);
    }
}
