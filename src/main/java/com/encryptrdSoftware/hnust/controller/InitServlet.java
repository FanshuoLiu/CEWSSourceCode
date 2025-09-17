//package com.encryptrdSoftware.hnust.controller;
//
//import com.encryptrdSoftware.hnust.dao.FileDAO;
//import com.encryptrdSoftware.hnust.dao.UserDAO;
//import com.encryptrdSoftware.hnust.model.*;
//import com.encryptrdSoftware.hnust.util.SecretUtils;
//import com.encryptrdSoftware.hnust.util.SerializeUtils;
//import com.encryptrdSoftware.hnust.util.StringUtils;
//import com.encryptrdSoftware.hnust.util.WatermarkingUtils;
//import org.gdal.gdal.gdal;
//import org.gdal.ogr.Layer;
//import org.gdal.ogr.ogr;
//
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//import java.io.*;
//import java.math.BigInteger;
//import java.security.SecureRandom;
//import java.util.*;
//
//@WebServlet("/init")
//public class InitServlet extends HttpServlet {
//    public static BigInteger Prime=null;
//
//    public static int type;
//    public static int k = 0;
//    public static int split;
//    static {
//        gdal.AllRegister();
//        ogr.RegisterAll();
//    }
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        doPost(req, resp);
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        req.setCharacterEncoding("UTF-8");
//        resp.setContentType("application/json;charset=UTF-8");
//        String btn;
//        HttpSession session = req.getSession(false);
//        if (session.getAttribute("username")==null){
//            resp.getWriter().write("{\"status\":\"error\",\"message\":\"请先登录\"}");
//            return;
//        }
//
//        FileDAO fileDAO = new FileDAO();
//        UserDAO userDAO = new UserDAO();
//        //获取用户的id
//        session = req.getSession();
//        Integer userId = (Integer) session.getAttribute("userId");
//
//            try {
//                String filePath = req.getParameter("filename1");
//                String bmpPath = req.getParameter("filename2");
//
//                System.out.println("shp文件名称:" + filePath);
//                String realFilePath = UploadServlet.Path + "/" + filePath;
//                String realBmpPath = UploadServlet.Path + "/" + bmpPath;
//                System.out.println("文件路径:" + realFilePath);
//                //获取shp文件的属性列表
//                List<LinkedHashMap<String, Object>> maps = SecretUtils.readShapefile(new File(realFilePath));
//                btn = req.getParameter("encryptBtn");
//                System.out.println("btn:" + btn);
//
//                StringBuilder builder=null;
//                List<Integer> pic=null;
//                if (btn == null) {
//                    btn = "水印";
//                    builder = WatermarkingUtils.createWatermarking(realBmpPath);
//                    int width=WatermarkingUtils.width;
//                    int height= WatermarkingUtils.height;
//                    pic=new ArrayList<>();
//                    pic.add(width);
//                    pic.add(height);
//                }else {
//                    split = Integer.parseInt(req.getParameter("num"));
//                    k = (split + 1) / 2;
//                    System.out.println("split:" + split);
//                    System.out.println("k:" + k);
//                }
//
//
//                // 打开Shapefile
//                Layer layer = Domain.getLayer(new File(realFilePath));
//
//                //计算要素的数量
//                long num = Domain.calPointsNumber(layer);
//                System.out.println("要素数量:" + num);
//                //获取图层的要素类型
//                type = layer.GetGeomType();
//                System.out.println("type:" + type);
//
//                //存放要素
//                Map<String, Object> map = Domain.SHPtoList(new File(realFilePath));
//                List<Shape> list = (List<Shape>) map.get("geometries");
//                //点集合
//                List<Point> pointList = new ArrayList<>();
//                //线集合
//                List<Line> lineList = new ArrayList<>();
//                //面集合
//                List<Polygon> polygonList = new ArrayList<>();
//                //多线集合
//                List<MultiLine> multiLineList = new ArrayList<>();
//
//                //点的加密域集合水印域集合
//                List<encryptedDomain> encryptedDomainList;
//                List<watermarkDomain> watermarkDomainList;
//                //线和面的加密域集合水印域集合
//                List<List<encryptedDomain>> encryptedDomainCollectionList = new ArrayList<>();
//                List<List<watermarkDomain>> watermarkDomainCollectionList = new ArrayList<>();
//
//                BigInteger radiusSecret = null;
//                BigInteger angleSecret = null;
//                List<BigInteger> radiusCoefficients = new ArrayList<>();
//                List<BigInteger> angleCoefficients = new ArrayList<>();
//                BigInteger randomRadiusBigInteger = null;
//                BigInteger randomAngleBigInteger = null;
//                List<BigInteger> radiusShares = null;
//                List<BigInteger> angleShares = null;
//                List<Point> polarPointShares = new ArrayList<>();
//                //存放所有加密后的点的份额
//                List<Point> points = new ArrayList<>();
//
//                String s= StringUtils.modifyString(filePath);
//                //判断文件是否加密
//                FileData encrypted = fileDAO.isEncrypted(s, userId);
//                FileData watermarked = fileDAO.isWatermarked(s, userId);
//                byte[] bytes;
//                switch (type) {
//                    case 1:
//                        Point point;
//                        for (Object obj : list) {
//                            if (obj instanceof Point) {
//                                point = (Point) obj;
//                                pointList.add(point);
//                            }
//                        }
//                        if (btn.equals("加密")) {
//                            if (encrypted!=null){
//                                if (encrypted.getEncrypted()==true){
//                                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"文件已加密，无法再次加密\"}");
//                                    return;
//                                }
//                            }
//                            System.out.println("处理点要素");
//
//                            // 打乱集合
//                            Collections.shuffle(pointList);
//                            // 分割集合
//                            List<Point> list1 = new ArrayList<>();
//                            List<Point> list2 = new ArrayList<>();
//                            int halfSize = pointList.size() / 2;
//
//                            for (int i = 0; i < pointList.size(); i++) {
//                                if (i < halfSize) {
//                                    list1.add(pointList.get(i));
//                                } else {
//                                    list2.add(pointList.get(i));
//                                }
//                            }
//                            Point polarPoint = Coordinate.calculateGeometricCenter(list1);
//                            Point polarAxisPoint = Coordinate.calculateGeometricCenter(list2);
//                            Coordinate center = new Coordinate(polarPoint, polarAxisPoint);
//                            Domain.adaptationFactor = 0.001;
//                            Domain.Qr = Domain.adaptationFactor * Math.sqrt(Math.pow(polarAxisPoint.getX() - polarPoint.getX(), 2) + Math.pow(polarAxisPoint.getY() - polarPoint.getY(), 2));
//                            Domain.Qa = 1;
//                            System.out.println("极点和极轴点为:" + polarPoint + " " + polarAxisPoint);
//                            System.out.println("Qr值：" + Domain.Qr);
//                            System.out.println();
//                            //计算其他点的极坐标,求加密域和水印域
//                            List<Point> polarPoints = center.calculatePolarCoordinates(pointList);
//                            encryptedDomainList = Domain.calEncrypt(polarPoints);
//                            watermarkDomainList = Domain.calWatermark(polarPoints);
//                            Prime = SecretUtils.generatePrime(encryptedDomainList);
//                            System.out.println("大素数为:" + Prime);
//                            List<List<BigInteger>> radiusList = new ArrayList<>();
//                            List<List<BigInteger>> angleList = new ArrayList<>();
//                            for (int i = 0; i < num; i++) {
//                                //生成极径秘密
//                                radiusSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerRadius()));
//                                //生成极角秘密
//                                angleSecret = new BigInteger(String.valueOf(encryptedDomainList.get(i).getIntegerAngle()));
//                                // 生成极径多项式系数
//                                radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, Prime);
//                                // 生成极角多项式系数
//                                angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, Prime);
//                                //生成极径极角份额
//                                radiusShares = SecretUtils.generateShares(radiusCoefficients, split, Prime, randomRadiusBigInteger, i);
//                                angleShares = SecretUtils.generateShares(angleCoefficients, split, Prime, randomAngleBigInteger, i);
//                                //随机获取前一个点的一个份额
//                                randomRadiusBigInteger = radiusShares.get(new SecureRandom().nextInt(split));
//                                randomAngleBigInteger = angleShares.get(new SecureRandom().nextInt(split));
//                                radiusList.add(radiusShares);
//                                angleList.add(angleShares);
//                                //计算极坐标密文
//                                for (int k = 0; k < split; k++) {
//                                    double radiusShare = (radiusShares.get(k).doubleValue() + watermarkDomainList.get(k).getDecimalRadius()) * Domain.Qr;
//                                    double angleShare = (angleShares.get(k).doubleValue() + watermarkDomainList.get(k).getDecimalAngle()) * Domain.Qa;
//                                    polarPointShares.add(new Point(radiusShare, angleShare));
//                                }
//                            }
//                            List<Point> cartesians = Coordinate.toCartesian(polarPointShares);
//                            //创建n个加密文件
//                            for (int i = 0; i < split; i++) {
//                                int d = 0;
//                                // 创建坐标点集合
//                                List<Point> coordinatesList = new ArrayList<>();
//                                for (int j = 0; j < num; j++) {
//                                    coordinatesList.add(new Point(cartesians.get(d + i).getX(), cartesians.get(d + i).getY()));
//                                    d += split;
//                                }
//                                SecretUtils.createSHP(coordinatesList, layer, filePath.substring(0, filePath.lastIndexOf(".")), btn,maps,i);
//                            }
//
//                                String filename= s;
//                                if (filePath.contains("水印")){
//                                    FileData data = fileDAO.getFileByfileName(filename);
//                                    byte[] fileBytes = data.getData();
//                                    Object[] objects = SerializeUtils.deSerialize(fileBytes);
//                                    System.out.println("objects[0]:"+objects[0]);
//                                    System.out.println("objects[]:"+objects[1]);
//                                    bytes = SerializeUtils.serialize(radiusList, angleList, watermarkDomainList,Prime,split,objects[0],objects[1]);
//                                    //更新数据库
//                                    fileDAO.updateFileByFilename(bytes,filename);
//                                }else {
//                                    //序列化watermarkDomainList/radiusList/angleList
//                                    bytes = SerializeUtils.serialize(radiusList, angleList, watermarkDomainList,Prime,split);
//                                    //存入数据库
//                                    fileDAO.addFile(userId, new FileData(0, filename, bytes));
//                                    //更新用户表的file_count属性
//                                    userDAO.updateFileCount(userId);
//                                }
//                                //更新文件加密状态
//                                fileDAO.updateEncrypted(1,filename);
//                                resp.getWriter().write("{\"status\":\"success\",\"message\":\"文件加密成功: " + filename + "\"}");
//                            //嵌入水印
//                        } else {
////                            if (watermarked!=null) {
////                                if (watermarked.getWatermarked()==true){
////                                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"文件已添加水印，无法再次添加水印\"}");
////                                    return;
////                                }
////                            }
//
//                            //确保比特串够分配
//                            int length = builder.length();
//                            List<String> strings;
//                            if (length < num){
//                                strings = WatermarkingUtils.distributeBits(String.valueOf(builder), (int)num);
//                            }else {
//                                strings = WatermarkingUtils.distributeBitString(String.valueOf(builder), (int)num);
//                            }
//                            encryptedDomainList= Domain.calEncrypt(pointList);
//                            watermarkDomainList = Domain.calWatermark(pointList);
//
//                            List<watermarkDomain> watermarkingList = new ArrayList<>();
//
//                            List<Point> watermarkedPoints = new ArrayList();
//                            int index = 0;
//                            //嵌入水印
//                            for (int i = 0; i < num; i++) {
//                                Double radiusWatermark;
//                                Double angleWatermark;
//                                try {
//                                    radiusWatermark = WatermarkingUtils.embedWatermark(strings.get(index), watermarkDomainList.get(i).getDecimalRadius());
//                                    angleWatermark = WatermarkingUtils.embedWatermark(strings.get(index), watermarkDomainList.get(i).getDecimalAngle());
//                                    watermarkingList.add(new watermarkDomain(radiusWatermark, angleWatermark));
//                                    double watermarkedRadius = (radiusWatermark + encryptedDomainList.get(i).getIntegerRadius());
//                                    double watermarkedAngle = (angleWatermark + encryptedDomainList.get(i).getIntegerAngle());
//                                    watermarkedPoints.add(new Point(watermarkedRadius, watermarkedAngle));
//                                    index++;
//                              }catch (NumberFormatException e){
//                                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"图片尺寸过大，请重新上传\"}");
//                                    return;
//                              }
//                            }
//                            SecretUtils.createSHP(watermarkedPoints, layer, filePath.substring(0, filePath.lastIndexOf(".")), "水印",maps);
//
//                                String filename= s;
//                            if (filePath.contains("加密")){
//                                System.out.println("filename:"+filename);
//                                FileData data = fileDAO.getFileByfileName(filename);
//                                byte[] fileBytes = data.getData();
//                                Object[] objects = SerializeUtils.deSerialize(fileBytes);
//                                bytes = SerializeUtils.serialize(objects[0],objects[1],objects[2],objects[3],objects[4],strings,pic);
//                                //更新数据库
//                                fileDAO.updateFileByFilename(bytes,filename);
//                            }else {
//                                //序列化
//                                bytes = SerializeUtils.serialize(strings,pic);
//                                //存入数据库
//                                fileDAO.addFile(userId, new FileData(0,filename, bytes));
//                                //更新用户表的file_count属性
//                                userDAO.updateFileCount(userId);
//                            }
//                            //更新文件水印状态
//                            fileDAO.updateWatermarked(1,filename);
//                            resp.getWriter().write("{\"status\":\"success\",\"message\":\"水印" + bmpPath + "嵌入成功\"}");
//                        }
//                        break;
//
//                    case 2:
//                        //每条线段的几何中心点
//                        List<Point> lineCenterList = new ArrayList<>();
//                        //每个线段的prime
//                        List<BigInteger> primeList = new ArrayList<>();
//                        //极径份额集合
//                        List<List<BigInteger>> LineRadiusShareList = new ArrayList<>();
//                        //极角份额集合
//                        List<List<BigInteger>> LineAngleShareList = new ArrayList<>();
//                        Line line;
//                        MultiLine multiLine;
//                        for (Object obj : list) {
//                            if (obj instanceof Line) {
//                                line = (Line) obj;
//                                lineList.add(line);
//                                //计算每条线段的中心点
//                                Point centerPoint = Coordinate.calculateGeometricCenter(line.getPoints());
//                                lineCenterList.add(centerPoint);
//                            } else if (obj instanceof MultiLine) {
//                                multiLine = (MultiLine)obj;
//                                multiLineList.add(multiLine);
//                                for (Line l : multiLine.getLines()){
//                                    //计算每条线段的中心点
//                                    Point centerPoint = Coordinate.calculateGeometricCenter(l.getPoints());
//                                    lineCenterList.add(centerPoint);
//                                }
//                            }
//                        }
//
//                        if (btn.equals("加密")) {
//                            if (encrypted!=null){
//                                if (encrypted.getEncrypted()==true){
//                                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"文件已加密，无法再次加密\"}");
//                                    return;
//                                }
//                            }
//                            List<List<Point>> splitLineCollection = Coordinate.splitCollection(lineCenterList);
//                            //构建坐标系
//                            Point polarPoint1 = Coordinate.calculateGeometricCenter(splitLineCollection.get(0));
//                            Point polarAxisPoint1 = Coordinate.calculateGeometricCenter(splitLineCollection.get(1));
//                            Coordinate coordinate = new Coordinate(polarPoint1, polarAxisPoint1);
//
//                            //构建量化步长
//                            Domain.adaptationFactor = 0.001;
//                            Domain.Qr = Domain.adaptationFactor * Math.sqrt(Math.pow(polarAxisPoint1.getX() - polarPoint1.getX(), 2) + Math.pow(polarAxisPoint1.getY() - polarPoint1.getY(), 2));
//                            Domain.Qa = 1;
//
//                            System.out.println("极点和极轴点为:" + polarPoint1 + " " + polarAxisPoint1);
//                            System.out.println("Qr值：" + Domain.Qr);
//
//                            //计算每条线的极坐标,求加密域和水印域
//                            for (int i = 0; i < lineList.size(); i++) {
//                                List<Point> linePoints = coordinate.calculatePolarCoordinates(lineList.get(i).getPoints());
//                                List<encryptedDomain> lineEncryptedDomains = Domain.calEncrypt(linePoints);
//                                List<watermarkDomain> lineWatermarkDomains = Domain.calWatermark(linePoints);
//                                BigInteger prime = SecretUtils.generatePrime(lineEncryptedDomains);
//                                primeList.add(prime);
//                                encryptedDomainCollectionList.add(lineEncryptedDomains);
//                                watermarkDomainCollectionList.add(lineWatermarkDomains);
//                            }
//                            //求prime
//                            Prime = Collections.max(primeList);
//                            System.out.println("大素数为:" + Prime);
//                            System.out.println("加密中...");
//                            for (int i = 0; i < lineList.size(); i++) {
//                                //对线段的每个点进行处理
//                                for (int j = 0; j < lineList.get(i).getLength(); j++) {
//                                    radiusSecret = new BigInteger(String.valueOf(encryptedDomainCollectionList.get(i).get(j).getIntegerRadius()));
//                                    angleSecret = new BigInteger(String.valueOf(encryptedDomainCollectionList.get(i).get(j).getIntegerAngle()));
//                                    radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, Prime);
//                                    angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, Prime);
//                                    radiusShares = SecretUtils.generateShares(radiusCoefficients, split, Prime, randomRadiusBigInteger, j);
//                                    angleShares = SecretUtils.generateShares(angleCoefficients, split, Prime, randomAngleBigInteger, j);
//                                    LineRadiusShareList.add(radiusShares);
//                                    LineAngleShareList.add(angleShares);
//                                    randomRadiusBigInteger = radiusShares.get(new Random().nextInt(split));
//                                    randomAngleBigInteger = angleShares.get(new Random().nextInt(split));
//                                    //计算加密份额
//                                    for (int l = 0; l < split; l++) {
//                                        points.add(new Point(Domain.Qr * (radiusShares.get(l).doubleValue() + watermarkDomainCollectionList.get(i).get(j).getDecimalRadius()), (angleShares.get(l).doubleValue() + watermarkDomainCollectionList.get(i).get(j).getDecimalAngle())));
//                                    }
//                                }
//                            }
//
//                            System.out.println("加密完成");
//                            //生成文件
//                            for (int i = 0; i < split; i++) {
//                                List<Line> lines = new ArrayList<>();
//                                int d = i;
//                                for (int a = 0; a < num; a++) {
//                                    // 创建坐标点集合
//                                    List<Point> coordinatesList = new ArrayList<>();
//                                    for (int j = 0; j < lineList.get(a).getLength(); j++) {
//                                        coordinatesList.add(new Point(points.get(d).getX(), points.get(d).getY()));
//                                        d += split;
//                                    }
//                                    lines.add(new Line(Coordinate.toCartesian(coordinatesList)));
//                                }
//                                SecretUtils.createSHP(lines, layer, filePath.substring(0, filePath.lastIndexOf(".")), btn,maps,i);
//                            }
//                            String filename= s;
//                            if (filePath.contains("水印")){
//                                FileData data = fileDAO.getFileByfileName(filename);
//                                byte[] fileBytes = data.getData();
//                                Object[] objects = SerializeUtils.deSerialize(fileBytes);
//                                bytes = SerializeUtils.serialize(LineRadiusShareList, LineAngleShareList, watermarkDomainCollectionList,Prime,split,objects[0],objects[1]);
//                                //更新数据库
//                                fileDAO.updateFileByFilename(bytes,filename);
//                            }else {
//                                //序列化watermarkDomainCollectionList/radiusShareList/angleSharesList
//                                bytes = SerializeUtils.serialize(LineRadiusShareList, LineAngleShareList, watermarkDomainCollectionList,Prime,split);
//                                //存入数据库
//                                fileDAO.addFile(userId, new FileData(0,filename, bytes));
//                                //更新用户文件数量
//                                userDAO.updateFileCount(userId);
//                            }
//                                //更新文件加密状态
//                                fileDAO.updateEncrypted(1,filename);
//                                resp.getWriter().write("{\"status\":\"success\",\"message\":\"文件加密成功: " + filename + "\"}");
//                        } else {
////                            if (watermarked!=null) {
////                                if (watermarked.getWatermarked()==true){
////                                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"文件已添加水印，无法再次添加水印\"}");
////                                    return;
////                                }
////                            }
//                            //确保比特串够分配
//                            System.out.println("比特串长度:"+builder.length());
//                            int sum = Line.getAllPoints(lineList);
//
//                            for (int i = 0; i < lineList.size(); i++) {
//                                List<encryptedDomain> lineEncryptedDomains = Domain.calEncrypt(lineList.get(i).getPoints());
//                                List<watermarkDomain> lineWatermarkDomains = Domain.calWatermark(lineList.get(i).getPoints());
//                                encryptedDomainCollectionList.add(lineEncryptedDomains);
//                                watermarkDomainCollectionList.add(lineWatermarkDomains);
//                            }
//
//                            System.out.println("点:"+sum);
//                            int length = builder.length();
//                            List<String> strings;
//                            if (length < sum){
//                                strings = WatermarkingUtils.distributeBits(String.valueOf(builder), sum);
//                            }else {
//                                strings = WatermarkingUtils.distributeBitString(String.valueOf(builder), sum);
//                            }
//                            List<watermarkDomain> watermarkingList = new ArrayList<>();
//                            int index = 0;
//                            List<Line> lines = new ArrayList<>();
//                            //嵌入水印
//                            for (int i = 0; i < num; i++) {
//                                List<Point> watermarkedPoints = new ArrayList();
//                                for (int j = 0; j < lineList.get(i).getLength(); j++) {
//                                    Double radiusWatermark = WatermarkingUtils.embedWatermark(strings.get(index), watermarkDomainCollectionList.get(i).get(j).getDecimalRadius());
//                                    Double angleWatermark = WatermarkingUtils.embedWatermark(strings.get(index), watermarkDomainCollectionList.get(i).get(j).getDecimalAngle());
//                                    watermarkingList.add(new watermarkDomain(radiusWatermark, angleWatermark));
//                                    double watermarkedRadius = (radiusWatermark + encryptedDomainCollectionList.get(i).get(j).getIntegerRadius());
//                                    double watermarkedAngle = (angleWatermark + encryptedDomainCollectionList.get(i).get(j).getIntegerAngle());
//                                    watermarkedPoints.add(new Point(watermarkedRadius, watermarkedAngle));
//                                    index++;
//                                }
//                                lines.add(new Line(watermarkedPoints));
//                            }
//                            SecretUtils.createSHP(lines, layer,filePath.substring(0, filePath.lastIndexOf(".")), btn,maps);
//
//                            String filename= s;
//                            if (filePath.contains("加密")){
//                                FileData data = fileDAO.getFileByfileName(filename);
//                                byte[] fileBytes = data.getData();
//                                Object[] objects = SerializeUtils.deSerialize(fileBytes);
//                                bytes = SerializeUtils.serialize(objects[0],objects[1],objects[2],objects[3],objects[4],strings,pic);
//                                //更新数据库
//                                fileDAO.updateFileByFilename(bytes,filename);
//                            }else {
//                                //序列化
//                                bytes = SerializeUtils.serialize(strings,pic);
//                                //存入数据库
//                                fileDAO.addFile(userId, new FileData(0, filename, bytes));
//                                //更新用户表的file_count属性
//                                userDAO.updateFileCount(userId);
//                            }
//                            //更新文件水印状态
//                            fileDAO.updateWatermarked(1,filename);
//                            resp.getWriter().write("{\"status\":\"success\",\"message\":\"水印" + bmpPath + "嵌入成功\"}");
//                        }
//                        break;
//                    case 3:
//                        //每个面的几何中心点
//                        List<Point> polygonCenterList = new ArrayList<>();
//                        //每个面的prime
//                        List<BigInteger> polygonPrimeList = new ArrayList<>();
//                        //面的极径份额集合
//                        List<List<BigInteger>> PolygonRadiusShareList = new ArrayList<>();
//                        //面的极角份额集合
//                        List<List<BigInteger>> PolygonAngleShareList = new ArrayList<>();
//                        Polygon polygon = null;
//                        for (Object obj : list) {
//                            if (obj != null){
//                                if (obj instanceof Polygon) {
//                                    polygon = (Polygon) obj;
//                                    if (filePath.contains("加密")||filePath.contains("水印")){
//                                        polygon=polygon.removeLastPoint();
//                                    }
//                                    polygonList.add(polygon);
//                                }
//                            }
//                            //计算每个面的中心点
//                            Point polygonCenterPoint = Coordinate.calculateGeometricCenter(polygon.getPoints());
//                            polygonCenterList.add(polygonCenterPoint);
//                        }
//
//                        if (btn.equals("加密")) {
//                            if (encrypted!=null){
//                                if (encrypted.getEncrypted()==true){
//                                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"文件已加密，无法再次加密\"}");
//                                    return;
//                                }
//                            }
//                            List<List<Point>> splitPolygonCollection = Coordinate.splitCollection(polygonCenterList);
//                            //构建坐标系
//                            Point polygonPolarPoint = Coordinate.calculateGeometricCenter(splitPolygonCollection.get(0));
//                            Point polygonPolarAxisPoint = Coordinate.calculateGeometricCenter(splitPolygonCollection.get(1));
//                            Coordinate polygonCoordinate = new Coordinate(polygonPolarPoint, polygonPolarAxisPoint);
//
//                            //构建量化步长
//                            Domain.adaptationFactor = 0.001;
//                            Domain.Qr = Domain.adaptationFactor * Math.sqrt(Math.pow(polygonPolarAxisPoint.getX() - polygonPolarPoint.getX(), 2) + Math.pow(polygonPolarAxisPoint.getY() - polygonPolarPoint.getY(), 2));
//                            Domain.Qa = 1;
//
//                            //计算每个面要素的极坐标,求加密域和水印域
//                            for (int i = 0; i < polygonList.size(); i++) {
//                                List<Point> polygonPoints = polygonCoordinate.calculatePolarCoordinates(polygonList.get(i).getPoints());
//                                List<encryptedDomain> polygonEncryptedDomains = Domain.calEncrypt(polygonPoints);
//                                List<watermarkDomain> polygonWatermarkDomains = Domain.calWatermark(polygonPoints);
//                                BigInteger prime = SecretUtils.generatePrime(polygonEncryptedDomains);
//                                polygonPrimeList.add(prime);
//                                encryptedDomainCollectionList.add(polygonEncryptedDomains);
//                                watermarkDomainCollectionList.add(polygonWatermarkDomains);
//                            }
//
//                            //求prime
//                            Prime = Collections.max(polygonPrimeList);
//                            System.out.println("大素数为:" + Prime);
//
//                            for (int i = 0; i < polygonList.size(); i++) {
//                                //对线段的每个点进行处理
//                                for (int j = 0; j < polygonList.get(i).getNum(); j++) {
//                                    radiusSecret = new BigInteger(String.valueOf(encryptedDomainCollectionList.get(i).get(j).getIntegerRadius()));
//                                    angleSecret = new BigInteger(String.valueOf(encryptedDomainCollectionList.get(i).get(j).getIntegerAngle()));
//                                    radiusCoefficients = SecretUtils.generatePolynomial(radiusSecret, k, Prime);
//                                    angleCoefficients = SecretUtils.generatePolynomial(angleSecret, k, Prime);
//                                    radiusShares = SecretUtils.generateShares(radiusCoefficients, split, Prime, randomRadiusBigInteger, j);
//                                    angleShares = SecretUtils.generateShares(angleCoefficients, split, Prime, randomAngleBigInteger, j);
//                                    PolygonRadiusShareList.add(radiusShares);
//                                    PolygonAngleShareList.add(angleShares);
//                                    randomRadiusBigInteger = radiusShares.get(new Random().nextInt(split));
//                                    randomAngleBigInteger = angleShares.get(new Random().nextInt(split));
//                                    //计算加密份额
//                                    for (int l = 0; l < split; l++) {
//                                        points.add(new Point(Domain.Qr * (radiusShares.get(l).doubleValue() + watermarkDomainCollectionList.get(i).get(j).getDecimalRadius()), (angleShares.get(l).doubleValue() + watermarkDomainCollectionList.get(i).get(j).getDecimalAngle())));
//                                    }
//                                }
//                            }
//                            System.out.println("加密完成");
//                            //生成文件
//                            for (int i = 0; i < split; i++) {
//                                List<Polygon> polygons = new ArrayList<>();
//                                int d = 0;
//                                for (int a = 0; a < num; a++) {
//                                    // 创建坐标点集合
//                                    List<Point> coordinatesList = new ArrayList<>();
//
//                                    for (int j = 0; j < polygonList.get(a).getNum(); j++) {
//                                        if (j == 0){
//                                        }
//                                        coordinatesList.add(new Point(points.get(d + i).getX(), points.get(d + i).getY()));
//                                        d += split;
//                                    }
//                                    polygons.add(new Polygon(Coordinate.toCartesian(coordinatesList)));
//                                }
//                                SecretUtils.createSHP(polygons, layer, filePath.substring(0, filePath.lastIndexOf(".")), btn,maps,i);
//                            }
//
//                            if (filePath.contains("水印")){
//                                filePath= s;
//                                FileData data = fileDAO.getFileByfileName(filePath);
//                                byte[] fileBytes = data.getData();
//                                Object[] objects = SerializeUtils.deSerialize(fileBytes);
//                                bytes = SerializeUtils.serialize(PolygonRadiusShareList, PolygonAngleShareList, watermarkDomainCollectionList,Prime,split,objects[0],objects[1]);
//                                //更新数据库
//                                fileDAO.updateFileByFilename(bytes,filePath);
//                            }else {
//                                //序列化watermarkDomainCollectionList/PolygonRadiusShareList/PolygonAngleShareList
//                                bytes = SerializeUtils.serialize(PolygonRadiusShareList, PolygonAngleShareList, watermarkDomainCollectionList,Prime,split);
//                                //存入数据库
//                                fileDAO.addFile(userId, new FileData(0, filePath, bytes));
//                                //更新用户表的file_count属性
//                                userDAO.updateFileCount(userId);
//                            }
//                                //更新文件加密状态
//                                fileDAO.updateEncrypted(1,filePath);
//                                resp.getWriter().write("{\"status\":\"success\",\"message\":\"文件加密成功: " + filePath + "\"}");
//
//                        } else {
////                            if (watermarked!=null) {
////                                if (watermarked.getWatermarked()==true){
////                                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"文件已添加水印，无法再次添加水印\"}");
////                                    return;
////                                }
////                            }
//                            //确保比特串够分配
//                            int sum = Polygon.getAllPoints(polygonList);
//                            System.out.println("点数：" + sum);
//                            int length = builder.length();
//                            List<String> strings;
//                            if (length < sum){
//                                strings = WatermarkingUtils.distributeBits(String.valueOf(builder), sum);
//                            }else {
//                                strings = WatermarkingUtils.distributeBitString(String.valueOf(builder), sum);
//                            }
//                            System.out.println("面的个数:"+pointList.size());
//                            for (int i = 0; i < polygonList.size(); i++) {
//                                List<encryptedDomain> polygonEncryptedDomains = Domain.calEncrypt(polygonList.get(i).getPoints());
//                                List<watermarkDomain> polygonWatermarkDomains = Domain.calWatermark(polygonList.get(i).getPoints());
//                                encryptedDomainCollectionList.add(polygonEncryptedDomains);
//                                watermarkDomainCollectionList.add(polygonWatermarkDomains);
//                            }
//                            List<watermarkDomain> watermarkingList = new ArrayList<>();
//
//                            List<Polygon> polygons = new ArrayList<>();
//                            int index = 0;
//                            //嵌入水印
//                            for (int i = 0; i < num; i++) {
//                                List<Point> watermarkedPoints = new ArrayList();
//                                for (int j = 0; j < polygonList.get(i).getNum(); j++) {
//                                    Double radiusWatermark = WatermarkingUtils.embedWatermark(strings.get(index), watermarkDomainCollectionList.get(i).get(j).getDecimalRadius());
//                                    Double angleWatermark = WatermarkingUtils.embedWatermark(strings.get(index), watermarkDomainCollectionList.get(i).get(j).getDecimalAngle());
//                                    watermarkingList.add(new watermarkDomain(radiusWatermark, angleWatermark));
//                                    double watermarkedRadius = (radiusWatermark + encryptedDomainCollectionList.get(i).get(j).getIntegerRadius());
//                                    double watermarkedAngle = (angleWatermark + encryptedDomainCollectionList.get(i).get(j).getIntegerAngle());
//                                    watermarkedPoints.add(new Point(watermarkedRadius, watermarkedAngle));
//                                    index++;
//                                }
//                                polygons.add(new Polygon(watermarkedPoints));
//                            }
//                            SecretUtils.createSHP(polygons, layer, filePath.substring(0, filePath.lastIndexOf(".")), btn,maps);
//
//                            String filename= s;
//                            if (filePath.contains("加密")){
//                                FileData data = fileDAO.getFileByfileName(filename);
//                                byte[] fileBytes = data.getData();
//                                Object[] objects = SerializeUtils.deSerialize(fileBytes);
//                                bytes = SerializeUtils.serialize(objects[0],objects[1],objects[2],objects[3],objects[4],strings,pic);
//                                //更新数据库
//                                fileDAO.updateFileByFilename(bytes,filename);
//                            }else {
//                                //序列化watermarkingList/w
//                                bytes = SerializeUtils.serialize(strings,pic);
//                                //存入数据库
//                                fileDAO.addFile(userId, new FileData(0, filename, bytes));
//                                //更新用户表的file_count属性
//                                userDAO.updateFileCount(userId);
//                            }
//
//                            //更新文件水印状态
//                            fileDAO.updateWatermarked(1,filename);
//                            resp.getWriter().write("{\"status\":\"success\",\"message\":\"水印" + bmpPath + "嵌入成功\"}");
//                        }
//                        break;
//                    default:
//                        resp.getWriter().write("{\"status\":\"error\",\"message\":\"不支持的加密类型\"}");
//                        return;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                resp.getWriter().write("{\"status\":\"error\",\"message\":\"文件加密失败: " + e.getMessage() + "\"}");
//            } finally {
//                resp.getWriter().close();
//            }
//    }
//}