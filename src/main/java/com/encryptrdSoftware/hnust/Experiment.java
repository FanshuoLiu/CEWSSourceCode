package com.encryptrdSoftware.hnust;

import org.gdal.gdal.gdal;
import org.gdal.ogr.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Experiment {
    // 实验参数
    private static final int WATERMARK_LENGTH = 4096; // 水印长度（需与自定义比特串长度一致）
    private static final double[] ATTACK_RATIOS = {0.1, 0.3, 0.5, 0.7, 0.9}; // 攻击比例

    // 自定义水印比特串（示例：32位二进制串）
    // 可以修改为你自己的比特串，但长度必须与WATERMARK_LENGTH一致

    public static void main(String[] args) throws IOException {
        // 初始化GDAL
        gdal.AllRegister();
        StringBuilder builder = createWatermarking("C:\\Users\\lfs\\Desktop\\results\\lw\\微信图片_20250728201037.bmp");
        int[] convert = convert(String.valueOf(builder));
        // 验证自定义水印长度是否与定义的长度一致
        if (convert.length != WATERMARK_LENGTH) {
            throw new IllegalArgumentException("自定义水印长度与WATERMARK_LENGTH不一致！");
        }

        String shpFilePath ="E:\\BaiduNetdiskDownload\\demo1 (2)\\demo1\\target\\demo1-1.0-SNAPSHOT\\uploads\\boundaries.shp";

        try {
            // 1. 从SHP文件读取原始顶点数据
            Vertex[] originalVertices = readVerticesFromSHP(shpFilePath);
            System.out.println("从SHP文件读取到 " + originalVertices.length + " 个顶点");

            // 2. 使用自定义水印（替代随机生成）
            int[] originalWatermark = convert;

            // 3. 嵌入水印
            Vertex[] watermarkedVertices = WatermarkUtils.embedWatermark(originalVertices, originalWatermark);

            System.out.println("\n=== 顶点插值攻击实验结果 ===");
            runInterpolationAttackExperiment(originalVertices, watermarkedVertices, originalWatermark);

            System.out.println("\n=== 顶点压缩攻击实验结果 ===");
            runCompressionAttackExperiment(originalVertices, watermarkedVertices, originalWatermark);

        } finally {
            // 清理GDAL/OGR资源
        }
    }

    /**
     * 从SHP文件读取顶点数据
     */
    /**
     * 从SHP文件读取顶点数据
     */
    private static Vertex[] readVerticesFromSHP(String filePath) {
        DataSource dataSource = ogr.Open(filePath);
        if (dataSource == null) {
            throw new RuntimeException("无法打开SHP文件: " + filePath);
        }

        Layer layer = dataSource.GetLayer(0);
        if (layer == null) {
            throw new RuntimeException("无法获取图层");
        }

        List<Vertex> verticesList = new ArrayList<>();

        Feature feature;
        while ((feature = layer.GetNextFeature()) != null) {
            try {
                Geometry geom = feature.GetGeometryRef();
                if (geom == null) continue;

                if (geom.GetGeometryName().equals("POINT")) {
                    addPointVertices(geom, verticesList);
                } else if (geom.GetGeometryName().equals("LINESTRING")) {
                    addLineStringVertices(geom, verticesList);
                } else if (geom.GetGeometryName().equals("POLYGON")) {
                    addPolygonVertices(geom, verticesList);
                }
            } finally {
                feature.delete();
            }
        }

        return verticesList.toArray(new Vertex[0]);
    }

    // 以下方法保持不变
    private static void addPointVertices(Geometry point, List<Vertex> verticesList) {
        double x = point.GetX();
        double y = point.GetY();
        verticesList.add(new Vertex(x, y));
    }

    private static void addLineStringVertices(Geometry lineString, List<Vertex> verticesList) {
        int pointCount = lineString.GetPointCount();
        for (int i = 0; i < pointCount; i++) {
            double[] coords = lineString.GetPoint(i);
            verticesList.add(new Vertex(coords[0], coords[1]));
        }
    }

    private static void addPolygonVertices(Geometry polygon, List<Vertex> verticesList) {
        int ringCount = polygon.GetGeometryCount();
        for (int i = 0; i < ringCount; i++) {
            Geometry ring = polygon.GetGeometryRef(i);
            addLineStringVertices(ring, verticesList);
        }
    }

    private static void runInterpolationAttackExperiment(Vertex[] originalVertices,
                                                         Vertex[] watermarkedVertices,
                                                         int[] originalWatermark) {
        System.out.println("攻击比例\t顶点数量\tNC值");
        System.out.println("--------------------------");

        for (double ratio : ATTACK_RATIOS) {
            Vertex[] attackedVertices = AttackUtils.vertexInterpolationAttack(watermarkedVertices, ratio);
            int[] extractedWatermark = WatermarkUtils.extractWatermark(attackedVertices, originalVertices, WATERMARK_LENGTH);
            double nc = WatermarkUtils.calculateNC(originalWatermark, extractedWatermark);

            System.out.printf("%.0f%%\t\t%d\t\t%.4f\n", ratio * 100, attackedVertices.length, nc);
        }
    }

    private static void runCompressionAttackExperiment(Vertex[] originalVertices,
                                                       Vertex[] watermarkedVertices,
                                                       int[] originalWatermark) {
        System.out.println("攻击比例\t顶点数量\tNC值");
        System.out.println("--------------------------");

        for (double ratio : ATTACK_RATIOS) {
            Vertex[] attackedVertices = AttackUtils.vertexCompressionAttack(watermarkedVertices, ratio);
            int[] extractedWatermark = WatermarkUtils.extractWatermark(attackedVertices, originalVertices, WATERMARK_LENGTH);
            double nc = WatermarkUtils.calculateNC(originalWatermark, extractedWatermark);

            System.out.printf("%.0f%%\t\t%d\t\t%.4f\n", ratio * 100, attackedVertices.length, nc);
        }
    }
    public static StringBuilder createWatermarking(String inputImagePath) throws IOException {
        File input = new File(inputImagePath);
        // 读取BMP图像文件
        BufferedImage image = ImageIO.read(input);

        // 构建比特串
        StringBuilder bitString = new StringBuilder();
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x <64; x++) {
                // 获取像素值（单色图像，只取R通道）
                int pixel = image.getRGB(x, y) & 0xFF; // 只取最低8位，忽略Alpha通道
                // 将像素值转换为比特：黑色为0，白色为1
                char bit = (pixel == 0) ? '0' : '1';
                // 添加到比特串中
                bitString.append(bit);
            }
        }
        bitString.length();
        // 输出比特串
        return bitString;
    }
    public static int[] convert(String binaryStr) {
        // 检查输入是否为空
        if (binaryStr == null || binaryStr.isEmpty()) {
            return new int[0];
        }

        int[] result = new int[binaryStr.length()];

        for (int i = 0; i < binaryStr.length(); i++) {
            char c = binaryStr.charAt(i);
            // 验证是否为合法的二进制字符
            if (c != '0' && c != '1') {
                throw new IllegalArgumentException("输入不是有效的二进制字符串");
            }
            // 将字符转换为对应的int值
            result[i] = c - '0';
        }

        return result;
    }
}