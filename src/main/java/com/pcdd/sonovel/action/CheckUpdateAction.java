package com.pcdd.sonovel.action;

import cn.hutool.core.comparator.VersionComparator;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.util.FileUtils;
import com.pcdd.sonovel.util.RandomUA;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

import java.io.File;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2024/11/10
 */
public class CheckUpdateAction {

    public static final String GHP = "https://ghproxy.net/";
    public static final String RELEASE_URL = "https://api.github.com/repos/freeok/so-novel/releases";
    public static final String ASSETS_URL = "https://github.com/freeok/so-novel/releases/download/{}/sonovel-{}.tar.gz";
    private final int timeoutMills;

    public CheckUpdateAction() {
        this.timeoutMills = 5_000;
    }

    public CheckUpdateAction(int timeout) {
        this.timeoutMills = timeout;
    }

    public void execute() {
        Console.log("<== 检查更新中...");

        try (HttpResponse resp = HttpUtil.createGet(RELEASE_URL)
                .timeout(timeoutMills)
                .header(Header.USER_AGENT, RandomUA.generate())
                .execute()) {

            Props sys = ConfigUtils.sys();
            String jsonStr = resp.body();
            JSONArray arr = JSONUtil.parseArray(jsonStr);
            JSONObject latest = JSONUtil.parseObj(arr.get(0));
            // v1.7.0
            String currentVersion = "v" + sys.getStr("version");
            // v1.7.0-beta.2
            String latestVersion = latest.get("tag_name", String.class);
            String latestUrl = latest.get("html_url", String.class);

            if (VersionComparator.INSTANCE.compare(currentVersion, latestVersion) < 0) {
                Console.log("<== 发现新版本: {} ({})", latestVersion, latestUrl);
                download(getDownloadUrl(latestVersion));
            } else {
                Console.log("<== {} 已是最新版本！({})", latestVersion, latestUrl);
            }
        } catch (Exception e) {
            Console.log(render("\n<== 更新失败，当前网络环境无法访问 GitHub，请稍后再试 ({})", "red"), e.getMessage());
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
        // 预获取文件大小
        long fileSize = FileUtils.fileSize(url);
        // 设置进度条
        ProgressBar bar = ProgressBar.builder()
                .setTaskName("Downloading new version")
                .setInitialMax(fileSize)
                .setStyle(ProgressBarStyle.ASCII)
                .setMaxRenderedLength(100)
                .setUpdateIntervalMillis(10)
                .build();
        //  下载到上一级路径
        File file = new File(System.getProperty("user.dir")).getParentFile();

        // 带进度显示的文件下载
        HttpUtil.downloadFile(url, file, new StreamProgress() {
            @Override
            public void start() {
                bar.setExtraMessage("Downloading ...");
            }

            @Override
            public void progress(long total, long read) {
                // 更新进度条
                bar.stepTo(read);
            }

            @Override
            public void finish() {
                bar.setExtraMessage("Done!");
            }
        });

        bar.close();
        Console.log("<== 下载位置: {}", file + File.separator + FileUtil.getName(url));
    }

}