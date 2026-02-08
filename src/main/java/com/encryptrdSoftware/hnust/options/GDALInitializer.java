package com.encryptrdSoftware.hnust.options;

import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

public class GDALInitializer implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            // 初始化GDAL（java.library.path已在Tomcat的setenv.bat中设置）
            gdal.AllRegister();
            ogr.RegisterAll();
            System.out.println("GDAL库初始化成功");
        } catch (Exception e) {
            System.err.println("GDAL库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 清理资源
        try {
            gdal.GDALDestroyDriverManager();
        } catch (Exception e) {
            System.err.println("GDAL资源清理失败: " + e.getMessage());
        }
    }
}
