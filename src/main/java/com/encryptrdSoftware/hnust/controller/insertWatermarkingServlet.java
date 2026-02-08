package com.encryptrdSoftware.hnust.controller;

import com.encryptrdSoftware.hnust.model.*;
import com.encryptrdSoftware.hnust.util.JSONParseUtils;
import com.encryptrdSoftware.hnust.util.SecretUtils;
import com.encryptrdSoftware.hnust.util.StringUtils;
import com.encryptrdSoftware.hnust.util.WatermarkingUtils;
import org.gdal.ogr.Layer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

@WebServlet("/insert")
public class insertWatermarkingServlet extends HttpServlet {

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
        Domain.btn="水印";
        String shpFile=null;
        String bmpFile=null;
        Map<String, Object> map1 = JSONParseUtils.parseJson(req, resp);
        shpFile = (String) map1.get("shpFile");
        bmpFile = (String) map1.get("bmpFile");
        String realshpFile = UploadServlet.Path + "/" + shpFile;
        String realbmpFile = UploadServlet.Path + "/" + bmpFile;
        System.out.println("水印路径:" + realbmpFile);
        //获取shp文件的属性列表
        List<LinkedHashMap<String, Object>> maps = SecretUtils.readShapefile(new File(realshpFile));

        // 打开Shapefile
        Layer layer = Domain.getLayer(new File(realshpFile));
        Map<String, Object> map = Domain.SHPtoList(new File(realshpFile));
        List<Shape> geometries = (List<Shape>) map.get("geometries");
        List<Point> points1 = (List<Point>) map.get("points1");
        List<Point> centerPoints = (List<Point>) map.get("centerPoints");
        List<Shape> watermarkedShape=new ArrayList<>();
        int num = (int) map.get("num");
        //计算要素的数量
        long n = Domain.calPointsNumber(layer);

        System.out.println("要素数量:" + num);
        //获取图层的要素类型
        int type = layer.GetGeomType();
        System.out.println("type:" + type);
        String s= StringUtils.modifyString(shpFile);
        if (type==1||type==4){
            int index=0;
            //计算嵌入水印后的坐标
            strings = WatermarkingUtils.initString(realbmpFile, (int) n);
            pic.add(WatermarkingUtils.width);
            pic.add(WatermarkingUtils.height);
            List<Point> points3;
               Coordinate coordinate = Coordinate.initCoordinate(points1);
               points3 = coordinate.calculatePolarCoordinates(points1);
            List<Point> points = WatermarkingUtils.calcuWatermarking(points3, strings, index);
            List<Point> points2 = Coordinate.recoverCartesian(points);
            try {
                SecretUtils.createSHP(points2, layer, shpFile.substring(0, shpFile.lastIndexOf(".")), "水印",maps);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            String filename= s;
            resp.getWriter().write("{\"status\":\"success\",\"message\":\"水印" + bmpFile + "嵌入成功\"}");
            return;
        }

        //计算除点外的要素
        int index=0;
        strings = WatermarkingUtils.initString(realbmpFile, num);
        pic.add(WatermarkingUtils.width);
        pic.add(WatermarkingUtils.height);
        Coordinate coordinate=null;
        List<Point> points4=null;
        coordinate = Coordinate.initCoordinate(centerPoints);
        for (Shape shape : geometries){
            if (shape instanceof Line){
                //计算line的水印坐标
                Line line = (Line) shape;
                List<Point> watermarkedPoints = WatermarkingUtils.calcuWatermarking1(coordinate,line.getPoints(), strings,index);
                List<Point> points = Coordinate.recoverCartesian(watermarkedPoints);
                watermarkedShape.add(new Line(points));
//                System.out.println(new Line(watermarkedPoints));
            } else if (shape instanceof MultiLine) {
                MultiLine multiLine = (MultiLine) shape;
                List<Line> lines = new ArrayList<>();
                for (Line line : multiLine.getLines()){
                    List<Point> watermarkedPoints = WatermarkingUtils.calcuWatermarking1(coordinate,line.getPoints(), strings,index);
                    lines.add(new Line(watermarkedPoints));
                }
                watermarkedShape.add(new MultiLine(lines));
            } else if (shape instanceof Polygon) {
                Polygon polygon = (Polygon) shape;
                if (shpFile.contains("加密")||shpFile.contains("解密")||shpFile.contains("提取")||shpFile.contains("水印")){
                    polygon = polygon.removeLastPoint();
                }
                //计算外环
                List<Point> exteriors = polygon.getExteriors();
                List<Point> exteriorPoints = WatermarkingUtils.calcuWatermarking1(coordinate,exteriors, strings,index);
                //若有内环，计算内环
                if (polygon.getInteriors()!=null){
                    List<List<Point>> watermarkedPolygon = new ArrayList<>();
                    for (List<Point> list : polygon.getInteriors()){
                        List<Point> watermarkedPoints = WatermarkingUtils.calcuWatermarking1(coordinate,list,strings,index);
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
                        if (shpFile.contains("加密")||shpFile.contains("解密")||shpFile.contains("提取")||shpFile.contains("水印")){
                            polygon = polygon.removeLastPoint();
                        }
                        List<Point> exteriors = polygon.getExteriors();
                        List<Point> exteriorPoints = WatermarkingUtils.calcuWatermarking1(coordinate,exteriors,strings,index);
                        //若有内环，计算内环
                        if (polygon.getInteriors()!=null){
                            List<List<Point>> watermarkedPolygon = new ArrayList<>();
                            for (List<Point> list : polygon.getInteriors()){
                                List<Point> watermarkedPoints = WatermarkingUtils.calcuWatermarking1(coordinate,list,strings,index);
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
            SecretUtils.createSHP(watermarkedShape, layer, shpFile.substring(0, shpFile.lastIndexOf(".")), "水印",maps);
            resp.getWriter().write("{\"status\":\"success\",\"message\":\"水印" + bmpFile + "嵌入成功\"}");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
            }
        }
    }
