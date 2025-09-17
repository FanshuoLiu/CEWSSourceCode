//package com.encryptrdSoftware.hnust;
//
//import com.encryptrdSoftware.hnust.controller.UploadServlet;
//import com.encryptrdSoftware.hnust.model.Domain;
//import com.encryptrdSoftware.hnust.model.Line;
//import com.encryptrdSoftware.hnust.model.MultiLine;
//import com.encryptrdSoftware.hnust.model.Shape;
//import com.vividsolutions.jts.geom.Coordinate;
//import org.gdal.gdal.gdal;
//import org.gdal.ogr.Layer;
//import org.gdal.ogr.ogr;
//import org.gdal.osr.SpatialReference;
//import org.geotools.data.*;
//import org.geotools.data.shapefile.ShapefileDataStore;
//import org.geotools.data.shapefile.ShapefileDataStoreFactory;
//import org.geotools.data.simple.SimpleFeatureCollection;
//import org.geotools.data.simple.SimpleFeatureIterator;
//import org.geotools.data.simple.SimpleFeatureSource;
//import org.geotools.data.simple.SimpleFeatureStore;
//import org.geotools.feature.DefaultFeatureCollection;
//import org.geotools.feature.FeatureCollection;
//import org.geotools.feature.FeatureIterator;
//import org.geotools.feature.simple.SimpleFeatureBuilder;
//import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
//import org.geotools.geojson.feature.FeatureJSON;
//import org.geotools.geometry.jts.JTSFactoryFinder;
//import org.geotools.referencing.CRS;
//import org.geotools.referencing.crs.DefaultGeographicCRS;
//import org.locationtech.jts.geom.*;
//import org.opengis.feature.Property;
//import org.opengis.feature.simple.SimpleFeature;
//import org.opengis.feature.simple.SimpleFeatureType;
//import org.opengis.feature.type.AttributeDescriptor;
//import org.opengis.feature.type.AttributeType;
//import org.opengis.filter.Filter;
//import org.opengis.filter.sort.SortOrder;
//
//import org.opengis.referencing.crs.CoordinateReferenceSystem;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.net.MalformedURLException;
//import java.nio.charset.Charset;
//import java.util.*;
//
//public class ShpModifier {
//   public static void createSHP(List<? extends Shape> coordinates, Layer layer, String filename, String btn,List<LinkedHashMap<String, Object>> properties,Object...arr) throws IOException, ClassNotFoundException {
//        File file;
//        if (btn.contains("水印")||btn.equals("解密")){
//           file = new File(UploadServlet.Path+File.separator+btn+filename+".shp");
//       }else {
//            file = new File(UploadServlet.Path+File.separator+btn+filename+((int)arr[0]+1)+".shp");
//        }
//
//        System.out.println(file);
//            Map<String, Object> params = new HashMap<>();
//            //指定shp的url
//            params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
//            //创建空间索引
//            params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.TRUE);
//            // 创建新的Shapefile数据存储
//            ShapefileDataStore dataStore = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
//            dataStore.setCharset(Charset.forName("UTF-8"));
//            SimpleFeatureType featureType = createFeatureType(layer,properties);
//            dataStore.createSchema(featureType);
//
//        try (Transaction transaction = new DefaultTransaction("create")) {
//            String typeName = dataStore.getTypeNames()[0];
//            SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
//            if (featureSource instanceof SimpleFeatureStore) {
//                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
//                DefaultFeatureCollection collection = new DefaultFeatureCollection();
//                GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
//                int index=0;
//                        // 根据几何类型处理不同形状
//                        for (Shape shape : coordinates) {
//                            if (shape instanceof com.encryptrdSoftware.hnust.model.Point) {
//                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
//                                com.encryptrdSoftware.hnust.model.Point point = (com.encryptrdSoftware.hnust.model.Point) shape;
//                                LinkedHashMap<String, Object> properties1 = properties.get(index);
//                                for (Map.Entry<String, Object> entry : properties1.entrySet()) {
//                                    featureBuilder.set(entry.getKey(),entry.getValue());
//                                }
//                                org.locationtech.jts.geom.Coordinate coords = new org.locationtech.jts.geom.Coordinate(point.getX(),point.getY());
//                                Point point1 = geometryFactory.createPoint(coords);
//                                featureBuilder.add(point1);
//                                SimpleFeature feature = featureBuilder.buildFeature(null);
//                                collection.add(feature);
//                                index++;
//                            }else if (shape instanceof com.encryptrdSoftware.hnust.model.MultiPoint) {
//                                // 创建 SimpleFeatureType
////                                SimpleFeatureType featureType = createFeatureType(layer, properties, "MULTIPOINT");
////                                dataStore.createSchema(featureType);
//                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
//                                LinkedHashMap<String, Object> properties1 = properties.get(index);
//                                // 填充特征属性
//                                for (Map.Entry<String, Object> entry : properties1.entrySet()) {
//                                    featureBuilder.set(entry.getKey(), entry.getValue());
//                                }
//                                // 处理 MultiPoint
//                                com.encryptrdSoftware.hnust.model.MultiPoint multiPoint = (com.encryptrdSoftware.hnust.model.MultiPoint) shape;
//                                Point[] points = new Point[multiPoint.getNum()];
//                                // 构建 Point 数组
//                                for (int i = 0; i < multiPoint.getNum(); i++) {
//                                    double x = multiPoint.getPoints().get(i).getX();
//                                    double y = multiPoint.getPoints().get(i).getY();
//                                    points[i] = geometryFactory.createPoint(new Coordinate(x, y));
//                                }
//                                // 使用 Point 数组创建 MultiPoint
//                                MultiPoint jtsMultiPoint = geometryFactory.createMultiPoint(points);
//                                // 将 JTS MultiPoint 添加到特征中
//                                featureBuilder.add(jtsMultiPoint);
//                                // 构建 SimpleFeature
//                                SimpleFeature feature = featureBuilder.buildFeature(null);
//                                collection.add(feature);
//                                index++;
//                            }else if (shape instanceof Line) {
////                                SimpleFeatureType featureType = createFeatureType(layer,properties,"LINESTRING");
////                                dataStore.createSchema(featureType);
//                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
//                                LinkedHashMap<String, Object> properties1 = properties.get(index);
//                                for (Map.Entry<String, Object> entry : properties1.entrySet()) {
//                                    featureBuilder.set(entry.getKey(),entry.getValue());
//                                }
//                                Line line = (Line) shape;
//                                org.locationtech.jts.geom.Coordinate[] coords = new org.locationtech.jts.geom.Coordinate[line.getLength()];
//                                for (int i = 0; i < line.getLength(); i++) {
//                                    coords[i] = new org.locationtech.jts.geom.Coordinate(line.getPoints().get(i).getX(), line.getPoints().get(i).getY());
//                                }
//                                LineString lineString = geometryFactory.createLineString(coords);
//                                featureBuilder.add(lineString);
//                                SimpleFeature feature = featureBuilder.buildFeature(null);
//                                collection.add(feature);
//                                index++;
//                            }else if (shape instanceof MultiLine) {
////                                SimpleFeatureType featureType = createFeatureType(layer,properties,"MULTILINESTRING");
////                                dataStore.createSchema(featureType);
//                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
//                                LinkedHashMap<String, Object> properties1 = properties.get(index);
//                                for (Map.Entry<String, Object> entry : properties1.entrySet()) {
//                                    featureBuilder.set(entry.getKey(), entry.getValue());
//                                }
//
//                                MultiLine multiLine = (MultiLine) shape;
//                                List<LineString> lineStrings = new ArrayList<>();
//
//                                for (Line line : multiLine.getLines()) {
//                                    int length = line.getLength();
//                                    org.locationtech.jts.geom.Coordinate[] coords = new org.locationtech.jts.geom.Coordinate[length];
//                                    for (int i = 0; i < length; i++) {
//                                        coords[i] = new org.locationtech.jts.geom.Coordinate(line.getPoints().get(i).getX(), line.getPoints().get(i).getY());
//                                    }
//                                    LineString lineString = geometryFactory.createLineString(coords);
//                                    lineStrings.add(lineString);
//                                }
//
//                                MultiLineString multiLineString = geometryFactory.createMultiLineString(lineStrings.toArray(new LineString[0]));
//                                featureBuilder.add(multiLineString);
//                                SimpleFeature feature = featureBuilder.buildFeature(null);
//                                collection.add(feature);
//                                index++;
//                            }else if (shape instanceof com.encryptrdSoftware.hnust.model.Polygon) {
////                                SimpleFeatureType featureType = createFeatureType(layer,properties,"POLYGON");
////                                dataStore.createSchema(featureType);
//                                org.locationtech.jts.geom.Polygon polygonShape = null;
//                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
//                                LinkedHashMap<String, Object> properties1 = properties.get(index);
//                                for (Map.Entry<String, Object> entry : properties1.entrySet()) {
//                                    featureBuilder.set(entry.getKey(), entry.getValue());
//                                }
//                                com.encryptrdSoftware.hnust.model.Polygon polygon = (com.encryptrdSoftware.hnust.model.Polygon) shape;
//
//                                // 检查外环数据
//                                List<com.encryptrdSoftware.hnust.model.Point> exteriors = polygon.getExteriors();
//
//                                org.locationtech.jts.geom.Coordinate[] coords = new org.locationtech.jts.geom.Coordinate[exteriors.size() + 1]; // 加1用于闭合
//                                for (int i = 0; i < exteriors.size(); i++) {
//                                    coords[i] = new org.locationtech.jts.geom.Coordinate(exteriors.get(i).getX(), exteriors.get(i).getY());
//                                }
//                                coords[exteriors.size()] = new org.locationtech.jts.geom.Coordinate(coords[0].x, coords[0].y); // 闭合
//
//                                org.locationtech.jts.geom.LinearRing outerRing = geometryFactory.createLinearRing(coords);
//                                if (polygon.getInteriors() != null && !polygon.getInteriors().isEmpty()) {
//                                    System.out.println("有内环");
//                                    LinearRing[] innerRings = new LinearRing[polygon.getInteriors().size()];
//                                    List<List<com.encryptrdSoftware.hnust.model.Point>> interiors = polygon.getInteriors();
//                                    for (int i = 0; i < interiors.size(); i++) {
//                                        List<com.encryptrdSoftware.hnust.model.Point> interior = interiors.get(i);
//                                        org.locationtech.jts.geom.Coordinate[] interiorCoords = new org.locationtech.jts.geom.Coordinate[interior.size() + 1];
//                                        for (int j = 0; j < interior.size(); j++) {
//                                            interiorCoords[j] = new org.locationtech.jts.geom.Coordinate(interior.get(j).getX(), interior.get(j).getY());
//                                        }
//                                        interiorCoords[interior.size()] = new org.locationtech.jts.geom.Coordinate(interiorCoords[0].x, interiorCoords[0].y);
//                                        innerRings[i] = geometryFactory.createLinearRing(interiorCoords);
//                                    }
//                                    polygonShape = geometryFactory.createPolygon(outerRing, innerRings);
//                                } else {
//                                    polygonShape = geometryFactory.createPolygon(outerRing);
//                                }
//
//                                featureBuilder.add(polygonShape);
//                                SimpleFeature feature = featureBuilder.buildFeature(null);
//                                collection.add(feature);
//                                index++;
//                            }else if (shape instanceof com.encryptrdSoftware.hnust.model.MultiPolygon) {
////                                SimpleFeatureType featureType = createFeatureType(layer,properties,"MULTIPOLYGON");
////                                dataStore.createSchema(featureType);
//                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
//                                LinkedHashMap<String, Object> properties1 = properties.get(index);
//                                for (Map.Entry<String, Object> entry : properties1.entrySet()) {
//                                    featureBuilder.set(entry.getKey(), entry.getValue());
//                                }
//
//                                com.encryptrdSoftware.hnust.model.MultiPolygon multiPolygon = (com.encryptrdSoftware.hnust.model.MultiPolygon) shape;
//                                List<org.locationtech.jts.geom.Polygon> polygons = new ArrayList<>();
//
//                                for (com.encryptrdSoftware.hnust.model.Polygon polygon : multiPolygon.getPolygons()) {
//                                    // 检查外环数据
//                                    List<com.encryptrdSoftware.hnust.model.Point> exteriors = polygon.getExteriors();
//                                    org.locationtech.jts.geom.Coordinate[] coords = new org.locationtech.jts.geom.Coordinate[exteriors.size() + 1]; // 加1用于闭合
//                                    for (int i = 0; i < exteriors.size(); i++) {
//                                        coords[i] = new org.locationtech.jts.geom.Coordinate(exteriors.get(i).getX(), exteriors.get(i).getY());
//                                    }
//                                    coords[exteriors.size()] = new org.locationtech.jts.geom.Coordinate(coords[0].x, coords[0].y); // 闭合
//
//                                    org.locationtech.jts.geom.LinearRing outerRing = geometryFactory.createLinearRing(coords);
//                                    LinearRing[] innerRings = null;
//
//                                    if (polygon.getInteriors() != null && !polygon.getInteriors().isEmpty()) {
//                                        innerRings = new LinearRing[polygon.getInteriors().size()];
//                                        List<List<com.encryptrdSoftware.hnust.model.Point>> interiors = polygon.getInteriors();
//                                        for (int i = 0; i < interiors.size(); i++) {
//                                            List<com.encryptrdSoftware.hnust.model.Point> interior = interiors.get(i);
//                                            org.locationtech.jts.geom.Coordinate[] interiorCoords = new org.locationtech.jts.geom.Coordinate[interior.size() + 1];
//                                            for (int j = 0; j < interior.size(); j++) {
//                                                interiorCoords[j] = new org.locationtech.jts.geom.Coordinate(interior.get(j).getX(), interior.get(j).getY());
//                                            }
//                                            interiorCoords[interior.size()] = new org.locationtech.jts.geom.Coordinate(interiorCoords[0].x, interiorCoords[0].y);
//                                            innerRings[i] = geometryFactory.createLinearRing(interiorCoords);
//                                        }
//                                    }
//
//                                    org.locationtech.jts.geom.Polygon polygonShape = geometryFactory.createPolygon(outerRing, innerRings);
//                                    polygons.add(polygonShape);
//                                }
//
//                                // 创建 MultiPolygon
//                                org.locationtech.jts.geom.MultiPolygon multiPolygonShape = geometryFactory.createMultiPolygon(polygons.toArray(new org.locationtech.jts.geom.Polygon[0]));
//
//                                featureBuilder.add(multiPolygonShape);
//                                SimpleFeature feature = featureBuilder.buildFeature(null);
//                                collection.add(feature);
//                                index++;
//                            }
//                        }
//
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
//    public static Class<? extends Geometry> getGeometryClass(Layer layer) {
//        // 根据 geomType 字符串返回对应的 JTS 几何类
//        switch (layer.GetGeomType()) {
//            case 1:
//                return Point.class;
//            case 2:
//                return LineString.class;
//            case 3:
//                return Polygon.class;
//            default:
//                throw new IllegalArgumentException("未知的几何类型");
//        }
//    }
//
//    // 创建要素类型
//    private static SimpleFeatureType createFeatureType(Layer layer,List<LinkedHashMap<String, Object>> properties) throws ClassNotFoundException {
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
//        builder.add("the_geom", getGeometryClass(layer));
//        // 添加其他属性
//        // 定义字符编码
//        Charset charset = Charset.forName("GBK");
//        LinkedHashMap<String, Object> properties1 = properties.get(0);
//        for (Map.Entry<String, Object> entry : properties1.entrySet()) {
//            Object value = entry.getValue();
//            System.out.println("属性类型：" +entry.getKey()+"  "+"属性值:"+value);
//                   if (value instanceof String){
//                       builder.add(entry.getKey(), String.class);
//                   }else {
//                       builder.add(entry.getKey(), Object.class);
//                   }
//        }
//        return builder.buildFeatureType();
//    }
//
//    // 方法：读取Shapefile并返回特征的LinkedHashMap集合
//    public static List<LinkedHashMap<String, Object>> readShapefile(File file) {
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
//}
