package com.encryptrdSoftware.hnust.model;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.util.*;

public class ShapefileWatermarkNC {

    /**
     * 从SHP文件提取水印比特（假设水印嵌入在x坐标的小数部分）
     * @param shpFilePath SHP文件路径
     * @return 水印比特列表（0或1）
     * @throws Exception 处理过程中的异常
     */
    public static List<Integer> extractWatermarkFromShapefile(String shpFilePath) throws Exception {
        List<Integer> watermark = new ArrayList<>();

        // 读取SHP文件
        Map<String, Object> params = new HashMap<>();
        params.put("url", new File(shpFilePath).toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(params);

        if (dataStore == null) {
            throw new IllegalArgumentException("无法打开SHP文件: " + shpFilePath);
        }

        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
        SimpleFeatureCollection features = featureSource.getFeatures();

        // 提取每个点的水印比特（这里假设水印嵌入规则：x坐标小数第4位为1则是1，否则为0）
        try (SimpleFeatureIterator iterator = features.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Point point = (Point) feature.getDefaultGeometry();
                double x = point.getX();

                // 提取x坐标的小数部分第4位作为水印比特（示例规则，需根据实际嵌入方式调整）
                double fractionalPart = x - Math.floor(x); // 取小数部分
                int bit = extractBitFromFraction(fractionalPart, 4); // 第4位
                watermark.add(bit);
            }
        } finally {
            dataStore.dispose();
        }

        return watermark;
    }

    /**
     * 从小数部分提取指定位置的比特（示例：第n位小数是否大于0.5）
     * @param fractionalPart 小数部分（0~1之间）
     * @param position 提取的位置（1-based）
     * @return 0或1
     */
    private static int extractBitFromFraction(double fractionalPart, int position) {
        // 例如：第4位小数 = floor(fractionalPart * 10^4) % 10
        double scaled = fractionalPart * Math.pow(10, position);
        int digit = (int) Math.floor(scaled) % 10;
        return digit >= 5 ? 1 : 0; // 大于等于5视为1，否则为0（示例规则）
    }

    /**
     * 计算两个水印的NC值（基于XNOR公式）
     * @param originalWatermark 原始水印比特列表
     * @param attackedWatermark 攻击后水印比特列表
     * @return NC值（0~1之间）
     */
    public static double calculateNC(List<Integer> originalWatermark, List<Integer> attackedWatermark) {
        if (originalWatermark == null || attackedWatermark == null) {
            throw new IllegalArgumentException("水印列表不能为null");
        }

        if (originalWatermark.size() != attackedWatermark.size()) {
            throw new IllegalArgumentException("两个水印的长度必须相同");
        }

        int total = originalWatermark.size();
        if (total == 0) {
            throw new IllegalArgumentException("水印不能为空");
        }

        int xnorSum = 0;
        for (int i = 0; i < total; i++) {
            int originalBit = originalWatermark.get(i) & 1; // 确保是0或1
            int attackedBit = attackedWatermark.get(i) & 1;

            // XNOR：相同为1，不同为0
            xnorSum += (originalBit == attackedBit) ? 1 : 0;
        }

        return (double) xnorSum / total;
    }

    public static void main(String[] args) {
        // 原始水印SHP文件和攻击后SHP文件路径
        String originalShpPath ="E:/BaiduNetdiskDownload/demo1 (2)/demo1/target/demo1-1.0-SNAPSHOT/uploads/水印places.shp";
        String attackedShpPath = "E:/BaiduNetdiskDownload/demo1 (2)/demo1/target/demo1-1.0-SNAPSHOT/uploads/attacked_points.shp";

        try {
            // 提取水印
            List<Integer> originalWatermark = extractWatermarkFromShapefile(originalShpPath);
            List<Integer> attackedWatermark = extractWatermarkFromShapefile(attackedShpPath);

            // 计算NC值
            double nc = calculateNC(originalWatermark, attackedWatermark);
            System.out.println("原始水印与攻击后水印的NC值: " + nc);
            System.out.println("NC值越接近1，水印保留越好；越接近0，攻击效果越好");

        } catch (Exception e) {
            System.err.println("计算失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

