package com.encryptrdSoftware.hnust.model;

import java.util.ArrayList;
import java.util.List;

public class MultiPoint implements Shape {
    private List<Point> points;

    public MultiPoint() {
        points = new ArrayList<>();
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public List<Point> getPoints() {
        return points;
    }

    public int getNum() {
        return points.size();
    }

    @Override
    public String toString() {
        return "MultiPoint{" +
                "points=" + points +
                '}';
    }
}
