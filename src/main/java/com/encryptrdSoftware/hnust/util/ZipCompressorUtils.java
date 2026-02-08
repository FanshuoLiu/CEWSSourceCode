package com.encryptrdSoftware.hnust.util;

import com.encryptrdSoftware.hnust.controller.UploadServlet;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCompressorUtils {
    public static String zipFiles(List<String> sourceFilePaths, String zipFilePath) {
        if (sourceFilePaths == null || sourceFilePaths.isEmpty()) {
            System.err.println("No files to zip.");
            return null;
        }
        try (FileOutputStream fos = new FileOutputStream(zipFilePath, false);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (String sourceFilePath : sourceFilePaths) {
                File fileToZip = new File(sourceFilePath);
                if (!fileToZip.exists()) {
                    System.err.println("File not found: " + sourceFilePath);
                    continue;
                }
                try (FileInputStream fis = new FileInputStream(fileToZip)) {
                    ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                    System.out.println("Added file: " + fileToZip.getName());
                } catch (IOException e) {
                    System.err.println("Failed to add file: " + sourceFilePath);
                    e.printStackTrace();
                }
            }
            System.out.println("Files compressed successfully to: " + zipFilePath);
            return new File(zipFilePath).getName();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Map<String,List<File>> getPackage() {
        // 指定要检查的目录
        File directory = new File(UploadServlet.Path);
        // 用于存储文件名（不包括扩展名）及对应的File集合
        Map<String, List<File>> groupedFiles = new HashMap<>();

        if (directory.isDirectory()) {
            // 遍历目录中的文件
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 检查文件是否为常规文件
                    if (file.isFile()&&!file.getName().contains(".zip")) {
                        String baseName = getBaseName(file.getName());
                        // 将路径添加到对应文件名的列表中
                        if (baseName!=null){
                            groupedFiles.computeIfAbsent(baseName, k -> new ArrayList<>()).add(file);
                        }
                    }
                }
            }
        } else {
            System.out.println("指定路径不是一个目录");
        }
        return groupedFiles;
    }

    private static String getBaseName(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');

        // 如果没有扩展名，返回原始名称
        if (lastIndexOfDot == -1) {
            return fileName;
        }

        int secondLastIndexOfDot = fileName.lastIndexOf('.', lastIndexOfDot - 1);

        if (secondLastIndexOfDot == -1) {
            return fileName.substring(0, lastIndexOfDot);
        }

        return fileName.substring(0, secondLastIndexOfDot);
    }
}