package com.example.aiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.Map;

/**
 * 网页搜索工具
 */
public class WebSearchTool {

    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "Search the web for information using Baidu search engine")
    public String searchWeb(@ToolParam(description = "The search query") String query) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("### 百度搜索结果\n\n");
        stringBuilder.append(doSearch(query));
        return stringBuilder.toString();
    }

    private String doSearch(String query) {
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);
        params.put("api_key", apiKey);
        params.put("engine", "baidu");

        try {
            String response = HttpUtil.get(SEARCH_API_URL, params);
            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            if (organicResults == null || organicResults.isEmpty()) {
                return "暂无搜索结果。\n\n";
            }

            int limit = Math.min(5, organicResults.size());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < limit; i++) {
                JSONObject result = organicResults.getJSONObject(i);
                String title = result.getStr("title", "无标题");
                String link = result.getStr("link", "");
                String snippet = result.getStr("snippet", "");
                String thumbnail = result.getStr("thumbnail", "");

                sb.append("**%d. [%s](%s)**\n".formatted(i + 1, title, link));
                if (!snippet.isEmpty()) {
                    sb.append("> %s\n".formatted(snippet));
                }
                if (!thumbnail.isEmpty()) {
                    sb.append("\n![%s](%s)\n".formatted(title, thumbnail));
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "搜索失败：%s\n\n".formatted(e.getMessage());
        }
    }
}
