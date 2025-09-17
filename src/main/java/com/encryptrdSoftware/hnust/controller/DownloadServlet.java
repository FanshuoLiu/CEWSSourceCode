package com.encryptrdSoftware.hnust.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String filename = request.getParameter("filename");

        // 检查文件名是否为空
        if (filename == null || filename.isEmpty()) {
            response.getWriter().println("文件名不能为空");
            return;
        }
        // 定义文件路径
        File downloadFile = new File(request.getServletContext().getRealPath("/uploads"), filename);
        // 检查文件是否存在
        if (downloadFile.exists() && !downloadFile.isDirectory()) {
            // 设置响应内容类型
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + new String(filename.getBytes("UTF-8"), "ISO-8859-1") + "\"");

            // 文件输入流
            try (FileInputStream inStream = new FileInputStream(downloadFile);
                 OutputStream outStream = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                response.getWriter().println("下载文件时发生错误: " + e.getMessage());
            }
        } else {
            response.getWriter().println("文件不存在或不是有效文件");
        }
    }
}