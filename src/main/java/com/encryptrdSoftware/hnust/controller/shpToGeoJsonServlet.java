package com.encryptrdSoftware.hnust.controller;
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


@WebServlet("/convert")
public class shpToGeoJsonServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
            doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String filePath = req.getParameter("filename");
        if (filePath == null || filePath.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "请选择一个文件");
            return;
        }

        String realFilePath = UploadServlet.Path + "/" + filePath;
        File shpFile = new File(realFilePath);
        if (!shpFile.exists() || !shpFile.getName().endsWith(".shp")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "未找到shp文件");
            return;
        }

        String geoJson;
        try {
            geoJson = convertShpToGeoJson(realFilePath);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to convert SHP to GeoJSON");
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
