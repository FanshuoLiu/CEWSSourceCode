package com.encryptrdSoftware.hnust.model;

import java.util.List;

public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private List<FileData> files; // 存储用户上传的文件
    private int filesCount; // 可选：用于存储文件数量

    // 构造函数
    public User(int id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.filesCount = 0; // 默认为0
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.filesCount = 0; // 默认为0
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public User() {
        this.filesCount = 0; // 默认为0
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email; // 返回邮箱
    }

    public void setEmail(String email) {
        this.email = email; // 设置邮箱
    }

    public List<FileData> getFiles() {
        return files; // 返回文件列表
    }

    public void setFiles(List<FileData> files) {
        this.files = files; // 设置文件列表
        this.filesCount = (files != null) ? files.size() : 0; // 计算文件数量
    }

    public int getFilesCount() {
        return filesCount; // 返回文件数量
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", filesCount=" + filesCount + // 显示文件数量
                '}';
    }
}
