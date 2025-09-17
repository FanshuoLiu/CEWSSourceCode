package com.encryptrdSoftware.hnust;

import java.util.Random;

public class WatermarkUtils {
    // 水印嵌入的微调量
    private static final double EMBED_DELTA = 0.001;
    // 随机数生成器
    private static final Random random = new Random(42); // 固定种子确保可重复性

    /**
     * 生成随机二进制水印
     * @param length 水印长度
     * @return 二进制水印数组
     */
    public static int[] generateWatermark(int length) {
        int[] watermark = new int[length];
        for (int i = 0; i < length; i++) {
            watermark[i] = random.nextBoolean() ? 1 : 0;
        }
        return watermark;
    }

    /**
     * 将水印嵌入顶点数据
     * @param vertices 原始顶点集合
     * @param watermark 要嵌入的水印
     * @return 嵌入水印后的顶点集合
     */
    public static Vertex[] embedWatermark(Vertex[] vertices, int[] watermark) {
        Vertex[] watermarkedVertices = new Vertex[vertices.length];
        int watermarkLength = watermark.length;

        for (int i = 0; i < vertices.length; i++) {
            Vertex original = vertices[i];
            Vertex embedded = original.copy();

            // 循环使用水印比特，对x坐标进行微调
            int bit = watermark[i % watermarkLength];
            if (bit == 1) {
                embedded.setX(original.getX() + EMBED_DELTA);
            } else {
                embedded.setX(original.getX() - EMBED_DELTA);
            }

            watermarkedVertices[i] = embedded;
        }

        return watermarkedVertices;
    }

    /**
     * 从顶点数据中提取水印
     * @param vertices 带水印的顶点集合
     * @param originalVertices 原始顶点集合（用于参考）
     * @param watermarkLength 水印长度
     * @return 提取的水印
     */
    public static int[] extractWatermark(Vertex[] vertices, Vertex[] originalVertices, int watermarkLength) {
        int[] extracted = new int[watermarkLength];
        int[] count = new int[watermarkLength]; // 统计每个比特位置的投票

        for (int i = 0; i < vertices.length; i++) {
            int pos = i % watermarkLength;
            double delta = vertices[i].getX() - originalVertices[i % originalVertices.length].getX();

            // 根据微调方向判断水印比特
            if (delta > 0) {
                count[pos]++;
            } else {
                count[pos]--;
            }
        }

        // 多数投票决定最终比特
        for (int i = 0; i < watermarkLength; i++) {
            extracted[i] = count[i] > 0 ? 1 : 0;
        }

        return extracted;
    }

    /**
     * 计算两个水印的归一化相关系数(NC)
     * @param w1 第一个水印
     * @param w2 第二个水印
     * @return NC值
     */
    public static double calculateNC(int[] w1, int[] w2) {
        if (w1.length != w2.length) {
            throw new IllegalArgumentException("水印长度必须相同");
        }

        int same = 0;
        for (int i = 0; i < w1.length; i++) {
            if (w1[i] == w2[i]) {
                same++;
            }
        }

        return (double) same / w1.length;
    }
}

