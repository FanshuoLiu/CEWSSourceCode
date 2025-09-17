package com.encryptrdSoftware.hnust.model;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.util.*;

// 二维坐标点类（内部使用）
class Point2D {
    private double x;
    private double y;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    @Override
    public String toString() {
        return x + "," + y;
    }
}

public class ShapefileInterpolationAttack {
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * 从SHP文件读取点要素
     * @param shpFilePath SHP文件路径（如"input.shp"）
     * @return 点坐标集合
     * @throws Exception 读取过程中的异常
     */
    public static List<Point2D> readPointsFromShapefile(String shpFilePath) throws Exception {
        List<Point2D> points = new ArrayList<>();

        // 配置数据存储参数
        Map<String, Object> params = new HashMap<>();
        params.put("url", new File(shpFilePath).toURI().toURL());

        // 获取数据存储
        DataStore dataStore = DataStoreFinder.getDataStore(params);
        if (dataStore == null) {
            throw new IllegalArgumentException("无法打开SHP文件: " + shpFilePath);
        }

        // 获取要素类型名称（通常是文件名）
        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
        SimpleFeatureCollection features = featureSource.getFeatures();

        // 遍历要素，提取点坐标
        try (SimpleFeatureIterator iterator = features.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Object geometry = feature.getDefaultGeometry();

                // 只处理点要素
                if (geometry instanceof Point) {
                    Point point = (Point) geometry;
                    points.add(new Point2D(point.getX(), point.getY()));
                } else {
                    System.err.println("跳过非点要素: " + feature.getID());
                }
            }
        } finally {
            dataStore.dispose();
        }

        return points;
    }

    /**
     * 将攻击后的点写入新的SHP文件（修复类型转换错误）
     */
    public static void writePointsToShapefile(List<Point2D> points, String outputShpPath) throws Exception {
        // 创建要素类型（代码不变）
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("AttackedPoints");
        typeBuilder.setCRS(DefaultGeographicCRS.WGS84);
        typeBuilder.add("the_geom", Point.class);
        typeBuilder.add("id", Integer.class);
        SimpleFeatureType featureType = typeBuilder.buildFeatureType();

        // 配置输出数据存储（代码不变）
        Map<String, Object> params = new HashMap<>();
        params.put("url", new File(outputShpPath).toURI().toURL());
        params.put("create spatial index", true);

        DataStore dataStore = DataStoreFinder.getDataStore(params);
        if (dataStore == null) {
            throw new IllegalArgumentException("无法创建输出SHP文件: " + outputShpPath);
        }

        dataStore.createSchema(featureType);
        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureStore featureStore = (SimpleFeatureStore) dataStore.getFeatureSource(typeName);

        // 构建要素集合
        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

        for (int i = 0; i < points.size(); i++) {
            Point2D point = points.get(i);
            Coordinate coordinate = new Coordinate(point.getX(), point.getY());
            Point geometry = geometryFactory.createPoint(coordinate);

            featureBuilder.add(geometry);
            featureBuilder.add(i);
            features.add(featureBuilder.buildFeature(null));
        }

        // 修复：使用ListFeatureCollection包装特征列表
        SimpleFeatureCollection featureCollection = new ListFeatureCollection(featureType, features);
        featureStore.addFeatures(featureCollection); // 这里不再需要强制转换

        dataStore.dispose();
    }

    /**
     * 执行顶点插值攻击（复用之前的核心逻辑）
     */
    public static List<Point2D> attack(List<Point2D> originalPoints, double controlPointRatio, double maxDisplacement) {
        if (originalPoints == null || originalPoints.isEmpty()) {
            throw new IllegalArgumentException("坐标点集合不能为空");
        }

        int totalPoints = originalPoints.size();
        int controlPointCount = (int) (totalPoints * controlPointRatio);
        controlPointCount = Math.max(controlPointCount, 4); // 至少4个控制点

        // 1. 随机选取控制点
        List<Point2D> controlPoints = selectControlPoints(originalPoints, controlPointCount);
        List<Point2D> displacedControls = displaceControlPoints(controlPoints, maxDisplacement);

        // 2. 插值计算所有点
        List<Point2D> attackedPoints = new ArrayList<>();
        for (Point2D point : originalPoints) {
            if (isControlPoint(controlPoints, point)) {
                int index = controlPoints.indexOf(point);
                attackedPoints.add(displacedControls.get(index));
            } else {
                Point2D transformed = interpolatePoint(point, controlPoints, displacedControls);
                attackedPoints.add(transformed);
            }
        }

        return attackedPoints;
    }

    // 以下为攻击逻辑的辅助方法（与之前版本相同）
    private static List<Point2D> selectControlPoints(List<Point2D> points, int count) {
        List<Point2D> controls = new ArrayList<>();
        Random random = new Random();
        while (controls.size() < count) {
            int index = random.nextInt(points.size());
            Point2D candidate = points.get(index);
            if (!controls.contains(candidate)) {
                controls.add(candidate);
            }
        }
        return controls;
    }

    private static List<Point2D> displaceControlPoints(List<Point2D> controls, double maxDisplacement) {
        List<Point2D> displaced = new ArrayList<>();
        Random random = new Random();
        for (Point2D p : controls) {
            double dx = (random.nextDouble() * 2 - 1) * maxDisplacement;
            double dy = (random.nextDouble() * 2 - 1) * maxDisplacement;
            displaced.add(new Point2D(p.getX() + dx, p.getY() + dy));
        }
        return displaced;
    }

    private static boolean isControlPoint(List<Point2D> controls, Point2D point) {
        for (Point2D cp : controls) {
            if (Math.abs(cp.getX() - point.getX()) < 1e-9 &&
                    Math.abs(cp.getY() - point.getY()) < 1e-9) {
                return true;
            }
        }
        return false;
    }

    private static Point2D interpolatePoint(Point2D point, List<Point2D> originalControls, List<Point2D> displacedControls) {
        List<Point2D> nearestOriginal = findNearestPoints(originalControls, point, 4);
        List<Point2D> nearestDisplaced = new ArrayList<>();
        for (Point2D p : nearestOriginal) {
            int index = originalControls.indexOf(p);
            nearestDisplaced.add(displacedControls.get(index));
        }

        double x0 = nearestOriginal.get(0).getX();
        double y0 = nearestOriginal.get(0).getY();
        double x1 = nearestOriginal.get(1).getX();
        double y1 = nearestOriginal.get(3).getY();

        double u = (point.getX() - x0) / (x1 - x0 + 1e-9); // 避免除零
        double v = (point.getY() - y0) / (y1 - y0 + 1e-9);

        double x = bilinearInterpolation(
                nearestDisplaced.get(0).getX(), nearestDisplaced.get(1).getX(),
                nearestDisplaced.get(2).getX(), nearestDisplaced.get(3).getX(),
                u, v
        );

        double y = bilinearInterpolation(
                nearestDisplaced.get(0).getY(), nearestDisplaced.get(1).getY(),
                nearestDisplaced.get(2).getY(), nearestDisplaced.get(3).getY(),
                u, v
        );

        return new Point2D(x, y);
    }

    private static List<Point2D> findNearestPoints(List<Point2D> points, Point2D target, int n) {
        List<Point2D> sorted = new ArrayList<>(points);
        sorted.sort(Comparator.comparingDouble(a -> distance(a, target)));
        return sorted.subList(0, Math.min(n, sorted.size()));
    }

    private static double distance(Point2D a, Point2D b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static double bilinearInterpolation(double a, double b, double c, double d, double u, double v) {
        double temp1 = a + u * (b - a);
        double temp2 = c + u * (d - c);
        return temp1 + v * (temp2 - temp1);
    }

    public static void main(String[] args) {
        // SHP文件路径
        String inputShpPath = "E:/BaiduNetdiskDownload/demo1 (2)/demo1/target/demo1-1.0-SNAPSHOT/uploads/水印places.shp";
        String outputShpPath = "attacked_points.shp";

        try {
            // 1. 从SHP文件读取点
            List<Point2D> originalPoints = readPointsFromShapefile(inputShpPath);
            System.out.println("成功读取 " + originalPoints.size() + " 个点要素");

            // 2. 执行顶点插值攻击（10%控制点，最大位移0.001单位，适合经纬度坐标）
            List<Point2D> attackedPoints = attack(originalPoints, 0.1, 0.001);

            // 3. 将结果写入新SHP文件
            writePointsToShapefile(attackedPoints, outputShpPath);
            System.out.println("攻击完成，结果已写入 " + outputShpPath);

        } catch (Exception e) {
            System.err.println("处理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

