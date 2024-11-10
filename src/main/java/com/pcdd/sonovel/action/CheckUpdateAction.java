package com.pcdd.sonovel.action;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.pcdd.sonovel.util.Settings;
import lombok.SneakyThrows;
import me.tongfei.progressbar.ProgressBar;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class CheckUpdateAction implements Action {

    public static final String GHP = "https://ghp.ci/";
    public static final String ASSETS_URL = "https://github.com/freeok/so-novel/releases/download/{}/sonovel-{}.tar.gz";

    @SneakyThrows
    @Override
    public void execute(Terminal terminal) {
        List<String> options = List.of("1.自动更新", "2.去官网下载");
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new StringsCompleter(options))
                .build();
        String cmd = reader.readLine("==> 按 Tab 键选择更新方式：").trim();

        if (options.get(1).equals(cmd)) {
            Desktop desktop = Desktop.getDesktop();
            if (!Desktop.isDesktopSupported()) {
                Console.log("当前平台不支持 java.awt.Desktop");
            }
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                Console.log("当前系统不支持打开浏览器功能。");
            }
            desktop.browse(URLUtil.toURI("https://github.com/freeok/so-novel/releases"));
        }

        if (options.get(0).equals(cmd)) {
            Console.log("<== 检查更新中...");

            Props sys = Settings.sys();
            String url = "https://api.github.com/repos/freeok/so-novel/releases";
            JSONArray arr = JSONUtil.parseArray(HttpUtil.get(url));
            JSONObject latest = JSONUtil.parseObj(arr.get(0));
            String latestVersion = latest.get("tag_name", String.class);
            String currentVersion = "v" + sys.getStr("version");

            if (latestVersion.compareTo(currentVersion) > 0) {
                Console.log("<== 发现新版本：{}", latest.get("tag_name", String.class));
                download(getDownloadUrl(latestVersion));

            } else {
                Console.log("<== 已是最新版本！");
            }
        }
    }

    private String getDownloadUrl(String version) {
        OsInfo osInfo = SystemUtil.getOsInfo();
        String osName = osInfo.getName();
        String arch = osInfo.getArch();
        String fileName = "windows";

        if (osName.contains("Windows")) {
            fileName = "windows";
        } else if (osName.contains("Mac")) {
            // 根据架构进一步细分
            fileName = "aarch64".equals(arch) ? "macos_arm64" : "macos_x64";
        } else if (osName.contains("Linux")) {
            fileName = "linux";
        }

        return GHP + StrUtil.format(ASSETS_URL, version, fileName);
    }

    private void download(String url) {
        // HEAD 请求不会下载文件内容，只会返回文件的元数据（例如文件大小）
        long fileSize = HttpUtil.createRequest(Method.HEAD, url)
                .timeout(10_000)
                .execute()
                .contentLength();
        // 设置进度条
        ProgressBar pb = new ProgressBar("Downloading", fileSize);

        // 弹出保存文件对话框，获取保存路径
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择下载位置");
        // 设置保存模式
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // 显示保存对话框
        int selection = fileChooser.showSaveDialog(null);
        // 如果用户选择了保存路径
        if (selection == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            // 带进度显示的文件下载
            HttpUtil.downloadFile(url, selectedDirectory, new StreamProgress() {
                @Override
                public void start() {
                    pb.setExtraMessage("下载中...");
                }

                @Override
                public void progress(long total, long step) {
                    // 更新进度条
                    pb.stepTo(step);
                }

                @Override
                public void finish() {
                    pb.setExtraMessage("下载完成");
                    pb.close();
                    Console.log("<== 文件下载位置：" + selectedDirectory + FileUtil.getName(url));
                }
            });
        }
    }

}
