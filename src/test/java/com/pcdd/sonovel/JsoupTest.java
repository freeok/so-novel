package com.pcdd.sonovel;

import cn.hutool.core.lang.Console;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JsoupTest {

    /**
     * jsoup-1.18.1增加了Response进度事件接口
     * https://github.com/jhy/jsoup/pull/2164
     */
    @SneakyThrows
    public static void main(String[] args) {
        String url = "https://www.baidu.com/";

        Connection con = Jsoup.connect(url).timeout(5000)
                .onResponseProgress((processed, total, percent, response) -> {
                    Console.log(percent + "%");
                });

        Document document = con.get();
    }

}
