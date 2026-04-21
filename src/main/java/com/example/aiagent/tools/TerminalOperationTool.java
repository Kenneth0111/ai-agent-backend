package com.example.aiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * 终端操作工具类（提供 Windows 命令执行功能）
 */
public class TerminalOperationTool {

    @Tool(description = "Execute a command in Windows terminal and return the output")
    public String executeCommand(@ToolParam(description = "The command to execute in Windows cmd") String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("GBK")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Process exited with code: ").append(exitCode);
            }
            return output.toString();
        } catch (Exception e) {
            return "Failed to execute command: " + e.getMessage();
        }
    }
}
