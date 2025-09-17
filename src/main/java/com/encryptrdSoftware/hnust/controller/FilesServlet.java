package com.encryptrdSoftware.hnust.controller;

import com.encryptrdSoftware.hnust.dao.FileDAO;
import com.encryptrdSoftware.hnust.model.FileData;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/files")
public class FilesServlet extends HttpServlet {
    private FileDAO fileDAO;
    @Override
    public void init(){
        fileDAO=new FileDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        int id = (Integer)req.getSession().getAttribute("userId");
        List<String> encryptedFiles = new ArrayList<>();
        List<String> watermarkedFiles = new ArrayList<>();
        List<String> encryptedAndWatermarkedFiles = new ArrayList<>();
        try {

            List<FileData> files = fileDAO.getFilesByUserId(id);
            for (FileData file : files) {
               if (files!=null){
                   if (file.getEncrypted()==true&&file.getWatermarked()==true){
                       encryptedAndWatermarkedFiles.add(file.getFileName());
                   } else if (file.getWatermarked()==true) {
                       watermarkedFiles.add(file.getFileName());
                   } else if (file.getEncrypted()==true) {
                       encryptedFiles.add(file.getFileName());
                   }
               }else {
                   resp.getWriter().write("{\"error\":\"无查询结果\"}");
               }
            }
            Map<String, List<String>> fileMap = new HashMap<>();
            fileMap.put("encrypted", encryptedFiles);
            fileMap.put("watermarked", watermarkedFiles);
            fileMap.put("encryptedAndWatermarked", encryptedAndWatermarkedFiles);
            out.write(new Gson().toJson(fileMap));
        }catch (Exception e){
            out.print("{\"error\":\"查询失败\"}");
        }finally {
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
