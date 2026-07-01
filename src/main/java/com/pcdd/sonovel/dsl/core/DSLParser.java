package com.pcdd.sonovel.dsl.core;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class DSLParser {

    public class Result {
        String init;
        List<Step> steps = new ArrayList<>();
    }

    public Result parse(String input) {
        Result result = new Result();

        int i = 0;
        int first = input.indexOf("@");

        result.init = first == -1 ? input : input.substring(0, first);

        i = first;

        while (i != -1 && i < input.length()) {
            int at = input.indexOf("@", i + 1);
            int colon = input.indexOf(":", i);

            if (colon == -1) break;

            String lang = input.substring(i + 1, colon);

            int nextAt = at == -1 ? input.length() : at;
            String code = input.substring(colon + 1, nextAt);

            result.steps.add(new Step(lang, code.trim()));

            i = at;
        }

        return result;
    }
}