package com.example.aiagent.agent;

import cn.hutool.core.text.CharSequenceUtil;
import com.example.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 提示
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int maxSteps = 10;
    private int currentStep = 0;

    // LLM
    private ChatClient chatClient;

    // Memory（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        // 1.基础校验
        if (state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + state);
        }
        if (CharSequenceUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        // 2.执行 先更改状态
        state = AgentState.RUNNING;
        // 记录上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            // 执行循环
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                currentStep = i + 1;
                log.info("Executing step {}/{}", currentStep, maxSteps);
                // 单步执行
                String stepResult = step();
                String result = "**Step " + currentStep + "：**\n\n" + stepResult;
                results.add(result);
            }
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("**已终止：** 已达到最大步数 (" + maxSteps + ")");
            }
            return String.join("\n\n---\n\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "Error: " + e.getMessage();
        } finally {
            // 3.清理资源
            cleanup();
        }
    }

    /**
     * 运行代理（流式输出）
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public SseEmitter runStream(String userPrompt) {
        // 设置一个超时时间为 5 分钟的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(300000L);
        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            // 1.基础校验
            try {
                if (state != AgentState.IDLE) {
                    sseEmitter.send("Cannot run agent from state: " + state);
                    sseEmitter.complete();
                    return;
                }
                if (CharSequenceUtil.isBlank(userPrompt)) {
                    sseEmitter.send("Cannot run agent with empty user prompt");
                    sseEmitter.complete();
                    return;
                }
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
            // 2.执行 先更改状态
            state = AgentState.RUNNING;
            // 记录上下文
            messageList.add(new UserMessage(userPrompt));
            // 保存结果列表
            List<String> results = new ArrayList<>();
            try {
                // 执行循环
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    currentStep = i + 1;
                    log.info("Executing step {}/{}", currentStep, maxSteps);
                    // 单步执行
                    String stepResult = step();
                    String result = "**Step " + currentStep + "：**\n\n" + stepResult;
                    results.add(result);
                    sseEmitter.send(result);
                }
                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    String terminateMsg = "**已终止：** 已达到最大步数 (" + maxSteps + ")";
                    results.add(terminateMsg);
                    sseEmitter.send(terminateMsg);
                }
                // 正常完成
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("Error executing agent", e);
                try {
                    sseEmitter.send("Error: " + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                // 3.清理资源
                cleanup();
            }
        });
        // 设置超时时间
        sseEmitter.onTimeout(() -> {
            state = AgentState.ERROR;
            cleanup();
            log.warn("SSE connection timeout");
        });
        // 设置完成回调
        sseEmitter.onCompletion(() -> {
            if (state == AgentState.RUNNING) {
                state = AgentState.FINISHED;
            }
            cleanup();
            log.info("SSE connection completed");
        });
        return sseEmitter;
    }

    /**
     * 执行单个步骤
     *
     * @return 步骤执行结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }
}
