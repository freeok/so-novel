package com.pcdd.sonovel.util;

import lombok.experimental.UtilityClass;

import java.util.Random;

/**
 * @author pcdd
 * Created at 2024/11/28
 */
@UtilityClass
public class RandomUA {

    private final String[] OPERATING_SYSTEMS = {
            "Windows NT 10.0; Win64; x64",
            "Windows NT 11.0; Win64; x64",
            "Macintosh; Intel Mac OS X 12_6",
            "Macintosh; Intel Mac OS X 13_4",
            "X11; Linux x86_64",
            "X11; Ubuntu; Linux x86_64"
    };

    private final String[] BROWSERS = {"Chrome", "Firefox", "Safari", "Edge"};

    private final int MIN_VERSION = 100; // Minimum browser version
    private final int MAX_VERSION = 131; // Maximum browser version

    private final Random RANDOM = new Random();

    public String generate() {
        String operatingSystem = OPERATING_SYSTEMS[RANDOM.nextInt(OPERATING_SYSTEMS.length)];
        String browser = BROWSERS[RANDOM.nextInt(BROWSERS.length)];
        int majorVersion = RANDOM.nextInt(MAX_VERSION - MIN_VERSION + 1) + MIN_VERSION;
        int minorVersion = RANDOM.nextInt(10); // Minor version is 0-9
        int buildVersion = RANDOM.nextInt(1000); // Build version is 0-4999

        return switch (browser) {
            case "Chrome" ->
                    String.format("Mozilla/5.0 (%s) AppleWebKit/537.36 (KHTML, like Gecko) %s/%d.0.%d Safari/537.36",
                            operatingSystem, browser, majorVersion, buildVersion);
            case "Firefox" -> String.format("Mozilla/5.0 (%s; rv:%d.0) Gecko/20100101 %s/%d.0",
                    operatingSystem, majorVersion, browser, majorVersion);
            case "Safari" ->
                    String.format("Mozilla/5.0 (%s) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/%d.1 Safari/605.1.15",
                            operatingSystem, majorVersion);
            case "Edge" ->
                    String.format("Mozilla/5.0 (%s) AppleWebKit/537.36 (KHTML, like Gecko) %s/%d.0.%d.0 Safari/537.36",
                            operatingSystem, browser, majorVersion, minorVersion);
            default -> "Unknown User-Agent";
        };
    }

}