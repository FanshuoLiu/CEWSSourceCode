package com.encryptrdSoftware.hnust.options;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class corsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("Access-Control-Allow-Origin", "localhost");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        if ("OPTIONS".equalsIgnoreCase(((javax.servlet.http.HttpServletRequest) request).getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    @Override
    public void destroy() {}
}
