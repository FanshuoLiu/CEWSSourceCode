package com.encryptrdSoftware.hnust.model;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.time.LocalDateTime;
import java.util.Arrays;

public class FileData {
    private int userId;
    private int id;
    private String fileName;
    private byte[] data; // 或者使用 LONGBLOB（在存储时）
    private LocalDateTime createAt;

    private Boolean isEncrypted;

    private Boolean isWatermarked;


    public FileData(int id, String fileName, byte[] data) {
        this.id = id;
        this.fileName = fileName;
        this.data = data;
    }
    public FileData(int userId,int id, String fileName, byte[] data) {
        this.userId=userId;
        this.id = id;
        this.fileName = fileName;
        this.data = data;
    }

    public FileData(int userId, int id, String fileName, byte[] data, LocalDateTime createAt) {
        this.userId = userId;
        this.id = id;
        this.fileName = fileName;
        this.data = data;
        this.createAt = createAt;
    }

    public FileData(int userId, int id, String fileName, byte[] data, LocalDateTime createAt, Boolean isEncrypted, Boolean isWatermarked, Boolean isEncryptedWatermarked) {
        this.userId = userId;
        this.id = id;
        this.fileName = fileName;
        this.data = data;
        this.createAt = createAt;
        this.isEncrypted = isEncrypted;
        this.isWatermarked = isWatermarked;
    }

    public FileData() {
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public Boolean getEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        isEncrypted = encrypted;
    }

    public Boolean getWatermarked() {
        return isWatermarked;
    }

    public void setWatermarked(Boolean watermarked) {
        isWatermarked = watermarked;
    }

    @Override
    public String toString() {
        return "FileData{" +
                "userId=" + userId +
                ", id=" + id +
                ", fileName='" + fileName + '\'' +
                ", createAt=" + createAt +
                ", isEncrypted=" + isEncrypted +
                ", isWatermarked=" + isWatermarked +
                '}';
    }
}
