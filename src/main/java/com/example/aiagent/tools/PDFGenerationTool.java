package com.example.aiagent.tools;

import com.example.aiagent.constant.FileConstant;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * PDF 生成工具类
 */
public class PDFGenerationTool {

    private static final String PDF_DIR = FileConstant.FILE_SAVE_DIR + "/pdf";

    @Tool(description = "Generate a PDF file with the given text content")
    public String generatePDF(
            @ToolParam(description = "The PDF file name (without .pdf extension)") String fileName,
            @ToolParam(description = "The text content to write into the PDF") String content) {
        try {
            Path dirPath = Paths.get(PDF_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            Path filePath = dirPath.resolve(fileName + ".pdf");

            try (PdfWriter writer = new PdfWriter(filePath.toString());
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document document = new Document(pdfDoc)) {

                PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
                document.setFont(font);
                document.setFontSize(12);

                for (String line : content.split("\n")) {
                    document.add(new Paragraph(line));
                }
            }

            return "PDF generated successfully: " + filePath.toAbsolutePath();
        } catch (Exception e) {
            return "Failed to generate PDF: " + e.getMessage();
        }
    }
}
