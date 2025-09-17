package com.encryptrdSoftware.hnust.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class JDBCUtils {

    /**
     * 建立数据库连接的方法
     * 通过读取配置文件中的数据库连接信息，创建并返回一个数据库连接对象
     *
     * @return 返回一个数据库连接对象
     * @throws Exception 如果连接失败，则抛出异常
     */
    public static Connection Connect() throws Exception {

        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jdbc.properties");
        if (resourceAsStream == null) {
            throw new FileNotFoundException("jdbc.properties 文件未找到，请确认文件位置是否正确！");
        }
        Properties properties = new Properties();
        properties.load(resourceAsStream);
        resourceAsStream.close();  // 关闭输入流
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");
        String url = properties.getProperty("url");
        String driverClass = properties.getProperty("driverClass");
        Class.forName(driverClass);
        Connection connection= DriverManager.getConnection(url,user,password);
        return connection;
    }

    /**
     * 关闭数据库连接和资源
     * 该方法用于在操作数据库后关闭连接、PreparedStatement和ResultSet对象
     *
     * @param c  数据库连接对象，可能为null
     * @param ps PreparedStatement对象，用于执行SQL语句，可能为null
     */
    public static void disConnect(Connection c, PreparedStatement ps,ResultSet resultSet){
        //关闭连接
        if (c!=null) {
            try {
                c.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        if (ps!=null){
            try {
                ps.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        if (resultSet!=null){
            try {
                resultSet.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
