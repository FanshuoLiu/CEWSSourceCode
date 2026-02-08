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
        if (jsonData.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "请求体为空");
            return  null;
        }
        try {
            requestData = objectMapper.readValue(jsonData, Map.class);

        }catch (Exception e){
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "处理请求失败");
        }
        return requestData;
    }
}
