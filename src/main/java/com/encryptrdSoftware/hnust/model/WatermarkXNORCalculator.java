package com.encryptrdSoftware.hnust.model;

public class WatermarkXNORCalculator {

    /**
     * 计算两个水印图像的归一化互相关(NC)值
     * 使用公式：NC = 1/(W×H) ∑∑ XNOR(w_ij, w'_ij)
     * @param originalWatermark 原始水印图像（二维数组，元素为0或1）
     * @param extractedWatermark 提取出的水印图像（二维数组，元素为0或1）
     * @return NC值，范围在[0, 1]之间
     */
    public static double calculateNC(int[][] originalWatermark, int[][] extractedWatermark) {
        // 检查输入矩阵是否为null
        if (originalWatermark == null || extractedWatermark == null) {
            throw new IllegalArgumentException("水印矩阵不能为null");
        }

        // 检查矩阵维度是否一致
        int rows = originalWatermark.length;
        if (rows != extractedWatermark.length) {
            throw new IllegalArgumentException("两个水印矩阵的行数必须相同");
        }

        if (rows == 0) {
            throw new IllegalArgumentException("水印矩阵不能为空");
        }

        int cols = originalWatermark[0].length;
        if (cols != extractedWatermark[0].length) {
            throw new IllegalArgumentException("两个水印矩阵的列数必须相同");
        }

        int totalPixels = rows * cols;
        int xnorSum = 0;

        // 计算所有位置的XNOR结果之和
        for (int i = 0; i < rows; i++) {
            // 检查每行长度是否一致
            if (originalWatermark[i].length != cols || extractedWatermark[i].length != cols) {
                throw new IllegalArgumentException("水印矩阵行长度不一致");
            }

            for (int j = 0; j < cols; j++) {
                // 确保值是0或1
                int original = originalWatermark[i][j] & 1;
                int extracted = extractedWatermark[i][j] & 1;

                // 计算XNOR：当两个值相同时为1，不同时为0
                // XNOR = NOT(XOR)，XOR = (a != b)，所以XNOR = (a == b)
                int xnor = (original == extracted) ? 1 : 0;
                xnorSum += xnor;
            }
        }

        // 计算并返回NC值
        return (double) xnorSum / totalPixels;
    }

    // 测试方法
    public static void main(String[] args) {
        // 创建测试用的原始水印
        int[][] original = {
                {1, 0, 1, 0},
                {0, 1, 0, 1},
                {1, 0, 1, 0},
                {0, 1, 0, 1}
        };

        // 创建与原始水印相同的提取水印（NC应该为1）
        int[][] extractedSame = {
                {1, 0, 1, 0},
                {0, 1, 0, 1},
                {1, 0, 1, 0},
                {0, 1, 0, 1}
        };

        // 创建与原始水印相反的提取水印（NC应该为0）
        int[][] extractedOpposite = {
                {0, 1, 0, 1},
                {1, 0, 1, 0},
                {0, 1, 0, 1},
                {1, 0, 1, 0}
        };

        // 创建部分相同的提取水印（NC应该为0.5）
        int[][] extractedPartial = {
                {1, 0, 1, 0},
                {0, 1, 0, 1},
                {0, 1, 0, 1},
                {1, 0, 1, 0}
        };

        // 计算并输出NC值
        System.out.println("相同水印的NC值: " + calculateNC(original, extractedSame));
        System.out.println("相反水印的NC值: " + calculateNC(original, extractedOpposite));
        System.out.println("部分相同水印的NC值: " + calculateNC(original, extractedPartial));
    }
}

