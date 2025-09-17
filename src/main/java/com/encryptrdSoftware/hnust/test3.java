//package com.encryptrdSoftware.hnust;
//
//import com.encryptrdSoftware.hnust.controller.UploadServlet;
//import com.encryptrdSoftware.hnust.dao.FileDAO;
//import com.encryptrdSoftware.hnust.model.Domain;
//import com.encryptrdSoftware.hnust.model.FileData;
//import com.encryptrdSoftware.hnust.model.Line;
//import com.encryptrdSoftware.hnust.model.Shape;
//import com.encryptrdSoftware.hnust.util.SecretUtils;
//import com.encryptrdSoftware.hnust.util.ZipCompressorUtils;
//import org.gdal.gdal.Dataset;
//import org.gdal.gdal.gdal;
//import org.gdal.ogr.*;
//import org.gdal.osr.SpatialReference;
//import org.geotools.data.DataStore;
//import org.geotools.data.DataStoreFinder;
//import org.geotools.data.DefaultTransaction;
//import org.geotools.data.Transaction;
//import org.geotools.data.shapefile.ShapefileDataStore;
//import org.geotools.data.shapefile.ShapefileDataStoreFactory;
//import org.geotools.data.simple.SimpleFeatureCollection;
//import org.geotools.data.simple.SimpleFeatureIterator;
//import org.geotools.data.simple.SimpleFeatureSource;
//import org.geotools.data.simple.SimpleFeatureStore;
//import org.geotools.feature.DefaultFeatureCollection;
//import org.geotools.feature.simple.SimpleFeatureBuilder;
//import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
//import org.geotools.geometry.jts.JTSFactoryFinder;
//import org.geotools.referencing.CRS;
//import org.geotools.referencing.crs.DefaultGeographicCRS;
//import org.locationtech.jts.geom.Geometry;
//import org.locationtech.jts.geom.GeometryFactory;
//import org.locationtech.jts.geom.LineString;
//import org.locationtech.jts.geom.Point;
//import org.locationtech.jts.geom.Polygon;
//import org.opengis.feature.Property;
//import org.opengis.feature.simple.SimpleFeature;
//import org.opengis.feature.simple.SimpleFeatureType;
//import org.opengis.referencing.crs.CoordinateReferenceSystem;
//
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.nio.file.*;
//import java.util.*;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
//
//public class test3{
//    static {
//        // 注册所有已知的驱动
//        gdal.AllRegister();
//        ogr.RegisterAll();
//    }
//    public static void main(String[] args) throws IOException {
//        // 打开矢量数据源（例如Shapefile）
//        String filePath = "E:\\BaiduNetdiskDownload\\demo1 (2)\\demo1\\target\\demo1-1.0-SNAPSHOT\\uploads\\abc123\\水印points.shp";
//        DataSource dataSource = ogr.Open(filePath, 0);
//        if (dataSource == null) {
//            System.err.println("Failed to open data source: " + filePath);
//            return;
//        }
//
//        // 打开 Shapefile
//        String shpFilePath = "path/to/your/file.shp"; // 替换为你的 shp 文件路径
//        DataSource dataset = ogr.Open(filePath);
//
//        if (dataset == null) {
//            System.err.println("无法打开 Shapefile: " + shpFilePath);
//            return;
//        }
//
//        // 获取图层
//        Layer layer = dataset.GetLayer(0);
//        Feature feature;
//        // 遍历要素
//        while ((feature = layer.GetNextFeature()) != null) {
//            org.gdal.ogr.Geometry geometry = feature.GetGeometryRef();
//            if (geometry != null) {
//                System.out.println("要素 ID: " + feature.GetFID());
//                System.out.println("坐标:");
//                // 根据几何类型输出坐标
//                if (geometry.GetGeometryType() == ogr.wkbPoint) {
//                    System.out.println(geometry.GetX() + ", " + geometry.GetY());
//                } else if (geometry.GetGeometryType() == ogr.wkbLineString) {
//                    for (int i = 0; i < geometry.GetPointCount(); i++) {
//                        System.out.println(geometry.GetX(i) + ", " + geometry.GetY(i));
//                    }
//                } else if (geometry.GetGeometryType() == ogr.wkbPolygon) {
//                    for (int i = 0; i < geometry.GetGeometryCount(); i++) {
//                        org.gdal.ogr.Geometry shell = geometry.GetGeometryRef(i);
//                        for (int j = 0; j < shell.GetPointCount(); j++) {
//                            System.out.println(shell.GetX(j) + ", " + shell.GetY(j));
//                        }
//                    }
//                }
//            }
//            feature.delete(); // 释放特征
//        }
//
//        // 清理
//        dataset.delete();
//
////        Feature feature;
////        while ((feature = layer.GetNextFeature())!=null){
////            Geometry geometry = feature.GetGeometryRef();
////            if (geometry.GetGeometryType() == ogr.wkbPolygon) {
////                // 从 Polygon 中获取环的数量
////                int ringCount = geometry.GetGeometryCount(); // 总环数，包括外环和内环
////                if (ringCount > 1) {
////                    System.out.println("该多边形有内环。");
////                } else {
////                    System.out.println("该多边形没有内环。");
////                }
////            } else {
////                System.out.println("该几何不是多边形。");
////            }
////        }
//
//
//    }
//    public static void createSHP2(List<? extends Shape> coordinates, Layer layer, String filename, String btn,List<LinkedHashMap<String, Object>> properties,Object...arr) throws IOException {
//        File file;
//        if (btn.equals("水印")||btn.equals("解密")){
//            file = new File("C:\\Users\\lfs\\Desktop\\test"+File.separator+btn+filename + ".shp");
//        }else {
//            file = new File("C:\\Users\\lfs\\Desktop\\test"+File.separator+btn+filename + ".shp");
//        }
//        System.out.println(file);
//        Map<String, Object> params = new HashMap<>();
//        //指定shp的url
//        params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
//        //创建空间索引
//        params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.TRUE);
//
//        // 创建新的Shapefile数据存储
//        ShapefileDataStore dataStore = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
//        SimpleFeatureType featureType = createFeatureType1(layer,properties);
//        dataStore.createSchema(featureType);
//        dataStore.setCharset(Charset.forName("UTF-8"));
//
//        try (Transaction transaction = new DefaultTransaction("create")) {
//            String typeName = dataStore.getTypeNames()[0];
//            SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
//            if (featureSource instanceof SimpleFeatureStore) {
//                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
//                DefaultFeatureCollection collection = new DefaultFeatureCollection();
//                GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
//
//                // 根据几何类型处理不同形状
//                switch (layer.GetGeomType()) {
//                    case 1: // 线
//                        int index=0;
//                        for (Shape shape : coordinates) {
//                            if (shape instanceof com.encryptrdSoftware.hnust.model.Point) {
//                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
//                                    LinkedHashMap<String, Object> properties1 = properties.get(index);
//                                    for (Map.Entry<String, Object> entry : properties1.entrySet()) {
//                                        featureBuilder.set(entry.getKey(),entry.getValue());
//                                    }
//                                com.encryptrdSoftware.hnust.model.Point point = (com.encryptrdSoftware.hnust.model.Point) shape;
//                                org.locationtech.jts.geom.Coordinate coords = new org.locationtech.jts.geom.Coordinate(point.getX(),point.getY());
//                                Point point1 = geometryFactory.createPoint(coords);
//                                featureBuilder.add(point1);
//                                SimpleFeature feature = featureBuilder.buildFeature(null);
//                                collection.add(feature);
//                            }
//                            index++;
//                        }
//                        System.out.println("文件生成成功");
//                        break; // 确保在处理完线后退出 switch
//
//                    case 2: // 线
//                        for (Shape shape : coordinates) {
//                            if (shape instanceof Line) {
//                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
//                                Line line = (Line) shape;
//                                org.locationtech.jts.geom.Coordinate[] coords = new org.locationtech.jts.geom.Coordinate[line.getLength()];
//                                for (int i = 0; i < line.getLength(); i++) {
//                                    coords[i] = new org.locationtech.jts.geom.Coordinate(line.getPoints().get(i).getX(), line.getPoints().get(i).getY());
//                                }
//                                LineString lineString = geometryFactory.createLineString(coords);
//                                featureBuilder.add(lineString);
//                                SimpleFeature feature = featureBuilder.buildFeature(null);
//                                collection.add(feature);
//                            }
//                        }
//                        System.out.println("文件生成成功");
//                        break;
//
//                    case 3: // 多边形
//                        for (Shape shape : coordinates) {
//                            if (shape instanceof com.encryptrdSoftware.hnust.model.Polygon) {
//                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
//                                com.encryptrdSoftware.hnust.model.Polygon polygon = (com.encryptrdSoftware.hnust.model.Polygon) shape;
//                                org.locationtech.jts.geom.Coordinate[] coords = new org.locationtech.jts.geom.Coordinate[polygon.getNum() + 1]; // 加1用于闭合
//                                for (int i = 0; i < polygon.getNum(); i++) {
//                                    coords[i] = new org.locationtech.jts.geom.Coordinate(polygon.getPoints().get(i).getX(), polygon.getPoints().get(i).getY());
//                                }
////                                // 将第一个点添加到最后以闭合多边形
//                                coords[polygon.getNum()] = new org.locationtech.jts.geom.Coordinate(coords[0].x, coords[0].y);
//
//                                Polygon polygonShape = geometryFactory.createPolygon(coords);
//                                Polygon rightPolygon = com.encryptrdSoftware.hnust.model.Polygon.ensureRightHandRule(polygonShape);
//                                featureBuilder.add(rightPolygon);
//                                SimpleFeature feature = featureBuilder.buildFeature(null);
//                                collection.add(feature);
//                            }
//                        }
//                        System.out.println("文件生成成功");
//                        break;
//                    default:
//                        System.out.println("不支持的几何类型");
//                }
//
//                featureStore.setTransaction(transaction);
//                featureStore.addFeatures(collection);
//                transaction.commit();
//            } else {
//                System.out.println("FeatureStore 不支持");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            dataStore.dispose();
//        }
//    }
//
//    // 创建要素类型
//    private static Class<? extends Geometry> getGeometryClass1(int geomType) {
//        switch (geomType) {
//            case 1:
//                return Point.class;
//            case 2:
//                return LineString.class;
//            case 3:
//                return Polygon.class;
//            default:
//                throw new IllegalArgumentException("不支持的几何类型: " + geomType);
//        }
//    }
//    // 创建要素类型
//    private static SimpleFeatureType createFeatureType1(Layer layer, List<LinkedHashMap<String, Object>> properties) {
//        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
//        builder.setName("Location");
//
//        // 获取坐标参考系统
//        SpatialReference spatialRef = layer.GetSpatialRef();
//        CoordinateReferenceSystem crs;
//
//        if (spatialRef != null) {
//            try {
//                String wkt = spatialRef.ExportToWkt();
//                crs = CRS.parseWKT(wkt);
//            } catch (Exception e) {
//                System.err.println("无法转换坐标参考系统: " + e.getMessage());
//                crs = DefaultGeographicCRS.WGS84; // 作为备用
//            }
//        } else {
//            crs = DefaultGeographicCRS.WGS84; // 默认坐标参考系统
//        }
//        builder.setCRS(crs);
//
//        // 检查并添加几何属性
//        builder.add("the_geom", getGeometryClass1(layer.GetGeomType()));
//        // 添加其他属性
//
//            LinkedHashMap<String, Object> properties1 = properties.get(0);
//            for (Map.Entry<String, Object> entry : properties1.entrySet()) {
//                Object value = entry.getValue();
//                System.out.println("属性类型：" +entry.getKey()+"  "+"属性值:"+value);
//                if (value instanceof String) {
//                    System.out.println("属于string");
//                    builder.add(cutName(entry.getKey()), String.class);
//                } else if (value instanceof Integer) {
//                    System.out.println("属于int");
//                    builder.add(entry.getKey(), Integer.class);
//                }
//            }
//            System.out.println("------------------------------");
//        return builder.buildFeatureType();
//    }
//
//    // 方法：读取Shapefile并返回特征的LinkedHashMap集合
//    public static List<LinkedHashMap<String, Object>> readShapefile1(File file) {
//        List<LinkedHashMap<String, Object>> featureList = new ArrayList<>();
//        try {
//            // 创建数据存储
//            Map<String, Object> map = new LinkedHashMap<>();
//            map.put("url", file.toURI().toString());
//            DataStore dataStore = DataStoreFinder.getDataStore(map);
//
//            // 获取要素源
//            String typeName = dataStore.getTypeNames()[0];
//            SimpleFeatureCollection collection = dataStore.getFeatureSource(typeName).getFeatures();
//
//            // 遍历特征
//            try (SimpleFeatureIterator iterator = collection.features()) {
//                while (iterator.hasNext()) {
//                    SimpleFeature feature = iterator.next();
//
//                    // 创建一个LinkedHashMap来存储特征属性
//                    LinkedHashMap<String, Object> attributeMap = new LinkedHashMap<>();
//
//                    // 使用传统for-each循环将字段名和值存储到LinkedHashMap中
//                    for (Property property : feature.getProperties()) {
//                        String name = String.valueOf(property.getName());
//                        Object value = property.getValue();
//                        if (name.equals("the_geom")){
//                            continue;
//                        }
//                        attributeMap.put(name, value);
//                    }
//
//                    // 将当前特征的属性Map添加到列表中
//                    featureList.add(attributeMap);
//                }
//            }
//
//            // 关闭数据存储
//            dataStore.dispose();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return featureList;
//    }
//
//    private static String cutName(String name) {
//       if (name.length()>10){
//           return name.substring(0, 10);
//       }
//       return name;
//    }
//}
