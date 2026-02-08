package com.encryptrdSoftware.hnust.model;

public class Point implements Shape {
    double x;
    double y;
    int fid;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public Point(double x, double y, int fid) {
        this.x = x;
        this.y = y;
        this.fid = fid;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

