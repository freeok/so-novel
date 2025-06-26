package com.pcdd.sonovel;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import cn.hutool.setting.Setting;
import com.openhtmltopdf.util.XRLog;
import com.pcdd.sonovel.action.*;
import com.pcdd.sonovel.context.HttpClientContext;
import com.pcdd.sonovel.core.OkHttpClientFactory;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.util.EnvUtils;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * <p>
 * Created at 2021/6/10
 * <p>
 * psh: {@code mvnd clean compile; mvn exec:java -Denv=dev}
 * <p>
 * bash: {@code mvnd clean compile && mvn exec:java -Denv=dev}
 */
public class Main {

    static {
        if (EnvUtils.isDev()) {
            Console.log(render("当前为开发环境！", "red"));
        }
        // 关闭 hutool 日志
        ConsoleLog.setLevel(Level.OFF);
        if (EnvUtils.isProd()) {
            // 关闭 openhtmltopdf 日志
            XRLog.listRegisteredLoggers().forEach(logger -> XRLog.setLevel(logger, java.util.logging.Level.OFF));
        }
    }

    private static AppConfig config = ConfigUtils.defaultConfig();

    public static void main(String[] args) {
        watchConfig();
        HttpClientContext.set(OkHttpClientFactory.create(config, true));

        if (config.getAutoUpdate() == 1) {
            new CheckUpdateAction(5000).execute();
        }
        inputMode();

        HttpClientContext.clear();
    }

    @SneakyThrows
    private static void inputMode() {
        Scanner sc = Console.scanner();
        printHint();

        while (true) {
            Console.log("\n" + """
                    q.聚合搜索\tw.指定搜索\te.批量下载
                    a.书源一览\ts.版本信息\td.配置信息
                    z.结束程序\tx.检查更新
                    """);
            Console.print(render("==> 请输入功能序号: ", "green"));
            String cmd = sc.nextLine().strip();

            switch (cmd.toLowerCase()) {
                case "q":
                    new AggregatedSearchAction().execute();
                    break;
                case "w":
                    new SingleSearchAction(config).execute();
                    break;
                case "e":
                    new BatchDownloadAction(config).execute();
                    break;

                case "a":
                    new ShowSourcesAction().execute();
                    break;
                case "s":
                    printHint();
                    break;
                case "d":
                    Console.log(JSONUtil.toJsonPrettyStr(config));
                    break;

                case "z":
                    Console.log("<== Bye :)");
                    System.exit(0);
                    return;
                case "x":
                    new CheckUpdateAction().execute();
                    break;

                default:
                    Console.log(render("无效的功能序号，请重新输入", "yellow"));
                    break;
            }
        }
    }

    private static void printHint() {
        Console.log(ResourceUtil.readUtf8Str("ascii-logo.txt"));
        ConsoleTable.create()
                // 是否转为全角
                .setSBCMode(false)
                .addHeader(render(StrUtil.format(" version {} ", config.getVersion()), "BG_BLUE", "ITALIC", "BOLD") + render(" 本项目开源且免费 ", "BG_MAGENTA", "BOLD"))
                .addHeader("导出格式: " + config.getExtName().toLowerCase())
                .addHeader("下载路径: " + new File(config.getDownloadPath()).getAbsolutePath())
                .addBody(render("使用前请务必阅读 readme.txt", "yellow"))
                .print();
    }

    private static void watchConfig() {
        String path;
        String configFilePath = System.getProperty("config.file");

        if (!FileUtil.exist(configFilePath)) {
            path = System.getProperty("user.dir") + File.separator + ConfigUtils.resolveConfigFileName();
        } else {
            path = Paths.get(configFilePath).toAbsolutePath().toString();
        }

        Setting setting = new Setting(path);
        // 监听配置文件
        setting.autoLoad(true, aBoolean -> {
            config = ConfigUtils.defaultConfig();
            Console.log("<== 配置文件修改成功！");
            printHint();
        });
    }

}