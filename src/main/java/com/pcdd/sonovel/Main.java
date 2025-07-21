package com.pcdd.sonovel;

import cn.hutool.core.lang.Console;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import com.openhtmltopdf.util.XRLog;
import com.pcdd.sonovel.action.CheckUpdateAction;
import com.pcdd.sonovel.context.HttpClientContext;
import com.pcdd.sonovel.core.OkHttpClientFactory;
import com.pcdd.sonovel.launch.CliLauncher;
import com.pcdd.sonovel.launch.TuiLauncher;
import com.pcdd.sonovel.repository.ClientReportRepository;
import com.pcdd.sonovel.util.ConfigWatcher;
import com.pcdd.sonovel.util.EnvUtils;
import picocli.CommandLine;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * <p>
 * Created at 2021/6/10
 * <p>
 * psh: {@code mvnd clean compile; mvn exec:java}
 * <p>
 * bash: {@code mvnd clean compile && mvn exec:java}
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

    public static void main(String[] args) {
        ConfigWatcher.watch();
        HttpClientContext.set(OkHttpClientFactory.create(ConfigWatcher.getConfig(), true));

        new Thread(ClientReportRepository::report).start();
        if (ConfigWatcher.getConfig().getAutoUpdate() == 1) {
            new CheckUpdateAction(5000).execute();
        }

        if (args.length == 0) {
            TuiLauncher.launch(ConfigWatcher.getConfig());
        } else {
            new CommandLine(new CliLauncher()).execute(args);
            System.exit(0);
        }

        HttpClientContext.clear();
    }

}