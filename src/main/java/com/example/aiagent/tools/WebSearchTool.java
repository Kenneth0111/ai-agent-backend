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

    @Tool(description = "Search the web for information using a search query, covering both Google and Baidu engines")
    public String searchWeb(@ToolParam(description = "The search query") String query) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("=== Google Results ===%n%n".formatted());
        stringBuilder.append(doSearch(query, "google"));
        stringBuilder.append("=== Baidu Results ===%n%n".formatted());
        stringBuilder.append(doSearch(query, "baidu"));
        return stringBuilder.toString();
    }

    private String doSearch(String query, String engine) {
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);
        params.put("api_key", apiKey);
        params.put("engine", engine);

        try {
            String response = HttpUtil.get(SEARCH_API_URL, params);
            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            if (organicResults == null || organicResults.isEmpty()) {
                return "No results found.%n%n".formatted();
            }

            int limit = Math.min(5, organicResults.size());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < limit; i++) {
                JSONObject result = organicResults.getJSONObject(i);
                sb.append("Result %d:%nTitle: %s%nLink: %s%nSnippet: %s%n%n".formatted(
                        i + 1,
                        result.getStr("title"),
                        result.getStr("link"),
                        result.getStr("snippet")));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Search failed: %s%n%n".formatted(e.getMessage());
        }
    }
}
