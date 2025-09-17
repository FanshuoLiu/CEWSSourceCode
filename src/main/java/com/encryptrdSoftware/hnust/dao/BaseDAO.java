package com.encryptrdSoftware.hnust.dao;

import com.encryptrdSoftware.hnust.util.JDBCUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class BaseDAO {
    /**
     * 执行SQL操作的方法，考虑事务
     *
     * @param connection 数据库连接对象，用于获取PreparedStatement对象
     * @param sql 预编译的SQL语句，占位符使用"?"表示
     * @param arr SQL语句中占位符对应的参数数组
     * @return 执行SQL语句影响的行数，如果发生异常则返回0
     */
    public int operate(Connection connection, String sql, Object...arr){
        PreparedStatement preparedStatement=null;
        try {
            preparedStatement=connection.prepareStatement(sql);
            for (int i=0;i<arr.length;i++){
                preparedStatement.setObject(i+1,arr[i]);
            }
            return preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            JDBCUtils.disConnect(null,preparedStatement,null);
        }
        return 0;
    }

    /**
     * 使用Java中的JDBC技术来查询数据库，并将结果映射为指定类型的对象，查询一条记录，考虑到事务
     * 该方法通过反射机制动态地创建指定类的对象，并为其属性赋值，从而实现查询结果的封装
     *
     * @param <A>  泛型参数，指定要返回的对象类型
     * @param conn 数据库连接对象，用于执行SQL语句
     * @param clazz 指定的返回类型A的Class对象，用于反射创建A类型的实例
     * @param sql 预编译的SQL语句，用于查询数据库
     * @param args SQL语句中占位符对应的参数数组
     * @return 返回查询结果封装的对象，如果没有查询到数据或者发生异常，则返回null
     */
    public <A> A select(Connection conn, Class<A> clazz, String sql, Object... args) {
        PreparedStatement preparedStatement = null;
        ResultSet set = null;
        try {
            preparedStatement = conn.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            // 执行，获取结果集
            set = preparedStatement.executeQuery();
            // 获取结果集的元数据
            ResultSetMetaData metaData = set.getMetaData();
            // 获取列数
            int columnCount = metaData.getColumnCount();
            if (set.next()) {
                A a = clazz.getDeclaredConstructor().newInstance();
                for (int i = 0; i < columnCount; i++) {
                    // 通过 ResultSet 获取列值
                    Object columnValue = set.getObject(i + 1);
                    // 获取列的别名
                    String columnLabel = metaData.getColumnLabel(i + 1);
                    columnLabel=convertToCamelCase(columnLabel);

                    // 反射获取指定的列名和列值
                    Field field = clazz.getDeclaredField(columnLabel);
                    // 可对 private 变量进行访问
                    field.setAccessible(true);
                    field.set(a, columnValue);
                }
                return a;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.disConnect(null, preparedStatement, set);
        }
        return null;
    }

    /**
     * 使用指定的Connection和SQL语句，查询多条数据，考虑事务
     * 该方法使用PreparedStatement安全执行查询，并映射到指定类的对象集合
     *
     * @param connection 数据库连接对象，用于执行SQL查询
     * @param clazz 要映射查询结果的类类型，用于创建返回的对象
     * @param sql 要执行的SQL查询语句
     * @param args SQL查询的参数，根据参数个数可变
     * @param <T> 泛型标记，表示查询结果的类型
     * @return 查询结果的List集合，元素类型为泛型T
     */
    public static <T> List<T> querys(Connection connection, Class<T> clazz, String sql, Object...args){
        PreparedStatement preparedStatement = null;
        ResultSet set = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            //执行，获取结果集
            set = preparedStatement.executeQuery();
            //获取结果集的元数据
            ResultSetMetaData metaData = set.getMetaData();
            //获取列数
            int columnCount = metaData.getColumnCount();
            //创建集合对象
            ArrayList<T> list = new ArrayList<>();
            while (set.next()) {
                T t = clazz.getDeclaredConstructor().newInstance();
                for (int i = 0; i < columnCount; i++) {
                    //通过ResultSet获取列值
                    Object columnValue = set.getObject(i + 1);
                    //获取列的别名,没有别名也可使用
                    String columnLabel = metaData.getColumnLabel(i + 1);
                    columnLabel=convertToCamelCase(columnLabel);
                    //反射获取指定的列名和列值
                    Field field = clazz.getDeclaredField(columnLabel);
                    field.setAccessible(true);
                    field.set(t, columnValue);
                }
                list.add(t);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            JDBCUtils.disConnect(null,preparedStatement,set);
        }
        return null;
    }

    private static String convertToCamelCase(String columnLabel) {
        String[] parts = columnLabel.split("_");
        StringBuilder camelCaseString = new StringBuilder(parts[0].toLowerCase());

        for (int i = 1; i < parts.length; i++) {
            camelCaseString.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1).toLowerCase());
        }

        return camelCaseString.toString();
    }
}
