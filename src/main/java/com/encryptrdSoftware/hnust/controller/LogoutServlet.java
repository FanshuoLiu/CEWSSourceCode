package com.encryptrdSoftware.hnust.controller;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 获取当前会话
        HttpSession session = req.getSession(false);
        if (session != null) {
            // 使会话失效
            session.invalidate();
        }
        // 重定向到登录页面
        resp.sendRedirect("login.jsp");
    }
}
