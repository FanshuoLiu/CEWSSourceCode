package com.encryptrdSoftware.hnust.controller;

import com.encryptrdSoftware.hnust.model.*;
import com.encryptrdSoftware.hnust.util.SecretUtils;
import com.encryptrdSoftware.hnust.util.StringUtils;
import com.encryptrdSoftware.hnust.util.WatermarkingUtils;
import org.gdal.gdal.gdal;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/watermark")
public class extractWatermarkingServlet extends HttpServlet {
    static {
        gdal.AllRegister();
        ogr.RegisterAll();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String filename1 = req.getParameter("filename1");
        System.out.println("文件名:" + filename1);
        if (!filename1.contains("水印")){
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"该shp不含水印，请重新上传\"}");
            return;
        }
        //获取用户的文件数据
        String filepath = UploadServlet.Path + File.separator + filename1;
        System.out.println("文件路径:" + filepath);
        List<LinkedHashMap<String, Object>> maps = SecretUtils.readShapefile(new File(filepath));
        Layer layer = Domain.getLayer(new File(filepath));
        Map<String, Object> map = Domain.SHPtoList(new File(filepath));
        List<Shape> list = (List<Shape>) map.get("geometries");
        System.out.println("要素数量：" + list.size());
        long num = layer.GetFeatureCount();
        int type = layer.GetGeomType();
        List<Shape> recoverShapes = new ArrayList<>();

        if (filename1.contains("水印")){
            filename1=filename1.replace("水印", "");
        }
        try {

            List<String> strings = insertWatermarkingServlet.strings;
            System.out.println("水印长度:"+strings.size());
            List<Integer> imgSize = insertWatermarkingServlet.pic;

            if (type == 1) {
                List<Point> points1 = (List<Point>) map.get("points1");
                System.out.println("点数:"+points1.size());
                List<Point> recoverPoints = new ArrayList();
                StringBuilder sb = new StringBuilder();

                List<encryptedDomain> encryptedDomains = Domain.calEncrypt(points1);
                List<watermarkDomain> watermarkDomains = Domain.calWatermark(points1);

                for (int i = 0; i < strings.size(); i++) {
                    String s = strings.get(i);
                    double x = Math.pow(2, s.length()) * watermarkDomains.get(i).getDecimalRadius() - Integer.parseInt(s, 2);
                    double y = Math.pow(2, s.length()) * watermarkDomains.get(i).getDecimalAngle() - Integer.parseInt(s, 2);
                    x = x + encryptedDomains.get(i).getIntegerRadius();
                    y = y + encryptedDomains.get(i).getIntegerAngle();
                    recoverPoints.add(new Point(x, y));
                    sb.append(s);
                }
                System.out.println("复原点数："+recoverPoints.size());
                SecretUtils.createSHP(recoverPoints, layer, filename1.substring(0,filename1.lastIndexOf(".")), "提取水印", maps);
                //处理点数大于比特串的情况
                int originalBitLength = imgSize.get(0) * imgSize.get(1);
//                if (num > originalBitLength) {
//                    int proportion = (int) (num / originalBitLength);
//                    String[] bitStrings = new String[originalBitLength];
//                    for (int i = 0; i <= proportion; i++) {
//                        if (i == proportion) {
//                            bitStrings[i] = sb.substring(i * originalBitLength);
//                        } else {
//                            bitStrings[i] = sb.substring(i * originalBitLength, (i + 1) * originalBitLength);
//                        }
//                    }
//                    String compareBitStrings = WatermarkingUtils.compareBitStrings(bitStrings);
//                    WatermarkingUtils.decodeImage(compareBitStrings, imgSize.get(0), imgSize.get(1), "提取"+filename1);
//                } else {
                    WatermarkingUtils.decodeImage(sb.toString(), imgSize.get(0), imgSize.get(1), "提取"+filename1);
//                }
                resp.getWriter().write("{\"status\":\"success\",\"message\":\"水印提取成功\"}");
                return;
            }
            int index = 0;
            StringBuilder sb = new StringBuilder();
            int number = (int) map.get("num");
            System.out.println("number:" + number);
            for (Shape shape : list) {
                if (shape instanceof Line) {
                    Line line = (Line) shape;
                    List<Point> recoverPoints = new ArrayList();

                    List<encryptedDomain> encryptedDomains = Domain.calEncrypt(line.getPoints());
                    List<watermarkDomain> watermarkDomains = Domain.calWatermark(line.getPoints());

                    for (int i = 0; i < line.getLength(); i++) {
                        String s = strings.get(index);
                        double x = Math.pow(2, s.length()) * watermarkDomains.get(i).getDecimalRadius() - Integer.parseInt(s, 2);
                        double y = Math.pow(2, s.length()) * watermarkDomains.get(i).getDecimalAngle() - Integer.parseInt(s, 2);
                        x = x + encryptedDomains.get(i).getIntegerRadius();
                        y = y + encryptedDomains.get(i).getIntegerAngle();
                        recoverPoints.add(new Point(x, y));
                        sb.append(s);
                        index++;
                    }
                    recoverShapes.add(new Line(recoverPoints));
                } else if (shape instanceof MultiLine) {
                    MultiLine multiLine = (MultiLine) shape;
                    List<Line> recoverLines = new ArrayList<>();
                    for (Line line : multiLine.getLines()) {
                        List<Point> recoverPoints = new ArrayList();
                        List<encryptedDomain> encryptedDomains = Domain.calEncrypt(line.getPoints());
                        List<watermarkDomain> watermarkDomains = Domain.calWatermark(line.getPoints());
                        for (int i = 0; i < line.getLength(); i++) {
                            String s = strings.get(index);
                            double x = Math.pow(2, s.length()) * watermarkDomains.get(i).getDecimalRadius() - Integer.parseInt(s, 2);
                            double y = Math.pow(2, s.length()) * watermarkDomains.get(i).getDecimalAngle() - Integer.parseInt(s, 2);
                            x = x + encryptedDomains.get(i).getIntegerRadius();
                            y = y + encryptedDomains.get(i).getIntegerAngle();
                            recoverPoints.add(new Point(x, y));
                            sb.append(s);
                            index++;
                        }
                        recoverLines.add(new Line(recoverPoints));
                    }
                    recoverShapes.add(new MultiLine(recoverLines));
                } else if (shape instanceof Polygon) {
                    Polygon polygon = (Polygon) shape;
                    polygon = polygon.removeLastPoint();

                    List<Point> exteriors = polygon.getExteriors();
                    List<Point> recoverPoints = new ArrayList();

                    List<encryptedDomain> encryptedDomains = Domain.calEncrypt(exteriors);
                    List<watermarkDomain> watermarkDomains = Domain.calWatermark(exteriors);

                    for (int i = 0; i < exteriors.size(); i++) {
                        String s = strings.get(index);
                        double x = Math.pow(2, s.length()) * watermarkDomains.get(i).getDecimalRadius() - Integer.parseInt(s, 2);
                        double y = Math.pow(2, s.length()) * watermarkDomains.get(i).getDecimalAngle() - Integer.parseInt(s, 2);
                        x = x + encryptedDomains.get(i).getIntegerRadius();
                        y = y + encryptedDomains.get(i).getIntegerAngle();
                        recoverPoints.add(new Point(x, y));
                        sb.append(s);
                        index++;
                    }
                    if (polygon.getInteriors().size() != 0) {
                        List<List<Point>> recoverInteriors = new ArrayList<>();
                        List<List<Point>> interiors = polygon.getInteriors();
                        List<Point> interiorPoints = new ArrayList<>();
                        for (List<Point> interior : interiors) {
                            List<encryptedDomain> encryptedDomain = Domain.calEncrypt(interior);
                            List<watermarkDomain> watermarkDomain = Domain.calWatermark(interior);

                            for (int i = 0; i < interior.size(); i++) {
                                String s = strings.get(index);
                                double x = Math.pow(2, s.length()) * watermarkDomain.get(i).getDecimalRadius() - Integer.parseInt(s, 2);
                                double y = Math.pow(2, s.length()) * watermarkDomain.get(i).getDecimalAngle() - Integer.parseInt(s, 2);
                                x = x + encryptedDomain.get(i).getIntegerRadius();
                                y = y + encryptedDomain.get(i).getIntegerAngle();
                                interiorPoints.add(new Point(x, y));
                                sb.append(s);
                                index++;
                            }
                            recoverInteriors.add(interiorPoints);
                        }
                        recoverShapes.add(new Polygon(recoverPoints, recoverInteriors));
                    } else {
                        recoverShapes.add(new Polygon(recoverPoints, null));
                    }
                } else if (shape instanceof MultiPolygon) {

                }
            }
            SecretUtils.createSHP(recoverShapes, layer, filename1.substring(0,filename1.lastIndexOf(".")), "提取水印", maps);
            //处理点数大于比特串的情况
            int originalBitLength = imgSize.get(0) * imgSize.get(1);
            if (num > originalBitLength) {
                String[] bitStrings = new String[originalBitLength];
                for (int i = 0; i <= num / originalBitLength; i++) {
                    if (i == num / originalBitLength) {
                        bitStrings[i] = sb.substring(i * originalBitLength);
                    } else {
                        bitStrings[i] = sb.substring(i * originalBitLength, (i + 1) * originalBitLength);
                    }
                }
                String compareBitStrings = String.valueOf(bitStrings);
                WatermarkingUtils.decodeImage(compareBitStrings, imgSize.get(0), imgSize.get(1), "提取"+filename1);
            } else {
                WatermarkingUtils.decodeImage(sb.toString(), imgSize.get(0), imgSize.get(1), "提取"+filename1);
            }
            resp.getWriter().write("{\"status\":\"success\",\"message\":\"水印提取成功\"}");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}





