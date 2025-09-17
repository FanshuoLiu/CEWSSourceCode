package com.encryptrdSoftware.hnust;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AttackUtils {
    private static final Random random = new Random(43); // 固定种子确保可重复性

    /**
     * 顶点插值攻击：添加冗余顶点
     * @param vertices 原始顶点集合
     * @param ratio 插值比例（0.1表示10%）
     * @return 插值后的顶点集合
     */
    public static Vertex[] vertexInterpolationAttack(Vertex[] vertices, double ratio) {
        if (ratio < 0 || ratio > 1) {
            throw new IllegalArgumentException("比例必须在0到1之间");
        }

        int numToAdd = (int) (vertices.length * ratio);
        List<Vertex> result = new ArrayList<>();

        // 先添加所有原始顶点
        for (Vertex v : vertices) {
            result.add(v);
        }

        // 随机选择顶点对之间插入新顶点
        for (int i = 0; i < numToAdd; i++) {
            // 随机选择两个相邻顶点
            int idx = random.nextInt(vertices.length - 1);
            Vertex v1 = vertices[idx];
            Vertex v2 = vertices[idx + 1];

            // 在两个顶点之间生成插值顶点
            double x = (v1.getX() + v2.getX()) / 2;
            double y = (v1.getY() + v2.getY()) / 2;
            result.add(new Vertex(x, y));
        }

        return result.toArray(new Vertex[0]);
    }

    /**
     * 顶点压缩攻击：移除冗余顶点
     * @param vertices 原始顶点集合
     * @param ratio 压缩比例（0.1表示10%）
     * @return 压缩后的顶点集合
     */
    public static Vertex[] vertexCompressionAttack(Vertex[] vertices, double ratio) {
        if (ratio < 0 || ratio >= 1) {
            throw new IllegalArgumentException("比例必须在0到1之间（不包含1）");
        }

        int numToKeep = (int) (vertices.length * (1 - ratio));
        // 确保至少保留一个顶点
        numToKeep = Math.max(numToKeep, 1);

        List<Vertex> result = new ArrayList<>();
        boolean[] kept = new boolean[vertices.length];
        int count = 0;

        // 随机选择要保留的顶点
        while (count < numToKeep) {
            int idx = random.nextInt(vertices.length);
            if (!kept[idx]) {
                kept[idx] = true;
                result.add(vertices[idx]);
                count++;
            }
        }

        return result.toArray(new Vertex[0]);
    }
}

