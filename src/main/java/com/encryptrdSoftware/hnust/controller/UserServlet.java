package com.encryptrdSoftware.hnust.controller;

import com.encryptrdSoftware.hnust.dao.UserDAO;
import com.encryptrdSoftware.hnust.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebServlet("/UserServlet")
public class UserServlet extends HttpServlet {
    private UserDAO userDAO=new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8"); // 更改为 JSON 响应
        String action = req.getParameter("action");
        if ("register".equals(action)) {
            String username = req.getParameter("username");
            String password = req.getParameter("password"); // 密码应进行哈希处理
            String email = req.getParameter("email");
            User newUser = new User(username, password, email);
            if (userDAO.register(newUser)) {
                resp.getWriter().write("{\"status\":\"success\",\"message\":\"注册成功\"}");
            } else {
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"该用户名已被注册\"}");
            }
        } else if ("login".equals(action)) {
            //通过参数false获取当前会话，如果不存在则返回 null
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("username") != null) {
                //用户已经登录，阻止重复登录
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"用户已登录\"}");
                return;
            }
            String username = req.getParameter("username");
            // 密码应进行哈希处理
            String password = req.getParameter("password");

            User user = userDAO.loginUser(username, password);
            System.out.println(user);
            if (user != null) {
                session = req.getSession();
                session.setAttribute("username", user.getUsername());
                session.setAttribute("userId", user.getId());
                resp.getWriter().write("{\"status\":\"success\",\"message\":\"登录成功\"}");
            } else {
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"账号或密码错误\"}");
            }
        }
    }
}
