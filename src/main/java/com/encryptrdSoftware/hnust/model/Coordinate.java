package com.encryptrdSoftware.hnust.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Coordinate implements Serializable {
    public static Point polarPoint=null;
    public static Point polarAxisPoint=null;

    public static double axisAngle=0.0;
    public static Random random = new Random();

    public static Coordinate coordinate=new Coordinate(null,null);

    public Coordinate(Point polarPoint, Point polarAxisPoint) {
        this.polarPoint = polarPoint;
        this.polarAxisPoint = polarAxisPoint;
    }

    public List<Point> calculatePolarCoordinates(List<Point> points){
        List<Point> list=new ArrayList<>();
        axisAngle=Math.toDegrees(Math.atan2(polarAxisPoint.getY()-polarPoint.getY(), polarAxisPoint.getX()-polarPoint.getX()));
        for (Object p:points) {
            Point point=(Point) p;
            double radius=Math.sqrt(Math.pow(point.getX()-polarPoint.getX(),2)+Math.pow(point.getY()-polarPoint.getY(),2));
            double angle=Math.toDegrees(Math.atan2(point.getY()-polarPoint.getY(),point.getX()-polarPoint.getX()))-axisAngle;
            if (angle<0){
                angle=360+angle;
            }
            list.add(new Point(radius/Domain.Qr,angle/Domain.Qa));
        }
        return list;
    }

    //直角转极坐标
    public static List<Point> toPolarPoints(List<Point> list){
        List<Point> list1=new ArrayList<>();
        for (int i=0;i<list.size();i++) {
            double radius=Math.sqrt(Math.pow(list.get(i).getX(),2)+Math.pow(list.get(i).getY(),2));
            double angle=Math.toDegrees(Math.atan2(list.get(i).getY(),list.get(i).getX()));
            if(angle<0){
                angle+=360;
            }
            list1.add(new Point(radius,angle));
        }

        return list1;
    }

    //还原直角坐标
    public static List<Point> recoverCartesian(List<Point> polarPoints){
        List<Point> cartesianPoints = new ArrayList<>();
        for (Point p : polarPoints) {
            double radius = p.getX() * Domain.Qr;
            double angle = p.getY() * Domain.Qa;
            double radians = Math.toRadians(angle + axisAngle);
            double x = radius * Math.cos(radians) + polarPoint.getX();
            double y = radius * Math.sin(radians) + polarPoint.getY();
            cartesianPoints.add(new Point(x, y));
        }
        return cartesianPoints;
    }

    // 计算点集合的平均中心
    public static Point calculateGeometricCenter(List<Point> points) {
        if (points.isEmpty()) {
            return null; // 或者返回一个特定的标识
        }
        double sumX = 0.0;
        double sumY = 0.0;
        for (Point p : points) {
            if (p==null){
                p=new Point(0,0);
            }
            sumX += p.getX();
            sumY += p.getY();
        }
        double centerX = (sumX / points.size());
        double centerY = (sumY / points.size());
        return new Point(centerX, centerY);
    }

    public static Coordinate initCoordinate(List<Point> points){
        if (Domain.btn=="加密"){
            Domain.adaptationFactor =0.01;
            Domain.Qa = 1;
        }else {
            Domain.adaptationFactor =0.00001;
            Domain.Qa = 0.000001;
        }
        List<Point> points1=new ArrayList<>();
        List<Point> points2=new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            if (i%2==0){
            points1.add(points.get(random.nextInt(points.size())));
            }else {
            points2.add(points.get(random.nextInt(points.size())));
            }
        }
        polarPoint = calculateGeometricCenter(points2);
        polarAxisPoint = calculateGeometricCenter(points1);
        System.out.println("极点和极轴点为:" + polarPoint + " " + polarAxisPoint);
        Domain.Qr = Domain.adaptationFactor * Math.sqrt(Math.pow(polarAxisPoint.getX() - polarPoint.getX(), 2) + Math.pow(polarAxisPoint.getY() - polarPoint.getY(), 2));
        System.out.println("QR:"+Domain.Qr);
        coordinate=new Coordinate(polarPoint, polarAxisPoint);
        return coordinate;
    }
}

