package com.encryptrdSoftware.hnust.dao;

import com.encryptrdSoftware.hnust.model.FileData;
import com.encryptrdSoftware.hnust.model.User;
import com.encryptrdSoftware.hnust.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UserDAO {
    private BaseDAO dao=new BaseDAO();
    public boolean register(User user) {
        String sql = "insert into users(username,password,email,files_count) values(?,?,?,?)";
        try {
            if (!isExistingUser(user.getUsername())){
            return dao.operate(JDBCUtils.Connect(), sql, user.getUsername(), user.getPassword(), user.getEmail(),user.getFilesCount()) > 0;
            }else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public User loginUser(String username, String password) {
        String sql = "select * from users where username=? and password=?";
       try {
           return dao.select(JDBCUtils.Connect(), User.class, sql, username, password);
       }catch (Exception e){
           e.printStackTrace();
           return null;
       }
    }

    public boolean isExistingUser(String username) {
        String sql = "select * from users where username=?";
        try {
            return dao.select(JDBCUtils.Connect(), User.class, sql, username) != null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateFileCount(Integer userId) {
        String sql = "update users set files_count = files_count + 1 WHERE id = ?";
        try{
            dao.operate(JDBCUtils.Connect(), sql, userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public List<FileData> queryEncryptedFilesByUserId(Integer userId) {
        String sql = "select * from files f join users u on f.user_id=u.id where u.id=?";
        try{
            return dao.querys(JDBCUtils.Connect(), FileData.class,sql,userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
