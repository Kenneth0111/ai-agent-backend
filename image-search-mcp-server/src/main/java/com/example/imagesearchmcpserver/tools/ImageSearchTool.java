package com.example.imagesearchmcpserver.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class ImageSearchTool {

    private static final String API_KEY = "jIK3GAerd3sPohIe0XzLvW0CakoFSUusBcAV7aKvD508gej9J8kijuVb";

    private static final String API_URL = "https://api.pexels.com/v1/search";

    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        String response = HttpRequest.get(API_URL)
                .header("Authorization", API_KEY)
                .form("query", query)
                .form("per_page", 5)
                .execute()
                .body();

        JSONObject json = JSONUtil.parseObj(response);
        JSONArray photos = json.getJSONArray("photos");
        if (photos == null || photos.isEmpty()) {
            return "未找到相关图片";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < photos.size(); i++) {
            JSONObject photo = photos.getJSONObject(i);
            String alt = photo.getStr("alt", "无描述");
            String photographer = photo.getStr("photographer", "未知");
            String url = photo.getJSONObject("src").getStr("original");
            result.append(String.format("%d. %s (摄影师: %s)\n   %s\n", i + 1, alt, photographer, url));
        }
        return result.toString();
    }

}
