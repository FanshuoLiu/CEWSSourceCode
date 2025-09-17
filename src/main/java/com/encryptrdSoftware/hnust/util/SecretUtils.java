package com.encryptrdSoftware.hnust.util;

import com.encryptrdSoftware.hnust.model.Line;
import com.encryptrdSoftware.hnust.model.MultiLine;
import com.encryptrdSoftware.hnust.model.Shape;
import com.encryptrdSoftware.hnust.model.encryptedDomain;
import com.encryptrdSoftware.hnust.controller.UploadServlet;
import org.gdal.ogr.Layer;
import org.gdal.osr.SpatialReference;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.*;


public class SecretUtils {
    //生成一个指定位长度的大素数，范围为3~2^bitlength-1
    // 缓存SecureRandom实例，避免频繁创建的性能损耗
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static BigInteger generatePrime(List<encryptedDomain> eList) {
        if (eList.isEmpty()) {
            throw new IllegalArgumentException("eList cannot be empty");
        }

        // 提取整数半径并转换为BigInteger（提前过滤空值，避免NPE）
        List<BigInteger> list = new ArrayList<>(eList.size());
        for (encryptedDomain e : eList) {
            if (e != null) {
                list.add(BigInteger.valueOf(e.getIntegerRadius()));
            }
        }

        if (list.isEmpty()) {
            throw new IllegalArgumentException("No valid integer radius found in eList");
        }

        // 计算最大值（O(n)操作，大数据量下无法避免）
        BigInteger maxNum = Collections.max(list);

        // 计算所需素数的位数：maxNum的位数+1，确保生成的素数一定大于maxNum
        // 原理：n位二进制数的范围是[2^(n-1), 2^n-1]，n+1位的数必然大于n位的数
        int requiredBits = maxNum.bitLength() + 1;

        // 确保至少生成2位素数（最小素数是2）
        requiredBits = Math.max(requiredBits, 2);
        BigInteger bigInteger = BigInteger.probablePrime(requiredBits, SECURE_RANDOM);
        System.out.println("生成的素数：" + bigInteger);
        // 生成素数（一次生成即可，无需循环检查）
        return bigInteger;
    }
    /*生成一个多项式
    secret：要共享的秘密，作为多项式的常数项。
    degree：多项式的次数，即多项式的最高次幂。
    prime：有限域的模数。*/
    public static List<BigInteger> generatePolynomial(BigInteger secret, int threshold, BigInteger prime) {
        // 1. 预设ArrayList容量为threshold，避免扩容
        List<BigInteger> coefficients = new ArrayList<>(threshold);
        coefficients.add(secret);
        // 2. 重用一个Random实例，减少临时对象创建
        Random random = new Random();
        int bitLength = prime.bitLength();

        for (int i = 1; i < threshold; i++) {
            // 3. 优化随机数生成：直接生成小于prime的随机数（避免先做大数再取模）
            BigInteger randomCoeff;
            do {
                // 生成不超过bitLength位的随机数
                randomCoeff = new BigInteger(bitLength, random);
            } while (randomCoeff.compareTo(prime) >= 0); // 确保结果小于prime

            coefficients.add(randomCoeff);
        }
        return coefficients;
    }



    public static List<BigInteger> generateShares(List<BigInteger> coefficients, int numShares, BigInteger prime,BigInteger randomSecret,int a) {
        // 1. 预设列表容量，避免扩容
        List<BigInteger> shares = new ArrayList<>(numShares);
        int coeffSize = coefficients.size();

        // 2. 提前判断a是否为0，减少循环内条件判断
        boolean useRandomSecretForLastCoeff = (a != 0) && (coeffSize > 0);

        for (int i = 1; i <= numShares; i++) {
            BigInteger xi = BigInteger.valueOf(i);
            BigInteger yi = BigInteger.ZERO;
            // 累积计算x^j（初始为x^0 = 1）
            BigInteger currentPower = BigInteger.ONE;

            for (int j = 0; j < coeffSize; j++) {
                // 3. 确定当前系数（避免循环内重复判断a的值）
                BigInteger coefficient;
                if (useRandomSecretForLastCoeff && j == coeffSize - 1) {
                    coefficient = randomSecret;
                } else {
                    coefficient = coefficients.get(j);
                }

                // 4. 计算项：coefficient * currentPower，并及时取模（控制数值大小）
                BigInteger term = coefficient.multiply(currentPower).mod(prime);
                // 累加后取模，避免yi过大
                yi = yi.add(term).mod(prime);

                // 5. 累积计算下一次的幂（x^(j+1) = x^j * xi），取模控制大小
                currentPower = currentPower.multiply(xi).mod(prime);
            }

            shares.add(yi);
        }

        return shares;
    }

    // 生成满足条件的份额

    // 检查份额中是否有大于等于 360 的值
//    private static boolean hasShareGreaterOrEqual360(List<BigInteger> shares) {
//        BigInteger limit = BigInteger.valueOf(360);
//        for (BigInteger share : shares) {
//            if (share.compareTo(limit) >= 0) {
//                return true;
//            }
//        }
//        return false;
//    }

    public static BigInteger recoverSecret(List<BigInteger> shares, List<Integer> indices, BigInteger prime) {
        BigInteger secret=BigInteger.ZERO;
        for (int i=0; i<shares.size();i++) {
            //分子
            BigInteger numerator=BigInteger.ONE;
            //分母
            BigInteger denominator=BigInteger.ONE;

            for (int j=0; j<shares.size();j++) {
                if (i!=j) {
                    BigInteger xj=BigInteger.valueOf(indices.get(j));
                    BigInteger xi=BigInteger.valueOf(indices.get(i));
                    //令x=0
                    numerator=numerator.multiply(BigInteger.ZERO.subtract(xj));
                    denominator=denominator.multiply(xi.subtract(xj));
//                    builder.append(numerator+"*"+"0-"+xj+"/"+denominator+"*"+"("+xi+"-"+xj+")");
                }
            }
            //modInverse：取模反,求分母的倒数
            BigInteger inverseDenominator=denominator.modInverse(prime);
            BigInteger term=shares.get(i).multiply(numerator).multiply(inverseDenominator);
            secret=secret.add(term).mod(prime);
        }
        return secret;
    }

    //生成shp文件
    public static void createSHP(List<? extends Shape> coordinates, Layer layer, String filename, String btn,Object...arr) throws IOException, ClassNotFoundException {
        File file;
        if (btn.contains("水印")||btn.equals("解密")||btn.equals("局部解密")){
           file = new File(UploadServlet.Path+File.separator+btn+filename+".shp");
       }else {
            file = new File(UploadServlet.Path+File.separator+btn+filename+((int)arr[0]+1)+".shp");
        }

        System.out.println(file);
            Map<String, Object> params = new HashMap<>();
            //指定shp的url
            params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
            //创建空间索引
            params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.TRUE);
            // 创建新的Shapefile数据存储
            ShapefileDataStore dataStore = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
            dataStore.setCharset(Charset.forName("UTF-8"));
            SimpleFeatureType featureType = createFeatureType(layer);
            dataStore.createSchema(featureType);

        try (Transaction transaction = new DefaultTransaction("create")) {
            String typeName = dataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
            if (featureSource instanceof SimpleFeatureStore) {
                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
                DefaultFeatureCollection collection = new DefaultFeatureCollection();
                GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
                int index=0;
                        // 根据几何类型处理不同形状
                        for (Shape shape : coordinates) {
                            if (shape instanceof com.encryptrdSoftware.hnust.model.Point) {
                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
                                com.encryptrdSoftware.hnust.model.Point point = (com.encryptrdSoftware.hnust.model.Point) shape;
                                org.locationtech.jts.geom.Coordinate coords = new org.locationtech.jts.geom.Coordinate(point.getX(),point.getY());
                                Point point1 = geometryFactory.createPoint(coords);
                                featureBuilder.add(point1);
                                SimpleFeature feature = featureBuilder.buildFeature(null);
                                collection.add(feature);
                                index++;
                            }else if (shape instanceof com.encryptrdSoftware.hnust.model.MultiPoint) {
                                // 创建 SimpleFeatureType
//                                SimpleFeatureType featureType = createFeatureType(layer, properties, "MULTIPOINT");
//                                dataStore.createSchema(featureType);
                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
                                // 处理 MultiPoint
                                com.encryptrdSoftware.hnust.model.MultiPoint multiPoint = (com.encryptrdSoftware.hnust.model.MultiPoint) shape;
                                Point[] points = new Point[multiPoint.getNum()];
                                // 构建 Point 数组
                                for (int i = 0; i < multiPoint.getNum(); i++) {
                                    double x = multiPoint.getPoints().get(i).getX();
                                    double y = multiPoint.getPoints().get(i).getY();
                                    points[i] = geometryFactory.createPoint(new Coordinate(x, y));
                                }
                                // 使用 Point 数组创建 MultiPoint
                                MultiPoint jtsMultiPoint = geometryFactory.createMultiPoint(points);
                                // 将 JTS MultiPoint 添加到特征中
                                featureBuilder.add(jtsMultiPoint);
                                // 构建 SimpleFeature
                                SimpleFeature feature = featureBuilder.buildFeature(null);
                                collection.add(feature);
                                index++;
                            }else if (shape instanceof Line) {
//                                SimpleFeatureType featureType = createFeatureType(layer,properties,"LINESTRING");
//                                dataStore.createSchema(featureType);
                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
                                Line line = (Line) shape;
                                org.locationtech.jts.geom.Coordinate[] coords = new org.locationtech.jts.geom.Coordinate[line.getLength()];
                                for (int i = 0; i < line.getLength(); i++) {
                                    coords[i] = new org.locationtech.jts.geom.Coordinate(line.getPoints().get(i).getX(), line.getPoints().get(i).getY());
                                }
                                LineString lineString = geometryFactory.createLineString(coords);
                                featureBuilder.add(lineString);
                                SimpleFeature feature = featureBuilder.buildFeature(null);
                                collection.add(feature);
                                index++;
                            }else if (shape instanceof MultiLine) {
//                                SimpleFeatureType featureType = createFeatureType(layer,properties,"MULTILINESTRING");
//                                dataStore.createSchema(featureType);
                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

                                MultiLine multiLine = (MultiLine) shape;
                                List<LineString> lineStrings = new ArrayList<>();

                                for (Line line : multiLine.getLines()) {
                                    int length = line.getLength();
                                    org.locationtech.jts.geom.Coordinate[] coords = new org.locationtech.jts.geom.Coordinate[length];
                                    for (int i = 0; i < length; i++) {
                                        coords[i] = new org.locationtech.jts.geom.Coordinate(line.getPoints().get(i).getX(), line.getPoints().get(i).getY());
                                    }
                                    LineString lineString = geometryFactory.createLineString(coords);
                                    lineStrings.add(lineString);
                                }

                                MultiLineString multiLineString = geometryFactory.createMultiLineString(lineStrings.toArray(new LineString[0]));
                                featureBuilder.add(multiLineString);
                                SimpleFeature feature = featureBuilder.buildFeature(null);
                                collection.add(feature);
                                index++;
                            }else if (shape instanceof com.encryptrdSoftware.hnust.model.Polygon) {
//                                SimpleFeatureType featureType = createFeatureType(layer,properties,"POLYGON");
//                                dataStore.createSchema(featureType);
                                org.locationtech.jts.geom.Polygon polygonShape = null;
                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

                                com.encryptrdSoftware.hnust.model.Polygon polygon = (com.encryptrdSoftware.hnust.model.Polygon) shape;

                                // 检查外环数据
                                List<com.encryptrdSoftware.hnust.model.Point> exteriors = polygon.getExteriors();

                                org.locationtech.jts.geom.Coordinate[] coords = new org.locationtech.jts.geom.Coordinate[exteriors.size() + 1]; // 加1用于闭合
                                for (int i = 0; i < exteriors.size(); i++) {
                                    coords[i] = new org.locationtech.jts.geom.Coordinate(exteriors.get(i).getX(), exteriors.get(i).getY());
                                }
                                coords[exteriors.size()] = new org.locationtech.jts.geom.Coordinate(coords[0].x, coords[0].y); // 闭合

                                org.locationtech.jts.geom.LinearRing outerRing = geometryFactory.createLinearRing(coords);
                                if (polygon.getInteriors() != null && !polygon.getInteriors().isEmpty()) {
                                    LinearRing[] innerRings = new LinearRing[polygon.getInteriors().size()];
                                    List<List<com.encryptrdSoftware.hnust.model.Point>> interiors = polygon.getInteriors();
                                    for (int i = 0; i < interiors.size(); i++) {
                                        List<com.encryptrdSoftware.hnust.model.Point> interior = interiors.get(i);
                                        org.locationtech.jts.geom.Coordinate[] interiorCoords = new org.locationtech.jts.geom.Coordinate[interior.size() + 1];
                                        for (int j = 0; j < interior.size(); j++) {
                                            interiorCoords[j] = new org.locationtech.jts.geom.Coordinate(interior.get(j).getX(), interior.get(j).getY());
                                        }
                                        interiorCoords[interior.size()] = new org.locationtech.jts.geom.Coordinate(interiorCoords[0].x, interiorCoords[0].y);
                                        innerRings[i] = geometryFactory.createLinearRing(interiorCoords);
                                    }
                                    polygonShape = geometryFactory.createPolygon(outerRing, innerRings);
                                } else {
                                    polygonShape = geometryFactory.createPolygon(outerRing);
                                }

                                featureBuilder.add(polygonShape);
                                SimpleFeature feature = featureBuilder.buildFeature(null);
                                collection.add(feature);
                                index++;
                            }else if (shape instanceof com.encryptrdSoftware.hnust.model.MultiPolygon) {
//                                SimpleFeatureType featureType = createFeatureType(layer,properties,"MULTIPOLYGON");
//                                dataStore.createSchema(featureType);
                                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

                                com.encryptrdSoftware.hnust.model.MultiPolygon multiPolygon = (com.encryptrdSoftware.hnust.model.MultiPolygon) shape;
                                List<org.locationtech.jts.geom.Polygon> polygons = new ArrayList<>();

                                for (com.encryptrdSoftware.hnust.model.Polygon polygon : multiPolygon.getPolygons()) {
                                    // 检查外环数据
                                    List<com.encryptrdSoftware.hnust.model.Point> exteriors = polygon.getExteriors();
                                    org.locationtech.jts.geom.Coordinate[] coords = new org.locationtech.jts.geom.Coordinate[exteriors.size() + 1]; // 加1用于闭合
                                    for (int i = 0; i < exteriors.size(); i++) {
                                        coords[i] = new org.locationtech.jts.geom.Coordinate(exteriors.get(i).getX(), exteriors.get(i).getY());
                                    }
                                    coords[exteriors.size()] = new org.locationtech.jts.geom.Coordinate(coords[0].x, coords[0].y); // 闭合

                                    org.locationtech.jts.geom.LinearRing outerRing = geometryFactory.createLinearRing(coords);
                                    LinearRing[] innerRings = null;

                                    if (polygon.getInteriors() != null && !polygon.getInteriors().isEmpty()) {
                                        innerRings = new LinearRing[polygon.getInteriors().size()];
                                        List<List<com.encryptrdSoftware.hnust.model.Point>> interiors = polygon.getInteriors();
                                        for (int i = 0; i < interiors.size(); i++) {
                                            List<com.encryptrdSoftware.hnust.model.Point> interior = interiors.get(i);
                                            org.locationtech.jts.geom.Coordinate[] interiorCoords = new org.locationtech.jts.geom.Coordinate[interior.size() + 1];
                                            for (int j = 0; j < interior.size(); j++) {
                                                interiorCoords[j] = new org.locationtech.jts.geom.Coordinate(interior.get(j).getX(), interior.get(j).getY());
                                            }
                                            interiorCoords[interior.size()] = new org.locationtech.jts.geom.Coordinate(interiorCoords[0].x, interiorCoords[0].y);
                                            innerRings[i] = geometryFactory.createLinearRing(interiorCoords);
                                        }
                                    }

                                    org.locationtech.jts.geom.Polygon polygonShape = geometryFactory.createPolygon(outerRing, innerRings);
                                    polygons.add(polygonShape);
                                }

                                // 创建 MultiPolygon
                                org.locationtech.jts.geom.MultiPolygon multiPolygonShape = geometryFactory.createMultiPolygon(polygons.toArray(new org.locationtech.jts.geom.Polygon[0]));

                                featureBuilder.add(multiPolygonShape);
                                SimpleFeature feature = featureBuilder.buildFeature(null);
                                collection.add(feature);
                                index++;
                            }
                        }


                featureStore.setTransaction(transaction);
                featureStore.addFeatures(collection);
                transaction.commit();
            } else {
                System.out.println("FeatureStore 不支持");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dataStore.dispose();
        }
    }



    public static Class<? extends Geometry> getGeometryClass(Layer layer) {
        // 根据 geomType 字符串返回对应的 JTS 几何类
        switch (layer.GetGeomType()) {
            case 1:
                return Point.class;
            case 2:
                return LineString.class;
            case 3:
                return Polygon.class;
            default:
                throw new IllegalArgumentException("未知的几何类型");
        }
    }

    // 创建要素类型
    private static SimpleFeatureType createFeatureType(Layer layer){
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Location");

        // 获取坐标参考系统
        SpatialReference spatialRef = layer.GetSpatialRef();
        CoordinateReferenceSystem crs;

        if (spatialRef != null) {
            try {
                String wkt = spatialRef.ExportToWkt();
                crs = getDefaultCRS();
            } catch (Exception e) {
                System.err.println("无法转换坐标参考系统: " + e.getMessage());
                crs = getDefaultCRS(); // 作为备用
            }
        } else {
            crs = getDefaultCRS(); // 默认坐标参考系统
        }
        if (crs!= null){
            builder.setCRS(crs);
        }else {
            throw new RuntimeException("无法获取默认EPSG:3857坐标系，创建FeatureType失败");
        }


        // 检查并添加几何属性
        builder.add("the_geom", getGeometryClass(layer));
        // 添加其他属性
        // 定义字符编码
//        Charset charset = Charset.forName("GBK");
        return builder.buildFeatureType();
    }

    private static CoordinateReferenceSystem getDefaultCRS() {
        try {
            // 通过EPSG代码获取3857坐标系（第二个参数true表示允许"强制匹配"，提高兼容性）
            return CRS.decode("EPSG:3857", true);
        } catch (FactoryException e) {
            System.err.println("获取默认EPSG:3857坐标系失败: " + e.getMessage());
            return null;
        }
    }

    // 方法：读取Shapefile并返回特征的LinkedHashMap集合
    public static List<LinkedHashMap<String, Object>> readShapefile(File file) {
        List<LinkedHashMap<String, Object>> featureList = new ArrayList<>();
        try {
            // 创建数据存储
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("url", file.toURI().toString());
            DataStore dataStore = DataStoreFinder.getDataStore(map);

            // 获取要素源
            String typeName = dataStore.getTypeNames()[0];
            SimpleFeatureCollection collection = dataStore.getFeatureSource(typeName).getFeatures();

            // 遍历特征
            try (SimpleFeatureIterator iterator = collection.features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();

                    // 创建一个LinkedHashMap来存储特征属性
                    LinkedHashMap<String, Object> attributeMap = new LinkedHashMap<>();

                    // 使用传统for-each循环将字段名和值存储到LinkedHashMap中
                    for (Property property : feature.getProperties()) {
                        String name = String.valueOf(property.getName());
                        Object value = property.getValue();
                        if (name.equals("the_geom")){
                            continue;
                        }
                        attributeMap.put(name, value);
                    }

                    // 将当前特征的属性Map添加到列表中
                    featureList.add(attributeMap);
                }
            }

            // 关闭数据存储
            dataStore.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureList;
    }

    public static String longestCommonPrefix(String[] strs) {
        if (strs == null || strs.length == 0) {
            return "";
        }

        // 以第一个字符串作为初始前缀
        String prefix = strs[0];

        for (int i = 1; i < strs.length; i++) {
            // 比较当前字符串与当前前缀
            while (strs[i].indexOf(prefix) != 0) {
                // 如果当前字符串不包含前缀，就缩短前缀
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) {
                    return ""; // 如果前缀为空，则返回空字符串
                }
            }
        }
        return prefix;
    }
}
