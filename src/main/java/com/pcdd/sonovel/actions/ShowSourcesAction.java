package com.pcdd.sonovel.actions;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import com.pcdd.sonovel.utils.SourceUtils;

/**
 * 书源一览
 *
 * @author pcdd
 * Created at 2025/1/9
 */
public class ShowSourcesAction {

    public void execute() {
        Console.log("<== 测试延迟中...");

        ConsoleTable asciiTables = ConsoleTable.create()
                .setSBCMode(false)
                .addHeader("ID", "书源", "延迟", "状态码", "URL");

        SourceUtils.getActivatedSourcesWithAvailabilityCheck()
                .forEach(e -> asciiTables.addBody(
                        e.getId() + "",
                        e.getName(),
                        e.getDelay() + " ms",
                        e.getCode() + "",
                        e.getUrl())
                );

        Console.table(asciiTables);
    }

}