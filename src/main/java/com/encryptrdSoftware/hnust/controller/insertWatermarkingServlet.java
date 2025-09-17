package com.encryptrdSoftware.hnust.controller;

import com.encryptrdSoftware.hnust.model.*;
import com.encryptrdSoftware.hnust.util.SecretUtils;
import com.encryptrdSoftware.hnust.util.StringUtils;
import com.encryptrdSoftware.hnust.util.WatermarkingUtils;
import org.gdal.gdal.gdal;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@WebServlet("/insert")
public class insertWatermarkingServlet extends HttpServlet {
    static {
        gdal.AllRegister();
        ogr.RegisterAll();
    }
    //比特长度
    static List<String> strings=new ArrayList<>();
    //图片大小
    static List<Integer> pic=new ArrayList<>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        pic.add(64);
        pic.add(64);
        String bmpPath = req.getParameter("filename2");
        String filePath = req.getParameter("filename1");
        String realFilePath = UploadServlet.Path + "/" + filePath;
        String realBmpPath = UploadServlet.Path + "/" + bmpPath;
        System.out.println("水印路径:" + realBmpPath);
        //获取shp文件的属性列表
        List<LinkedHashMap<String, Object>> maps = SecretUtils.readShapefile(new File(realFilePath));

        // 打开Shapefile
        Layer layer = Domain.getLayer(new File(realFilePath));
        Map<String, Object> map = Domain.SHPtoList(new File(realFilePath));
        List<Shape> geometries = (List<Shape>) map.get("geometries");
        List<Point> points1 = (List<Point>) map.get("points1");
        List<Shape> watermarkedShape=new ArrayList<>();
        int num = (int) map.get("num");
        //计算要素的数量
        long n = Domain.calPointsNumber(layer);

        System.out.println("要素数量:" + num);
        //获取图层的要素类型
        int type = layer.GetGeomType();
        System.out.println("type:" + type);
        String s= StringUtils.modifyString(filePath);
        if (type==1){
            int index=0;
            //计算嵌入水印后的坐标
            strings = WatermarkingUtils.initString(realBmpPath, (int) n);
            System.out.println("字符串:" + strings);
            pic.add(WatermarkingUtils.width);
            pic.add(WatermarkingUtils.height);
            List<Point> points = WatermarkingUtils.calcuWatermarking(points1, strings, index);
            try {
                SecretUtils.createSHP(points, layer, filePath.substring(0, filePath.lastIndexOf(".")), "水印",maps);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            String filename= s;
            resp.getWriter().write("{\"status\":\"success\",\"message\":\"水印" + bmpPath + "嵌入成功\"}");
            return;
        }

        //计算除点外的要素
        int index=0;
        strings = WatermarkingUtils.initString(realBmpPath, num);
        pic.add(WatermarkingUtils.width);
        pic.add(WatermarkingUtils.height);
        for (Shape shape : geometries){
            if (shape instanceof Line){
                //计算line的水印坐标
                Line line = (Line) shape;
                List<Point> watermarkedPoints = WatermarkingUtils.calcuWatermarking(line.getPoints(), strings,index);
                watermarkedShape.add(new Line(watermarkedPoints));
            } else if (shape instanceof MultiLine) {
                MultiLine multiLine = (MultiLine) shape;
                List<Line> lines = new ArrayList<>();
                for (Line line : multiLine.getLines()){
                    List<Point> watermarkedPoints = WatermarkingUtils.calcuWatermarking(line.getPoints(), strings,index);
                    lines.add(new Line(watermarkedPoints));
                }
                watermarkedShape.add(new MultiLine(lines));
            } else if (shape instanceof Polygon) {
                Polygon polygon = (Polygon) shape;
                if (filePath.contains("加密")||filePath.contains("解密")||filePath.contains("提取")||filePath.contains("水印")){
                    polygon = polygon.removeLastPoint();
                }
                //计算外环
                List<Point> exteriors = polygon.getExteriors();
                List<Point> exteriorPoints = WatermarkingUtils.calcuWatermarking(exteriors, strings,index);
                //若有内环，计算内环
                if (polygon.getInteriors()!=null){
                    List<List<Point>> watermarkedPolygon = new ArrayList<>();
                    for (List<Point> list : polygon.getInteriors()){
                        List<Point> watermarkedPoints = WatermarkingUtils.calcuWatermarking(list,strings,index);
                        watermarkedPolygon.add(watermarkedPoints);
                    }
                    watermarkedShape.add(new Polygon(exteriorPoints, watermarkedPolygon));
                }else {
                    watermarkedShape.add(new Polygon(exteriorPoints,null));
                }
                } else if (shape instanceof MultiPolygon) {
                    MultiPolygon multiPolygon = (MultiPolygon)shape;
                    List<Polygon> polygons = new ArrayList<>();
                    for (Polygon polygon : multiPolygon.getPolygons()){
                        if (filePath.contains("加密")||filePath.contains("解密")||filePath.contains("提取")||filePath.contains("水印")){
                            polygon = polygon.removeLastPoint();
                        }
                        List<Point> exteriors = polygon.getExteriors();
                        List<Point> exteriorPoints = WatermarkingUtils.calcuWatermarking(exteriors,strings,index);
                        //若有内环，计算内环
                        if (polygon.getInteriors()!=null){
                            List<List<Point>> watermarkedPolygon = new ArrayList<>();
                            for (List<Point> list : polygon.getInteriors()){
                                List<Point> watermarkedPoints = WatermarkingUtils.calcuWatermarking(list,strings,index);
                                watermarkedPolygon.add(watermarkedPoints);
                            }
                            polygons.add(new Polygon(exteriorPoints, watermarkedPolygon));
                        }else {
                            polygons.add(new Polygon(exteriorPoints,null));
                        }
                    }
                    watermarkedShape.add(new MultiPolygon(polygons));
            }
        }

        try {
            SecretUtils.createSHP(watermarkedShape, layer, filePath.substring(0, filePath.lastIndexOf(".")), "水印",maps);
            resp.getWriter().write("{\"status\":\"success\",\"message\":\"水印" + bmpPath + "嵌入成功\"}");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
            }
        }
    }
