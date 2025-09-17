package com.encryptrdSoftware.hnust.model;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.ogr.*;

import java.util.ArrayList;
import java.util.List;


class Point1 {
    double x, y;
    int fid; // 保留原始FID用于调试
    Point1(double x, double y, int fid) {
        this.x = x;
        this.y = y;
        this.fid = fid;
    }
}

public class veshi {

    static {
        // 初始化GDAL
        gdal.AllRegister();
        ogr.RegisterAll();
    }

    /**
     * 提取坐标的小数部分
     */
    private static double getDecimalPart(double coordinate) {
        return coordinate - Math.floor(coordinate);
    }

    /**
     * 读取SHP文件中所有点的坐标小数部分
     */
    private static List<double[]> readPointDecimals(String shpFilePath) throws Exception {
        // 打开SHP文件
        DataSource dataSource = ogr.Open(shpFilePath, 0); // 0表示只读
        if (dataSource == null) {
            throw new Exception("无法打开SHP文件: " + shpFilePath + "，错误信息: " + gdal.GetLastErrorMsg());
        }

        // 获取第一个图层
        Layer layer = dataSource.GetLayer(0);
        if (layer == null) {
            throw new Exception("无法获取图层: " + shpFilePath);
        }

        List<double[]> decimalPoints = new ArrayList<>();

        // 遍历所有要素
        Feature feature;
        while ((feature = layer.GetNextFeature()) != null) {
            Geometry geom = feature.GetGeometryRef();

            // 检查是否为点要素
            if (geom == null || geom.GetGeometryType() != ogr.wkbPoint) {
                throw new Exception("SHP文件包含非点要素: " + shpFilePath);
            }

            // 获取点坐标
            double x = geom.GetX();
            double y = geom.GetY();

            // 提取小数部分
            double xDecimal = getDecimalPart(x);
            double yDecimal = getDecimalPart(y);

            decimalPoints.add(new double[]{xDecimal, yDecimal});

            // 释放资源
            feature.delete();
        }

        // 释放数据源
        dataSource.delete();

        return decimalPoints;
    }

    /**
     * 计算两点之间的欧氏距离
     */
    private static double calculateDistance(double[] point1, double[] point2) {
        double dx = point1[0] - point2[0];
        double dy = point1[1] - point2[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 计算一组值的RMSE
     */
    private static double calculateRMSE(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }

        double sumOfSquares = 0.0;
        for (double value : values) {
            sumOfSquares += value * value;
        }

        return Math.sqrt(sumOfSquares / values.size());
    }

    /**
     * 计算两个SHP文件点坐标小数部分的各种RMSE
     * 返回一个包含三个值的数组: [x方向RMSE, y方向RMSE, 距离RMSE]
     */
    public static double[] calculateDecimalRMSEs(String shpPath1, String shpPath2) throws Exception {
        // 读取两个文件的点坐标小数部分
        List<double[]> points1 = readPointDecimals(shpPath1);
        List<double[]> points2 = readPointDecimals(shpPath2);

        // 检查点数量是否一致
        if (points1.size() != points2.size()) {
            throw new Exception("两个SHP文件的点数量不一致: " +
                    points1.size() + " vs " + points2.size());
        }

        // 分别存储x差异、y差异和距离
        List<Double> xDifferences = new ArrayList<>();
        List<Double> yDifferences = new ArrayList<>();
        List<Double> distances = new ArrayList<>();

        for (int i = 0; i < points1.size(); i++) {
            double x1 = points1.get(i)[0];
            double y1 = points1.get(i)[1];
            double x2 = points2.get(i)[0];
            double y2 = points2.get(i)[1];

            // 计算x和y方向的差异
            double xDiff = x1 - x2;
            double yDiff = y1 - y2;

            xDifferences.add(xDiff);
            yDifferences.add(yDiff);
            distances.add(calculateDistance(points1.get(i), points2.get(i)));
        }

        // 计算并返回三种RMSE
        return new double[] {
                calculateRMSE(xDifferences),
                calculateRMSE(yDifferences),
                calculateRMSE(distances)
        };
    }

    public static void main(String[] args) {
        // 直接在代码中指定SHP文件路径
        String shpPath1 = "E:/BaiduNetdiskDownload/demo1 (2)/demo1/target/demo1-1.0-SNAPSHOT/uploads/水印places.shp";  // 第一个SHP文件路径
        String shpPath2 = "E:/BaiduNetdiskDownload/demo1 (2)/demo1/target/demo1-1.0-SNAPSHOT/uploads/places.shp";  // 第二个SHP文件路径

        try {
            double[] rmseResults = calculateDecimalRMSEs(shpPath1, shpPath2);

            System.out.printf("X坐标小数部分的RMSE: %.6f%n", rmseResults[0]);
            System.out.printf("Y坐标小数部分的RMSE: %.6f%n", rmseResults[1]);
            System.out.printf("对应点距离的RMSE: %.6f%n", rmseResults[2]);
        } catch (Exception e) {
            System.err.println("处理过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 清理GDAL资源
            gdal.GDALDestroyDriverManager();
        }
    }
}
