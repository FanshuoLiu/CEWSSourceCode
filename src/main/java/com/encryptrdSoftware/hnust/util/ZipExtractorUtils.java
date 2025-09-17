package com.encryptrdSoftware.hnust.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractorUtils {

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File dir = new File(destDirectory);
        // 创建目标目录
        if (!dir.exists()) {
            dir.mkdirs();
        }

        boolean hasShp = false;  // 标记是否包含 .shp 文件
        boolean hasShx = false;  // 标记是否包含 .shx 文件
        boolean hasDbf = false;  // 标记是否包含 .dbf 文件
        ZipInputStream zipInputStream = null;

        try {
            zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry entry;

            // 首次遍历以检查所有必要的文件
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                // 检查文件扩展名并设置标记
                if (entryName.endsWith(".shp")) {
                    hasShp = true;
                } else if (entryName.endsWith(".shx")) {
                    hasShx = true;
                } else if (entryName.endsWith(".dbf")) {
                    hasDbf = true;
                }
                zipInputStream.closeEntry(); // 关闭当前条目
            }

            // 检查是否含有所有三种文件
            if (!hasShp || !hasShx || !hasDbf) {
                 new File(zipFilePath).delete();
                throw new IOException("上传失败：必须包含 .shp、.shx 和 .dbf 文件。");
            }

            // 重新读取流以实际解压文件
            zipInputStream.close(); // 关闭当前流
            zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath)); // 重新打开流

            // 开始解压文件
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String filePath = destDirectory + File.separator + entry.getName();
//                System.out.println("解压文件: " + filePath);
                // 如果条目是目录，则创建目录
                if (entry.isDirectory()) {
                    File newDir = new File(filePath);
                    newDir.mkdirs();
                } else {
                    // 如果条目是文件，则写入文件
                    try (FileOutputStream fos = new FileOutputStream(filePath)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zipInputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            throw e; // 继续抛出异常以便上层调用处理
        } finally {
            if (zipInputStream != null) {
                zipInputStream.close(); // 确保在 finally 块中关闭流
            }
        }
    }
}
