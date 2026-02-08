package com.encryptrdSoftware.hnust.util;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import java.io.File;
import java.nio.charset.Charset;
import java.util.*;


public class StringUtils {
    public static String modifyString(String s) {
        String s1 = s.substring(0, s.lastIndexOf("."));
        if (s1.contains("加密")){
            s1 = s1.replace("加密", "");
        }
        if (s1.contains("水印")){
            s1 = s1.replace("水印", "");
        }
        if (Character.isDigit(s1.charAt(s1.length() - 1))){
            s1=s1.substring(0,s1.length()-1);
        }
        return s1;
    }

    public static Map<String, Set<Object>> deduplicateAttrToMap(List<LinkedHashMap<String, Object>> attrTable) {
        // 初始化返回的Map（LinkedHashMap保持key的插入顺序）
        Map<String, Set<Object>> keyUniqueValues = new LinkedHashMap<>();

        if (attrTable == null || attrTable.isEmpty()) {
            return keyUniqueValues;
        }

        for (LinkedHashMap<String, Object> attrRow : attrTable) {
            if (attrRow == null || attrRow.isEmpty()) {
                continue;
            }

            for (Map.Entry<String, Object> entry : attrRow.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value == null || (!key.contains("市") && !key.contains("fclass"))) {
                    continue;
                }

                keyUniqueValues.computeIfAbsent(key, k -> new LinkedHashSet<>());

                keyUniqueValues.get(key).add(value);
            }
        }

        return keyUniqueValues;
    }

    public static Map<Object, List<Integer>> getAttrValueToIndexes(File file, String attrName) {
        Map<Object, List<Integer>> attrValueIndexesMap = new LinkedHashMap<>();
        try {
            // 创建数据存储
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("url", file.toURI().toURL());
            DataStore dataStore = DataStoreFinder.getDataStore(map);

            // 设置UTF-8编码
            if (dataStore instanceof ShapefileDataStore) {
                ((ShapefileDataStore) dataStore).setCharset(Charset.forName("UTF-8"));
            }

            // 校验要素类型
            String[] typeNames = dataStore.getTypeNames();
            if (typeNames == null || typeNames.length == 0) {
                System.err.println("SHP文件无可用的要素类型！");
                dataStore.dispose();
                return attrValueIndexesMap;
            }
            String typeName = typeNames[0];
            SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
            SimpleFeatureCollection collection = featureSource.getFeatures();

            SimpleFeatureType featureType = featureSource.getSchema();
            boolean isAttrExist = false;
            List<PropertyDescriptor> descriptors = (List<PropertyDescriptor>) featureType.getDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                String propName = descriptor.getName().getLocalPart();
                if (propName.equals(attrName)) {
                    isAttrExist = true;
                    break;
                }
            }

            if (!isAttrExist) {
                System.err.printf("SHP文件中不存在属性名：%s%n", attrName);
                dataStore.dispose();
                return attrValueIndexesMap;
            }

            try (SimpleFeatureIterator iterator = collection.features()) {
                int index = 0;
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    Object attrValue = feature.getAttribute(attrName);

                    List<Integer> indexList;
                    if (attrValueIndexesMap.containsKey(attrValue)) {
                        indexList = attrValueIndexesMap.get(attrValue);
                    } else {
                        indexList = new ArrayList<Integer>();
                        attrValueIndexesMap.put(attrValue, indexList);
                    }

                    indexList.add(index);
                    index++;
                }
            }
            dataStore.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return attrValueIndexesMap;
    }

    public static List<Integer> getIndexesByAttrValue(File file, String attrName, Object attrValue) {
        Map<Object, List<Integer>> map = getAttrValueToIndexes(file, attrName);
        if (map.containsKey(attrValue)) {
            return map.get(attrValue);
        } else {
            return new ArrayList<Integer>();
        }
    }

    public static <T> List<T> getElementsByIndexes(List<T> targetList, List<Integer> indexes) {
        List<T> resultList = new ArrayList<T>();
        if (targetList == null || targetList.isEmpty()) {
            System.err.println("目标集合为空，无法提取元素！");
            return resultList;
        }

        if (indexes == null || indexes.isEmpty()) {
            System.err.println("索引列表为空，无元素可提取！");
            return resultList;
        }

        int targetListSize = targetList.size();
        for (int i = 0; i < indexes.size(); i++) {
            Integer index = indexes.get(i);
            if (index == null) {
                System.err.printf("第%d个索引为null，跳过！%n", i);
                continue;
            }
            if (index < 0 || index >= targetListSize) {
                System.err.printf("索引%d越界（目标集合长度：%d），跳过！%n", index, targetListSize);
                continue;
            }
            T element = targetList.get(index);
            resultList.add(element);
        }
        return resultList;
    }

}
