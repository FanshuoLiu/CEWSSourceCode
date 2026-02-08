package com.encryptrdSoftware.hnust.controller;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/listFiles")
public class ListFilesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        File uploadDir = new File(getServletContext().getRealPath("/uploads"));
        // 用于存储文件名,一个集合对应一个select标签
        List<String> shpFiles = new ArrayList<>();
        List<String> bmpFiles = new ArrayList<>();
        List<String> encryptedFiles = new ArrayList<>();
        List<String> properties = new ArrayList<>();

        // 遍历目录，填充文件名列表
        String[] files = uploadDir.list();
        if (files != null) {
            for (String file : files) {
                if (file.endsWith(".shp")) {
                    shpFiles.add(file);
                } else if (file.endsWith(".bmp")) {
                    bmpFiles.add(file);
                }
                if (file.contains("加密")&&file.endsWith(".shp")) {
                    encryptedFiles.add(file);
                }
                if (file.endsWith(".zip")&&(file.contains("加密")||file.contains("水印"))){
                }
                if (file.endsWith(".bin")){
                    properties.add(file);
                }
            }
        }
        Map<String, List<String>> fileMap = new HashMap<>();
        fileMap.put("shp", shpFiles);
        fileMap.put("bmp", bmpFiles);
        fileMap.put("encrypted", encryptedFiles);
        fileMap.put("properties", properties);

// 将 Map 转换为 JSON 格式并返回
        out.write(new Gson().toJson(fileMap));
        System.out.println("返回成功");
    }
}