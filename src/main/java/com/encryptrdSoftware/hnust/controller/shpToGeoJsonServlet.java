package com.encryptrdSoftware.hnust.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;


@WebServlet("/convert")
public class shpToGeoJsonServlet extends HttpServlet {
    private ObjectMapper objectMapper = new ObjectMapper(); // 创建 ObjectMapper 实例
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
            doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("调用了转换方法");
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String filename = null;
        // 读取请求体内容
        StringBuilder requestBody = new StringBuilder();
        String line1;
        try (BufferedReader reader = req.getReader()) {
            while ((line1 = reader.readLine()) != null) {
                requestBody.append(line1);
            }
        }
        String jsonData = requestBody.toString();
        if (jsonData.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "请求体为空");
            return;
        }
        try {
            // 将 JSON 字符串解析为 Map 或者您可以定义一个对应的 Java Bean
            Map<String, Object> requestData = objectMapper.readValue(jsonData, Map.class);

            // 从 Map 中获取参数
            filename = (String) requestData.get("filename");
        }catch (Exception e){
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "处理请求错误");
        }

        if (filename == null || filename.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "请选择一个文件");
            return;
        }

        String realfilename = UploadServlet.Path + "/" + filename;
        File shpFile = new File(realfilename);
        if (!shpFile.exists() || !shpFile.getName().endsWith(".shp")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "未找到shp文件");
            return;
        }

        String geoJson;
        try {
            geoJson = convertShpToGeoJson(realfilename);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "转换失败");
            return;
        }

        // 返回 GeoJSON 字符串
        resp.getWriter().write(geoJson);
        resp.getWriter().flush();
    }

    private static String convertShpToGeoJson(String file) throws Exception {
        File shpFile = new File(file);
        FileDataStore store = FileDataStoreFinder.getDataStore(shpFile);
        SimpleFeatureCollection collection = store.getFeatureSource().getFeatures();
        FeatureJSON featureJSON = new FeatureJSON();
        StringWriter writer = new StringWriter();
        featureJSON.writeFeatureCollection(collection, writer);
        return writer.toString();
    }
}
