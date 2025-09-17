//package com.encryptrdSoftware.hnust.util;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class SerializeUtils {
//    public static Object[] deSerialize(byte[] data) throws IOException, ClassNotFoundException {
//        List<Object> objects = new ArrayList<>();
//        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
//             ObjectInputStream ois = new ObjectInputStream(bais)) {
//            Object obj;
//            while ((obj = ois.readObject()) != null) {
//                objects.add(obj);
//            }
//        } catch (EOFException e) {
//        }
//        return objects.toArray(new Object[0]);
//    }
//
//    public static byte[] serialize(Object...arr) throws IOException {
//        ByteArrayOutputStream BO = new ByteArrayOutputStream();
//        ObjectOutputStream OOS = new ObjectOutputStream(BO);
//       for (Object obj:arr){
//          OOS.writeObject(obj);
//       }
//       OOS.close();
//       return BO.toByteArray();
//    }
//    public static byte[] convertFileToByteArray(String filePath) {
//        File file = new File(filePath);
//        byte[] fileContent = new byte[(int) file.length()];
//
//        try (FileInputStream fis = new FileInputStream(file)) {
//            fis.read(fileContent);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return fileContent;
//    }
//}
