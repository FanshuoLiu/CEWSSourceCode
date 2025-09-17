package com.encryptrdSoftware.hnust.util;

public class StringUtils {
    public static String modifyString(String s) {
        String s1 = s.substring(0, s.lastIndexOf("."));
        if (s1.contains("加密")){
            s1 = s1.replace("加密", "");
        }
        if (s1.contains("水印")){
            s1 = s1.replace("水印", "");
        }
        if (Character.isDigit(s1.charAt(s1.length() - 1))){
            s1=s1.substring(0,s1.length()-1);
        }
        return s1;
    }
}
