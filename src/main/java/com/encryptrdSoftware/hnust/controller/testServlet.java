//package com.encryptrdSoftware.hnust.controller;
//
//import com.encryptrdSoftware.hnust.dao.FileDAO;
//import com.encryptrdSoftware.hnust.dao.UserDAO;
//import com.encryptrdSoftware.hnust.model.*;
//import com.encryptrdSoftware.hnust.util.SecretUtils;
//import com.encryptrdSoftware.hnust.util.StringUtils;
//import com.encryptrdSoftware.hnust.util.WatermarkingUtils;
//import org.gdal.gdal.gdal;
//import org.gdal.ogr.Layer;
//import org.gdal.ogr.ogr;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.math.BigInteger;
//import java.security.SecureRandom;
//import java.util.*;
//
//public class testServlet {
//    static {
//        gdal.AllRegister();
//        ogr.RegisterAll();
//    }
//    public static void main(String[] args) {
//          for (int s = 0; s < 30; s++){
//              String realFilePath = "E:\\三江源\\数据\\shp数据\\全国shp数据\\ceshi\\random_points.shp";
//              //获取份额数量
//              int split = 9;
//              int k = (split + 1) / 2;
//              long startTime = System.currentTimeMillis(); // 记录开始时间
//              // 获取layer
//              Layer layer = Domain.getLayer(new File(realFilePath));
//
//              //计算要素的数量
//              int num = (int) Domain.calPointsNumber(layer);
//              //获取图层的要素类型
//              int type = layer.GetGeomType();
//
//              //存放要素
//              Map<String, Object> map = Domain.SHPtoList(new File(realFilePath));
//              List<Shape> list = null;
//              List<Point> centerPoints = (List<Point>) map.get("centerPoints");
//              List<Point> points1 = (List<Point>) map.get("points1");
//              Coordinate coordinate=null;
//              List<List<Shape>> lists = new ArrayList<>();
//              if (type!=1){
//                  list=(List<Shape>) map.get("geometries");
//                  for (int i = 0; i <split;i++){
//                      List<Shape> shape=new ArrayList<>();
//                      lists.add(shape);
//                  }
//                  coordinate = Coordinate.initCoordinate(centerPoints);
//              }else {
//                  coordinate = Coordinate.initCoordinate(points1);
//              }
//              List<Point> points2 = coordinate.calculatePolarCoordinates(points1);
//              BigInteger prime = SecretUtils.generatePrime(Domain.calEncrypt(points2));
//
//              //加密域集合水印域集合
//              List<encryptedDomain> encryptedDomainList;
//              List<watermarkDomain> watermarkDomainList;
//              //线和面的加密域集合水印域集合
//              List<List<watermarkDomain>> watermarkDomainCollectionList = new ArrayList<>(num);
//              //存放极径和极角
//              List<List<BigInteger>> radiusList = new ArrayList<>();
//              List<List<BigInteger>> angleList = new ArrayList<>();
//
//              BigInteger randomRadiusBigInteger=null;
//              BigInteger randomAngleBigInteger = null;
//              if (type == 1){
//                  encryptedDomainList = Domain.calEncrypt(points2);
//                  watermarkDomainList = Domain.calWatermark(points2);
//                  List<Point> polarPointShares = new ArrayList<>();
//                  Random random = new Random();
//                  for (int i = 0; i < num; i++) {
//                      //生成极径秘密
//                      BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
//                      //生成极角秘密
//                      BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
//                      // 生成极径多项式系数
//                      List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
//                      // 生成极角多项式系数
//                      List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, prime);
//                      //生成极径极角份额
//                      List<BigInteger> radiusShares = SecretUtils.generateShares(radiusCoefficients, split,prime, randomRadiusBigInteger, i);
//                      List<BigInteger> angleShares = SecretUtils.generateShares(angleCoefficients, split, prime, randomAngleBigInteger, i);
//                      //随机获取前一个点的一个份额
//                      randomRadiusBigInteger = radiusShares.get(random.nextInt(split));
//                      randomAngleBigInteger = angleShares.get(random.nextInt(split));
//                      radiusList.add(radiusShares);
//                      angleList.add(angleShares);
//                      //计算极坐标密文
//                      for (int a = 0; a < split; a++) {
//                          double radiusShare = (radiusShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalRadius()) * Domain.Qr;
//                          double angleShare = (angleShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalAngle()) * Domain.Qa;
//                          polarPointShares.add(new Point(radiusShare, angleShare));
//                      }
//                  }
//                  long endTime = System.currentTimeMillis(); // 记录结束时间
//                  long duration = endTime - startTime; // 计算所花费的时间
//                  List<Point> cartesians = Coordinate.toCartesian(polarPointShares);
//                  System.out.println("代码执行时间: " + duration + " 毫秒");
//              }else {
//                  for (Shape shape:list){
//                      if (shape instanceof Line){
//                          Line line = (Line) shape;
//                          List<Point> linePoints = line.getPoints();
//                          List<Point> polarPoints = coordinate.calculatePolarCoordinates(linePoints);
//                          encryptedDomainList = Domain.calEncrypt(polarPoints);
//                          watermarkDomainList = Domain.calWatermark(polarPoints);
//                          watermarkDomainCollectionList.add(watermarkDomainList);
//                          List<Point> polarPointShares = new ArrayList<>();
//                          for (int i = 0; i < linePoints.size(); i++) {
//                              //生成极径秘密
//                              BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
//                              //生成极角秘密
//                              BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
//                              // 生成极径多项式系数
//                              List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
//                              // 生成极角多项式系数
//                              List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k,prime);
//                              //生成极径极角份额
//                              List<BigInteger> radiusShares = SecretUtils.generateShares(radiusCoefficients, split, prime, randomRadiusBigInteger, i);
//                              List<BigInteger> angleShares = SecretUtils.generateShares(angleCoefficients, split, prime, randomAngleBigInteger, i);
//                              //随机获取前一个点的一个份额
//                              randomRadiusBigInteger = radiusShares.get(new SecureRandom().nextInt(split));
//                              randomAngleBigInteger = angleShares.get(new SecureRandom().nextInt(split));
//                              radiusList.add(radiusShares);
//                              angleList.add(angleShares);
//                              //计算极坐标密文
//                              for (int a = 0; a < split; a++) {
//                                  double radiusShare = (radiusShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalRadius()) * Domain.Qr;
//                                  double angleShare = (angleShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalAngle()) * Domain.Qa;
//                                  polarPointShares.add(new Point(radiusShare, angleShare));
//                              }
//                          }
//                          List<Point> cartesians = Coordinate.toCartesian(polarPointShares);
//                          for (int i = 0; i < split; i++) {
//                              int d = i;
//                              // 创建坐标点集合
//                              List<Point> coordinatesList = new ArrayList<>();
//                              for (int j = 0; j <linePoints.size(); j++) {
//                                  coordinatesList.add(new Point(cartesians.get(d).getX(), cartesians.get(d).getY()));
//                                  d += split;
//                              }
//                              lists.get(i).add(new Line(coordinatesList));
//                          }
//                      } else if (shape instanceof MultiLine) {
//                          MultiLine multiLine = (MultiLine) shape;
//                          List<Point> polarPointShares = new ArrayList<>();
//                          for (Line line1 : multiLine.getLines()){
//                              List<Point> linePoints = line1.getPoints();
//                              List<Point> polarPoints = coordinate.calculatePolarCoordinates(linePoints);
//                              encryptedDomainList = Domain.calEncrypt(polarPoints);
//                              watermarkDomainList = Domain.calWatermark(polarPoints);
//                              watermarkDomainCollectionList.add(watermarkDomainList);
//
//                              for (int i = 0; i < line1.getLength(); i++) {
//                                  //生成极径秘密
//                                  BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
//                                  //生成极角秘密
//                                  BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
//                                  // 生成极径多项式系数
//                                  List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
//                                  // 生成极角多项式系数
//                                  List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, prime);
//                                  //生成极径极角份额
//                                  List<BigInteger> radiusShares = SecretUtils.generateShares(radiusCoefficients, split, prime, randomRadiusBigInteger, i);
//                                  List<BigInteger> angleShares = SecretUtils.generateShares(angleCoefficients, split, prime, randomAngleBigInteger, i);
//                                  //随机获取前一个点的一个份额
//                                  randomRadiusBigInteger = radiusShares.get(new SecureRandom().nextInt(split));
//                                  randomAngleBigInteger = angleShares.get(new SecureRandom().nextInt(split));
//                                  radiusList.add(radiusShares);
//                                  angleList.add(angleShares);
//                                  //计算极坐标密文
//                                  for (int a = 0; a < split; a++) {
//                                      double radiusShare = (radiusShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalRadius()) * Domain.Qr;
//                                      double angleShare = (angleShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalAngle()) * Domain.Qa;
//                                      polarPointShares.add(new Point(radiusShare, angleShare));
//                                  }
//                              }
//                          }
//                          List<Point> cartesians = Coordinate.toCartesian(polarPointShares);
//                          for (int i = 0; i < split; i++) {
//                              int d = i;
//                              List<Line> lineList = new ArrayList<>();
//                              for (int j = 0; j <multiLine.getLines().size(); j++){
//                                  // 创建坐标点集合
//                                  List<Point> coordinatesList = new ArrayList<>();
//                                  for (int a = 0; a <multiLine.getLines().get(j).getLength(); a++) {
//                                      coordinatesList.add(new Point(cartesians.get(d).getX(), cartesians.get(d).getY()));
//                                      d += split;
//                                  }
//                                  lineList.add(new Line(coordinatesList));
//                              }
//                              lists.get(i).add(new MultiLine(lineList));
//                          }
//                      } else if (shape instanceof Polygon) {
//                          Polygon polygon = (Polygon) shape;
//                          //计算外环
//                          List<Point> exteriorPoints = polygon.getExteriors();
//                          List<Point> polarPoints = coordinate.calculatePolarCoordinates(exteriorPoints);
//                          encryptedDomainList = Domain.calEncrypt(polarPoints);
//                          watermarkDomainList = Domain.calWatermark(polarPoints);
//                          watermarkDomainCollectionList.add(watermarkDomainList);
//                          List<Point> polarPointShares = new ArrayList<>(exteriorPoints.size()*split);
//                          for (int i = 0; i < exteriorPoints.size(); i++) {
//                              //生成极径秘密
//                              BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
//                              //生成极角秘密
//                              BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
//                              // 生成极径多项式系数
//                              List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
//                              // 生成极角多项式系数
//                              List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, prime);
//                              //生成极径极角份额
//                              List<BigInteger> radiusShares = SecretUtils.generateShares(radiusCoefficients, split, prime, randomRadiusBigInteger, i);
//                              List<BigInteger> angleShares = SecretUtils.generateShares(angleCoefficients, split, prime, randomAngleBigInteger, i);
//                              //随机获取前一个点的一个份额
//                              randomRadiusBigInteger = radiusShares.get(new SecureRandom().nextInt(split));
//                              randomAngleBigInteger = angleShares.get(new SecureRandom().nextInt(split));
//                              radiusList.add(radiusShares);
//                              angleList.add(angleShares);
//                              //计算极坐标密文
//                              for (int a = 0; a < split; a++) {
//                                  double radiusShare = (radiusShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalRadius()) * Domain.Qr;
//                                  double angleShare = (angleShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalAngle()) * Domain.Qa;
//                                  polarPointShares.add(new Point(radiusShare, angleShare));
//                              }
//                          }
//                          List<Point> cartesians = Coordinate.toCartesian(polarPointShares);
//                          List<List<Point>> exteriorList=new ArrayList<>();
//                          for (int i = 0; i < split; i++) {
//                              int d = i;
//                              // 创建坐标点集合
//                              List<Point> coordinatesList = new ArrayList<>();
//                              for (int j = 0; j <exteriorPoints.size(); j++) {
//                                  coordinatesList.add(new Point(cartesians.get(d).getX(), cartesians.get(d).getY()));
//                                  d += split;
//                              }
//                              exteriorList.add(coordinatesList);
//                          }
//                          if (polygon.getInteriors().size()!=0){
//                              List<List<Point>> interiors = polygon.getInteriors();
//                              List<Point> points = new ArrayList<>();
//                              for (List<Point> interior : interiors){
//                                  List<Point> interiorPoints = coordinate.calculatePolarCoordinates(interior);
//                                  encryptedDomainList = Domain.calEncrypt(interiorPoints);
//                                  watermarkDomainList = Domain.calWatermark(interiorPoints);
//                                  watermarkDomainCollectionList.add(watermarkDomainList);
//                                  for (int i = 0; i < interior.size(); i++) {
//                                      //生成极径秘密
//                                      BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
//                                      //生成极角秘密
//                                      BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
//                                      // 生成极径多项式系数
//                                      List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
//                                      // 生成极角多项式系数
//                                      List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, prime);
//                                      //生成极径极角份额
//                                      List<BigInteger> radiusShares = SecretUtils.generateShares(radiusCoefficients, split, prime, randomRadiusBigInteger, i);
//                                      List<BigInteger> angleShares = SecretUtils.generateShares(angleCoefficients, split, prime, randomAngleBigInteger, i);
//                                      //随机获取前一个点的一个份额
//                                      randomRadiusBigInteger = radiusShares.get(new SecureRandom().nextInt(split));
//                                      randomAngleBigInteger = angleShares.get(new SecureRandom().nextInt(split));
//                                      radiusList.add(radiusShares);
//                                      angleList.add(angleShares);
//                                      //计算极坐标密文
//                                      for (int a = 0; a < split; a++) {
//                                          double radiusShare = (radiusShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalRadius()) * Domain.Qr;
//                                          double angleShare = (angleShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalAngle()) * Domain.Qa;
//                                          points.add(new Point(radiusShare, angleShare));
//                                      }
//                                  }
//                              }
//                              List<Point> cartesiansPoints = Coordinate.toCartesian(points);
//                              for (int i = 0; i < split; i++) {
//                                  int d = i;
//                                  List<List<Point>> interiorList=new ArrayList<>(polygon.getInteriors().size());
//                                  for (int j = 0; j <polygon.getInteriors().size(); j++){
//                                      // 创建坐标点集合
//                                      List<Point> coordinatesList = new ArrayList<>(polygon.getInteriors().get(j).size());
//                                      for (int a = 0; a <polygon.getInteriors().get(j).size(); a++) {
//                                          coordinatesList.add(new Point(cartesiansPoints.get(d).getX(),cartesiansPoints.get(d).getY()));
//                                          d += split;
//                                      }
//                                      interiorList.add(coordinatesList);
//                                  }
//                                  lists.get(i).add(new Polygon(exteriorList.get(i),interiorList));
//                              }
//                          }else {
//                              for (int i = 0; i < split; i++) {
//                                  lists.get(i).add(new Polygon(exteriorList.get(i),null));
//                              }
//                          }
//                      } else if (shape instanceof MultiPolygon) {
//                          MultiPolygon multiPolygon = (MultiPolygon) shape;
//                          List<Polygon> polygons = multiPolygon.getPolygons();
//                          List<Polygon> polygonList=new ArrayList<>();
//                          for (Polygon polygon : polygons) {
//                              //计算外环
//                              List<Point> exteriorPoints = polygon.getExteriors();
//                              List<Point> polarPoints = coordinate.calculatePolarCoordinates(exteriorPoints);
//                              encryptedDomainList = Domain.calEncrypt(polarPoints);
//                              watermarkDomainList = Domain.calWatermark(polarPoints);
//                              watermarkDomainCollectionList.add(watermarkDomainList);
//                              List<Point> polarPointShares = new ArrayList<>(exteriorPoints.size() * split);
//
//                              for (int i = 0; i < exteriorPoints.size(); i++) {
//                                  //生成极径秘密
//                                  BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
//                                  //生成极角秘密
//                                  BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
//                                  // 生成极径多项式系数
//                                  List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
//                                  // 生成极角多项式系数
//                                  List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, prime);
//                                  //生成极径极角份额
//                                  List<BigInteger> radiusShares = SecretUtils.generateShares(radiusCoefficients, split, prime, randomRadiusBigInteger, i);
//                                  List<BigInteger> angleShares = SecretUtils.generateShares(angleCoefficients, split, prime, randomAngleBigInteger, i);
//                                  //随机获取前一个点的一个份额
//                                  randomRadiusBigInteger = radiusShares.get(new SecureRandom().nextInt(split));
//                                  randomAngleBigInteger = angleShares.get(new SecureRandom().nextInt(split));
//                                  radiusList.add(radiusShares);
//                                  angleList.add(angleShares);
//                                  //计算极坐标密文
//                                  for (int a = 0; a < split; a++) {
//                                      double radiusShare = (radiusShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalRadius()) * Domain.Qr;
//                                      double angleShare = (angleShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalAngle()) * Domain.Qa;
//                                      polarPointShares.add(new Point(radiusShare, angleShare));
//                                  }
//                              }
//
//                              List<Point> cartesians = Coordinate.toCartesian(polarPointShares);
//                              List<List<Point>> exteriorList = new ArrayList<>();
//
//                              for (int i = 0; i < split; i++) {
//                                  int d = i;
//                                  // 创建坐标点集合
//                                  List<Point> coordinatesList = new ArrayList<>();
//                                  for (int j = 0; j < exteriorPoints.size(); j++) {
//                                      coordinatesList.add(new Point(cartesians.get(d).getX(), cartesians.get(d).getY()));
//                                      d += split;
//                                  }
//                                  exteriorList.add(coordinatesList);
//                              }
//
//                              if (polygon.getInteriors().size() != 0) {
//                                  List<List<Point>> interiors = polygon.getInteriors();
//                                  List<Point> points = new ArrayList<>();
//
//                                  for (List<Point> interior : interiors) {
//                                      List<Point> interiorPoints = coordinate.calculatePolarCoordinates(interior);
//                                      encryptedDomainList = Domain.calEncrypt(interiorPoints);
//                                      watermarkDomainList = Domain.calWatermark(interiorPoints);
//
//                                      for (int i = 0; i < interior.size(); i++) {
//                                          //生成极径秘密
//                                          BigInteger radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
//                                          //生成极角秘密
//                                          BigInteger angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
//                                          // 生成极径多项式系数
//                                          List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, prime);
//                                          // 生成极角多项式系数
//                                          List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, prime);
//                                          //生成极径极角份额
//                                          List<BigInteger> radiusShares = SecretUtils.generateShares(radiusCoefficients, split, prime, randomRadiusBigInteger, i);
//                                          List<BigInteger> angleShares = SecretUtils.generateShares(angleCoefficients, split, prime, randomAngleBigInteger, i);
//                                          //随机获取前一个点的一个份额
//                                          randomRadiusBigInteger = radiusShares.get(new SecureRandom().nextInt(split));
//                                          randomAngleBigInteger = angleShares.get(new SecureRandom().nextInt(split));
//                                          radiusList.add(radiusShares);
//                                          angleList.add(angleShares);
//
//                                          //计算极坐标密文
//                                          for (int a = 0; a < split; a++) {
//                                              double radiusShare = (radiusShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalRadius()) * Domain.Qr;
//                                              double angleShare = (angleShares.get(a).doubleValue() + watermarkDomainList.get(i).getDecimalAngle()) * Domain.Qa;
//                                              points.add(new Point(radiusShare, angleShare));
//                                          }
//                                      }
//                                  }
//                                  List<Point> cartesiansPoints = Coordinate.toCartesian(points);
//                                  for (int i = 0; i < split; i++) {
//                                      int d = i;
//                                      List<List<Point>> interiorList = new ArrayList<>(polygon.getInteriors().size());
//                                      for (int j = 0; j < polygon.getInteriors().size(); j++) {
//                                          // 创建坐标点集合
//                                          List<Point> coordinatesList = new ArrayList<>(polygon.getInteriors().get(j).size());
//                                          for (int a = 0; a < polygon.getInteriors().get(j).size(); a++) {
//                                              coordinatesList.add(new Point(cartesiansPoints.get(d).getX(), cartesiansPoints.get(d).getY()));
//                                              d += split;
//                                          }
//                                          interiorList.add(coordinatesList);
//                                      }
//                                  }
//                              }
////                    polygonList.add()
//                          }
//                      }
//                  }
//              long endTime = System.currentTimeMillis(); // 记录结束时间
//              long duration = endTime - startTime; // 计算所花费的时间
//              System.out.println("代码执行时间: " + duration + " 毫秒");
//              }
//          }
//    }
//    }