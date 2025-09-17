//package com.encryptrdSoftware.hnust;
//
//import com.encryptrdSoftware.hnust.model.*;
//import com.encryptrdSoftware.hnust.util.SecretUtils;
//import org.gdal.gdal.gdal;
//import org.gdal.ogr.DataSource;
//import org.gdal.ogr.Layer;
//import org.gdal.ogr.ogr;
//
//import java.io.File;
//import java.io.IOException;
//import java.math.BigInteger;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Random;
//
//public class test2 {
//    public static int type;
//    public static int k = 0;
//    public final static int N = 5;
//    static {
//        gdal.AllRegister();
//        ogr.RegisterAll();
//    }
//    public static void main(String[] args) throws IOException {
////        String path = "C:\\Users\\lfs\\Desktop\\shp文件\\测试文件\\testSHP.shp";
//        String path = "E:\\三江源\\数据\\shp数据\\全国shp数据\\中国湖泊.shp";
//        // 打开Shapefile
//
//        DataSource dataSource = ogr.Open(path);
//        if (dataSource == null) {
//            System.err.println("Failed to open shapefile: " + path);
//            return; // 返回空列表
//        }
//        Layer layer = dataSource.GetLayer(0);
//        k = (N + 1) / 2;
//        //计算要素的数量
//        long num = Domain.calPointsNumber(layer);
//        System.out.println("面的个数："+num);
//        //获取图层名称
////        String s = layer.GetName();
////        System.out.println(s);
//        //获取特征数量
////        long l = layer.GetFeatureCount();
////        System.out.println(l);
//        type = layer.GetGeomType();
//        List<Shape> list = Domain.SHPtoList(new File(path));
//        List<Point> pointList = new ArrayList<>();
//        List<Line> lineList = new ArrayList<>();
//        List<Polygon> polygonList = new ArrayList<>();
//        List<List<encryptedDomain>> encryptedDomainList = new ArrayList<>();
//        List<List<watermarkDomain>> watermarkDomainList = new ArrayList<>();
////        if (type == 1) {
////            for (Object obj : list) {
////                if (obj instanceof Point) {
////                    pointList.add((Point) obj);
////                }
////            }
////            List<encryptedDomain> encryptedDomains = Domain.calEncrypt(pointList);
////            List<watermarkDomain> watermarkDomains = Domain.calWatermark(pointList);
////            System.out.println(encryptedDomains);
////            System.out.println(watermarkDomains);
////            List<List<Point>> lists = Coordinate.splitCollection(pointList);
////            Point polarPoint = Coordinate.calculateGeometricCenter(lists.get(0));
////            Point polarAxisPoint = Coordinate.calculateGeometricCenter(lists.get(1));
////            //构建量化步长
////            Domain.adaptationFactor = 0.001;
////            Domain.Qr = Domain.adaptationFactor * Math.sqrt(Math.pow(polarAxisPoint.getX() - polarPoint.getX(), 2) + Math.pow(polarAxisPoint.getY() - polarPoint.getY(), 2));
////            Domain.Qa = 1;
////
////            System.out.println("极点和极轴点为:" + polarPoint + " " + polarAxisPoint);
////            System.out.println("Qr值：" + Domain.Qr);
////            //计算极坐标
////            Coordinate coordinate = new Coordinate(polarPoint, polarAxisPoint);
////            List<Point> polarPoints = coordinate.calculatePolarCoordinates(pointList);
////            System.out.println(polarPoints);
////
////        }
//        //每个面的几何中心点
//        List<Point> PolygonCenterList = new ArrayList<>();
//        //计算完面的极坐标
////        List<List<Point>> linePolarList = new ArrayList<>();
//        //每个面的prime
//        List<BigInteger> primeList=new ArrayList<>();
//        Polygon polygon = null;
//        for (Object obj : list) {
//            if (obj instanceof Polygon) {
//                polygon = (Polygon) obj;
//                polygonList.add(polygon);
//            }
//            Point point = Coordinate.calculateGeometricCenter(polygon.getPoints());
//            PolygonCenterList.add(point);
//        }
//        List<List<Point>> splitLineCollection = Coordinate.splitCollection(PolygonCenterList);
//        //构建坐标系
//        Point polarPoint = Coordinate.calculateGeometricCenter(splitLineCollection.get(0));
//        Point polarAxisPoint = Coordinate.calculateGeometricCenter(splitLineCollection.get(1));
//        Coordinate coordinate = new Coordinate(polarPoint, polarAxisPoint);
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
//        for (int i = 0; i < polygonList.size(); i++) {
//            List<Point> points = coordinate.calculatePolarCoordinates(polygonList.get(i).getPoints());
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
//        System.out.println("大素数为："+maxPrime);
//
//        BigInteger radiusSecret=null;
//        BigInteger angleSecret=null;
//        List<BigInteger> radiusCoefficients=new ArrayList<>();
//        List<BigInteger> angleCoefficients=new ArrayList<>();
//        BigInteger randomRadiusBigInteger=null;
//        BigInteger randomAngleBigInteger=null;
//        List<BigInteger> radiusShares=null;
//        List<BigInteger> angleShares=null;
//        List<Point> polarPointShares=new ArrayList<>();
//        List<Point> points=new ArrayList<>();
//
//        for (int i = 0; i < polygonList.size(); i++) {
//            //对面的每个点进行处理
//            for (int j = 0; j < polygonList.get(i).getNum(); j++) {
//                radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).get(j).getIntegerRadius()));
//                angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).get(j).getIntegerAngle()));
//                radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, maxPrime);
//                angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, maxPrime);
//                radiusShares= SecretUtils.generateShares(radiusCoefficients,N,maxPrime,randomRadiusBigInteger,j);
//                angleShares= SecretUtils.generateShares(angleCoefficients,N,maxPrime,randomAngleBigInteger,j);
//                randomRadiusBigInteger=radiusShares.get(new Random().nextInt(5));
//                randomAngleBigInteger=angleShares.get(new Random().nextInt(5));
//                System.out.println("随机极径份额为:"+randomRadiusBigInteger);
//                System.out.println("随机极角份额为："+randomAngleBigInteger);
//                System.out.println("面"+(i+1)+"的第"+(j+1)+"个点的极径秘密为:"+radiusSecret);
//                System.out.println("面"+(i+1)+"的第"+(j+1)+"个点的极角秘密为:"+angleSecret);
//                System.out.println("面"+(i+1)+"的第"+(j+1)+"个点的极径多项式为:"+radiusCoefficients);
//                System.out.println("面"+(i+1)+"的第"+(j+1)+"个点的极角多项式为:"+angleCoefficients);
//                System.out.println("面"+(i+1)+"的第"+(j+1)+"个点的极径份额为:"+radiusShares);
//                System.out.println("面"+(i+1)+"的第"+(j+1)+"个点的极角份额为:"+angleShares);
//                for (int l = 0; l <N; l++) {
//                    System.out.println("面"+(i+1)+"的第"+(j+1)+"个点的第"+(l+1)+"个极坐标份额为:"+"("+ Domain.Qr*(radiusShares.get(l).doubleValue()+watermarkDomainList.get(i).get(j).getDecimalRadius())+","+(angleShares.get(l).doubleValue()+watermarkDomainList.get(i).get(j).getDecimalAngle())+")");
//                    points.add(new Point(Domain.Qr*(radiusShares.get(l).doubleValue()+watermarkDomainList.get(i).get(j).getDecimalRadius()),(angleShares.get(l).doubleValue()+watermarkDomainList.get(i).get(j).getDecimalAngle())));
//                }
//                System.out.println();
//            }
//        }
//
//        for (int i = 0; i < 5; i++) {
//            List<Polygon> polygons=new ArrayList<>();
//            int d=0;
//            for (int a = 0; a < num; a++) {
//                // 创建坐标点集合
//                List<Point> coordinatesList = new ArrayList<>();
//                for (int j = 0; j < polygonList.get(a).getNum(); j++) {
//                    coordinatesList.add(new Point(points.get(d+i).getX(), points.get(d+i).getY()));
//                    d+=5;
//                }
//                polygons.add(new Polygon(Coordinate.toCartesian(coordinatesList)));
//            }
////            SecretUtils.createSHP1(polygons,layer,"加密");
//        }
//    }
//
//
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
//}
//
