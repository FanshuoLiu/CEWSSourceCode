package com.encryptrdSoftware.hnust.model;



import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.LinearRing;

import java.util.ArrayList;
import java.util.List;

public class Polygon implements Shape {
    private List<Point> exteriors=new ArrayList<>();
    private List<List<Point>> interiors=new ArrayList<>();

    // 构造函数
    public Polygon(List<Point> exteriors, List<List<Point>> interiors) {
        if (exteriors != null) {
            this.exteriors = new ArrayList<>(exteriors); // 复制传入的外环
        }
        if (interiors != null) {
            this.interiors = new ArrayList<>(interiors); // 复制传入的内环
        }
    }

    public Polygon removeLastPoint() {
        List<Point> newExteriors=exteriors;
        List<List<Point>> newInteriors=interiors;
        if (exteriors.size() >= 1) {
        newExteriors = exteriors.subList(0, exteriors.size() - 1);
        } else if (interiors.size() <= 1) {
        newInteriors = interiors.subList(0, interiors.size() - 1);
        }
        return new Polygon(newExteriors, newInteriors);
    }

    public int getNum() {
        int sum=0;
        if (interiors!=null){
            for (List<Point> list:interiors){
                sum+=list.size();
            }
            return sum+exteriors.size();
        }
        return exteriors.size();
    }
    // 获取外环
    public List<Point> getExteriors() {
        return exteriors;
    }
    //获取内环
    public List<List<Point>> getInteriors() {
        return interiors;
    }
    public static int getAllPoints(List<Polygon> polygons) {
        int sum=0;
        for (int i = 0; i < polygons.size(); i++) {
            sum+=polygons.get(i).getNum();
        }
        return sum;
    }

    @Override
    public String toString() {
        return "Polygon{" +
                "exteriors=" + exteriors +
                ", interiors=" + interiors +
                '}';
    }
}