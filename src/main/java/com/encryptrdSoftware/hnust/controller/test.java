package com.encryptrdSoftware.hnust.controller;

import com.encryptrdSoftware.hnust.model.Domain;
import com.encryptrdSoftware.hnust.model.Line;
import com.encryptrdSoftware.hnust.model.Point;
import org.gdal.gdal.gdal;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class test {
    static {
        gdal.AllRegister();
        ogr.RegisterAll();
    }

    public static void main(String[] args) {
        String shpFilePath = "E:\\BaiduNetdiskDownload\\demo1 (2)\\demo1\\target\\demo1-1.0-SNAPSHOT\\uploads\\waterWays.shp";
        Map<String, Object> map1 = Domain.SHPtoList(new File(shpFilePath));
        List<Line> geometries = (List<Line>) map1.get("geometries");
        System.out.println("点数:" + geometries.size());
        List<Integer> lengths = (List<Integer>) map1.get("lengths");
        System.out.println("lengths:" + lengths);
        int maxIndex = findMaxIndex(lengths);
        System.out.println("maxIndex: " + maxIndex);
        System.out.println(lengths.get(maxIndex));
    }

    public static <T extends Comparable<T>> int findMaxIndex(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("集合不能为null或空");
        }

        int maxIndex = 0;
        T maxValue = list.get(0);

        for (int i = 1; i < list.size(); i++) {
            T current = list.get(i);
            // 比较当前元素与最大值
            if (current.compareTo(maxValue) > 0) {
                maxValue = current;
                maxIndex = i;
            }
        }

        return maxIndex;
    }
}
