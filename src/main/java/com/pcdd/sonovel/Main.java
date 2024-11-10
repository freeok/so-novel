package com.pcdd.sonovel;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import com.pcdd.sonovel.action.CheckUpdateAction;
import com.pcdd.sonovel.action.DownloadAction;
import com.pcdd.sonovel.util.Settings;
import lombok.SneakyThrows;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.List;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2021/6/10 16:18
 */
public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        List<String> options = List.of("下载小说", "结束程序", "检查更新");
        Terminal terminal = TerminalBuilder.terminal();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new StringsCompleter(options))
                .build();
        String prompt = "根据需要选择 (按 Tab 键完成): ";

        printHint();
        while (true) {
            String cmd = reader.readLine(prompt).trim();
            if (!options.contains(cmd)) {
                Console.error("无效的选项，请重新选择。");
            }

            if ("下载小说".equals(cmd)) {
                new DownloadAction().execute(terminal);
            }
            if ("检查更新".equals(cmd)) {
                new CheckUpdateAction().execute(terminal);
            }
            if ("结束程序".equals(cmd)) {
                Console.log("<== bye :)");
                break;
            }
        }

        terminal.close();
    }

    private static void printHint() {
        Props sys = Settings.sys();
        Props usr = Settings.usr();
        Console.table(ConsoleTable.create()
                // 是否转为全角
                .setSBCMode(false)
                .addHeader(render(StrUtil.format("@|BG_blue,ITALIC,BOLD  so-novel v{} |@", sys.getStr("version"))) + "（本项目开源且免费）")
                .addHeader("官方地址：https://github.com/freeok/so-novel")
                .addHeader("当前书源：" + sys.getStr("index_url"))
                .addHeader(render("导出格式：@|blue " + usr.getStr("extName") + "|@"))
                .addBody("使用须知")
                .addBody("1. 下载受书源、网络等因素影响，若下载失败可尝试增大爬取间隔")
                .addBody("2. 请按要求输入，然后按回车（Enter）执行")
                .addBody("3. 可输入 exit 结束程序")
        );
    }

}
