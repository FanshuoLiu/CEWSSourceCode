package com.encryptrdSoftware.hnust.controller;
import com.encryptrdSoftware.hnust.util.JSONParseUtils;
import com.encryptrdSoftware.hnust.model.*;
import com.encryptrdSoftware.hnust.util.SecretUtils;
import com.encryptrdSoftware.hnust.util.StringUtils;
import org.gdal.ogr.Layer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

@WebServlet("/encrypt")
public class encryptedServlet extends HttpServlet {
    public static List<List<BigInteger>> radiusList = null;
    public static List<List<BigInteger>> angleList = null;
    public static List<watermarkDomain> watermarkDomainList=null;
    public static int split;
    public static BigInteger prime;
    public static  List<Point> cartesiansPointslist = null;
    public static List<Point> originalPoints =null;

    public static List<List<watermarkDomain>> watermarkDomainCollectionList = null;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        Domain.btn="加密";
        watermarkDomainCollectionList=new ArrayList<>();
        radiusList=new ArrayList<>();
        angleList=new ArrayList<>();
        cartesiansPointslist= new ArrayList<>();
        originalPoints=new ArrayList<>();
        watermarkDomainList=new ArrayList<>();
        String filename=null;
        Map<String, Object> map1 = JSONParseUtils.parseJson(req, resp);
        filename = (String) map1.get("filename");
        split = (int) map1.get("num");
        System.out.println("shp文件名称:" + filename);
        String realfilename = UploadServlet.Path + "/" + filename;
        System.out.println("文件路径:" + realfilename);
        //获取shp文件的属性列表
        int k = (split + 1) / 2;
        System.out.println("split:" + split);
        System.out.println("k:" + k);
        List<LinkedHashMap<String, Object>> maps = SecretUtils.readShapefile(new File(realfilename));
//        // 外层：遍历List中的每个LinkedHashMap
//        for (LinkedHashMap<String, Object> map : maps) {
//            // 内层：遍历当前LinkedHashMap的键值对（保持插入顺序）
//            for (Map.Entry<String, Object> entry : map.entrySet()) {
//                String key = entry.getKey();
//                Object value = entry.getValue();
//                System.out.print(key + "=" + value + " "); // 同一行输出当前map的所有键值对
//            }
//            System.out.println(); // 每个LinkedHashMap输出完后换行，分隔不同元素
//        }
        Map<String, Set<Object>> setMap = StringUtils.deduplicateAttrToMap(maps);
        // 获取layer
        Layer layer = Domain.getLayer(new File(realfilename));

        //计算要素的数量
        int num = (int) Domain.calPointsNumber(layer);
        System.out.println("要素数量:" + num);
        //获取图层的要素类型
        int type = layer.GetGeomType();
        System.out.println("type:" + type);
        SecureRandom random = new SecureRandom();
        //存放要素
        Map<String, Object> map = Domain.SHPtoList(new File(realfilename));
        List<Shape> list = (List<Shape>) map.get("geometries");
        System.out.println("shape:"+list.size());
        List<Point> centerPoints = (List<Point>) map.get("centerPoints");
        List<Point> points1 = (List<Point>) map.get("points1");
        originalPoints.addAll(points1);
        Coordinate coordinate=null;
        List<List<Shape>> lists = new ArrayList<>();
        if (type!=1){
            for (int i = 0; i <split;i++){
                List<Shape> shape=new ArrayList<>();
                lists.add(shape);
            }
            if (!filename.contains("加密")||!filename.contains("水印")){
                coordinate = Coordinate.initCoordinate(centerPoints);
            }
        }else {
            if (!filename.contains("加密")||!filename.contains("水印")){
                coordinate = Coordinate.initCoordinate(points1);
            }
        }

        List<Point> points2 = coordinate.calculatePolarCoordinates(points1);
        System.out.println("极坐标转换完成");
        prime = SecretUtils.generatePrime(Domain.calEncrypt(points2));
        System.out.println("大素数为生成完成");
        List<encryptedDomain> encryptedDomainList;
        BigInteger randomRadiusBigInteger=null;
        BigInteger randomAngleBigInteger = null;
        long startTime = System.currentTimeMillis(); // 记录开始时间
        List<Point> points = new ArrayList<>();
        if (type == 1||type == 4){
            encryptedDomainList = Domain.calEncrypt(points2);
            watermarkDomainList = Domain.calWatermark(points2);
            System.out.println("点要素的加密域水印域集合生成完成");
            List<Point> polarPointShares = new ArrayList<>(num*split);

            calculateShare(num,encryptedDomainList,watermarkDomainList,k,randomRadiusBigInteger,randomAngleBigInteger,random,polarPointShares);

            System.out.println("极坐标密文生成完成");
            long endTime = System.currentTimeMillis(); // 记录结束时间
            long duration = endTime - startTime; // 计算所花费的时间
            List<Point> cartesiansPoints = Coordinate.recoverCartesian(polarPointShares);
            System.out.println("代码执行时间: " + duration + " 毫秒");
            //创建n个加密文件
            for (int i = 0; i < split; i++) {
                int d = i;
                // 创建坐标点集合
                List<Point> coordinatesList = new ArrayList<>();
                for (int j = 0; j < num; j++) {
                    coordinatesList.add(new Point(cartesiansPoints.get(d).getX(), cartesiansPoints.get(d).getY()));
                    d += split;
                }
                try {
                    SecretUtils.createSHP(coordinatesList, layer, filename.substring(0, filename.lastIndexOf(".")),"加密",maps,i);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            resp.getWriter().write("{\"status\":\"success\",\"message\":\"文件加密成功: \"}");
            return;
        }
        int b=1;
        int text=0;
        System.out.println("list大小"+list.size());
        for (Shape shape:list){
            if (shape instanceof Line){
                text++;
                Line line = (Line) shape;
                List<Point> linePoints = line.getPoints();
                List<Point> polarPoints = coordinate.calculatePolarCoordinates(linePoints);
                encryptedDomainList = Domain.calEncrypt(polarPoints);
                watermarkDomainList = Domain.calWatermark(polarPoints);
//              System.out.println("线要素"+b+"的加密域水印域集合生成完成");
                watermarkDomainCollectionList.add(watermarkDomainList);
                List<Point> polarPointShares = new ArrayList<>();

                calculateShare(linePoints.size(),encryptedDomainList,watermarkDomainList,k,randomRadiusBigInteger,randomAngleBigInteger,random,polarPointShares);

                List<Point> cartesiansPoints = Coordinate.recoverCartesian(polarPointShares);
//                System.out.println("线要素"+b+"的直角密文生成完成");
                b++;
                cartesiansPointslist.addAll(cartesiansPoints);
                if (split!=1){
                    for (int i = 0; i < split; i++) {
                        int d = i;
                        // 创建坐标点集合
                        List<Point> coordinatesList = new ArrayList<>();
                        for (int j = 0; j <linePoints.size(); j++) {
                            coordinatesList.add(new Point(cartesiansPoints.get(d).getX(), cartesiansPoints.get(d).getY()));
                            d += split;
                        }
                        lists.get(i).add(new Line(coordinatesList));
                    }
                }else {
                    lists.get(0).add(new Line(cartesiansPoints));
                }
            } else if (shape instanceof Polygon) {
                Polygon polygon = (Polygon) shape;
                text++;
                processPolygon(polygon, filename, coordinate, k, randomRadiusBigInteger, randomAngleBigInteger, random, split, lists, points);
            } else if (shape instanceof MultiPolygon) {
                MultiPolygon multiPolygon = (MultiPolygon) shape;
                text++;
                processMultiPolygon(multiPolygon, filename, coordinate, k, randomRadiusBigInteger, randomAngleBigInteger, random, split, lists, points);
            }
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("代码执行时间: " + duration + " 毫秒");
        for (int i = 0; i < split; i++){
            try {
                SecretUtils.createSHP(lists.get(i),layer,filename.substring(0, filename.lastIndexOf(".")),"加密",maps,i);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        StringBuilder json = new StringBuilder();
        json.append("{")
                .append("\"status\":\"success\",")
                .append("\"message\":\"文件加密成功: ").append(filename).append("\"")
                .append("}");
        resp.getWriter().write(json.toString());
        return;
    }
    static void calculateShare(int length,List<encryptedDomain> encryptedDomainList, List<watermarkDomain> watermarkDomainList,int k,BigInteger randomRadiusBigInteger,BigInteger randomAngleBigInteger,Random random,List<Point> polarPointShares){
        for (int i = 0; i < length; i++) {
            //生成极径秘密
            BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
            //生成极角秘密
            BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
            // 生成极径多项式系数
            List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
            // 生成极角多项式系数
            List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k,BigInteger.valueOf(367));
            //生成极径极角份额
            List<BigInteger> radiusShares = SecretUtils.generateShares(radiusCoefficients, split, prime, randomRadiusBigInteger, i);
            List<BigInteger> angleShares = SecretUtils.generateShares(angleCoefficients, split, BigInteger.valueOf(367), randomAngleBigInteger, i);
            //随机获取前一个点的一个份额
            randomRadiusBigInteger = radiusShares.get(random.nextInt(split));
            randomAngleBigInteger = angleShares.get(random.nextInt(split));
            radiusList.add(radiusShares);
            angleList.add(angleShares);
            //计算极坐标密文
            for (int a = 0; a < split; a++) {
                double radiusShare = (radiusShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalRadius()) * Domain.Qr;
                double angleShare = (angleShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalAngle()) * Domain.Qa;
                polarPointShares.add(new Point(radiusShare, angleShare));
            }
        }
    }
    
    void processPolygon(Polygon polygon, String filename, Coordinate coordinate, int k, BigInteger randomRadiusBigInteger, BigInteger randomAngleBigInteger, Random random, int split, List<List<Shape>> lists, List<Point> points) throws IOException {
        if (filename.contains("加密")||filename.contains("解密")||filename.contains("提取")||filename.contains("水印")){
            polygon = polygon.removeLastPoint();
        }
        //计算外环
        List<Point> exteriorPoints = polygon.getExteriors();
        List<Point> polarPoints = coordinate.calculatePolarCoordinates(exteriorPoints);
        List<encryptedDomain> encryptedDomainList = Domain.calEncrypt(polarPoints);
        List<watermarkDomain> watermarkDomainList = Domain.calWatermark(polarPoints);
        watermarkDomainCollectionList.add(watermarkDomainList);
        List<Point> polarPointShares = new ArrayList<>();

        calculateShare(exteriorPoints.size(), encryptedDomainList, watermarkDomainList, k, randomRadiusBigInteger, randomAngleBigInteger, random, polarPointShares);

        List<Point> cartesiansPoints = Coordinate.recoverCartesian(polarPointShares);
        cartesiansPointslist.addAll(cartesiansPoints);
        List<List<Point>> exteriorList=new ArrayList<>();
        for (int i = 0; i < split; i++) {
            int d = i;
            // 创建坐标点集合
            List<Point> coordinatesList = new ArrayList<>();
            for (int j = 0; j < exteriorPoints.size(); j++) {
                coordinatesList.add(new Point(cartesiansPoints.get(d).getX(), cartesiansPoints.get(d).getY()));
                d += split;
            }
            exteriorList.add(coordinatesList);
        }
        if (polygon.getInteriors().size()!=0){
            System.out.println("有内环，"+"环数："+polygon.getInteriors().size()+"点个数："+polygon.getInteriors().get(0).size());
            List<List<Point>> interiors = polygon.getInteriors();
            for (List<Point> interior : interiors){
                List<Point> interiorPoints = coordinate.calculatePolarCoordinates(interior);
                List<encryptedDomain> encryptedDomainList1 = Domain.calEncrypt(interiorPoints);
                List<watermarkDomain> watermarkDomainList1 = Domain.calWatermark(interiorPoints);
                watermarkDomainCollectionList.add(watermarkDomainList1);

                calculateShare(interiorPoints.size(), encryptedDomainList1, watermarkDomainList1, k, randomRadiusBigInteger, randomAngleBigInteger, random, points);

            }
            List<Point> cartesiansPoints1 = Coordinate.recoverCartesian(points);
            cartesiansPointslist.addAll(cartesiansPoints1);
            for (int i = 0; i < split; i++) {
                int d = i;
                List<List<Point>> interiorList=new ArrayList<>(polygon.getInteriors().size());
                for (int j = 0; j < polygon.getInteriors().size(); j++){
                    // 创建坐标点集合
                    List<Point> coordinatesList = new ArrayList<>(polygon.getInteriors().get(j).size());
                    for (int a = 0; a < polygon.getInteriors().get(j).size(); a++) {
                        coordinatesList.add(new Point(cartesiansPoints1.get(d).getX(), cartesiansPoints1.get(d).getY()));
                        d += split;
                    }
                    interiorList.add(coordinatesList);
                }
                lists.get(i).add(new Polygon(exteriorList.get(i), interiorList));
            }
        }else {
            for (int i = 0; i < split; i++) {
                lists.get(i).add(new Polygon(exteriorList.get(i), null));
            }
        }
    }
    
    void processMultiPolygon(MultiPolygon multiPolygon, String filename, Coordinate coordinate, int k, BigInteger randomRadiusBigInteger, BigInteger randomAngleBigInteger, Random random, int split, List<List<Shape>> lists, List<Point> points) throws IOException {
        List<Polygon> polygons = multiPolygon.getPolygons();
        List<List<Polygon>> multiPolygonLists = new ArrayList<>();
        
        // 为每个split创建一个空的Polygon列表
        for (int i = 0; i < split; i++) {
            multiPolygonLists.add(new ArrayList<>());
        }
        
        // 处理每个Polygon
        for (Polygon polygon : polygons) {
            if (filename.contains("加密")||filename.contains("解密")||filename.contains("提取")||filename.contains("水印")){
                polygon = polygon.removeLastPoint();
            }
            
            // 计算外环
            List<Point> exteriorPoints = polygon.getExteriors();
            List<Point> polarPoints = coordinate.calculatePolarCoordinates(exteriorPoints);
            List<encryptedDomain> encryptedDomainList = Domain.calEncrypt(polarPoints);
            List<watermarkDomain> watermarkDomainList = Domain.calWatermark(polarPoints);
            watermarkDomainCollectionList.add(watermarkDomainList);
            List<Point> polarPointShares = new ArrayList<>();

            calculateShare(exteriorPoints.size(), encryptedDomainList, watermarkDomainList, k, randomRadiusBigInteger, randomAngleBigInteger, random, polarPointShares);

            List<Point> cartesiansPoints = Coordinate.recoverCartesian(polarPointShares);
            cartesiansPointslist.addAll(cartesiansPoints);
            
            // 为每个split创建一个处理后的Polygon
            for (int i = 0; i < split; i++) {
                int d = i;
                // 创建坐标点集合
                List<Point> coordinatesList = new ArrayList<>();
                for (int j = 0; j < exteriorPoints.size(); j++) {
                    coordinatesList.add(new Point(cartesiansPoints.get(d).getX(), cartesiansPoints.get(d).getY()));
                    d += split;
                }
                
                // 处理内环
                if (polygon.getInteriors().size()!=0){
                    List<List<Point>> interiorList = new ArrayList<>();
                    List<List<Point>> interiors = polygon.getInteriors();
                    for (List<Point> interior : interiors){
                        List<Point> interiorPoints = coordinate.calculatePolarCoordinates(interior);
                        List<encryptedDomain> encryptedDomainList1 = Domain.calEncrypt(interiorPoints);
                        List<watermarkDomain> watermarkDomainList1 = Domain.calWatermark(interiorPoints);
                        watermarkDomainCollectionList.add(watermarkDomainList1);

                        calculateShare(interiorPoints.size(), encryptedDomainList1, watermarkDomainList1, k, randomRadiusBigInteger, randomAngleBigInteger, random, points);

                        List<Point> cartesiansPoints1 = Coordinate.recoverCartesian(points);
                        cartesiansPointslist.addAll(cartesiansPoints1);
                        
                        int d1 = i;
                        List<Point> interiorCoordinatesList = new ArrayList<>();
                        for (int a = 0; a < interior.size(); a++) {
                            interiorCoordinatesList.add(new Point(cartesiansPoints1.get(d1).getX(), cartesiansPoints1.get(d1).getY()));
                            d1 += split;
                        }
                        interiorList.add(interiorCoordinatesList);
                    }
                    multiPolygonLists.get(i).add(new Polygon(coordinatesList, interiorList));
                }else {
                    multiPolygonLists.get(i).add(new Polygon(coordinatesList, null));
                }
            }
        }
        
        // 为每个split创建一个MultiPolygon并添加到lists中
        for (int i = 0; i < split; i++) {
            lists.get(i).add(new MultiPolygon(multiPolygonLists.get(i)));
        }
    }
}