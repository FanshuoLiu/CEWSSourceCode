package com.encryptrdSoftware.hnust.controller;

import com.encryptrdSoftware.hnust.dao.FileDAO;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/delete")
public class deleteFilesServlet extends HttpServlet {

    private FileDAO fileDAO;
    @Override
    public void init(){
        fileDAO=new FileDAO();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        fileDAO.deleteFile((Integer) req.getSession().getAttribute("userId"));
        resp.getWriter().write("{\"status\":\"success\",\"message\":\"删除成功\"}");

    }
}
