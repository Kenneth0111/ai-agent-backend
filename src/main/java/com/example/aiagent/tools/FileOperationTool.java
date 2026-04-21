package com.example.aiagent.tools;

import com.example.aiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件操作工具类（提供文件读写功能）
 */
public class FileOperationTool {

    private static final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "The file name to read") String fileName) {
        Path filePath = Paths.get(FILE_DIR, fileName);
        if (!Files.exists(filePath)) {
            return "File not found: " + fileName;
        }
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "Failed to read file: " + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file")
    public String writeFile(
            @ToolParam(description = "The file name to write") String fileName,
            @ToolParam(description = "The content to write") String content) {
        try {
            Path dirPath = Paths.get(FILE_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            Path filePath = dirPath.resolve(fileName);
            Files.writeString(filePath, content, StandardCharsets.UTF_8);
            return "File written successfully: " + filePath.toAbsolutePath();
        } catch (IOException e) {
            return "Failed to write file: " + e.getMessage();
        }
    }
}
