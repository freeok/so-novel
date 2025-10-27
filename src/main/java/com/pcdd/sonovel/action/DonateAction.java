package com.pcdd.sonovel.action;

import cn.hutool.core.lang.Console;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2025/10/27
 */
public class DonateAction {
    public static void execute() {
        Console.log(render("感谢您的支持！请通过以下方式赞助：\n", "red", "bold"));
        Console.log(render("支付宝： https://ghfast.top/github.com/freeok/so-novel/blob/main/assets/donation-alipay.png?raw=true", "blue", "bold"));
        Console.log(render("微信：   https://ghfast.top/github.com/freeok/so-novel/blob/main/assets/donation-wechat.jpg?raw=true", "green", "bold"));
        Console.log(render("(打开链接图片后，扫码即可赞助，您的支持是我持续更新的动力！)", "ITALIC"));
    }

    public static void main(String[] args) {
        execute();
    }
}