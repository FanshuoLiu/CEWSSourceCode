package com.encryptrdSoftware.hnust;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

public class ReadShapefilePoints {
    public static void main(String[] args) {
        // 初始化GDAL
        ogr.RegisterAll();

        // 检查输入参数
        if (args.length != 1) {
            System.err.println("用法: java ReadShapefilePoints <shapefile路径>");
            System.exit(1);
        }
        String path="E:\\BaiduNetdiskDownload\\demo1 (2)\\demo1\\target\\demo1-1.0-SNAPSHOT\\uploads\\水印places.shp";


    }
}

