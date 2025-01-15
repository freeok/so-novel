package com.pcdd.sonovel;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import cn.hutool.setting.Setting;
import com.pcdd.sonovel.action.CheckUpdateAction;
import com.pcdd.sonovel.action.DownloadAction;
import com.pcdd.sonovel.action.ShowSourcesAction;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.ConfigUtils;
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
 * Created at 2021/6/10
 * <p>
 * {@code mvnd clean compile && mvn exec:java}
 * <p>
 * {@code mvnd clean compile; mvn exec:java}
 */
public class Main {

    public static final List<String> options = List.of(
            "0.结束程序",
            "1.下载小说",
            "2.检查更新",
            "3.书源一览",
            "4.使用须知",
            "5.查看配置文件"
    );

    static {
        // release 前改为 Level.OFF
        ConsoleLog.setLevel(Level.OFF);
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
        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();
        Scanner sc = Console.scanner();
        printHint();

        while (true) {
            Console.log("\n" + StrUtil.join(" ", options));
            System.out.print("==> 请输入功能序号: ");
            String cmd = sc.nextLine();

            if ("0".equals(cmd)) {
                Console.log("<== Bye :)");
                System.exit(0);
                break;
            } else if ("1".equals(cmd)) {
                new DownloadAction(config).execute(terminal);
            } else if ("2".equals(cmd)) {
                new CheckUpdateAction().execute();
            } else if ("3".equals(cmd)) {
                new ShowSourcesAction().execute();
            } else if ("4".equals(cmd)) {
                printHint();
            } else if ("5".equals(cmd)) {
                Console.log(JSONUtil.toJsonPrettyStr(config));
            } else {
                Console.error("无效的选项，请重新输入");
            }
        }
    }

    @SneakyThrows
    private static void selectMode() {
        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new StringsCompleter(options))
                .build();

        printHint();

        while (true) {
            String cmd = reader.readLine("按 Tab 键选择功能: ").trim();

            if (!options.contains(cmd)) {
                Console.error("无效的选项，请重新选择");
            }
            if (options.get(0).equals(cmd)) {
                Console.log("<== Bye :)");
                System.exit(0);
                break;
            }
            if (options.get(1).equals(cmd)) {
                new DownloadAction(config).execute(terminal);
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
        }

        terminal.close();
    }

    private static void printHint() {
        Rule rule = new Source(config).rule;
        Console.table(ConsoleTable.create()
                // 是否转为全角
                .setSBCMode(false)
                .addHeader(render(StrUtil.format("@|BG_blue,ITALIC,BOLD  so-novel v{} |@", config.getVersion())) + "（本项目开源且免费）")
                .addHeader("官方地址：https://github.com/freeok/so-novel")
                .addHeader(StrUtil.format("当前书源：{} ({} ID: {})", rule.getUrl(), rule.getName(), rule.getId()))
                .addHeader(render("导出格式：@|blue " + config.getExtName() + "|@"))
                .addBody(render("@|yellow 使用前请务必阅读 readme.txt|@"))
        );
    }

    private static void watchConfig() {
        String path = System.getProperty("user.dir") + File.separator + "config.ini";
        Setting setting = new Setting(path);
        // 监听配置文件
        setting.autoLoad(true, aBoolean -> {
            config = ConfigUtils.config();
            Console.log("<== 配置文件修改成功！");
            printHint();
        });
    }

}