package com.pcdd.sonovel;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import cn.hutool.setting.Setting;
import com.pcdd.sonovel.action.CheckUpdateAction;
import com.pcdd.sonovel.action.DownloadAction;
import com.pcdd.sonovel.model.ConfigBean;
import com.pcdd.sonovel.parse.BookParser;
import com.pcdd.sonovel.util.ConfigUtils;
import lombok.SneakyThrows;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2021/6/10
 * <p>
 * IDEA 的终端无法开发 jline 程序，因此在 wt 中运行
 * 启动命令：mvn clean compile exec:java
 */
public class Main {

    private static ConfigBean config = ConfigUtils.config();

    @SneakyThrows
    public static void main(String[] args) {
        ConsoleLog.setLevel(Level.ALL);
        watchConfig();
        if (Boolean.TRUE.equals(config.getAutoUpdate())) {
            new CheckUpdateAction().execute();
        }
        run();
    }

    private static void run() throws IOException {
        List<String> options = List.of("1.下载小说", "2.检查更新", "3.查看配置文件", "4.使用须知", "5.结束程序");
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
                new DownloadAction(config).execute(terminal);
            }
            if (options.get(1).equals(cmd)) {
                new CheckUpdateAction().execute();
            }
            if (options.get(2).equals(cmd)) {
                Console.log(config);
            }
            if (options.get(3).equals(cmd)) {
                printHint();
            }
            if (options.get(4).equals(cmd)) {
                Console.log("<== Bye :)");
                System.exit(0);
                break;
            }
        }

        terminal.close();
    }

    private static void printHint() {
        Console.table(ConsoleTable.create()
                // 是否转为全角
                .setSBCMode(false)
                .addHeader(render(StrUtil.format("@|BG_blue,ITALIC,BOLD  so-novel v{} |@", config.getVersion())) + "（本项目开源且免费）")
                .addHeader("官方地址：https://github.com/freeok/so-novel")
                .addHeader(StrUtil.format("当前书源：{} (ID: {})", new BookParser(config.getSourceId()).rule.getUrl(), config.getSourceId()))
                .addHeader(render("导出格式：@|blue " + config.getExtName() + "|@"))
                .addBody("使用须知")
                .addBody("1. 请按要求操作，然后按 Enter 键执行")
                .addBody("2. 下载受书源、网络环境等因素影响。若出现 xx timed out，建议检查网络环境或稍后再试")
                .addBody("3. 若章节下载失败，可尝试增大爬取间隔，直至合适为止")
                .addBody("4. 若认为下载速度较慢，可适当减小爬取间隔，直至合适为止")
                .addBody("5. 爬取间隔过小会导致当前 IP 被有反爬机制的书源限流，可能短时间内将无法使用该书源")
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
