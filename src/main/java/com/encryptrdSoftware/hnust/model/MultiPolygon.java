package com.encryptrdSoftware.hnust.model;

import java.util.List;

public class MultiPolygon implements Shape{
    private List<Polygon> polygons;

    public MultiPolygon(List<Polygon> polygons) {
        this.polygons = polygons;
    }
    public void addPolygon(Polygon polygon) {
        this.polygons.add(polygon);
    }
    public List<Polygon> getPolygons() {
        return polygons;
    }
    public static int getAllPoints(List<MultiPolygon> multiPolygons) {
        int num = 0;
        for (MultiPolygon multiPolygon : multiPolygons) {
            num += Polygon.getAllPoints(multiPolygon.getPolygons());
        }
        return num;
    }

    @Override
    public String toString() {
        return "MultiPolygon{" +
                "polygons=" + polygons +
                '}';
    }
}
