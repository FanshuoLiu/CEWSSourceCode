package com.encryptrdSoftware.hnust.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Coordinate implements Serializable {
    private static final long serialVersionUID = 1L;
    public static Point polarPoint;
    public static Point polarAxisPoint;

    public static double axisAngle;
    public static Random random = new Random();

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

    //极坐标转直角
    public static List<Point> toCartesian(List<Point> list){
        List<Point> list1=new ArrayList<>();
        for (Point p:list) {
            double radius=p.getX();
            double angle=p.getY();
            double x=(radius*Math.cos(Math.toRadians(angle+axisAngle)))+polarPoint.getX();
            double y=(radius*Math.sin(Math.toRadians(angle+axisAngle)))+polarPoint.getY();
            list1.add(new Point(x,y));
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

    //拆分集合
//    public static <T> List<List<T>> splitCollection(List<T> originalCollection) {
//        // 创建两个空列表，用于存储划分后的结果
//        List<T> setA = new ArrayList<>();
//        List<T> setB = new ArrayList<>();
//
//        // 打乱原集合元素顺序
//        Collections.shuffle(originalCollection);
//
//        int index = 0;
//        for (T element : originalCollection) {
//            if (index % 2 == 0) {
//                setA.add(element);
//            } else {
//                setB.add(element);
//            }
//            index++;
//        }
//        List<List<T>> result = new ArrayList<>();
//        result.add(setA);
//        result.add(setB);
//        return result;
//    }



    public static Coordinate initCoordinate(List<Point> points){
        Domain.adaptationFactor =0.1;
        Domain.Qa = 1;
        polarPoint = calculateGeometricCenter(points);
        List<Point> points1=new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            points1.add(points.get(random.nextInt(points.size())));
        }
        polarAxisPoint = Coordinate.calculateGeometricCenter(points1);
        System.out.println("极点和极轴点为:" + polarPoint + " " + polarAxisPoint);
//        Domain.Qr = Domain.adaptationFactor * Math.sqrt(Math.pow(polarAxisPoint.getX() - polarPoint.getX(), 2) + Math.pow(polarAxisPoint.getY() - polarPoint.getY(), 2));
        System.out.println("QR:"+Domain.Qr);
        return new Coordinate(polarPoint, polarAxisPoint);
    }
}

