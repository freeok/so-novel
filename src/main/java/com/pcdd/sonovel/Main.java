package com.pcdd.sonovel;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import com.openhtmltopdf.util.XRLog;
import com.pcdd.sonovel.action.CheckUpdateAction;
import com.pcdd.sonovel.context.HttpClientContext;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.core.OkHttpClientFactory;
import com.pcdd.sonovel.launch.CliLauncher;
import com.pcdd.sonovel.launch.TuiLauncher;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.repository.ClientReportRepository;
import com.pcdd.sonovel.util.EnvUtils;
import com.pcdd.sonovel.web.WebServer;
import picocli.CommandLine;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

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

    private static final AppConfig APP_CONFIG = AppConfigLoader.APP_CONFIG;

    static {
        if (EnvUtils.isDev()) {
            Console.log(render("当前为开发环境！", "red"));
            Console.log("-".repeat(100));
            RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
            Console.log("JVM 名称: " + mxBean.getVmName());
            Console.log("JVM 供应商: " + mxBean.getVmVendor());
            Console.log("JVM 版本: " + mxBean.getVmVersion());
            Console.log("启动参数: " + mxBean.getInputArguments());
            // Console.log("类路径: " + mxBean.getClassPath());
            Console.log("-".repeat(100));
        }
        // 关闭 hutool 日志
        ConsoleLog.setLevel(Level.OFF);
        if (EnvUtils.isProd()) {
            // 关闭 openhtmltopdf 日志
            XRLog.listRegisteredLoggers().forEach(logger -> XRLog.setLevel(logger, java.util.logging.Level.OFF));
        }
    }

    public static void main(String[] args) {
        HttpClientContext.set(OkHttpClientFactory.create(APP_CONFIG));

        new Thread(ClientReportRepository::report).start();
        if (APP_CONFIG.getAutoUpdate() == 1) {
            new CheckUpdateAction(5000).execute();
        }

        String mode = System.getProperty("mode", "tui");

        if ("web".equalsIgnoreCase(mode) || APP_CONFIG.getWebEnabled() == 1) {
            new WebServer().start();
        } else if (args.length == 0 && "tui".equalsIgnoreCase(mode)) {
            TuiLauncher.launch(APP_CONFIG);
        } else if (args.length > 0 || "cli".equalsIgnoreCase(mode)) {
            new CommandLine(new CliLauncher()).execute(ArrayUtil.isEmpty(args) ? new String[]{"-h"} : args);
            System.exit(0);
        } else {
            Console.error("启动失败，请通过 -Dmode=web|tui|cli 指定启动模式");
            System.exit(1);
        }

        HttpClientContext.clear();
    }

}