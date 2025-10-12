package com.pcdd.sonovel.repository;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.ClientReport;
import com.pcdd.sonovel.util.ConfigWatcher;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;

/**
 * 仅限代理用户
 */
@UtilityClass
public class ClientReportRepository {

    private final String WORKERS_URL;
    private final int MAX_RETRIES = 3;
    private final long RETRY_DELAY_MS = 3000;

    static {
        WORKERS_URL = Base64.decodeStr("aHR0cHM6Ly9zb25vdmVsLWQxLmhlbGxvLXBjZGQud29ya2Vycy5kZXY");
    }

    /**
     * 报告客户端信息
     */
    public static void report() {
        int retries = 0;
        boolean success = false;

        while (retries < MAX_RETRIES && !success) {
            try {
                ClientReport report = new ClientReport();
                report.setUsername(System.getProperty("user.name"));
                report.setHostName(System.getenv("COMPUTERNAME"));
                report.setMacAddress(NetUtil.getMacAddress(InetAddress.getLocalHost()));
                report.setOsName(System.getProperty("os.name"));
                report.setOsArch(System.getProperty("os.arch"));
                report.setOsVersion(System.getProperty("os.version"));
                report.setLocalIp(NetUtil.getLocalhostStr());
                report.setAppVersion(ConfigWatcher.getConfig().getVersion());
                String now = DateUtil.now();
                report.setCreatedAt(now);
                report.setUpdatedAt(now);

                HttpResponse resp = HttpRequest.post(WORKERS_URL + "/report")
                        .header("Content-Type", "application/json")
                        .body(JSONUtil.toJsonStr(report))
                        .execute();
                resp.close();
                success = true;

            } catch (Exception e) {
                retries++;
                if (retries < MAX_RETRIES) {
                    // 如果还有重试机会，则等待一段时间
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); // 重新设置中断状态
                        break; // 线程中断，立即停止重试
                    }
                }
            }
        }
    }

}