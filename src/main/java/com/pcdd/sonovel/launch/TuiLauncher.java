package com.pcdd.sonovel.launch;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.action.*;
import com.pcdd.sonovel.model.AppConfig;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.Scanner;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * 基于文本的用户界面 (Text-based User Interface)
 *
 * @author pcdd
 * Created at 2025/7/10
 */
@UtilityClass
public class TuiLauncher {

    public void launch(AppConfig config) {
        Scanner sc = Console.scanner();
        printHint(config);

        while (true) {
            Console.log("\n" + """
                    q.聚合搜索\tw.独立搜索\te.批量下载
                    a.书源一览\ts.版本信息\td.配置信息
                    z.结束程序\tx.检查更新
                    """);
            Console.print(render("==> 请输入功能序号: ", "green"));
            String cmd = sc.nextLine().strip();

            switch (cmd.toLowerCase()) {
                case "q" -> new AggregatedSearchAction().execute();
                case "w" -> new SingleSearchAction(config).execute();
                case "e" -> new BatchDownloadAction(config).execute();
                case "a" -> new ShowSourcesAction().execute();
                case "s" -> printHint(config);
                case "d" -> Console.log(JSONUtil.toJsonPrettyStr(config));
                case "z" -> {
                    Console.log("<== Bye :)");
                    System.exit(0);
                }
                case "x" -> new CheckUpdateAction().execute();
                default -> Console.log(render("无效的功能序号，请重新输入", "yellow"));
            }
        }
    }

    public void printHint(AppConfig config) {
        Console.log(ResourceUtil.readUtf8Str("ascii-logo.txt"));
        ConsoleTable.create()
                .setSBCMode(false)
                .addHeader(render(StrUtil.format(" version {} ", config.getVersion()), "BG_BLUE", "ITALIC", "BOLD") + render(" 本项目开源且免费 ", "BG_MAGENTA", "BOLD"))
                .addHeader("激活规则: " + config.getActiveRules())
                .addHeader("导出格式: " + config.getExtName().toLowerCase())
                .addHeader("下载路径: " + new File(config.getDownloadPath()).getAbsolutePath())
                .addBody(render("首次使用请务必阅读 readme.txt，配置文件是 config.ini", "yellow"))
                .print();
    }

}