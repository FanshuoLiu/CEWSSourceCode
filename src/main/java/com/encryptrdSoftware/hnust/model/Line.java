package com.encryptrdSoftware.hnust.model;

import java.util.List;

public class Line implements Shape {
    private List<Point> points; // 存储点的列表


    public Line(List<Point> points) {
        this.points = points;
    }

    public Line() {
    }

    public List<Point> getPoints() {
        return points;
    }

    public int getLength(){return points.size();}

    public void addPoint(Point point) {
        points.add(point);
    }

    public static int getAllPoints(List<Line> lines) {
        int sum=0;
        for (int i = 0; i < lines.size(); i++) {
            sum+=lines.get(i).getLength();
        }
        return sum;
    }

    @Override
    public String toString() {
        return "Line{" +
                "points=" + points +
                '}';
    }



}
