package com.pcdd.sonovel.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.util.EnvUtils;
import com.pcdd.sonovel.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * @author pcdd
 * Created at 2025/4/17
 */
@AllArgsConstructor
public class PdfMergeHandler implements PostProcessingHandler {

    private final AppConfig config;

    private static File getFontFile() {
        // 获取用户自定义字体文件
        String basePath = EnvUtils.isDev() ? "bundle/fonts/" : "fonts/";
        File fontFile = new File(basePath + "SoNovel.ttf");
        if (fontFile.exists()) {
            return fontFile;
        }

        String os = SystemUtil.getOsInfo().getName().toLowerCase();
        List<String> candidates;

        if (os.contains("windows")) {
            candidates = List.of(
                    "C:/Windows/Fonts/msyh.ttc",
                    "C:/Windows/Fonts/simsun.ttc",
                    "C:/Windows/Fonts/simhei.ttf"
            );
        } else if (os.contains("mac")) {
            candidates = List.of(
                    "/System/Library/Fonts/Supplemental/PingFang.ttc",
                    "/System/Library/Fonts/Supplemental/STHeiti Medium.ttc",
                    "/System/Library/Fonts/Supplemental/STSong.ttc"
            );
        } else if (os.contains("linux")) {
            candidates = List.of(
                    "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
                    "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
                    "/usr/share/fonts/truetype/arphic/ukai.ttc"
            );
        } else {
            throw new UnsupportedOperationException("无法获取 %s 默认字体文件，请自行准备 TTF 格式的中文字体，重命名为 SoNovel.ttf 并放在 fonts 目录下".formatted(os));
        }

        return getFirstAvailableFont(candidates);
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

    public static File getFirstAvailableFont(List<String> paths) {
        for (String path : paths) {
            if (FileUtil.exist(path)) {
                return FileUtil.file(path);
            }
        }
        throw new UnsupportedOperationException("无法获取默认字体文件，请自行准备 TTF 格式的中文字体，重命名为 SoNovel.ttf 并放在 fonts 目录下");

    }

    @SneakyThrows
    @Override
    public void handle(Book book, File saveDir) {
        // 获取 chapter_html 目录下所有 HTML 文件并合并内容
        String htmlContent = getHtmlContentFromDirectory(saveDir);
        String outputPath = StrUtil.format("{}{}({}).pdf",
                config.getDownloadPath() + File.separator,
                book.getBookName(),
                book.getAuthor());
        OutputStream out = new FileOutputStream(outputPath);

        // 使用 openhtmltopdf 合并 HTML 文件并生成 PDF
        new PdfRendererBuilder()
                .useFastMode()
                .useFont(getFontFile(), "SoNovel")
                // 10.3 寸屏幕
                .useDefaultPageSize(7.36f, 9.76f, PdfRendererBuilder.PageSizeUnits.INCHES)
                .withHtmlContent("""
                        <html>
                        <head>
                          <style>
                            body {
                              font-family: 'SoNovel', sans-serif;
                            }
                            p {
                               font-size: 18px;
                               text-indent: 2em;
                               line-height: 1.6;
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


}