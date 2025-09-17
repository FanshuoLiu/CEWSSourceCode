package com.encryptrdSoftware.hnust;

public class Vertex {
    private double x;
    private double y;

    // 构造函数
    public Vertex(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // getter和setter方法
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    // 复制顶点
    public Vertex copy() {
        return new Vertex(this.x, this.y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
