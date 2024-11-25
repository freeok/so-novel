package com.pcdd.sonovel.action;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
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
import org.jline.terminal.Terminal;

import java.io.File;

public class CheckUpdateAction implements Action {

    public static final String GHP = "https://ghp.ci/";
    public static final String ASSETS_URL = "https://github.com/freeok/so-novel/releases/download/{}/sonovel-{}.tar.gz";

    @SneakyThrows
    @Override
    public void execute(Terminal terminal) {
        Console.log("<== 检查更新中...");

        Props sys = Settings.sys();
        String url = "https://api.github.com/repos/freeok/so-novel/releases";
        JSONArray arr = JSONUtil.parseArray(HttpUtil.get(url));
        JSONObject latest = JSONUtil.parseObj(arr.get(0));
        String currentVersion = "v" + sys.getStr("version");
        String latestVersion = latest.get("tag_name", String.class);
        String latestUrl = latest.get("html_url", String.class);

        if (latestVersion.compareTo(currentVersion) > 0) {
            Console.log("<== 发现新版本: {} ({})", latestVersion, latestUrl);
            download(getDownloadUrl(latestVersion));
        } else {
            Console.log("<== {} 已是最新版本！({})", latestVersion, latestUrl);
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
        ProgressBar pb = new ProgressBar("下载最新版", fileSize);

        //  下载到上一级路径
        File file = new File(System.getProperty("user.dir")).getParentFile();

        // 带进度显示的文件下载
        HttpUtil.downloadFile(url, file, new StreamProgress() {
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
                Console.log("<== 下载位置: {}", file + File.separator + FileUtil.getName(url));
            }
        });
    }

}
