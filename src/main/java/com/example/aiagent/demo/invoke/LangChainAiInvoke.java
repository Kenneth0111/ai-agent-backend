package com.example.aiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LangChainAiInvoke {

    public static void main(String [] args) {
        ChatLanguageModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-max")
                .build();
        String chat = qwenChatModel.chat("你好，帮我查看一下今天的油价。");
        log.info(chat);
    }
}
