package com.encryptrdSoftware.hnust.controller;

import com.encryptrdSoftware.hnust.util.ZipCompressorUtils;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/zipFiles")
public class listZipFilesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, List<File>> listMap = ZipCompressorUtils.getPackage();
        Set<Map.Entry<String, List<File>>> set = listMap.entrySet();
        List<String> zipFiles=new ArrayList();
        PrintWriter out = resp.getWriter();
        for (Map.Entry<String, List<File>> entry : set) {
            String key = entry.getKey();
            List<File> value = entry.getValue();
            List<String> filePaths = new ArrayList<>();
            for (File file : value) {
                filePaths.add(file.getAbsolutePath());
            }
            String s = ZipCompressorUtils.zipFiles(filePaths, UploadServlet.Path + File.separator + key + ".zip");
            zipFiles.add(s);
        }
        File file=new File(UploadServlet.Path);
        for (File f:file.listFiles()){
            if (f.getName().endsWith(".bin")){
                zipFiles.add(f.getName());
            }
        }
        Map<String, List<String>> fileMap = new HashMap<>();
        fileMap.put("download", zipFiles);
        out.write(new Gson().toJson(fileMap));
    }
}
