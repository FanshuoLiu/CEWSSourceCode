package com.encryptrdSoftware.hnust.util;

import com.encryptrdSoftware.hnust.controller.UploadServlet;
import com.encryptrdSoftware.hnust.model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WatermarkingUtils {
    public static int width = 0;
    public static int height = 0;
    public static StringBuilder createWatermarking(String inputImagePath) throws IOException {
        File input = new File(inputImagePath);
        // 读取BMP图像文件
        BufferedImage image = ImageIO.read(input);
        width = image.getWidth();
        height = image.getHeight();

        // 构建比特串
        StringBuilder bitString = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                char bit = (pixel == 0) ? '0' : '1';
                bitString.append(bit);
            }
        }
        bitString.length();
        // 输出比特串
        return bitString;
    }

    public static List<String> distributeBitString(String bitString, int x) {
        List<String> result = new ArrayList<>();

        int totalLength = bitString.length();
        int avgLength = totalLength / x;
        int remainder = totalLength % x;

        int startIndex = 0;
        for (int i = 0; i < x; i++) {
            int partLength = avgLength;
            if (i < remainder) {
                partLength++;
            }

            StringBuilder part = new StringBuilder();
            int zerosToAdd = avgLength - partLength;

            for (int j = 0; j < zerosToAdd; j++) {
                part.append("0");
            }

            part.append(bitString, startIndex, startIndex + partLength);
            startIndex += partLength;

            result.add(part.toString());
        }
        return result;
    }

    public static void decodeImage(String encodedString,int width,int height,String filename){
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (index < encodedString.length()) { // 添加边界检查
                    int bitValue = Integer.parseInt(String.valueOf(encodedString.charAt(index)));
                    int color = bitValue == 0 ? 0 : 255; // 黑色或白色

                    image.setRGB(x, y, (color << 16) | (color << 8) | color); // 设置RGB值
                    index++;
                } else {
                    // 处理字符串长度不足的情况
                    break; // 提前结束循环
                }
            }
        }
        String string = StringUtils.modifyString(filename);
        File outputImage = new File(UploadServlet.Path+File.separator+string+".bmp");
        try {
            ImageIO.write(image, "bmp", outputImage);
            System.out.println("水印提取成功");
        } catch (IOException e) {
            System.out.println("水印提取失败");
            e.printStackTrace();
        }
    }

    public static Double embedWatermark(String string,double waterDomain) {
       int s=string.length();
        long w = Integer.parseInt(string, 2);
        double watermarkValue = waterDomain/Math.pow(2, s);
        double  watermarkedValue=w/Math.pow(2,s)+ watermarkValue;
        return watermarkedValue;
    }

    public static List<String> distributeBits(String bitString, int num) {
        int length = bitString.length();
        String[] allocations = new String[num];

        // 初始化分配结果
        for (int i = 0; i < num; i++) {
            allocations[i] = "";
        }

        // 遍历每个点并分配比特
        for (int i = 0; i < num; i++) {
            int bitIndex = i % length;
            allocations[i] += bitString.charAt(bitIndex);
        }

        return Arrays.asList(allocations);
    }

    public static String compareBitStrings(String[] bitStrings) {
        if (bitStrings == null || bitStrings.length == 0) {
            return "";
        }
        int length = bitStrings[0].length();
        int[] count0 = new int[length];
        int[] count1 = new int[length];

        for (String bitString : bitStrings) {
          if (bitString != null){
              for (int i = 0; i < bitString.length(); i++) {
                  if (bitString.charAt(i) == '0') {
                      count0[i]++;
                  } else {
                      count1[i]++;
                  }
              }
          }
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (count0[i] > count1[i]) {
                result.append('0');
            } else if (count1[i] > count0[i]) {
                result.append('1');
            } else {
                result.append(bitStrings[0].charAt(i));
            }
        }
        return result.toString();
    }

    public static List<String> initString(String path,int num) throws IOException {
        //确保比特串够分配
        StringBuilder builder = createWatermarking(path);
        int length = builder.length();
        System.out.println("比特串长度:"+length);
        if (length < num){
            return WatermarkingUtils.distributeBits(String.valueOf(builder), num);
        }else {
            return WatermarkingUtils.distributeBitString(String.valueOf(builder), num);
        }
    }

    public static List<Point> calcuWatermarking(List<Point> list,List<String> strings,int index){
        List<encryptedDomain> encryptedDomains = Domain.calEncrypt(list);
        List<watermarkDomain> watermarkDomains = Domain.calWatermark(list);
        List<Point> watermarkedPoints = new ArrayList<>();
        for (int i = 0; i < list.size();i++){
            Double radius = WatermarkingUtils.embedWatermark(strings.get(index), watermarkDomains.get(i).getDecimalRadius());
            Double angle = WatermarkingUtils.embedWatermark(strings.get(index), watermarkDomains.get(i).getDecimalAngle());
            Point point=new Point(radius+encryptedDomains.get(i).getIntegerRadius(), angle+encryptedDomains.get(i).getIntegerAngle());
            watermarkedPoints.add(point);
            index++;
        }
        return watermarkedPoints;
    }
    public static List<Point> calcuWatermarking1(Coordinate coordinate,List<Point> list,List<String> strings,int index){
        List<Point> points = coordinate.calculatePolarCoordinates(list);
        List<encryptedDomain> encryptedDomains = Domain.calEncrypt(points);
        List<watermarkDomain> watermarkDomains = Domain.calWatermark(points);
        List<Point> watermarkedPoints = new ArrayList<>();
        for (int i = 0; i < list.size();i++){
            Double radius = WatermarkingUtils.embedWatermark(strings.get(index), watermarkDomains.get(i).getDecimalRadius());
            Double angle = WatermarkingUtils.embedWatermark(strings.get(index), watermarkDomains.get(i).getDecimalAngle());
            Point point=new Point(radius+encryptedDomains.get(i).getIntegerRadius(), angle+encryptedDomains.get(i).getIntegerAngle());
            watermarkedPoints.add(point);
            index++;
        }
        return watermarkedPoints;
    }

}

