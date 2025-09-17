package com.encryptrdSoftware.hnust.model;
import org.gdal.gdal.gdal;
import org.gdal.ogr.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Domain {
    public static double adaptationFactor;
    public static double Qr=0.1;
    public static double Qa;
    static Feature feature = null;
    static Geometry geometry = null;

    //创建加密域
    public static List<encryptedDomain> calEncrypt(List<Point> list) {
        List<encryptedDomain> list1 = new ArrayList<>(list.size());
        for (Point point : list) {
            int intR = (int) point.getX();
            int intA = (int) point.getY();
            list1.add(new encryptedDomain(intR, intA));
        }
        return list1;
    }

    //创建水印域
    public static List<watermarkDomain> calWatermark(List<Point> list) {
        List<watermarkDomain> result = new ArrayList<>(list.size());
        for (Point point : list) {
            // 直接计算加密值，不保存中间结果
            int intR = (int) point.getX();
            int intA = (int) point.getY();
            // 直接计算水印并添加到结果
            double decR = point.getX() - intR;
            double decA = point.getY() - intA;
            result.add(new watermarkDomain(decR, decA));
        }
        return result;
    }

    // 将shp文件中的点存入list<Point>集合
    public static Map<String,Object> SHPtoList(File filePath) {
        Map<String,Object> maps=new HashMap<>();
        List<Shape> geometries = new ArrayList<>();
        List<Point> centerPoints=new ArrayList<>();
        List<Point> points1=new ArrayList<>();
        List<Integer> lengths=new ArrayList<>();
        int num=0;
        Layer layer = getLayer(filePath);
        if (layer == null) {
            throw new IllegalArgumentException("无法获取图层");
        }
        Feature feature;
        try {
            while ((feature = layer.GetNextFeature()) != null) {
                Geometry geometry = feature.GetGeometryRef();
                if (geometry!= null) {
                    switch (geometry.GetGeometryType()) {
                        // 点
                        case 1:
                            processPoint(geometry,points1);
                            break;
                        // 线
                        case 2:
                            List<Point> points = processLine(geometry);
                            points1.addAll(points);
                            centerPoints.add(Coordinate.calculateGeometricCenter(points));
                            geometries.add(new Line(points));
                            num+=points.size();
                            lengths.add(points.size());
                            break;
                        // 面
                        case 3:
                            List<Point> exteriorPoints = new ArrayList<>();
                            List<List<Point>> interiors = new ArrayList<>();
                            Geometry exteriorRing = geometry.GetGeometryRef(0);
                            if (exteriorRing!= null) {
                                int exteriorPointCount = exteriorRing.GetPointCount();
                                for (int i = 0; i < exteriorPointCount; i++) {
                                    double x = exteriorRing.GetX(i);
                                    double y = exteriorRing.GetY(i);
                                    exteriorPoints.add(new Point(x, y));
                                }
                                num+=exteriorPointCount;
                                centerPoints.add(Coordinate.calculateGeometricCenter(exteriorPoints));
                                points1.addAll(exteriorPoints);
                            }

                            int ringCount = geometry.GetGeometryCount();
                            for (int i = 1; i < ringCount; i++) { // 从1开始，0是外环
                                Geometry interiorRing = geometry.GetGeometryRef(i);
                                if (interiorRing != null) {
                                    List<Point> interiorPoints = new ArrayList<>();
                                    int interiorPointCount = interiorRing.GetPointCount();
                                    for (int j = 0; j < interiorPointCount; j++) {
                                        double x = interiorRing.GetX(j);
                                        double y = interiorRing.GetY(j);
                                        interiorPoints.add(new Point(x, y));
                                        num+=interiorPointCount;
                                    }
                                    interiors.add(interiorPoints);
                                    centerPoints.add(Coordinate.calculateGeometricCenter(interiorPoints));
                                    points1.addAll(interiorPoints);
                                }
                            }
                                    geometries.add(new Polygon(exteriorPoints,interiors));
//
                            break;
                        case 5:
                            List<Line> lines = new ArrayList<>();
                            for (int j = 0; j < geometry.GetGeometryCount(); j++) {
                                Geometry g = geometry.GetGeometryRef(j);
                                List<Point> pointList = processLine(g);
                                lines.add(new Line(pointList));
                                centerPoints.add(Coordinate.calculateGeometricCenter(pointList));
                                points1.addAll(pointList);
                                num+=pointList.size();
                            }
                            geometries.add(new MultiLine(lines));
                            break;
                        case 6:
                            List<Polygon> polygons = new ArrayList<>();
                            for (int j = 0; j < geometry.GetGeometryCount(); j++) {
                                Geometry polygonGeometry = geometry.GetGeometryRef(j);
                                List<Point> exteriorPointsPolygon = new ArrayList<>();
                                List<List<Point>> interiorsPolygon = new ArrayList<>();

                                // 处理外环
                                Geometry exteriorRing1 = polygonGeometry.GetGeometryRef(0);
                                if (exteriorRing1 != null) {
                                    int exteriorPointCount = exteriorRing1.GetPointCount();
                                    for (int i = 0; i < exteriorPointCount; i++) {
                                        double x = exteriorRing1.GetX(i);
                                        double y = exteriorRing1.GetY(i);
                                        exteriorPointsPolygon.add(new Point(x, y));
                                    }
                                    num += exteriorPointCount;
                                    centerPoints.add(Coordinate.calculateGeometricCenter(exteriorPointsPolygon));
                                    points1.addAll(exteriorPointsPolygon);
                                }

                                // 处理内环
                                int ringCountPolygon = polygonGeometry.GetGeometryCount();
                                for (int i = 1; i < ringCountPolygon; i++) { // 从1开始，0是外环
                                    Geometry interiorRing = polygonGeometry.GetGeometryRef(i);
                                    if (interiorRing != null) {
                                        List<Point> interiorPointsPolygon = new ArrayList<>();
                                        int interiorPointCount = interiorRing.GetPointCount();
                                        for (int k = 0; k < interiorPointCount; k++) {
                                            double x = interiorRing.GetX(k);
                                            double y = interiorRing.GetY(k);
                                            interiorPointsPolygon.add(new Point(x, y));
                                        }
                                        num += interiorPointCount;
                                        interiorsPolygon.add(interiorPointsPolygon);
                                        centerPoints.add(Coordinate.calculateGeometricCenter(interiorPointsPolygon));
                                        points1.addAll(interiorPointsPolygon);
                                    }
                                }
                                polygons.add(new Polygon(exteriorPointsPolygon, interiorsPolygon));
                            }
                            geometries.add(new MultiPolygon(polygons));
                            break;
                        default:
                            System.out.println("不支持的集合类型！");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("处理图层时发生错误", e);
        } finally {
            closeLayer(layer);
        }
        maps.put("geometries",geometries);
        maps.put("centerPoints",centerPoints);
        maps.put("num",num);
        maps.put("points1",points1);
        maps.put("lengths",lengths);
        return maps;
    }

    public static void processPoint(Geometry geometry,List<Point> points){
        for (int i = 0; i < geometry.GetPointCount(); i++) {
            double x = geometry.GetX(i);
            double y = geometry.GetY(i);
            points.add(new Point(x, y));
        }
    }

    public static List<Point> processLine(Geometry geometry){
        List<Point> linePoints = new ArrayList<>();
        for (int i = 0; i < geometry.GetPointCount(); i++) {
            double x = geometry.GetX(i);
            double y = geometry.GetY(i);
            linePoints.add(new Point(x, y));
        }
       return linePoints;
    }

    private static void closeLayer(Layer layer) {
        if (layer != null) {
            layer.delete();
        }
    }

        //获取layer对象
        public static Layer getLayer (File filePath){
            // 读取Shapefile
            gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
            DataSource dataSource = ogr.Open(String.valueOf(filePath), 0);
            if (dataSource != null) {
                return dataSource.GetLayer(0);
            }
            return null;
        }

        //计算shp文件点的数量
        public static long calPointsNumber (Layer layer){
            long n;
            n = layer.GetFeatureCount();
            return n;
        }

        public static List<Point> toList (File filePath){
            List<Point> list = new ArrayList<>();
            Layer layer = getLayer(filePath);
            if (layer == null) {
                System.out.println("无法获取图层");
                return null;
            }
            while ((feature = layer.GetNextFeature()) != null) {
                // 获取要素的几何对象
                geometry = feature.GetGeometryRef();
                double x = geometry.GetX();
                double y = geometry.GetY();
                list.add(new Point(x, y));
            }
            return list;
        }
    public static List<Feature> readShapefile(String shpFilePath) {
        List<Feature> features = new ArrayList<>();

        // 初始化 GDAL
        gdal.AllRegister();
        ogr.RegisterAll();

        // 打开 Shapefile
        DataSource dataset = ogr.Open(shpFilePath);

        if (dataset == null) {
            System.err.println("无法打开 Shapefile: " + shpFilePath);
            return features; // 返回空集合
        }

        // 获取图层
        Layer layer = dataset.GetLayer(0);
        Feature feature;

        // 遍历要素
        while ((feature = layer.GetNextFeature()) != null) {
            features.add(feature); // 添加特征到集合
        }

        // 清理
        dataset.delete();
        return features; // 返回要素集合
    }
}










