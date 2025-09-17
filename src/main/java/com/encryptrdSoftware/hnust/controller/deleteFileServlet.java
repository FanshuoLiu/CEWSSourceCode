package com.encryptrdSoftware.hnust.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@WebServlet("/deleteFiles")
public class deleteFileServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8"); // 更改为 JSON 响应
        // 指定要删除文件的目录
       if (UploadServlet.Path!=null){
           String directoryPath = UploadServlet.Path;
           File directory = new File(directoryPath);
           if (directory.exists() && directory.isDirectory()) {
               File[] files = directory.listFiles();
               if (files != null) {
                   for (File file : files) {
                       if (file.isFile()) {
                           file.delete();
                       }
                   }
               }
           }
           response.getWriter().write("{\"status\":\"success\",\"message\":\"解密成功\"}");
       }else {
           response.getWriter().write("{\"status\":\"error\",\"message\":\"删除失败\"}");
           return;
       }
    }
}
