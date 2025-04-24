package com.pcdd.sonovel;

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
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.util.JsoupUtils;
import lombok.SneakyThrows;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.util.List;
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
        if (System.getProperty("env", "dev").equalsIgnoreCase("prod")) {
            // 关闭 openhtmltopdf 日志
            XRLog.listRegisteredLoggers().forEach(logger -> XRLog.setLevel(logger, java.util.logging.Level.OFF));
        }
        // 关闭 hutool 日志
        ConsoleLog.setLevel(Level.OFF);
        JsoupUtils.trustAllSSL();
    }

    private static AppConfig config = ConfigUtils.config();

    public static void main(String[] args) {
        watchConfig();
        if (config.getAutoUpdate() == 1) {
            new CheckUpdateAction(5000).execute();
        }
        if (config.getInteractiveMode() == 1) {
            inputMode();
        } else if (config.getInteractiveMode() == 2) {
            selectMode();
        } else {
            inputMode();
        }
    }

    @SneakyThrows
    private static void inputMode() {
        Scanner sc = Console.scanner();
        printHint();

        while (true) {
            Console.log("\n" + """
                    q.聚合搜索\tw.搜索指定书源\te.批量下载
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
                    Console.error("无效的序号，请重新输入");
                    break;
            }
        }
    }

    @SneakyThrows
    private static void selectMode() {
        List<String> options = List.of(
                "a.结束程序",
                "b.聚合搜索",
                "c.检查更新",
                "d.书源一览",
                "e.版本信息",
                "f.配置信息",
                "g.批量下载",
                "h.搜索指定书源"
        );

        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new StringsCompleter(options))
                .build();

        printHint();

        while (true) {
            String cmd = reader.readLine("按 Tab 键选择功能: ").strip();

            if (!options.contains(cmd)) {
                Console.error("无效的选项，请重新选择");
            }
            if (options.get(0).equals(cmd)) {
                Console.log("<== Bye :)");
                System.exit(0);
                break;
            }
            if (options.get(1).equals(cmd)) {
                new AggregatedSearchAction().execute();
            }
            if (options.get(2).equals(cmd)) {
                new CheckUpdateAction().execute();
            }
            if (options.get(3).equals(cmd)) {
                new ShowSourcesAction().execute();
            }
            if (options.get(4).equals(cmd)) {
                printHint();
            }
            if (options.get(5).equals(cmd)) {
                Console.log(JSONUtil.toJsonPrettyStr(config));
            }
            if (options.get(6).equals(cmd)) {
                new BatchDownloadAction(config).execute();
            }
            if (options.get(7).equals(cmd)) {
                new SingleSearchAction(config).execute();
            }
        }

        terminal.close();
    }

    private static void printHint() {
        Console.log(ResourceUtil.readUtf8Str("ascii-logo.txt"));
        Rule r = new Source(config).rule;
        ConsoleTable.create()
                // 是否转为全角
                .setSBCMode(false)
                .addHeader(render(StrUtil.format(" version {} ", config.getVersion()), "BG_BLUE", "ITALIC", "BOLD") + render(" 本项目开源且免费 ", "BG_MAGENTA", "BOLD"))
                .addHeader("导出格式: " + config.getExtName().toLowerCase())
                .addHeader("下载路径: " + new File(config.getDownloadPath()).getAbsolutePath())
                .addHeader(StrUtil.format("指定书源: {} (ID: {})", r.getName(), r.getId()))
                .addBody(render("使用前请务必阅读 readme.txt", "yellow"))
                .print();
    }

    private static void watchConfig() {
        String path = System.getProperty("user.dir") + File.separator + ConfigUtils.resolveConfigFileName();
        Setting setting = new Setting(path);
        // 监听配置文件
        setting.autoLoad(true, aBoolean -> {
            config = ConfigUtils.config();
            Console.log("<== 配置文件修改成功！");
            printHint();
        });
    }

}