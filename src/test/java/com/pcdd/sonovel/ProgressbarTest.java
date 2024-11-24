/*
package com.pcdd.sonovel;

import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpUtil;
import me.tongfei.progressbar.ProgressBar;

import java.io.File;

public class ProgressbarTest {

    // ANSI 转义码
    private static final String WHITE_TEXT = "\u001B[37m";       // 白色字体
    private static final String LIGHT_BLUE_BAR = "\u001B[94m";   // 浅蓝色
    private static final String RESET_COLOR = "\u001B[0m";       // 重置颜色

    public static void main(String[] args) {
        String fileUrl = "https://ghp.ci/https://github.com/freeok/so-novel/releases/download/v1.6.1/sonovel-windows.tar.gz"; // 下载文件的 URL
        String targetPath = "/" + System.currentTimeMillis() + "sonovel-windows.tar.gz"; // 本地存储路径

        // 预获取文件大小
        long fileSize = HttpUtil.createGet(fileUrl)
                .timeout(10000)  // 设置超时（可选）
                .execute()
                .contentLength();

        // 设置进度条
        try (ProgressBar progressBar = new ProgressBar("Downloading", fileSize)) {
            // 带进度显示的文件下载
            HttpUtil.downloadFile(fileUrl, new File(targetPath), new StreamProgress() {
                @Override
                public void start() {
                    Console.log("开始下载。。。。");
                }

                @Override
                public void progress(long l, long l1) {
                    progressBar.stepTo(l1); // 更新进度条
                    Console.log(l);
                }

                @Override
                public void finish() {
                    Console.log("下载完成！");
                }
            });
        }

    }
}



*/
