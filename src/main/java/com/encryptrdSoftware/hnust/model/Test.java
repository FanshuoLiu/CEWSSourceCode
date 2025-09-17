//package com.encryptrdSoftware.hnust.model;
//
//import com.encryptrdSoftware.hnust.util.SecretUtils;
//import org.gdal.ogr.Layer;
//import org.gdal.ogr.ogr;
//
//import java.io.File;
//import java.io.IOException;
//import java.math.BigInteger;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//public class Test {
//    static int k=0;
//    //点的个数
//    public static long n=0;
//    //极径的随机份额
//    static BigInteger randomRadiusBigInteger;
//    //极角的随机份额
//    static BigInteger randomAngleBigInteger;
//
//
//    private static BigInteger radiusSecret=null;
//    private static BigInteger angleSecret=null;
//    static BigInteger prime=null;
//
//    static Point polarPoint;
//    static Point polarAxisPoint;
//
//    static{
//        ogr.RegisterAll();
//    }
//    //存放angleShares的集合
//    static List<List<BigInteger>> angleSharesList=new ArrayList<>();
//    //存放索引的集合
//    static List<Integer> indices=new ArrayList<>();
//    static List<BigInteger> radiusShares=new ArrayList<>();
//    static List<BigInteger> angleShares=new ArrayList<>();
//    //加密后的极坐标密文
//    static List<Point> polarCoordinateShares = new ArrayList<>();
//    //加密域
//    static List<encryptedDomain> encryptedDomains=new ArrayList<>();
//    //水印域
//    static List<watermarkDomain> watermarkDomains=new ArrayList<>();
//    //密文状态下含水印的极坐标
//    static List<Point> watermarkedPolarCoordinate=new ArrayList<>();
//    public static void main(String[] args) throws IOException {
//        //点集合
//        List<Point> points = new ArrayList<>();
//        //加密后的极径份额
//        List<Double> encryptedRadiusShares=new ArrayList<>();
//        //加密后的极角份额
//        List<Double> encryptedAngleShares=new ArrayList<>();
//        //加密后的直角坐标份额
//        List<Point> encryptedShares=new ArrayList<>();
//        //加密后的极径加密域
//        List<Integer> encryptedRadiusDomains = new ArrayList<>();
//        //加密后的极角加密域
//        List<Integer> encryptedAngleDomains = new ArrayList<>();
//        //加密后的极径水印域
//        List<Double> watermarkingRadiusDomains = new ArrayList<>();
//        //加密后的极角水印域
//        List<Double> watermarkingAngleDomains = new ArrayList<>();
//        //嵌入水印后的极径水印特征
//        List<Double> watermarkedRadiusDomains=new ArrayList<>();
//        //嵌入水印后的极角水印特征
//        List<Double> watermarkedAngleDomains=new ArrayList<>();
//        //密文状态下含水印的直角坐标
//        List<Point> watermarkedCoordinate=new ArrayList<>();
//        //密文状态下含水印的加密域
//        List<encryptedDomain> encryptedWatermarkedEncryptedDomains=new ArrayList();
//        //密文状态下含水印的水印域
//        List<watermarkDomain> encryptedWatermarkedWatermarkDomains=new ArrayList();
//        //极径水印
//        List<Double> radiusWatermark=new ArrayList();
//        //极角水印
//        List<Double> angleWatermark=new ArrayList();
//        //恢复的水印域极径
//        List<Double> recoverWatermarkedRadius=new ArrayList();
//        //复恢复的水印域极角
//        List<Double> recoverWatermarkedAngle=new ArrayList();
//        //嵌入水印前的极径
//        List<Point> recoverPolarPoints=new ArrayList();
//        //含水印的明文极径
//        List<Double> watermarkedDecryptedRadiusShares=new ArrayList();
//        //含水印的明文极角
//        List<Double> watermarkedDecryptedAngleShares=new ArrayList();
//        //含水印的明文极径极角加密域
//        List<encryptedDomain> watermarkedPlaintextEncryptedDomain=new ArrayList<>();
//        //含水印的明文极径极角水印域
//        List<watermarkDomain> watermarkedPlaintextWatermarkingDomain=new ArrayList<>();
//        //含水印的明文极径水印域提取的水印
//        List<Double> plaintextRadiusWatermark=new ArrayList<>();
//        //含水印的明文极角水印域提取的水印
//        List<Double> plaintextAngleWatermark=new ArrayList<>();
//        //恢复极径水印
//        List<Double> plaintextRadiusWatermarkRecover=new ArrayList<>();
//        //恢复极角水印
//        List<Double> plaintextAngleWatermarkRecover=new ArrayList<>();
//
//        String  file="E:\\三江源\\数据\\shp数据\\全国shp数据\\省会城市.shp";
//        //获取layer对象并计算点的个数
//        Layer layer = Domain.getLayer(new File(file));
//        //shp文件转为集合
//        points = Domain.toList(new File(file));
//
//        //选取极点
//        polarPoint = new Point(5,67);
//        //选取极轴点
//        polarAxisPoint = new Point(1000,1000);
//        //构建量化步长
//        Domain.adaptationFactor = 0.001;
//        Domain.Qr = Domain.adaptationFactor * Math.sqrt(Math.pow(polarAxisPoint.getX() - polarPoint.getX(), 2) + Math.pow(polarAxisPoint.getY() - polarPoint.getY(), 2));
//        Domain.Qa = 1;
//
//        n= Domain.calPointsNumber(layer);
//        System.out.println(n);
//
//        System.out.println("极点和极轴点为:"+polarPoint+" "+polarAxisPoint);
//        System.out.println("Qr值："+ Domain.Qr);
//
//        //构建坐标系并计算其他点的极坐标
//        Coordinate coordinate = new Coordinate(polarPoint, polarAxisPoint);
//
//        List<Point> polarPoints = coordinate.calculatePolarCoordinates(points);
//
//        System.out.println("\n");
//        //生成k
//        k=(5+1)/2;
//
//        //构建加密特征域和水印特征域
//        encryptedDomains = Domain.calEncrypt(polarPoints);
//        watermarkDomains = Domain.calWatermark(polarPoints);
//
//        prime = BigInteger.valueOf(10001);
//        System.out.println("大素数p为："+prime);
//
//
//        List<List<Point>> watermarkedSharesList=new ArrayList<>();
//        int b=0,c=5;
//        for (int a = 0; a < n; a++) {
//            System.out.println("直角点："+points.get(a));
//            System.out.println("极点："+polarPoints.get(a));
//            System.out.println(encryptedDomains.get(a));
//            System.out.println(watermarkDomains.get(a));
//            //生成极径秘密
//            radiusSecret = new BigInteger(String.valueOf(encryptedDomains.get(a).getIntegerRadius()));
//            //生成极角秘密
//            angleSecret = new BigInteger(String.valueOf(encryptedDomains.get(a).getIntegerAngle()));
//            // 生成极径多项式系数
//            List<BigInteger> radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, 3, prime);
//            System.out.println(radiusCoefficients);
//            // 生成极角多项式系数
//            List<BigInteger> angleCoefficients = SecretUtils.generatePolynomial(angleSecret, 3, prime);
//            System.out.println(angleCoefficients);
//            radiusShares = SecretUtils.generateShares(radiusCoefficients, 5, prime,randomRadiusBigInteger,a);
//            angleShares = SecretUtils.generateShares(angleCoefficients,5, prime,randomAngleBigInteger,a);
//
//            //随机获取前一个点的一个份额
//            randomRadiusBigInteger=radiusShares.get(new Random().nextInt(5));
//            randomAngleBigInteger=angleShares.get(new Random().nextInt(5));
//            angleSharesList.add(angleShares);
//            //计算加密后的极坐标份额
//            for (int k = 0; k < 5; k++) {
//                double radiusShare=radiusShares.get(k).doubleValue()+watermarkDomains.get(a).getDecimalRadius();
//                radiusShare=radiusShare* Domain.Qr;
//                double angleShare=angleShares.get(k).doubleValue()+watermarkDomains.get(a).getDecimalAngle();
//                angleShare=angleShare* Domain.Qa;
//                polarCoordinateShares.add(new Point(radiusShare,angleShare));
//                encryptedRadiusShares.add(radiusShare);
//                encryptedAngleShares.add(angleShare);
//            }
//            //计算polarCoordinateShares的直角份额
//            encryptedShares= Coordinate.toCartesian(polarCoordinateShares);
//
//            //基于加密极坐标极坐标构建加密特征域和水印特征域
//            for (int i = 0; i <5; i++) {
//                List<Double> list=encryptedRadiusShares.subList(b,c);
//                List<Double> list1=encryptedAngleShares.subList(b,c);
//                int radiusInteger = (int)Math.floor(list.get(i));
//                double radiusDecimal = list.get(i) - radiusInteger;
//                int  angleInteger = (int)Math.floor(list1.get(i));
//                double angleDecimal = list1.get(i) - angleInteger;
//                encryptedRadiusDomains.add(radiusInteger);
//                encryptedAngleDomains.add(angleInteger);
//                watermarkingRadiusDomains.add(radiusDecimal);
//                watermarkingAngleDomains.add(angleDecimal);
//            }
////
////
////
//            System.out.println(randomRadiusBigInteger);
//            System.out.println(randomAngleBigInteger);
//            System.out.println("极径加密份额" + (a + 1) + ": " + radiusShares);
//            System.out.println("极角加密份额" + (a + 1) + ": " + angleShares);
//            System.out.println("加密后的极径份额："+encryptedRadiusShares.subList(b,c));
//            System.out.println("加密后的极角份额："+encryptedAngleShares.subList(b,c));
//            System.out.println("构建加密后份额的加密水印域");
//            System.out.println("加密特征域极径份额encryptedRadiusDomains："+encryptedRadiusDomains.subList(b,c));
//            System.out.println("加密特征域极角份额encryptedAngleDomains："+encryptedAngleDomains.subList(b,c));
//            System.out.println("水印特征域极径份额watermarkingRadiusDomains："+watermarkingRadiusDomains.subList(b,c));
//            System.out.println("水印特征域极角份额watermarkingAngleDomains："+watermarkingAngleDomains.subList(b,c));
//            System.out.println("点"+(a+1)+"的极坐标密文为polarCoordinateShares："+polarCoordinateShares.subList(b,c));
//            System.out.println("点"+(a+1)+"的直角密文为encryptedShares："+encryptedShares.subList(b,c));
//
//            b+=5;
//            c+=5;
//            System.out.println();
//        }
////
//        String path="C:\\Users\\lfs\\Desktop\\test\\省会城市.shp";
//        //创建n个加密文件
//        for (int i = 0; i < 5; i++) {
//            int d=0;
//            // 创建坐标点集合
//            List<Point> coordinatesList = new ArrayList<>();
//            for (int j = 0; j < n; j++) {
//                coordinatesList.add(new Point(encryptedShares.get(d+i).getX(), encryptedShares.get(d+i).getY()));
//                d+=5;
//            }
//        }
//
//
//        //选择用于恢复的部分极径极角密文份额的索引
////        //选取的k个文件
////        List<File> files = new ArrayList<>();
////
////        Set<Integer> uniqueIntegers= Secret.createRandomNumber(k, Secret.NUM_SHARES);
////        Iterator<Integer> iterator = uniqueIntegers.iterator();
////        while (iterator.hasNext()){
////            Integer integer=iterator.next();
////            files.add(Secret.fileList.get(integer));
////            indices.add(integer+1);
////        }
////
////        List<List<BigInteger>> rLists = new ArrayList<>();
////        List<List<BigInteger>> aLists = new ArrayList<>();
////        List<List<Point>> list1=new ArrayList<>();
////        //修正后的极径
////        List<BigInteger> list3 = new ArrayList<>();
////        //修正后的极角
////        List<BigInteger> list4 = new ArrayList<>();
////        int a=0;
////        for (File f:files) {
////            List<Point> list = Domain.SHPtoList(f);
////            List<Point> points1 = Coordinate.recoverPolarPoints(list,a);
////            list1.add(points1);
////            a++;
////        }
////
////        List<Point> recoverPoints=new ArrayList();
////        for (int i = 0; i < n; i++) {
////        List<Point> list=new ArrayList();
////        List<BigInteger> radius = new ArrayList<>();
////        List<BigInteger> thean = new ArrayList<>();
////            for (int j = 0; j < list1.size(); j++) {
////                radius.add(BigInteger.valueOf((long) (list1.get(j).get(i).getX()/Domain.Qr)));
////                thean.add(BigInteger.valueOf((long) (list1.get(j).get(i).getY()/Domain.Qa)));
////                list.add(new Point(list1.get(j).get(i).getX(),list1.get(j).get(i).getY()));
////            }
////            BigInteger recoveredRadiusSecret1 = Secret.recoverSecret(radius, indices, prime);
////            System.out.println("极径："+recoveredRadiusSecret1);
////            BigInteger recoveredAngleSecret1 = Secret.recoverSecret(thean, indices, prime);
////            System.out.println("极角："+recoveredAngleSecret1);
////            double a1=(recoveredRadiusSecret1.intValue()+watermarkDomains.get(i).getDecimalRadius());
////            double a2=(recoveredAngleSecret1.intValue()+watermarkDomains.get(i).getDecimalAngle());
////            recoverPoints.add(new Point(a1,a2));
////
////        }
////        List<Point> cartesian = Coordinate.recoverCartesian(recoverPoints);
////        List<double[]> recoverList = new ArrayList<>();
////        for (int j = 0; j < n; j++) {
////            recoverList.add(new double[]{cartesian.get(j).getX(),cartesian.get(j).getY()});
////        }
////        Secret.createSHP(recoverList);
////
////        Secret.fileList.clear();
//
////        //创建n个含水印的加密文件
////        for (int i = 0; i <Secret.NUM_SHARES; i++) {
////            int d=0;
////            // 创建坐标点集合
////            List<double[]> coordinatesList = new ArrayList<>();
////            for (int j = 0; j < n; j++) {
////                coordinatesList.add(new double[]{watermarkedCoordinate.get(d+i).getX(), watermarkedCoordinate.get(d+i).getY()});
////                d+=5;
////            }
////           Secret.createSHP(coordinatesList);
////        }
////
////        //选取k个文件
////
////        //提取水印及恢复
////        List<List<watermarkDomain>> watermarkDomainList=new ArrayList<>();
////        for (int j=0;j<Secret.fileList.size();j++) {
////            int d=0;
////            List<Point> list = Domain.SHPtoList(Secret.fileList.get(j));
////            List<Point> toPolarPoints = Coordinate.toPolarPoints(list);
////            List<Point> points1 = Coordinate.recoverAngle(toPolarPoints, j);
////            List<watermarkDomain> watermarkDomains1 = Domain.calWatermark(points1);
////            watermarkDomainList.add(watermarkDomains1);
////        }
////
////        int num=0;
////        StringBuilder sb=new StringBuilder();
////        for (int i = 0; i <n; i++) {
////            for (int j = 0; j < Secret.NUM_SHARES; j++) {
////                double decimalRadius = watermarkDomainList.get(j).get(i).getDecimalRadius();
////                int w = Watermarking.extractWatermark(strings.get(num),decimalRadius);
////                double radius=Math.pow(2,strings.get(num).length())*decimalRadius-w;
////                recoverWatermarkedRadius.add(radius);
////                String s = Integer.toBinaryString(w);
////                String s1 = Watermarking.addLeadingZeros(s,strings.get(num).length());
////                num++;
////                double decimalAngle = watermarkDomainList.get(j).get(i).getDecimalAngle();
////                int w1 = Watermarking.extractWatermark(strings.get(num),decimalAngle);
////                double angle=Math.pow(2,strings.get(num).length())*decimalAngle-w1;
////                recoverWatermarkedAngle.add(angle);
////                String s2 = Integer.toBinaryString(w1);
////                String s3 = Watermarking.addLeadingZeros(s2, strings.get(num).length());
////                sb.append(s1);
////                sb.append(s3);
////                num++;
////            }
////        }
////        Watermarking.decodeImage(String.valueOf(sb));
////        //嵌入水印前的极坐标
////        for (int i = 0; i < recoverWatermarkedAngle.size(); i++) {
////            double radius=encryptedRadiusDomains.get(i)+recoverWatermarkedRadius.get(i);
////            double angle=encryptedAngleDomains.get(i)+recoverWatermarkedAngle.get(i);
////            recoverPolarPoints.add(new Point(radius,angle));
////        }
//
//        //明文域水印的提取及原始明文的恢复
////        List<encryptedDomain> encryptedDomainList = Domain.calEncrypt(recoverPolarPoints);
////        List<watermarkDomain> watermarkDomainList1 = Domain.calWatermark(recoverPolarPoints);
//    }
//}
//
