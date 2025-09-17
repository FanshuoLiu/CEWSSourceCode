package com.encryptrdSoftware.hnust.controller;

import com.encryptrdSoftware.hnust.model.*;
import com.encryptrdSoftware.hnust.util.SecretUtils;
import com.encryptrdSoftware.hnust.util.StringUtils;
import org.gdal.gdal.gdal;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
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
    static {
        gdal.AllRegister();
        ogr.RegisterAll();
    }
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
        watermarkDomainCollectionList=new ArrayList<>();
        radiusList=new ArrayList<>();
        angleList=new ArrayList<>();
        cartesiansPointslist= new ArrayList<>();
        originalPoints=new ArrayList<>();
        watermarkDomainList=new ArrayList<>();
        String filePath = req.getParameter("filename1");
        System.out.println("shp文件名称:" + filePath);
        String realFilePath = UploadServlet.Path + "/" + filePath;
        System.out.println("文件路径:" + realFilePath);
        //获取shp文件的属性列表
        //获取份额数量
        split = Integer.parseInt(req.getParameter("num"));
        int k = (split + 1) / 2;
        System.out.println("split:" + split);
        System.out.println("k:" + k);

        // 获取layer
        Layer layer = Domain.getLayer(new File(realFilePath));

        //计算要素的数量
        int num = (int) Domain.calPointsNumber(layer);
        System.out.println("要素数量:" + num);
        //获取图层的要素类型
        int type = layer.GetGeomType();
        System.out.println("type:" + type);
        //存放要素
        Map<String, Object> map = Domain.SHPtoList(new File(realFilePath));
        List<Shape> list = (List<Shape>) map.get("geometries");
        List<Point> centerPoints = (List<Point>) map.get("centerPoints");
        List<Point> points1 = (List<Point>) map.get("points1");
        originalPoints.addAll(points1);
        Coordinate coordinate;
        List<List<Shape>> lists = new ArrayList<>();
        if (type!=1){
            for (int i = 0; i <split;i++){
                List<Shape> shape=new ArrayList<>();
                lists.add(shape);
            }
            coordinate = Coordinate.initCoordinate(centerPoints);
        }else {
            coordinate = Coordinate.initCoordinate(points1);
            System.out.println(1);
        }

        List<Point> points2 = coordinate.calculatePolarCoordinates(points1);
        System.out.println("极坐标转换完成");
//        System.out.println("极坐标点数:"+points2);
        prime = SecretUtils.generatePrime(Domain.calEncrypt(points2));
        System.out.println("大素数为生成完成");
//        System.out.println("熵为："+(Math.log(prime.doubleValue())/Math.log(2)+Math.log(367)/Math.log(2))*points1.size());
        //加密域集合水印域集合
        List<encryptedDomain> encryptedDomainList;
        BigInteger randomRadiusBigInteger=null;
        BigInteger randomAngleBigInteger = null;
        long startTime = System.currentTimeMillis(); // 记录开始时间
        List<Point> points = new ArrayList<>();
        if (type == 1){
            encryptedDomainList = Domain.calEncrypt(points2);
            watermarkDomainList = Domain.calWatermark(points2);
            System.out.println("点要素的加密域水印域集合生成完成");
            List<Point> polarPointShares = new ArrayList<>(num*split);
            for (int i = 0; i < num; i++) {
                //生成极径秘密
                BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
                //生成极角秘密
                BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
                // 生成极径多项式系数
                List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
                // 生成极角多项式系数
                List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, BigInteger.valueOf(367));
                //生成极径极角份额
                List<BigInteger> radiusShares = SecretUtils.generateShares(radiusCoefficients, split,prime, randomRadiusBigInteger, i);
                List<BigInteger> angleShares = SecretUtils.generateShares(angleCoefficients, split, BigInteger.valueOf(367), randomAngleBigInteger, i);
                //随机获取前一个点的一个份额
                randomRadiusBigInteger = radiusShares.get(new SecureRandom().nextInt(split));
                randomAngleBigInteger = angleShares.get(new SecureRandom().nextInt(split));
                radiusList.add(radiusShares);
                angleList.add(angleShares);
                //计算极坐标密文
                for (int a = 0; a < split; a++) {
                    double radiusShare = (radiusShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalRadius()) * Domain.Qr;
                    double angleShare = (angleShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalAngle()) * Domain.Qa;
                    polarPointShares.add(new Point(radiusShare, angleShare));
                }
            }
            System.out.println("极坐标密文生成完成");
            long endTime = System.currentTimeMillis(); // 记录结束时间
            long duration = endTime - startTime; // 计算所花费的时间
            List<Point> cartesiansPoints = Coordinate.toCartesian(polarPointShares);
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
                    SecretUtils.createSHP(coordinatesList, layer, filePath.substring(0, filePath.lastIndexOf(".")),"加密",i);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
//            List<List<Point>> result = new ArrayList<>();
//            // 初始化三个子集合
//            result.add(new ArrayList<>());
//            result.add(new ArrayList<>());
//            result.add(new ArrayList<>());
//
//            for (int i = 0; i < cartesiansPoints.size(); i++) {
//                int remainder = i % 3;
//                result.get(remainder).add(cartesiansPoints.get(i));
//            }
//            System.out.println("result:" + result.get(0));
//            System.out.println();
//            System.out.println("result:" + result.get(1));
//            System.out.println();
//            System.out.println("result:" + result.get(2));
//            List<List<Double>> result1 = new ArrayList<>();
//            // 初始化三个子集合
//            result1.add(new ArrayList<>());
//            result1.add(new ArrayList<>());
//            result1.add(new ArrayList<>());
//            for (int i = 0; i < originalPoints.size(); i++) {
//                double v = Math.sqrt(Math.pow(result.get(0).get(i).getX() - originalPoints.get(i).getX(), 2) + Math.pow(result.get(0).get(i).getY() - originalPoints.get(i).getY(), 2));
//                result1.get(0).add(v);
//                double v1= Math.sqrt(Math.pow(result.get(1).get(i).getX() - originalPoints.get(i).getX(), 2) + Math.pow(result.get(1).get(i).getY() - originalPoints.get(i).getY(), 2));
//                result1.get(1).add(v1);
//                double v2 = Math.sqrt(Math.pow(result.get(2).get(i).getX() - originalPoints.get(i).getX(), 2) + Math.pow(result.get(2).get(i).getY() - originalPoints.get(i).getY(), 2));
//                result1.get(2).add(v2);
//            }
//            System.out.println("最大距离："+ Collections.max(result1.get(0))+"  "+Collections.max(result1.get(1))+"  "+Collections.max(result1.get(2)));
//            System.out.println("最小距离："+ Collections.min(result1.get(0))+"  "+Collections.min(result1.get(1))+"  "+Collections.min(result1.get(2)));
//            double rmse = calculateRMSE(result1.get(0));
//            double rmse1 = calculateRMSE(result1.get(1));
//            double rmse2 = calculateRMSE(result1.get(2));
//            System.out.println("距离集合的均方根误差为: " + rmse+"  "+rmse1+"  "+rmse2);

            resp.getWriter().write("{\"status\":\"success\",\"message\":\"文件加密成功: \"}");
            return;
        }
        SecureRandom random = new SecureRandom();
        for (Shape shape:list){
            if (shape instanceof Line){
                Line line = (Line) shape;
                List<Point> linePoints = line.getPoints();
                List<Point> polarPoints = coordinate.calculatePolarCoordinates(linePoints);
                encryptedDomainList = Domain.calEncrypt(polarPoints);
                watermarkDomainList = Domain.calWatermark(polarPoints);
                watermarkDomainCollectionList.add(watermarkDomainList);
                List<Point> polarPointShares = new ArrayList<>();
                for (int i = 0; i < linePoints.size(); i++) {
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
                List<Point> cartesiansPoints = Coordinate.toCartesian(polarPointShares);
                cartesiansPointslist.addAll(cartesiansPoints);
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
            } else if (shape instanceof MultiLine) {
                MultiLine multiLine = (MultiLine) shape;
                List<Point> polarPointShares = new ArrayList<>();
                for (Line line1 : multiLine.getLines()){
                    List<Point> linePoints = line1.getPoints();
                    List<Point> polarPoints = coordinate.calculatePolarCoordinates(linePoints);
                    encryptedDomainList = Domain.calEncrypt(polarPoints);
                    watermarkDomainList = Domain.calWatermark(polarPoints);
                    watermarkDomainCollectionList.add(watermarkDomainList);

                    for (int i = 0; i < line1.getLength(); i++) {
                        //生成极径秘密
                        BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
                        //生成极角秘密
                        BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
                        // 生成极径多项式系数
                        List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
                        // 生成极角多项式系数
                        List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, BigInteger.valueOf(367));
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
                List<Point> cartesians = Coordinate.toCartesian(polarPointShares);
                for (int i = 0; i < split; i++) {
                    int d = i;
                    List<Line> lineList = new ArrayList<>();
                    for (int j = 0; j <multiLine.getLines().size(); j++){
                        // 创建坐标点集合
                        List<Point> coordinatesList = new ArrayList<>();
                        for (int a = 0; a <multiLine.getLines().get(j).getLength(); a++) {
                            coordinatesList.add(new Point(cartesians.get(d).getX(), cartesians.get(d).getY()));
                            d += split;
                        }
                        lineList.add(new Line(coordinatesList));
                    }
                    lists.get(i).add(new MultiLine(lineList));
                }
            } else if (shape instanceof Polygon) {
                Polygon polygon = (Polygon) shape;
                if (filePath.contains("加密")||filePath.contains("解密")||filePath.contains("提取")||filePath.contains("水印")){
                    polygon = polygon.removeLastPoint();
                }
                //计算外环
                List<Point> exteriorPoints = polygon.getExteriors();
                List<Point> polarPoints = coordinate.calculatePolarCoordinates(exteriorPoints);
                encryptedDomainList = Domain.calEncrypt(polarPoints);
                watermarkDomainList = Domain.calWatermark(polarPoints);
                watermarkDomainCollectionList.add(watermarkDomainList);
                List<Point> polarPointShares = new ArrayList<>(exteriorPoints.size()*split);
                for (int i = 0; i < exteriorPoints.size(); i++) {
                    //生成极径秘密
                    BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
                    //生成极角秘密
                    BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
                    // 生成极径多项式系数
                    List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
                    // 生成极角多项式系数
                    List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, BigInteger.valueOf(367));
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
                List<Point> cartesiansPoints = Coordinate.toCartesian(polarPointShares);
                cartesiansPointslist.addAll(cartesiansPoints);
                List<List<Point>> exteriorList=new ArrayList<>();
                for (int i = 0; i < split; i++) {
                    int d = i;
                    // 创建坐标点集合
                    List<Point> coordinatesList = new ArrayList<>();
                    for (int j = 0; j <exteriorPoints.size(); j++) {
                        coordinatesList.add(new Point(cartesiansPoints.get(d).getX(), cartesiansPoints.get(d).getY()));
                        d += split;
                    }
                    exteriorList.add(coordinatesList);
                }
                if (polygon.getInteriors().size()!=0){
//                    System.out.println("有内环，"+"环数："+polygon.getInteriors().size()+"点个数："+polygon.getInteriors().get(0).size());
                    List<List<Point>> interiors = polygon.getInteriors();
                    for (List<Point> interior : interiors){
                        List<Point> interiorPoints = coordinate.calculatePolarCoordinates(interior);
                        encryptedDomainList = Domain.calEncrypt(interiorPoints);
                        watermarkDomainList = Domain.calWatermark(interiorPoints);
                        watermarkDomainCollectionList.add(watermarkDomainList);
                        for (int i = 0; i < interior.size(); i++) {
                            //生成极径秘密
                            BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
                            //生成极角秘密
                            BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
                            // 生成极径多项式系数
                            List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
                            // 生成极角多项式系数
                            List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, BigInteger.valueOf(367));
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
                                points.add(new Point(radiusShare, angleShare));
                            }
                        }
                    }
                    List<Point> cartesiansPoints1 = Coordinate.toCartesian(points);
                    cartesiansPointslist.addAll(cartesiansPoints1);
                    for (int i = 0; i < split; i++) {
                        int d = i;
                        List<List<Point>> interiorList=new ArrayList<>(polygon.getInteriors().size());
                        for (int j = 0; j <polygon.getInteriors().size(); j++){
                            // 创建坐标点集合
                            List<Point> coordinatesList = new ArrayList<>(polygon.getInteriors().get(j).size());
                            for (int a = 0; a <polygon.getInteriors().get(j).size(); a++) {
                                coordinatesList.add(new Point(cartesiansPoints1.get(d).getX(),cartesiansPoints1.get(d).getY()));
                                d += split;
                            }
                            interiorList.add(coordinatesList);
                        }
                        lists.get(i).add(new Polygon(exteriorList.get(i),interiorList));
                    }
                }else {
                    for (int i = 0; i < split; i++) {
                        lists.get(i).add(new Polygon(exteriorList.get(i),null));
                    }
                }
            } else if (shape instanceof MultiPolygon) {
//                System.out.println("多面");
                MultiPolygon multiPolygon = (MultiPolygon) shape;
                List<Polygon> polygons = multiPolygon.getPolygons();
                for (Polygon polygon : polygons) {
                    if (filePath.contains("加密")||filePath.contains("解密")||filePath.contains("提取")||filePath.contains("水印")){
                        polygon = polygon.removeLastPoint();
                    }
                    //计算外环
                    List<Point> exteriorPoints = polygon.getExteriors();
                    List<Point> polarPoints = coordinate.calculatePolarCoordinates(exteriorPoints);
                    encryptedDomainList = Domain.calEncrypt(polarPoints);
                    watermarkDomainList = Domain.calWatermark(polarPoints);
                    watermarkDomainCollectionList.add(watermarkDomainList);
                    List<Point> polarPointShares = new ArrayList<>(exteriorPoints.size() * split);

                    for (int i = 0; i < exteriorPoints.size(); i++) {
                        //生成极径秘密
                        BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
                        //生成极角秘密
                        BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
                        // 生成极径多项式系数
                        List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
                        // 生成极角多项式系数
                        List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, BigInteger.valueOf(367));
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

                    List<Point> cartesians = Coordinate.toCartesian(polarPointShares);
                    List<List<Point>> exteriorList = new ArrayList<>();

                    for (int i = 0; i < split; i++) {
                        int d = i;
                        // 创建坐标点集合
                        List<Point> coordinatesList = new ArrayList<>();
                        for (int j = 0; j < exteriorPoints.size(); j++) {
                            coordinatesList.add(new Point(cartesians.get(d).getX(), cartesians.get(d).getY()));
                            d += split;
                        }
                        exteriorList.add(coordinatesList);
                    }


                    if (polygon.getInteriors().size() != 0) {
                        List<List<Point>> interiors = polygon.getInteriors();
                        for (List<Point> interior : interiors) {
                            List<Point> interiorPoints = coordinate.calculatePolarCoordinates(interior);
                            encryptedDomainList = Domain.calEncrypt(interiorPoints);
                            watermarkDomainList = Domain.calWatermark(interiorPoints);
                            for (int i = 0; i < interior.size(); i++) {
                                //生成极径秘密
                                BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
                                //生成极角秘密
                                BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
                                // 生成极径多项式系数
                                List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
                                // 生成极角多项式系数
                                List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, BigInteger.valueOf(367));
                                //生成极径极角份额
                                List<BigInteger> radiusShares = SecretUtils.generateShares(radiusCoefficients, split, prime, randomRadiusBigInteger, i);
                                List<BigInteger> angleShares = SecretUtils.generateShares(angleCoefficients, split, BigInteger.valueOf(367), randomAngleBigInteger, i);
                                //随机获取前一个点的一个份额
                                randomRadiusBigInteger = radiusShares.get(new SecureRandom().nextInt(split));
                                randomAngleBigInteger = angleShares.get(new SecureRandom().nextInt(split));
                                radiusList.add(radiusShares);
                                angleList.add(angleShares);

                                //计算极坐标密文
                                for (int a = 0; a < split; a++) {
                                    double radiusShare = (radiusShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalRadius()) * Domain.Qr;
                                    double angleShare = (angleShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalAngle()) * Domain.Qa;
                                    points.add(new Point(radiusShare, angleShare));
                                }
                            }
                        }
                        List<Point> cartesiansPoints = Coordinate.toCartesian(points);
                        for (int i = 0; i < split; i++) {
                            int d = i;
                            List<List<Point>> interiorList = new ArrayList<>(polygon.getInteriors().size());
                            for (int j = 0; j < polygon.getInteriors().size(); j++) {
                                // 创建坐标点集合
                                List<Point> coordinatesList = new ArrayList<>(polygon.getInteriors().get(j).size());
                                for (int a = 0; a < polygon.getInteriors().get(j).size(); a++) {
                                    coordinatesList.add(new Point(cartesiansPoints.get(d).getX(), cartesiansPoints.get(d).getY()));
                                    d += split;
                                }
                                interiorList.add(coordinatesList);
                            }
                            lists.get(i).add(new Polygon(exteriorList.get(i), interiorList));
                        }
                    }
//                    polygonList.add()
                }
            }
        }
        long endTime = System.currentTimeMillis(); // 记录结束时间
        long duration = endTime - startTime; // 计算所花费的时间
        System.out.println("代码执行时间: " + duration + " 毫秒");
        for (int i = 0; i < split; i++){
            try {
                SecretUtils.createSHP(lists.get(i),layer,filePath.substring(0, filePath.lastIndexOf(".")),"加密",i);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
//        List<List<Point>> result = new ArrayList<>();
//        // 初始化三个子集合
//        result.add(new ArrayList<>());
//        result.add(new ArrayList<>());
//        result.add(new ArrayList<>());
//
//        for (int i = 0; i < cartesiansPointslist.size(); i++) {
//            int remainder = i % 3;
//            result.get(remainder).add(cartesiansPointslist.get(i));
//        }
//        List<List<Double>> result1 = new ArrayList<>();
//        // 初始化三个子集合
//        result1.add(new ArrayList<>());
//        result1.add(new ArrayList<>());
//        result1.add(new ArrayList<>());
//        System.out.println("原始点："+originalPoints);
//        System.out.println("加密:"+result.get(0));
//        for (int i = 0; i < originalPoints.size(); i++) {
//            double v = Math.sqrt(Math.pow(result.get(0).get(i).getX() - originalPoints.get(i).getX(), 2) + Math.pow(result.get(0).get(i).getY() - originalPoints.get(i).getY(), 2));
//            result1.get(0).add(v);
//            double v1= Math.sqrt(Math.pow(result.get(1).get(i).getX() - originalPoints.get(i).getX(), 2) + Math.pow(result.get(1).get(i).getY() - originalPoints.get(i).getY(), 2));
//            result1.get(1).add(v1);
//            double v2 = Math.sqrt(Math.pow(result.get(2).get(i).getX() - originalPoints.get(i).getX(), 2) + Math.pow(result.get(2).get(i).getY() - originalPoints.get(i).getY(), 2));
//            result1.get(2).add(v2);
//        }
//        System.out.println("最大距离："+ Collections.max(result1.get(0))+"  "+Collections.max(result1.get(1))+"  "+Collections.max(result1.get(2)));
//        System.out.println("最小距离："+ Collections.min(result1.get(0))+"  "+Collections.min(result1.get(1))+"  "+Collections.min(result1.get(2)));
//        double rmse = calculateRMSE(result1.get(0));
//        double rmse1 = calculateRMSE(result1.get(1));
//        double rmse2 = calculateRMSE(result1.get(2));
//        System.out.println("距离集合的均方根误差为: " + rmse+"  "+rmse1+"  "+rmse2);
//        System.out.println("距离集合的标准均方根误差为: " + rmse/Collections.max(result1.get(0))+"  "+rmse1/Collections.max(result1.get(1))+"  "+rmse2/Collections.max(result1.get(2)));
        StringBuilder json = new StringBuilder();
        json.append("{")
                .append("\"status\":\"success\",")
                .append("\"message\":\"文件加密成功: ").append(filePath).append("\"")
                .append("}");
        resp.getWriter().write(json.toString());
        return;
    }

//    public static double calculateRMSE(List<Double> distances) {
//        if (distances == null || distances.size() == 0) {
//            throw new IllegalArgumentException("输入的距离集合不能为空");
//        }
//
//        double sumOfSquaredDifferences = 0;
//        int n = distances.size();
//
//        for (int i = 0; i < n; i++) {
//            double diff = distances.get(i) - 0;  // 这里假设对比值为0，如果有实际对比值，替换此处即可
//            sumOfSquaredDifferences += diff * diff;
//        }
//
//        return Math.sqrt(sumOfSquaredDifferences / n);
//    }
}