package com.pcdd.sonovel;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.action.CheckUpdateAction;
import com.pcdd.sonovel.action.DownloadAction;
import com.pcdd.sonovel.parse.BookParser;
import lombok.SneakyThrows;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.List;

import static com.pcdd.sonovel.util.ConfigConst.*;
import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2021/6/10 16:18
 * <p>
 * IDEA 的终端无法开发 jline 程序，因此在 wt 中运行
 * 启动命令：mvn clean compile exec:java
 */
public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        List<String> options = List.of("1.下载小说", "2.检查更新", "3.查看配置文件", "4.结束程序");
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
                new DownloadAction().execute(terminal);
            }
            if (options.get(1).equals(cmd)) {
                new CheckUpdateAction().execute(terminal);
            }
            if (options.get(2).equals(cmd)) {
                Console.log(cm.getUsr());
            }
            if (options.get(3).equals(cmd)) {
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
                .addHeader(render(StrUtil.format("@|BG_blue,ITALIC,BOLD  so-novel v{} |@", VERSION)) + "（本项目开源且免费）")
                .addHeader("官方地址：https://github.com/freeok/so-novel")
                .addHeader(StrUtil.format("当前书源：{} (ID: {})", new BookParser(SOURCE_ID).rule.getUrl(), SOURCE_ID))
                .addHeader(render("导出格式：@|blue " + EXT_NAME + "|@"))
                .addBody("使用须知")
                .addBody("1. 下载受书源、网络等因素影响，若下载失败可尝试修改爬取间隔，直至合适为止")
                .addBody("2. 请按要求操作，然后按 Enter 键执行")
        );
    }

}
