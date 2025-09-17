//package com.encryptrdSoftware.hnust;
//
//import com.encryptrdSoftware.hnust.model.*;
//import com.encryptrdSoftware.hnust.util.SecretUtils;
//import com.encryptrdSoftware.hnust.util.WatermarkingUtils;
//import org.gdal.gdal.gdal;
//import org.gdal.ogr.Layer;
//
//import java.io.File;
//import java.io.IOException;
//import java.math.BigInteger;
//import java.util.*;
//
//public class test1 {
//    public static int type;
//    public static int k = 0;
//    public final static int N = 5;
//
//    public static int split = 4;
//
//    public static void main(String[] args) throws IOException {
////        String path = "C:\\Users\\lfs\\Desktop\\shp文件\\测试文件\\testSHP.shp";
//        String path = "E:\\三江源\\数据\\shp数据\\全国shp数据\\主要铁路.shp";
//        // 打开Shapefile
//        gdal.AllRegister();
//        Layer layer = Domain.getLayer(new File(path));
//        k = (N + 1) / 2;
//        //计算要素的数量
//        long num = Domain.calPointsNumber(layer);
//        System.out.println(num);
//        //获取图层名称
//        type = layer.GetGeomType();
//        System.out.println(type);
//        List<Shape> list = Domain.SHPtoList(new File(path));
//        List<Point> pointList = new ArrayList<>();
//        List<Line> lineList = new ArrayList<>();
//        List<Polygon> polygonList = new ArrayList<>();
//        List<List<encryptedDomain>> encryptedDomainList = new ArrayList<>();
//        List<List<watermarkDomain>> watermarkDomainList = new ArrayList<>();
//
//        //每条线段的几何中心点
//        List<Point> lineCenterList = new ArrayList<>();
//        //计算完线段的极坐标
//        List<List<Point>> linePolarList = new ArrayList<>();
//        //每个线段的prime
//        List<BigInteger> primeList = new ArrayList<>();
//        Line line = null;
//        for (Object obj : list) {
//            if (obj instanceof Line) {
//                line = (Line) obj;
//                lineList.add(line);
//            }
//            Point point = Coordinate.calculateGeometricCenter(line.getPoints());
//            lineCenterList.add(point);
//        }
//        List<List<Point>> splitLineCollection = Coordinate.splitCollection(lineCenterList);
//        //构建坐标系
//        Point polarPoint = Coordinate.calculateGeometricCenter(splitLineCollection.get(0));
//        Point polarAxisPoint = Coordinate.calculateGeometricCenter(splitLineCollection.get(1));
//        Coordinate coordinate = new Coordinate(polarPoint, polarAxisPoint);
//        System.out.println();
//
//        //构建量化步长
//        Domain.adaptationFactor = 0.001;
//        Domain.Qr = Domain.adaptationFactor * Math.sqrt(Math.pow(polarAxisPoint.getX() - polarPoint.getX(), 2) + Math.pow(polarAxisPoint.getY() - polarPoint.getY(), 2));
//        Domain.Qa = 1;
//
//        System.out.println("极点和极轴点为:" + polarPoint + " " + polarAxisPoint);
//        System.out.println("Qr值：" + Domain.Qr);
//        System.out.println();
//        //计算其他点的极坐标,求加密域和水印域
//        for (int i = 0; i < lineList.size(); i++) {
//            List<Point> points = coordinate.calculatePolarCoordinates(lineList.get(i).getPoints());
//            List<encryptedDomain> encryptedDomains = Domain.calEncrypt(points);
//            List<watermarkDomain> watermarkDomains = Domain.calWatermark(points);
//            BigInteger prime = SecretUtils.generatePrime(encryptedDomains);
//            primeList.add(prime);
//            encryptedDomainList.add(encryptedDomains);
//            watermarkDomainList.add(watermarkDomains);
//        }
////            System.out.println(linePolarList);
////            System.out.println();
////            System.out.println(encryptedDomainList);
////            System.out.println();
////            System.out.println(watermarkDomainList);
//
//        //求prime
//        BigInteger maxPrime = Collections.max(primeList);
//        System.out.println("大素数为：" + maxPrime);
//
//        BigInteger radiusSecret = null;
//        BigInteger angleSecret = null;
//        List<BigInteger> radiusCoefficients = new ArrayList<>();
//        List<BigInteger> angleCoefficients = new ArrayList<>();
//        BigInteger randomRadiusBigInteger = null;
//        BigInteger randomAngleBigInteger = null;
//        List<BigInteger> radiusShares = null;
//        List<BigInteger> angleShares = null;
//        List<Point> polarPointShares = new ArrayList<>();
//        List<Point> points = new ArrayList<>();
//        List<List<BigInteger>> radiusSharesList = new ArrayList<>();
//        List<List<BigInteger>> angleSharesList = new ArrayList<>();
//
//        for (int i = 0; i < lineList.size(); i++) {
//            //对线段的每个点进行处理
//            for (int j = 0; j < lineList.get(i).getLength(); j++) {
//                radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).get(j).getIntegerRadius()));
//                angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).get(j).getIntegerAngle()));
//                radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, maxPrime);
//                angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, maxPrime);
//                radiusShares = SecretUtils.generateShares(radiusCoefficients, N, maxPrime, randomRadiusBigInteger, j);
//                angleShares = SecretUtils.generateShares(angleCoefficients, N, maxPrime, randomAngleBigInteger, j);
//                radiusSharesList.add(radiusShares);
//                angleSharesList.add(angleShares);
//                randomRadiusBigInteger = radiusShares.get(new Random().nextInt(5));
//                randomAngleBigInteger = angleShares.get(new Random().nextInt(5));
////                System.out.println("随机极径份额为:"+randomRadiusBigInteger);
////                System.out.println("随机极角份额为："+randomAngleBigInteger);
////                System.out.println("线段"+(i+1)+"的第"+(j+1)+"个点的极径秘密为:"+radiusSecret);
////                System.out.println("线段"+(i+1)+"的第"+(j+1)+"个点的极角秘密为:"+angleSecret);
////                System.out.println("线段"+(i+1)+"的第"+(j+1)+"个点的极径多项式为:"+radiusCoefficients);
////                System.out.println("线段"+(i+1)+"的第"+(j+1)+"个点的极角多项式为:"+angleCoefficients);
////                System.out.println("线段"+(i+1)+"的第"+(j+1)+"个点的极径份额为:"+radiusShares);
////                System.out.println("线段"+(i+1)+"的第"+(j+1)+"个点的极角份额为:"+angleShares);
//                for (int l = 0; l < N; l++) {
////                    System.out.println("线段"+(i+1)+"的第"+(j+1)+"个点的第"+(l+1)+"个极坐标份额为:"+"("+ Domain.Qr*(radiusShares.get(l).doubleValue()+watermarkDomainList.get(i).get(j).getDecimalRadius())+","+(angleShares.get(l).doubleValue()+watermarkDomainList.get(i).get(j).getDecimalAngle())+")");
//                    points.add(new Point(Domain.Qr * (radiusShares.get(l).doubleValue() + watermarkDomainList.get(i).get(j).getDecimalRadius()), (angleShares.get(l).doubleValue() + watermarkDomainList.get(i).get(j).getDecimalAngle())));
//                }
//            }
//        }
//
//        //生成文件
//        for (int i = 0; i < 5; i++) {
//            List<Line> lines = new ArrayList<>();
//            int d = 0;
//            for (int a = 0; a < num; a++) {
//                // 创建坐标点集合
//                List<Point> coordinatesList = new ArrayList<>();
//                for (int j = 0; j < lineList.get(a).getLength(); j++) {
//                    coordinatesList.add(new Point(points.get(d + i).getX(), points.get(d + i).getY()));
//                    d += 5;
//                }
//                lines.add(new Line(Coordinate.toCartesian(coordinatesList)));
//            }
////            SecretUtils.createSHP1(lines, layer,"主要铁路","加密");
//        }
//
////        //添加水印
////        String img="C:\\Users\\lfs\\Desktop\\test2.bmp";
////        StringBuilder watermarking = WatermarkingUtils.createWatermarking(img);
////        int length = watermarking.length();
////        int n1=1;
////        while (true){
////            if (watermarking.length()<points.size()){
////                watermarking=watermarking.append(watermarking);
////                n1++;
////            }
////            if (watermarking.length()>points.size()){
////                break;
////            }
////        }
////
////        List<String> strings = WatermarkingUtils.distributeBitString(String.valueOf(watermarking), points.size());
////
////        List<encryptedDomain> encryptedDomains = Domain.calEncrypt(points);
////        List<watermarkDomain> watermarkDomains = Domain.calWatermark(points);
////
////        List<Point> watermarkedPoints=new ArrayList<>();
////        for (int i = 0; i < points.size(); i++) {
////            double watermarkedR = encryptedDomains.get(i).getIntegerRadius() + WatermarkingUtils.embedWatermark(strings.get(i), watermarkDomains.get(i).getDecimalRadius());
////            double watermarkedA = encryptedDomains.get(i).getIntegerAngle() + WatermarkingUtils.embedWatermark(strings.get(i), watermarkDomains.get(i).getDecimalAngle());
////            watermarkedPoints.add(new Point(watermarkedR,watermarkedA));
////        }
////        for (int i = 0; i < 5; i++) {
////            List<Line> lines1=new ArrayList<>();
////            int d=0;
////            for (int a = 0; a < num; a++) {
////            // 创建坐标点集合
////            List<Point> coordinatesList = new ArrayList<>();
////            for (int j = 0; j < lineList.get(a).getLength(); j++) {
////                coordinatesList.add(new Point(watermarkedPoints.get(d+i).getX(), watermarkedPoints.get(d+i).getY()));
////                d+=5;
////            }
////            lines1.add(new Line(Coordinate.toCartesian(coordinatesList)));
//////                System.out.println("极坐标："+coordinatesList);
//////                System.out.println();
//////                System.out.println("直角坐标："+Coordinate.toCartesian(coordinatesList));
//////                System.out.println();
////            }
//////            Secret.createSHP(lines1,layer);
////        }
//        //解密
//        List<Integer> indices = new ArrayList<>();
//        Set<Integer> uniqueIntegers = SecretUtils.createRandomNumber(k, 5);
//        Iterator<Integer> iterator = uniqueIntegers.iterator();
//        while (iterator.hasNext()) {
//            Integer integer = iterator.next();
//            System.out.println("选区的：" + integer);
//            indices.add(integer + 1);
//        }
//
//        int n = 0;
//        List<Line> recoverLines = new ArrayList<>();
//        for (int i = 0; i < lineList.size(); i++) {
//            List<Point> list1 = new ArrayList<>();
//                System.out.println("长度:"+lineList.get(i).getLength());
//            for (int j = 0; j < lineList.get(i).getLength(); j++) {
//                List<BigInteger> radiusIntegers = radiusSharesList.get(n);
//                List<BigInteger> angleIntegers = angleSharesList.get(n);
//                List<BigInteger> bigIntegers1 = new ArrayList<>();
//                List<BigInteger> bigIntegers2 = new ArrayList<>();
//                for (int l = 0; l < indices.size(); l++) {
//                    bigIntegers1.add(radiusIntegers.get(indices.get(l) - 1));
//                    bigIntegers2.add(angleIntegers.get(indices.get(l) - 1));
//                }
//                BigInteger recoverRadius = SecretUtils.recoverSecret(bigIntegers1, indices, maxPrime);
//                BigInteger recoverAngle = SecretUtils.recoverSecret(bigIntegers2, indices, maxPrime);
//                double x = recoverRadius.doubleValue() + watermarkDomainList.get(i).get(j).getDecimalRadius();
//                double y = recoverAngle.doubleValue() + watermarkDomainList.get(i).get(j).getDecimalAngle();
//                list1.add(new Point(x, y));
//                n++;
//            }
//            List<Point> points1 = Coordinate.recoverCartesian(list1);
//            recoverLines.add(new Line(points1));
//        }
//        System.out.println(recoverLines);
////        SecretUtils.createSHP1(recoverLines, layer, "主要铁路","解密");
//
////        //提取水印
////        StringBuilder sb=new StringBuilder();
////        for (int i = 0; i < watermarkedPoints.size(); i++) {
////            int watermark = WatermarkingUtils.extractWatermark(strings.get(i), watermarkedPoints.get(i).getX());
////            String s = Integer.toBinaryString(watermark);
////            String s1 = WatermarkingUtils.addLeadingZeros(s, strings.get(i).length());
////            sb.append(s1);
////            if (sb.length()>=length){
////                break;
////            }
////        }
////        System.out.println(sb==watermarking);
////        System.out.println(sb.length());
////        System.out.println(watermarking.length());
////        WatermarkingUtils.decodeImage(String.valueOf(sb));
////    }
//
//
////        for (int k = 0; k <Secret.NUM_SHARES; k++) {
////            double radiusShare=radiusShares.get(k).doubleValue()+watermarkDomains.get(a).getDecimalRadius();
////            radiusShare=radiusShare*Domain.Qr;
////            double angleShare=angleShares.get(k).doubleValue()+watermarkDomains.get(a).getDecimalAngle();
////            angleShare=angleShare*Domain.Qa;
////            polarCoordinateShares.add(new Point(radiusShare,angleShare));
////            encryptedRadiusShares.add(radiusShare);
////            encryptedAngleShares.add(angleShare);
////        }
//
////        }else {
////            List<Point> polygonCenterList=new ArrayList<>();
////            List<List<Point>> polygonPolarList=new ArrayList<>();
////            Polygon polygon=null;
////            for (Object obj : list) {
////                if (obj instanceof Polygon) {
////                    polygon=(Polygon) obj;
////                    polygonList.add(polygon);
////                }
////                Point point = Coordinate.calculateGeometricCenter(polygon.getCoordinates());
////                polygonCenterList.add(point);
////                List<encryptedDomain> encryptedDomains = Domain.calEncrypt(polygon.getCoordinates());
////                List<watermarkDomain> watermarkDomains = Domain.calWatermark(polygon.getCoordinates());
////                encryptedDomainList.add(encryptedDomains);
////                watermarkDomainList.add(watermarkDomains);
////            }
////            List<List<Point>> splitPolygonCollection = Coordinate.splitCollection(polygonCenterList);
////            Point polarPoint = Coordinate.calculateGeometricCenter(splitPolygonCollection.get(0));
////            Point polarAxisPoint=Coordinate.calculateGeometricCenter(splitPolygonCollection.get(1));
////            System.out.println(encryptedDomainList);
////            System.out.println(watermarkDomainList);
////            //构建量化步长
////            Domain.adaptationFactor = 0.001;
////            Domain.Qr = Domain.adaptationFactor * Math.sqrt(Math.pow(polarAxisPoint.getX() - polarPoint.getX(), 2) + Math.pow(polarAxisPoint.getY() - polarPoint.getY(), 2));
////            Domain.Qa = 1;
////
////            System.out.println("极点和极轴点为:"+polarPoint+" "+polarAxisPoint);
////            System.out.println("Qr值："+Domain.Qr);
////            //构建坐标系并计算其他点的极坐标
////            Coordinate coordinate = new Coordinate(polarPoint, polarAxisPoint);
////            for (int i = 0; i < lineList.size(); i++) {
////                List<Point> points = coordinate.calculatePolarCoordinates(lineList.get(i).getPoints());
////                polygonPolarList.add(points);
////            }
////        }
//
//    }
//}
//
//
//
