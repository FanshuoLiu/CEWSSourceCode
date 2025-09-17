package com.encryptrdSoftware.hnust.controller;

import com.encryptrdSoftware.hnust.model.*;
import com.encryptrdSoftware.hnust.util.SecretUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.gdal.gdal.gdal;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

@WebServlet("/decrypt")
public class decryptServlet extends HttpServlet {
    static {
        gdal.AllRegister();
        ogr.RegisterAll();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8"); // 更改为 JSON 响应
        // 读取请求体
        StringBuilder sb = new StringBuilder();
        String text;
        try (BufferedReader reader = req.getReader()) {
            while ((text = reader.readLine()) != null) {
                sb.append(text);
            }
        }
        String requestBody = sb.toString(); // 获取请求体内容
        Gson gson = new Gson();
        try {
            // 解析 JSON 数据
            JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);

            JsonArray indicesArray = jsonObject.getAsJsonArray("selectedIndices");
            JsonArray valuesArray = jsonObject.getAsJsonArray("selectedValues");
            String allOptions = jsonObject.get("allOptions").getAsString();
//            String properties = jsonObject.get("properties").getAsString();
            String[] strings = allOptions.split(",");

            // 获取选中的索引和内容
            Integer[] indices = new Integer[indicesArray.size()];
            String[] values = new String[valuesArray.size()];

            //判断k个及以上个文件的类型是否一致
            Integer firstGeomType = 0; // 存储第一个几何类型
            Layer layer = null;

            for (int i = 0; i < indicesArray.size(); i++) {
                //恢复的索引从1开始
                indices[i] = indicesArray.get(i).getAsInt()+1;
                values[i] = valuesArray.get(i).getAsString();
                layer = Domain.getLayer(new File(UploadServlet.Path + "/" + values[i]));
                int currentGeomType = layer.GetGeomType();
                if (firstGeomType == 0) {
                    firstGeomType = currentGeomType; // 记录第一个几何类型
                } else if (!firstGeomType.equals(currentGeomType)) {
                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"请选择相同要素的文件\"}");
                    return; // 如果当前几何类型与第一个不一致则返回
                }
            }
            List<LinkedHashMap<String, Object>> maps = SecretUtils.readShapefile(new File(UploadServlet.Path + "/" + values[0]));
            Map<String, Object> map = Domain.SHPtoList(new File(UploadServlet.Path + "/" + values[0]));
            List<Shape> list = (List<Shape>) map.get("geometries");
            int index=0;

            String s = SecretUtils.longestCommonPrefix(values);
            if (s!=null&&s.length()<=values[0].substring(0,values[0].lastIndexOf(".")-1).length()){
                for (int i=0;i<strings.length;i++){
                    if (strings[i].contains(s)){
                        index=i;
                        break;
                    }
                }
            }

            List<Shape> recoverShapes=new ArrayList<>();

//            byte[] bytes = SerializeUtils.convertFileToByteArray(UploadServlet.Path + "/" + properties);
//            Object[] objects = SerializeUtils.deSerialize(bytes);
//            List<List<BigInteger>> radiusList = (List<List<BigInteger>>) objects[0];
//            List<List<BigInteger>> angleList = (List<List<BigInteger>>) objects[1];
//            List<watermarkDomain> watermarkingList=null;
//            List<List<watermarkDomain>> watermarkingDomainList=null;
//
//            if (firstGeomType==1){
//                watermarkingList = (List<watermarkDomain>) objects[2];
//            }else {
//                watermarkingDomainList = (List<List<watermarkDomain>>) objects[2];
//            }
//            BigInteger Prime=(BigInteger) objects[3];
//            int split=(int) objects[4];

//            if (indicesArray.size() <(split+1)/2) {
//                resp.getWriter().write("{\"status\":\"error\",\"message\":\"请至少选择: "+(split+1)/2+"个文件\"}");
//                return;
//            } else if (indicesArray.size() > split) {
//                resp.getWriter().write("{\"status\":\"error\",\"message\":\"请至多选择: "+split+"个文件\"}");
//            }

            if (s==null){
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"请选择相同文件\"}");
                return;
            }

            for (int i = 0; i < indices.length; i++){
                indices[i]=indices[i]-index;
            }
            long startTime = System.currentTimeMillis(); // 记录开始时间
            //选择用于恢复的部分极径极角密文份额的索引
            //选取的k个文件的索引
            List<Integer> integers = Arrays.asList(indices);
            if (firstGeomType == 1) {
                try {
                    List<Point> recoverPoints = new ArrayList<>();
                    for (int i = 0; i < encryptedServlet.radiusList.size(); i++) {
                        List<BigInteger> selectedRadius = new ArrayList<>();
                        List<BigInteger> selectedAngle = new ArrayList<>();
                        for (int j = 0; j < integers.size(); j++) {
                            selectedRadius.add(encryptedServlet.radiusList.get(i).get(integers.get(j) - 1));
                            selectedAngle.add(encryptedServlet.angleList.get(i).get(integers.get(j) - 1));
                        }
//                        System.out.println("selectedRadius: " + selectedRadius);
                        BigInteger recoverRadius = SecretUtils.recoverSecret(selectedRadius, integers, encryptedServlet.prime);
//                        System.out.println("recoverRadius: " + recoverRadius);
                        double x = recoverRadius.doubleValue() + encryptedServlet.watermarkDomainList.get(i).getDecimalRadius();
//                        System.out.println("recoverRadius: " + x);
                        BigInteger recoverAngle = SecretUtils.recoverSecret(selectedAngle, integers, BigInteger.valueOf(367));
                        double y = recoverAngle.doubleValue() + encryptedServlet.watermarkDomainList.get(i).getDecimalAngle();
                        recoverPoints.add(new Point(x, y));
                    }
                    //转为原始直角坐标并生成文件
                    List<Point> points = Coordinate.recoverCartesian(recoverPoints);
                    long endTime = System.currentTimeMillis(); // 记录结束时间
                    long duration = endTime - startTime; // 计算所花费的时间
                    System.out.println("解密代码执行时间: " + duration + " 毫秒");
                    String value = values[0];
                    String s1 = value.replace("加密", "");
                    SecretUtils.createSHP(points,layer,s1.substring(0, s1.lastIndexOf(".")),"解密", maps);
                    resp.getWriter().write("{\"status\":\"success\",\"message\":\"解密成功\"}");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

                int number=0;
                int pIndex=0;
                for (Shape shape:list){
                    if (shape instanceof Line){
                        List<Point> recoverPoints=new ArrayList<>();
                        for (int k = 0; k <encryptedServlet.watermarkDomainCollectionList.get(number).size(); k++){
                            List<BigInteger> radiusIntegers = encryptedServlet.radiusList.get(pIndex);
                            List<BigInteger> angleIntegers = encryptedServlet.angleList.get(pIndex);
                            List<BigInteger> selectedRadius=new ArrayList<>();
                            List<BigInteger> selectedAngle=new ArrayList<>();
                            for (int j =0;j<integers.size();j++){
                                selectedRadius.add(radiusIntegers.get(integers.get(j)-1));
                                selectedAngle.add(angleIntegers.get(integers.get(j)-1));
                            }
                            BigInteger recoverRadius = SecretUtils.recoverSecret(selectedRadius, integers,encryptedServlet.prime);
                            double x=recoverRadius.doubleValue()+encryptedServlet.watermarkDomainCollectionList.get(number).get(k).getDecimalRadius();
                            BigInteger recoverAngle = SecretUtils.recoverSecret(selectedAngle, integers,BigInteger.valueOf(367));
                            double y=recoverAngle.doubleValue()+encryptedServlet.watermarkDomainCollectionList.get(number).get(k).getDecimalAngle();
                            recoverPoints.add(new Point(x,y));
                            pIndex++;
                        }
                            number++;
                        recoverShapes.add(new Line(Coordinate.recoverCartesian(recoverPoints)));
                    } else if (shape instanceof MultiLine) {
                            MultiLine multiLine = (MultiLine) shape;
                            List<Point> recoverPoints=new ArrayList<>();
                            List<Line> recoverLines = new ArrayList<>();
                            for (int i=0;i<multiLine.getLines().size();i++){
                                for (int k = 0; k <encryptedServlet.watermarkDomainCollectionList.get(number).size(); k++){
                                    List<BigInteger> radiusIntegers = encryptedServlet.radiusList.get(pIndex);
                                    List<BigInteger> angleIntegers = encryptedServlet.angleList.get(pIndex);
                                    List<BigInteger> selectedRadius=new ArrayList<>();
                                    List<BigInteger> selectedAngle=new ArrayList<>();
                                    for (int j =0;j<integers.size();j++){
                                        selectedRadius.add(radiusIntegers.get(integers.get(j)-1));
                                        selectedAngle.add(angleIntegers.get(integers.get(j)-1));
                                    }
                                    BigInteger recoverRadius = SecretUtils.recoverSecret(selectedRadius, integers,encryptedServlet.prime);
                                    double x=recoverRadius.doubleValue()+encryptedServlet.watermarkDomainCollectionList.get(number).get(k).getDecimalRadius();
                                    BigInteger recoverAngle = SecretUtils.recoverSecret(selectedAngle, integers,BigInteger.valueOf(367));
                                    double y=recoverAngle.doubleValue()+encryptedServlet.watermarkDomainCollectionList.get(number).get(k).getDecimalAngle();
                                    recoverPoints.add(new Point(x,y));
                                    pIndex++;
                                }
                                    number++;
                                recoverLines.add(new Line(Coordinate.recoverCartesian(recoverPoints)));
                            }
                            recoverShapes.add(new MultiLine(recoverLines));
                    } else if (shape instanceof Polygon) {
                        Polygon polygon = (Polygon) shape;
                        polygon = polygon.removeLastPoint();
                        List<Point> recoverPoints=new ArrayList<>();
                        for (int k = 0; k <encryptedServlet.watermarkDomainCollectionList.get(number).size(); k++){
                            List<BigInteger> radiusIntegers = encryptedServlet.radiusList.get(pIndex);
                            List<BigInteger> angleIntegers = encryptedServlet.angleList.get(pIndex);
                            List<BigInteger> selectedRadius=new ArrayList<>();
                            List<BigInteger> selectedAngle=new ArrayList<>();
                            for (int j =0;j<integers.size();j++){
                                selectedRadius.add(radiusIntegers.get(integers.get(j)-1));
                                selectedAngle.add(angleIntegers.get(integers.get(j)-1));
                            }
                            BigInteger recoverRadius = SecretUtils.recoverSecret(selectedRadius,integers,encryptedServlet.prime);
                            double x=recoverRadius.doubleValue()+encryptedServlet.watermarkDomainCollectionList.get(number).get(k).getDecimalRadius();
                            BigInteger recoverAngle = SecretUtils.recoverSecret(selectedAngle, integers,BigInteger.valueOf(367));
                            double y=recoverAngle.doubleValue()+encryptedServlet.watermarkDomainCollectionList.get(number).get(k).getDecimalAngle();
                            recoverPoints.add(new Point(x,y));
                            pIndex++;
                        }
                        number++;
                        if (polygon.getInteriors().size()!=0){
                            List<List<Point>> recoverInteriors = new ArrayList<>();
                            List<List<Point>> interiors = polygon.getInteriors();
                            List<Point> interiorPoints = new ArrayList<>();
                            for (int i=0;i<interiors.size();i++){
                                for (int k = 0; k <encryptedServlet.watermarkDomainCollectionList.get(number).size(); k++){
                                    List<BigInteger> radiusIntegers = encryptedServlet.radiusList.get(pIndex++);
                                    List<BigInteger> angleIntegers = encryptedServlet.angleList.get(pIndex);
                                    List<BigInteger> selectedRadius=new ArrayList<>();
                                    List<BigInteger> selectedAngle=new ArrayList<>();
                                    for (int j =0;j<integers.size();j++){
                                        selectedRadius.add(radiusIntegers.get(integers.get(j)-1));
                                        selectedAngle.add(angleIntegers.get(integers.get(j)-1));
                                    }
                                    BigInteger recoverRadius = SecretUtils.recoverSecret(selectedRadius, integers,encryptedServlet.prime);
                                    double x=recoverRadius.doubleValue()+encryptedServlet.watermarkDomainCollectionList.get(number).get(k).getDecimalRadius();
                                    BigInteger recoverAngle = SecretUtils.recoverSecret(selectedAngle, integers,BigInteger.valueOf(367));
                                    double y=recoverAngle.doubleValue()+encryptedServlet.watermarkDomainCollectionList.get(number).get(k).getDecimalAngle();
                                    interiorPoints.add(new Point(x,y));
                                }
                                    number++;
                                recoverInteriors.add(Coordinate.recoverCartesian(interiorPoints));
                            }
                            recoverShapes.add(new Polygon(Coordinate.recoverCartesian(recoverPoints),recoverInteriors));

                        }else {
                            recoverShapes.add(new Polygon(Coordinate.recoverCartesian(recoverPoints),null));
                        }
                    } else if (shape instanceof MultiPolygon) {

                    } else {
                        resp.getWriter().write("{\"status\":\"error\",\"message\":\"不支持的类型\"}");
                        return;
                    }
                }
            long endTime = System.currentTimeMillis(); // 记录结束时间
            long duration = endTime - startTime; // 计算所花费的时间
            System.out.println("代码执行时间: " + duration + " 毫秒");
                String value = values[0];
                String s1 = value.replace("加密", "");
//                SecretUtils.createSHP(recoverShapes, layer,s1.substring(0, s1.lastIndexOf(".")), "解密", maps);
                resp.getWriter().write("{\"status\":\"success\",\"message\":\"解密成功\"}");
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            resp.getWriter().write("JSON 解析异常");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
