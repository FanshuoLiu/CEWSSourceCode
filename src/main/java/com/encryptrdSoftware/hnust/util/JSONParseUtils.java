package com.encryptrdSoftware.hnust.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class JSONParseUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();
    public static Map<String,Object> parseJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        StringBuilder requestBody = new StringBuilder();
        String line1;
        Map<String, Object> requestData = null;
        try (BufferedReader reader = req.getReader()) {
            while ((line1 = reader.readLine()) != null) {
                requestBody.append(line1);
            }
        }
        String jsonData = requestBody.toString();
        System.out.println("Received JSON data: " + jsonData); // 调试打印
        if (jsonData.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request body is empty");
            return  null;
        }
        try {
            // 将 JSON 字符串解析为 Map 或者您可以定义一个对应的 Java Bean
            requestData = objectMapper.readValue(jsonData, Map.class);

        }catch (Exception e){
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
        }
        return requestData;
    }
}
