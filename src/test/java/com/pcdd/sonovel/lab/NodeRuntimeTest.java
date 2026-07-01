package com.pcdd.sonovel.lab;

import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Host;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 测试 Javet 的 Node.js 模式
 *
 * @author pcdd
 * Created at 2026/7/1
 */
class NodeRuntimeTest {

    @Test
    void test() throws Exception {
        try (NodeRuntime nodeRuntime = V8Host.getNodeInstance().createV8Runtime()) {
            System.out.println(nodeRuntime.getVersion());

            // CommonJS 测试代码
            var scripts = List.of(
                    """
                            (() => {
                                console.log("========== 示例1：系统信息 ==========");
                            
                                const os = require("os");
                                const path = require("path");
                            
                                console.log({
                                    platform: os.platform(),
                                    arch: os.arch(),
                                    cpuCount: os.cpus().length,
                                    cwd: process.cwd(),
                                    home: os.homedir(),
                                    file: path.join("a", "b", "c.txt")
                                });
                            })();
                            """,

                    """
                            (() => {
                                console.log("========== 示例2：读取当前目录 ==========");
                            
                                const fs = require("fs");
                                const path = require("path");
                            
                                const files = fs.readdirSync(process.cwd())
                                    .map(name => ({
                                        name,
                                        isDirectory: fs.statSync(path.join(process.cwd(), name)).isDirectory()
                                    }));
                            
                                console.log(files);
                            })();
                            """,

                    """
                            (() => {
                                console.log("========== 示例3：SHA-256 ==========");
                            
                                const fs = require("fs");
                                const crypto = require("crypto");
                            
                                const data = fs.readFileSync("pom.xml");
                            
                                const sha256 = crypto
                                    .createHash("sha256")
                                    .update(data)
                                    .digest("hex");
                            
                                console.log(sha256);
                            })();
                            """,

                    """
                            (() => {
                                     console.log("========== 示例4：HTTPS 请求 ==========");
                            
                                     const { execFileSync } = require("child_process");
                            
                                     const json = execFileSync(
                                         "curl",
                                         ["https://jsonplaceholder.typicode.com/todos/1"],
                                         { encoding: "utf8" }
                                     );
                            
                                     console.log(JSON.parse(json));
                                 })();
                            """,

                    """
                            (() => {
                                console.log("========== 示例5：综合测试 ==========");
                            
                                const os = require("os");
                                const fs = require("fs");
                                const path = require("path");
                                const crypto = require("crypto");
                            
                                const info = {
                                    platform: os.platform(),
                                    release: os.release(),
                                    cpus: os.cpus().length,
                                    memoryGB: (os.totalmem() / 1024 / 1024 / 1024).toFixed(2),
                                    cwd: process.cwd()
                                };
                            
                                const files = fs.readdirSync(process.cwd());
                            
                                const hash = crypto
                                    .createHash("sha256")
                                    .update(files.join(","))
                                    .digest("hex");
                            
                                console.log({
                                    ...info,
                                    fileCount: files.length,
                                    hash,
                                    sample: files.slice(0, 5).map(name => path.basename(name))
                                });
                            })();
                            """
            );

            for (String script : scripts) {
                nodeRuntime.getExecutor(script).executeVoid();
                System.out.println();
            }

        }
    }

}