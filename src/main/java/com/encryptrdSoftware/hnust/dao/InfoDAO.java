package com.encryptrdSoftware.hnust.dao;

import javax.sound.midi.MidiDevice;
import java.sql.Connection;
import java.util.List;

public interface InfoDAO {
    //将info对象添加到数据库
    void insert(Connection conn, MidiDevice.Info info);
    //通过名字来删除表中的一条记录
    void deleteByName(Connection conn,String name);
    //针对内存中的info对象，修改数据表中的指定记录
    void update(Connection conn, MidiDevice.Info info);
    //通过名字来查询对应的info对象
    void getInfoByName(Connection conn,String name);
    //查询表中所有记录构成的集合
    List<MidiDevice.Info> getAll(Connection conn);
    //获取表中的记录数量
    long getCount(Connection conn);
    //还可以声明查询salary的最大值的名字等等
}
