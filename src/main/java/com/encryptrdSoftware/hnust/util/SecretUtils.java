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
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.*;


public class SecretUtils {

        public static BigInteger generatePrime(List<encryptedDomain> eList) {
            if (eList.isEmpty()) {
                throw new IllegalArgumentException("集合不能为空");
            }

            List<BigInteger> list = new ArrayList<>(eList.size());
            for (encryptedDomain e : eList) {
                if (e != null) {
                    list.add(BigInteger.valueOf(e.getIntegerRadius()));
                }
            }

            if (list.isEmpty()) {
                throw new IllegalArgumentException("无效集合");
            }

            BigInteger maxNum = Collections.max(list);
            System.out.println("maxNum:" + maxNum);

            BigInteger start = maxNum.add(BigInteger.ONE);
            BigInteger prime = start;

            while (!prime.isProbablePrime(100)) {
                prime = prime.add(BigInteger.ONE);
            }

            System.out.println("生成的素数：" + prime);
            return prime;
        }

    /*生成一个多项式
    secret：要共享的秘密，作为多项式的常数项。
    degree：多项式的次数，即多项式的最高次幂。
    prime：有限域的模数。*/
    public static List<BigInteger> generatePolynomial(BigInteger secret, int threshold, BigInteger prime) {
        List<BigInteger> coefficients = new ArrayList<>(threshold);
        coefficients.add(secret);
        Random random = new Random();
        int bitLength = prime.bitLength();

        for (int i = 1; i < threshold; i++) {
            BigInteger randomCoeff;
            do {
                randomCoeff = new BigInteger(bitLength, random);
            } while (randomCoeff.compareTo(prime) >= 0);

            coefficients.add(randomCoeff);
        }
        return coefficients;
    }



    public static List<BigInteger> generateShares(List<BigInteger> coefficients, int numShares, BigInteger prime,BigInteger randomSecret,int a) {
        List<BigInteger> shares = new ArrayList<>(numShares);
        int coeffSize = coefficients.size();

        boolean useRandomSecretForLastCoeff = (a != 0) && (coeffSize > 0);

        for (int i = 1; i <= numShares; i++) {
            BigInteger xi = BigInteger.valueOf(i);
            BigInteger yi = BigInteger.ZERO;
            BigInteger currentPower = BigInteger.ONE;

            for (int j = 0; j < coeffSize; j++) {
                BigInteger coefficient;
                if (useRandomSecretForLastCoeff && j == coeffSize - 1) {
                    coefficient = randomSecret;
                } else {
                    coefficient = coefficients.get(j);
                }

                BigInteger term = coefficient.multiply(currentPower).mod(prime);
                yi = yi.add(term).mod(prime);

                currentPower = currentPower.multiply(xi).mod(prime);
            }

            shares.add(yi);
        }
        return shares;
    }

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

                }
            }
            //modInverse：取模反,求分母的倒数
            BigInteger inverseDenominator=denominator.modInverse(prime);
            BigInteger term=shares.get(i).multiply(numerator).multiply(inverseDenominator);
            secret=secret.add(term).mod(prime);
        }
        return secret;
    }

    private enum GeometryCategory {
        POINT_CATEGORY, LINE_CATEGORY, POLYGON_CATEGORY
    }

    private static final String FEATURE_TYPE_NAME = "SHP_Feature";

    public static void createSHP(List<? extends Shape> coordinates,
                                 Layer layer,
                                 String filename,
                                 String btn,
                                 List<LinkedHashMap<String, Object>> attrTable,
                                 Object...arr) throws IOException, ClassNotFoundException {
        // 前置校验
        if (coordinates == null || coordinates.isEmpty()) {
            throw new IllegalArgumentException("几何要素集合coordinates不能为空！");
        }

        File baseDir = new File(UploadServlet.Path);
        if (!baseDir.exists()) {
            throw new IOException("基础路径不存在：" + UploadServlet.Path);
        }
        if (!baseDir.canWrite()) {
            throw new IOException("基础路径无写入权限：" + UploadServlet.Path);
        }
        File file = buildShpFile(baseDir, btn, filename, arr);
        System.out.println("生成SHP文件路径：" + file.getAbsolutePath());

        if (file.exists()) {
            deleteShpFiles(file);
            System.out.println("已删除旧的SHP文件：" + file.getAbsolutePath());
        }

        Shape firstShape = coordinates.get(0);
        GeometryCategory category = getGeometryCategory(firstShape);
        validateGeometryCategory(coordinates, category);
        String targetGeometryType = getTargetMultiGeometryType(category);

        Map<String, Object> params = new HashMap<>();
        try {
            params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new IOException("文件URL构建失败：" + file.getAbsolutePath(), e);
        }

        params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.TRUE);
        params.put(ShapefileDataStoreFactory.ENABLE_SPATIAL_INDEX.key, Boolean.TRUE);
        params.put(ShapefileDataStoreFactory.MEMORY_MAPPED.key, Boolean.FALSE); // 避免文件占用

        ShapefileDataStore dataStore = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
        if (dataStore == null) {
            throw new IOException("创建ShapefileDataStore失败！请检查路径/权限/GeoTools依赖");
        }
        dataStore.setCharset(Charset.forName("UTF-8"));

        SimpleFeatureType featureType = createFeatureTypeWithAttrs(attrTable, targetGeometryType);
        if (featureType == null) {
            throw new IllegalArgumentException("创建SimpleFeatureType失败！");
        }
        if (featureType.getGeometryDescriptor() == null) {
            throw new IllegalStateException("几何字段未被GeoTools识别！请检查是否使用JTS的org.locationtech.jts.geom包下的类");
        }
        dataStore.createSchema(featureType);
        String[] typeNames = dataStore.getTypeNames();
        if (typeNames == null || typeNames.length == 0) {
            throw new IOException("DataStore中无可用的TypeName，Schema创建失败！");
        }

        String useTypeName = FEATURE_TYPE_NAME;
        if (!Arrays.asList(typeNames).contains(useTypeName)) {
            useTypeName = typeNames[0];
        }
        System.out.println("使用的TypeName：" + useTypeName);

        SimpleFeatureStore featureStore = null;
        try {

            org.geotools.data.FeatureSource featureSource = dataStore.getFeatureSource(useTypeName);
            // 校验是否可转换为可写的SimpleFeatureStore
            if (featureSource instanceof org.geotools.data.simple.SimpleFeatureStore) {
                featureStore = (org.geotools.data.simple.SimpleFeatureStore) featureSource;
            } else {
                throw new IOException("FeatureSource无法转换为SimpleFeatureStore！当前类型：" + featureSource.getClass().getName());
            }
        } catch (ClassCastException e) {
            throw new IOException("FeatureSource转换为SimpleFeatureStore失败！GeoTools版本不兼容或DataStore只读", e);
        }

        if (featureStore == null) {
            throw new IOException("无法获取可写的SimpleFeatureStore！");
        }

        DefaultFeatureCollection collection = new DefaultFeatureCollection();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        int index = 0;

        for (Shape shape : coordinates) {
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
            Geometry jtsGeometry = buildJtsGeometry(shape, category, geometryFactory);
            if (jtsGeometry == null) {
                throw new IOException("转换自定义Shape为JTS几何对象失败：" + shape.getClass().getName());
            }
            featureBuilder.add(jtsGeometry);
            bindAttributes(featureBuilder, featureType, attrTable, index);
            SimpleFeature feature = featureBuilder.buildFeature(null);
            collection.add(feature);
            index++;
        }

        try (Transaction transaction = new DefaultTransaction("create")) {
            featureStore.setTransaction(transaction);
            featureStore.addFeatures(collection);
            transaction.commit();
            System.out.printf("SHP文件创建成功，共生成 %d 个%s类型要素%n", index, targetGeometryType);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("写入SHP失败：" + e.getMessage());
        } finally {
            if (dataStore != null) {
                dataStore.dispose();
            }
        }
    }

    private static void deleteShpFiles(File shpFile) throws IOException {
        String baseName = shpFile.getName().substring(0, shpFile.getName().lastIndexOf("."));
        File parentDir = shpFile.getParentFile();
        String[] suffixes = {".shp", ".shx", ".dbf", ".prj", ".qix", ".fix"};
        for (String suffix : suffixes) {
            File f = new File(parentDir, baseName + suffix);
            if (f.exists()) {
                if (!f.delete()) {
                    throw new IOException("无法删除旧文件：" + f.getAbsolutePath() + "，请关闭占用该文件的程序（如QGIS/资源管理器）");
                }
            }
        }
    }

    private static SimpleFeatureType createFeatureTypeWithAttrs(List<LinkedHashMap<String, Object>> attrTable,
                                                                String targetGeometryType) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(FEATURE_TYPE_NAME);
        builder.setCRS(DefaultGeographicCRS.WGS84);

        // 获取JTS几何类（全限定名）
        Class<? extends Geometry> jtsGeometryClass = getJtsGeometryClass(targetGeometryType);
        if (jtsGeometryClass == null) {
            throw new IllegalArgumentException("不支持的JTS几何类型：" + targetGeometryType);
        }

        builder.add("the_geom", jtsGeometryClass);

        if (attrTable != null && !attrTable.isEmpty()) {
            LinkedHashMap<String, Object> firstRow = attrTable.get(0);
            for (Map.Entry<String, Object> entry : firstRow.entrySet()) {
                String fieldName = validateFieldName(entry.getKey());
                Class<?> fieldClass = getFieldClass(entry.getValue());
                builder.add(fieldName, fieldClass);
                System.out.println("添加属性字段：" + fieldName + "，类型：" + fieldClass.getSimpleName());
            }
        }

        SimpleFeatureType featureType = builder.buildFeatureType();
        System.out.println("FeatureType总字段数：" + featureType.getAttributeCount());
        return featureType;
    }

    private static File buildShpFile(File baseDir, String btn, String filename, Object...arr) {
        String fileName;
        if (btn.contains("水印")||btn.equals("解密")||btn.equals("局部解密")){
            fileName = btn + filename + ".shp";
        }else {
            fileName = btn + filename + ((int)arr[0]+1) + ".shp";
        }
        return new File(baseDir, fileName);
    }

    private static GeometryCategory getGeometryCategory(Shape shape) {
        if (shape == null) {
            throw new IllegalArgumentException("Shape对象为null！");
        }
        if (shape instanceof com.encryptrdSoftware.hnust.model.Point || shape instanceof com.encryptrdSoftware.hnust.model.MultiPoint) {
            return GeometryCategory.POINT_CATEGORY;
        } else if (shape instanceof Line || shape instanceof MultiLine) {
            return GeometryCategory.LINE_CATEGORY;
        } else if (shape instanceof com.encryptrdSoftware.hnust.model.Polygon || shape instanceof com.encryptrdSoftware.hnust.model.MultiPolygon) {
            return GeometryCategory.POLYGON_CATEGORY;
        } else {
            throw new IllegalArgumentException("不支持的自定义几何类型：" + shape.getClass().getName());
        }
    }

    private static void validateGeometryCategory(List<? extends Shape> coordinates, GeometryCategory targetCategory) {
        for (Shape shape : coordinates) {
            GeometryCategory currentCategory = getGeometryCategory(shape);
            if (currentCategory != targetCategory) {
                throw new IllegalArgumentException(
                        String.format("几何要素类型不统一！当前要素类型%s属于%s，目标大类为%s",
                                shape.getClass().getName(), currentCategory, targetCategory)
                );
            }
        }
    }

    private static String getTargetMultiGeometryType(GeometryCategory category) {
        if (category == GeometryCategory.POINT_CATEGORY) {
            return "MULTIPOINT";
        } else if (category == GeometryCategory.LINE_CATEGORY) {
            return "MULTILINESTRING";
        } else if (category == GeometryCategory.POLYGON_CATEGORY) {
            return "MULTIPOLYGON";
        } else {
            return "MULTIPOLYGON";
        }
    }

    private static Class<? extends Geometry> getJtsGeometryClass(String geometryType) {
        switch (geometryType.toUpperCase()) {
            case "MULTIPOINT":
                return org.locationtech.jts.geom.MultiPoint.class;
            case "MULTILINESTRING":
                return org.locationtech.jts.geom.MultiLineString.class;
            case "MULTIPOLYGON":
                return org.locationtech.jts.geom.MultiPolygon.class;
            case "POINT":
                return org.locationtech.jts.geom.Point.class;
            case "LINESTRING":
                return org.locationtech.jts.geom.LineString.class;
            case "POLYGON":
                return org.locationtech.jts.geom.Polygon.class;
            default:
                throw new IllegalArgumentException("不支持的JTS几何类型：" + geometryType);
        }
    }

    private static Geometry buildJtsGeometry(Shape shape, GeometryCategory category, GeometryFactory gf) {
        switch (category) {
            case POINT_CATEGORY:
                return convertCustomPointToJts(shape, gf);
            case LINE_CATEGORY:
                return convertCustomLineToJts(shape, gf);
            case POLYGON_CATEGORY:
                return convertCustomPolygonToJts(shape, gf);
            default:
                throw new IllegalArgumentException("不支持的几何大类：" + category);
        }
    }

    private static Geometry convertCustomPointToJts(Shape shape, GeometryFactory gf) {
        if (shape instanceof com.encryptrdSoftware.hnust.model.MultiPoint) {
            com.encryptrdSoftware.hnust.model.MultiPoint customMp = (com.encryptrdSoftware.hnust.model.MultiPoint) shape;
            org.locationtech.jts.geom.Point[] jtsPoints = new org.locationtech.jts.geom.Point[customMp.getNum()];
            for (int i = 0; i < customMp.getNum(); i++) {
                com.encryptrdSoftware.hnust.model.Point customP = customMp.getPoints().get(i);
                jtsPoints[i] = gf.createPoint(new Coordinate(customP.getX(), customP.getY()));
            }
            return gf.createMultiPoint(jtsPoints);
        } else if (shape instanceof com.encryptrdSoftware.hnust.model.Point) {
            com.encryptrdSoftware.hnust.model.Point customP = (com.encryptrdSoftware.hnust.model.Point) shape;
            org.locationtech.jts.geom.Point jtsP = gf.createPoint(new Coordinate(customP.getX(), customP.getY()));
            return gf.createMultiPoint(new org.locationtech.jts.geom.Point[]{jtsP});
        } else {
            throw new IllegalArgumentException("点类仅支持自定义的Point/MultiPoint：" + shape.getClass().getName());
        }
    }

    private static Geometry convertCustomLineToJts(Shape shape, GeometryFactory gf) {
        if (shape instanceof MultiLine) {
            MultiLine customMl = (MultiLine) shape;
            List<LineString> jtsLines = new ArrayList<>();
            for (Line customLine : customMl.getLines()) {
                Coordinate[] coords = new Coordinate[customLine.getLength()];
                for (int i = 0; i < customLine.getLength(); i++) {
                    com.encryptrdSoftware.hnust.model.Point p = customLine.getPoints().get(i);
                    coords[i] = new Coordinate(p.getX(), p.getY());
                }
                jtsLines.add(gf.createLineString(coords));
            }
            return gf.createMultiLineString(jtsLines.toArray(new LineString[0]));
        } else if (shape instanceof Line) {
            Line customLine = (Line) shape;
            Coordinate[] coords = new Coordinate[customLine.getLength()];
            for (int i = 0; i < customLine.getLength(); i++) {
                com.encryptrdSoftware.hnust.model.Point p = customLine.getPoints().get(i);
                coords[i] = new Coordinate(p.getX(), p.getY());
            }
            LineString jtsLine = gf.createLineString(coords);
            return gf.createMultiLineString(new LineString[]{jtsLine});
        } else {
            throw new IllegalArgumentException("线类仅支持自定义的Line/MultiLine：" + shape.getClass().getName());
        }
    }

    private static Geometry convertCustomPolygonToJts(Shape shape, GeometryFactory gf) {
        if (shape instanceof com.encryptrdSoftware.hnust.model.MultiPolygon) {
            com.encryptrdSoftware.hnust.model.MultiPolygon customMp = (com.encryptrdSoftware.hnust.model.MultiPolygon) shape;
            List<org.locationtech.jts.geom.Polygon> jtsPolygons = new ArrayList<>();
            for (com.encryptrdSoftware.hnust.model.Polygon customP : customMp.getPolygons()) {
                jtsPolygons.add(convertCustomSinglePolygonToJts(customP, gf));
            }
            return gf.createMultiPolygon(jtsPolygons.toArray(new org.locationtech.jts.geom.Polygon[0]));
        } else if (shape instanceof com.encryptrdSoftware.hnust.model.Polygon) {
            com.encryptrdSoftware.hnust.model.Polygon customP = (com.encryptrdSoftware.hnust.model.Polygon) shape;
            org.locationtech.jts.geom.Polygon jtsP = convertCustomSinglePolygonToJts(customP, gf);
            return gf.createMultiPolygon(new org.locationtech.jts.geom.Polygon[]{jtsP});
        } else {
            throw new IllegalArgumentException("面类仅支持自定义的Polygon/MultiPolygon：" + shape.getClass().getName());
        }
    }

    private static org.locationtech.jts.geom.Polygon convertCustomSinglePolygonToJts(com.encryptrdSoftware.hnust.model.Polygon customP, GeometryFactory gf) {
        List<com.encryptrdSoftware.hnust.model.Point> exteriorPoints = customP.getExteriors();
        Coordinate[] exteriorCoords = new Coordinate[exteriorPoints.size() + 1];
        for (int i = 0; i < exteriorPoints.size(); i++) {
            com.encryptrdSoftware.hnust.model.Point p = exteriorPoints.get(i);
            exteriorCoords[i] = new Coordinate(p.getX(), p.getY());
        }
        exteriorCoords[exteriorPoints.size()] = exteriorCoords[0];
        LinearRing outerRing = gf.createLinearRing(exteriorCoords);

        LinearRing[] innerRings = null;
        if (customP.getInteriors() != null && !customP.getInteriors().isEmpty()) {
            innerRings = new LinearRing[customP.getInteriors().size()];
            for (int i = 0; i < customP.getInteriors().size(); i++) {
                List<com.encryptrdSoftware.hnust.model.Point> interiorPoints = customP.getInteriors().get(i);
                Coordinate[] interiorCoords = new Coordinate[interiorPoints.size() + 1];
                for (int j = 0; j < interiorPoints.size(); j++) {
                    com.encryptrdSoftware.hnust.model.Point p = interiorPoints.get(j);
                    interiorCoords[j] = new Coordinate(p.getX(), p.getY());
                }
                interiorCoords[interiorPoints.size()] = interiorCoords[0];
                innerRings[i] = gf.createLinearRing(interiorCoords);
            }
        }

        return gf.createPolygon(outerRing, innerRings);
    }

    private static String validateFieldName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return "FIELD_" + System.currentTimeMillis();
        }
        String validName = fieldName.length() > 10 ? fieldName.substring(0, 10) : fieldName;
        validName = validName.replaceAll("[^a-zA-Z0-9_]", "_");
        if (validName.matches("^\\d.*")) {
            validName = "F_" + validName;
        }
        return validName;
    }

    private static Class<?> getFieldClass(Object value) {
        if (value == null) {
            return String.class;
        } else if (value instanceof Integer) {
            return Integer.class;
        } else if (value instanceof Double) {
            return Double.class;
        } else if (value instanceof Long) {
            return Long.class;
        } else if (value instanceof Boolean) {
            return Boolean.class;
        } else {
            return String.class;
        }
    }

    private static void bindAttributes(SimpleFeatureBuilder builder, SimpleFeatureType featureType,
                                       List<LinkedHashMap<String, Object>> attrTable, int index) {
        if (attrTable == null || attrTable.isEmpty() || index >= attrTable.size()) {
            return;
        }
        LinkedHashMap<String, Object> attrRow = attrTable.get(index);
        for (int i = 1; i < featureType.getAttributeCount(); i++) {
            String fieldName = featureType.getDescriptor(i).getLocalName();
            Object fieldValue = attrRow.getOrDefault(fieldName, null);
            builder.add(fieldValue);
        }
    }

    public static Class<? extends Geometry> getGeometryClass(Layer layer) {
        switch (layer.GetGeomType()) {
            case 1:
                return org.locationtech.jts.geom.Point.class;
            case 2:
                return org.locationtech.jts.geom.LineString.class;
            case 3:
                return org.locationtech.jts.geom.Polygon.class;
            case 4:
                return org.locationtech.jts.geom.MultiPoint.class;
            case 5:
                return org.locationtech.jts.geom.MultiLineString.class;
            case 6:
                return org.locationtech.jts.geom.MultiPolygon.class;
            default:
                return org.locationtech.jts.geom.MultiPolygon.class;
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

        builder.add("the_geom", getGeometryClass(layer));
        return builder.buildFeatureType();
    }

    private static CoordinateReferenceSystem getDefaultCRS() {
        try {
            return CRS.decode("EPSG:3857", true);
        } catch (FactoryException e) {
            System.err.println("获取默认EPSG:3857坐标系失败: " + e.getMessage());
            return null;
        }
    }

    public static List<LinkedHashMap<String, Object>> readShapefile(File file) {
        List<LinkedHashMap<String, Object>> featureList = new ArrayList<>();
        try {
            // 创建数据存储
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("url", file.toURI().toString());
            DataStore dataStore = DataStoreFinder.getDataStore(map);
            
            // 设置字符编码为UTF-8，解决中文乱码问题
            if (dataStore instanceof ShapefileDataStore) {
                ((ShapefileDataStore) dataStore).setCharset(Charset.forName("UTF-8"));
            }

            // 获取要素源
            String typeName = dataStore.getTypeNames()[0];
            SimpleFeatureCollection collection = dataStore.getFeatureSource(typeName).getFeatures();

            // 遍历特征
            try (SimpleFeatureIterator iterator = collection.features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();

                    LinkedHashMap<String, Object> attributeMap = new LinkedHashMap<>();

                    for (Property property : feature.getProperties()) {
                        String name = String.valueOf(property.getName());
                        Object value = property.getValue();
                        if (name.equals("the_geom")){
                            continue;
                        }
                        attributeMap.put(name, value);
                    }
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
        String prefix = strs[0];

        for (int i = 1; i < strs.length; i++) {
            while (strs[i].indexOf(prefix) != 0) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) {
                    return "";
                }
            }
        }
        return prefix;
    }
}
