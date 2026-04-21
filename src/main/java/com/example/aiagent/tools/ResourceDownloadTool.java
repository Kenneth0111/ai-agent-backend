package com.example.aiagent.tools;

import com.example.aiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 资源下载工具类（提供URL资源下载功能）
 */
public class ResourceDownloadTool {

    private static final String DOWNLOAD_DIR = FileConstant.FILE_SAVE_DIR + "/download";

    @Tool(description = "Download a file from a URL and save it locally")
    public String downloadResource(
            @ToolParam(description = "The URL of the resource to download") String url,
            @ToolParam(description = "The file name to save as") String fileName) {
        try {
            Path dirPath = Paths.get(DOWNLOAD_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            Path filePath = dirPath.resolve(fileName);
            URL downloadUrl = URI.create(url).toURL();
            try (InputStream in = downloadUrl.openStream()) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            return "File downloaded successfully: " + filePath.toAbsolutePath();
        } catch (IOException e) {
            return "Failed to download file: " + e.getMessage();
        }
    }
}
