package com.encryptrdSoftware.hnust.dao;

import com.encryptrdSoftware.hnust.model.FileData;
import com.encryptrdSoftware.hnust.util.JDBCUtils;

import java.util.List;

public class FileDAO {
    private BaseDAO dao = new BaseDAO();

    // 添加文件
    public boolean addFile(int userId, FileData fileData) {
        String sql = "insert into files(user_id, file_name,data) values(?,?,?)";
        try {
            return dao.operate(JDBCUtils.Connect(), sql, userId, fileData.getFileName(), fileData.getData()) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // 删除文件
    public boolean deleteFile(int userId) {
        String sql = "delete from files where user_id=?";
        try {
            return dao.operate(JDBCUtils.Connect(), sql, userId) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //根据用户ID获取文件
    public List<FileData> getFilesByUserId(int userId) {
        String sql = "select user_id,file_name,data,is_encrypted,is_watermarked from files where user_id=?";
        try {
            return dao.querys(JDBCUtils.Connect(), FileData.class, sql, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 根据用户ID获取文件名
    public List<FileData> getFileNameByUserId(int userId) {
        String sql = "select file_name from files where user_id=?";
        try {
            return dao.querys(JDBCUtils.Connect(), FileData.class, sql, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean updateFileByFilename(byte[] bytes,String filename) {
        String sql = "update files set data=? where file_name=?";
        try {
            return dao.operate(JDBCUtils.Connect(), sql, bytes,filename)>0;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public FileData getFileByfileName(String filename) {
        String sql = "select file_name,data from files where file_name=?";
        try {
            return dao.select(JDBCUtils.Connect(), FileData.class, sql,filename);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public FileData isEncrypted(String filename,int userId) {
        List<FileData> data = getFileNameByUserId(userId);
        for (FileData file:data) {
            System.out.println(file.getFileName());
            if (file.getFileName().equals(filename)){
                String sql = "select is_encrypted from files where file_name=?";
                try {
                    return dao.select(JDBCUtils.Connect(), FileData.class, sql, filename);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }else{
                return null;
            }
        }
        return null;
    }
    public Boolean updateEncrypted(int isEncrypted,String filename) {
        String sql = "update files set is_encrypted=? where file_name=?";
        try {
            return dao.operate(JDBCUtils.Connect(), sql, isEncrypted,filename) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public FileData isWatermarked(String filename,int userId) {
        List<FileData> data = getFileNameByUserId(userId);
        for (FileData file:data) {
            if (file.getFileName().equals(filename)){
                String sql = "select is_watermarked from files where file_name=?";
                try {
                    return dao.select(JDBCUtils.Connect(), FileData.class, sql, filename);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
            }
        }
        }
        return null;
    }

    public Boolean updateWatermarked(int isWatermarked,String filename) {
        String sql = "update files set is_watermarked=? where file_name=?";
        try {
            return dao.operate(JDBCUtils.Connect(), sql, isWatermarked,filename) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
