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
        if (!dir.exists()) {
            dir.mkdirs();
        }
        boolean hasShp = false;
        boolean hasShx = false;
        boolean hasDbf = false;
        ZipInputStream zipInputStream = null;

        try {
            zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
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

            zipInputStream.close(); // 关闭当前流
            zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath)); // 重新打开流

            while ((entry = zipInputStream.getNextEntry()) != null) {
                String filePath = destDirectory + File.separator + entry.getName();
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
            throw e;
        } finally {
            if (zipInputStream != null) {
                zipInputStream.close();
            }
        }
    }
}
