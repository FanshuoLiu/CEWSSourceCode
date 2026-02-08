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
import java.util.stream.Collectors;

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
    /**
     * 对SHP属性表按key筛选（含"市"或"fclass"）+ 去重，返回键值对集合
     * @param attrTable 原始属性表（List<LinkedHashMap<String, Object>>）
     * @return Map<String, Set<Object>> - key：含"市"或"fclass"的属性名；value：该key去重后的value集合（LinkedHashSet保持顺序）
     */
    public static Map<String, Set<Object>> deduplicateAttrToMap(List<LinkedHashMap<String, Object>> attrTable) {
        // 初始化返回的Map（LinkedHashMap保持key的插入顺序）
        Map<String, Set<Object>> keyUniqueValues = new LinkedHashMap<>();

        // 1. 边界校验：属性表为空直接返回空Map
        if (attrTable == null || attrTable.isEmpty()) {
            return keyUniqueValues;
        }

        // 2. 遍历属性表的每一行，筛选+收集去重value
        for (LinkedHashMap<String, Object> attrRow : attrTable) {
            if (attrRow == null || attrRow.isEmpty()) {
                continue; // 跳过空行
            }

            for (Map.Entry<String, Object> entry : attrRow.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 筛选条件：key含"市"或"fclass" + value非null
                if (value == null || (!key.contains("市") && !key.contains("fclass"))) {
                    continue;
                }

                // 初始化key对应的Set（不存在则新建LinkedHashSet，保持value插入顺序）
                keyUniqueValues.computeIfAbsent(key, k -> new LinkedHashSet<>());
                // 添加value到Set（自动去重）
                keyUniqueValues.get(key).add(value);
            }
        }

        // 3. 返回最终的键值对集合（无符合条件的key则返回空Map）
        return keyUniqueValues;
    }

    // 核心方法：获取指定属性名的“属性值-要素索引”映射（修复getPropertyDescriptors错误）
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

            // 核心修复：用getDescriptors()替代getPropertyDescriptors()（跨版本兼容）
            SimpleFeatureType featureType = featureSource.getSchema();
            boolean isAttrExist = false;
            // 遍历所有属性描述符（标准方法：getDescriptors()）
            List<PropertyDescriptor> descriptors = (List<PropertyDescriptor>) featureType.getDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                String propName = descriptor.getName().getLocalPart();
                if (propName.equals(attrName)) {
                    isAttrExist = true;
                    break;
                }
            }
            // 如果属性名全局不存在，直接提示并返回
            if (!isAttrExist) {
                System.err.printf("SHP文件中不存在属性名：%s%n", attrName);
                dataStore.dispose();
                return attrValueIndexesMap;
            }

            // 遍历要素，记录索引和属性值
            try (SimpleFeatureIterator iterator = collection.features()) {
                int index = 0; // 要素的索引（从0开始）
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    // 获取指定属性的值（属性名已全局校验）
                    Object attrValue = feature.getAttribute(attrName);

                    // 传统方式：判断属性值是否已存在于Map中（无Lambda）
                    List<Integer> indexList;
                    if (attrValueIndexesMap.containsKey(attrValue)) {
                        indexList = attrValueIndexesMap.get(attrValue);
                    } else {
                        indexList = new ArrayList<Integer>();
                        attrValueIndexesMap.put(attrValue, indexList);
                    }
                    // 将当前索引添加到列表
                    indexList.add(index);
                    index++; // 索引自增
                }
            }
            dataStore.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return attrValueIndexesMap;
    }

    // 便捷方法：获取指定属性名+属性值对应的所有索引（无Lambda）
    public static List<Integer> getIndexesByAttrValue(File file, String attrName, Object attrValue) {
        Map<Object, List<Integer>> map = getAttrValueToIndexes(file, attrName);
        if (map.containsKey(attrValue)) {
            return map.get(attrValue);
        } else {
            return new ArrayList<Integer>();
        }
    }
// ========== 新增核心方法：根据索引列表提取目标集合的对应元素 ==========
    /**
     * 根据索引列表，从目标集合中提取对应位置的元素，返回新集合
     * @param targetList 目标集合（比如readShapefile返回的属性表集合）
     * @param indexes 索引列表（比如getIndexesByAttrValue返回的列表）
     * @param <T> 目标集合的元素类型（如LinkedHashMap<String, Object>）
     * @return 包含目标集合中对应索引元素的新集合（顺序与索引列表一致）
     */
    public static <T> List<T> getElementsByIndexes(List<T> targetList, List<Integer> indexes) {
        List<T> resultList = new ArrayList<T>();
        // 校验目标集合为空的情况
        if (targetList == null || targetList.isEmpty()) {
            System.err.println("目标集合为空，无法提取元素！");
            return resultList;
        }
        // 校验索引列表为空的情况
        if (indexes == null || indexes.isEmpty()) {
            System.err.println("索引列表为空，无元素可提取！");
            return resultList;
        }

        // 遍历索引列表，提取对应元素（无Lambda，传统循环）
        int targetListSize = targetList.size();
        for (int i = 0; i < indexes.size(); i++) {
            Integer index = indexes.get(i);
            // 校验索引合法性：跳过负数、大于等于集合长度的索引
            if (index == null) {
                System.err.printf("第%d个索引为null，跳过！%n", i);
                continue;
            }
            if (index < 0 || index >= targetListSize) {
                System.err.printf("索引%d越界（目标集合长度：%d），跳过！%n", index, targetListSize);
                continue;
            }
            // 提取对应索引的元素并添加到结果集合
            T element = targetList.get(index);
            resultList.add(element);
        }
        return resultList;
    }

}
