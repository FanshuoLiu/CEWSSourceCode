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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@WebServlet("/localDecryption")
public class localDecryption extends HttpServlet {
    static {
        gdal.AllRegister();
        ogr.RegisterAll();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
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
                if (currentGeomType==1){
                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"请选择线或面文件\"}");
                    return;
                }
                if (firstGeomType == 0) {
                    firstGeomType = currentGeomType; // 记录第一个几何类型
                } else if (!firstGeomType.equals(currentGeomType)) {
                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"请选择相同要素的文件\"}");
                    return; // 如果当前几何类型与第一个不一致则返回
                }
            }
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

            String s2 = UploadServlet.Path + "/" + values[0];
            Map<String, Object> map1 = Domain.SHPtoList(new File(s2));
            List<Line> geometries = (List<Line>) map1.get("geometries");
            System.out.println("点数:" + geometries.size());
            List<Integer> lengths=(List<Integer>)map1.get("lengths");
            System.out.println("lengths:"+ lengths);
            int maxIndex = findMaxIndex(lengths)+1;
            int pIndex=maxIndex*encryptedServlet.split;
            List<Shape> list = new ArrayList<>();
            System.out.println("maxIndex: " + maxIndex);
                if (firstGeomType==2){
                    List<Point> recoverPoints=new ArrayList<>();
                    List<watermarkDomain> watermarkDomainList = encryptedServlet.watermarkDomainCollectionList.get(maxIndex-1);
                    for (int k = 0; k <watermarkDomainList.size(); k++){
                        List<BigInteger> radiusIntegers = encryptedServlet.radiusList.get(pIndex);
                        List<BigInteger> angleIntegers = encryptedServlet.angleList.get(pIndex++);
                        List<BigInteger> selectedRadius=new ArrayList<>();
                        List<BigInteger> selectedAngle=new ArrayList<>();
                        for (int j =0;j<integers.size();j++){
                            selectedRadius.add(radiusIntegers.get(integers.get(j)-1));
                            selectedAngle.add(angleIntegers.get(integers.get(j)-1));
                        }
                        BigInteger recoverRadius = SecretUtils.recoverSecret(selectedRadius, integers,encryptedServlet.prime);
                        double x= recoverRadius.doubleValue() + watermarkDomainList.get(k).getDecimalRadius();
                        BigInteger recoverAngle = SecretUtils.recoverSecret(selectedAngle, integers,BigInteger.valueOf(367));
                        double y= recoverAngle.doubleValue() + watermarkDomainList.get(k).getDecimalAngle();
                        recoverPoints.add(new Point(x,y));
                    }
                    System.out.println("recover:"+recoverPoints);
                    geometries.add(new Line(Coordinate.recoverCartesian(recoverPoints)));
                    list.add(new Line(Coordinate.recoverCartesian(recoverPoints)));
                }else{
                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"不支持的类型\"}");
                    return;
                }

            long endTime = System.currentTimeMillis(); // 记录结束时间
            long duration = endTime - startTime; // 计算所花费的时间
            System.out.println("代码执行时间: " + duration + " 毫秒");
            String value = values[0];
            String s1 = value.replace("加密", "");
            SecretUtils.createSHP(list, layer,s1.substring(0, s1.lastIndexOf(".")), "局部解密");
            resp.getWriter().write("{\"status\":\"success\",\"message\":\"解密成功\"}");
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            resp.getWriter().write("JSON 解析异常");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    // 适用于List集合的方法，因为List可以通过索引访问元素
    public static <T extends Comparable<T>> int findMaxIndex(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("集合不能为null或空");
        }

        int maxIndex = 0;
        T maxValue = list.get(0);

        for (int i = 1; i < list.size(); i++) {
            T current = list.get(i);
            // 比较当前元素与最大值
            if (current.compareTo(maxValue) > 0) {
                maxValue = current;
                maxIndex = i;
            }
        }

        return maxIndex;
    }
}
