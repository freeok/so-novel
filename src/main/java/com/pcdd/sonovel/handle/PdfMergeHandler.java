package com.pcdd.sonovel.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @author pcdd
 * Created at 2025/4/17
 */
@AllArgsConstructor
public class PdfMergeHandler implements PostProcessingHandler {
    private final AppConfig config;

    @SneakyThrows
    @Override
    public void handle(Book book, File saveDir) {
        // 获取字体文件
        String fontPath = System.getProperty("env", "dev").equalsIgnoreCase("dev")
                ? "bundle/fonts/PingFangSC-Regular.ttf"
                : "fonts/PingFangSC-Regular.ttf";
        File fontFile = new File(fontPath);

        // 获取 chapter_html 目录下所有 HTML 文件并合并内容
        String htmlContent = getHtmlContentFromDirectory(saveDir);
        String outputPath = StrUtil.format("{}{}（{}）.pdf",
                config.getDownloadPath() + File.separator,
                book.getBookName(),
                book.getAuthor());
        OutputStream out = new FileOutputStream(outputPath);

        // 使用 openhtmltopdf 合并 HTML 文件并生成 PDF
        new PdfRendererBuilder()
                .useFastMode()
                .useFont(fontFile, "PingFangSC")
                // 10.3 寸屏幕
                .useDefaultPageSize(7.36f, 9.76f, PdfRendererBuilder.PageSizeUnits.INCHES)
                .withHtmlContent("""
                        <html>
                        <head>
                          <style>
                            body {
                              font-family: 'PingFangSC', 'Microsoft YaHei', sans-serif;
                            }
                            p {
                              font-size: 16px;
                              text-indent: 2em;
                            }
                            /* 关键：每个.chapter 类的 div 都从新页开始 */
                            .chapter {
                              page-break-before: always;
                              break-before: page;
                            }
                            /* 避免首章空白页 */
                            .chapter:first-child {
                              page-break-before: avoid;
                              break-before: auto;
                            }
                          </style>
                        </head>
                        <body>
                        %s
                        </body>
                        </html>
                        """.formatted(htmlContent), null)
                .toStream(out)
                .run();
        out.close();
    }

    /**
     * 读取指定目录下所有 HTML 文件内容
     */
    private static String getHtmlContentFromDirectory(File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory path");
        }

        StringBuilder mergedHtml = new StringBuilder();
        FileUtils.sortFilesByName(directory).stream()
                .filter(f -> f.getName().endsWith(".html"))
                .forEach(f -> {
                    String chapterHtml = FileUtil.readUtf8String(f);
                    mergedHtml
                            .append("<div class=\"chapter\">")
                            .append(chapterHtml)
                            .append("</div>");
                });

        return mergedHtml.toString();
    }

}