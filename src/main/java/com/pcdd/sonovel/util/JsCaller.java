package com.pcdd.sonovel.util;

import lombok.experimental.UtilityClass;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

@UtilityClass
public class JsCaller {

    private final Context context = Context
            .newBuilder("js")
            .allowAllAccess(true)
            .build();

    public String call(String jsCode, String rValue) {
        context.eval("js", """
                function func(r) {
                    %s
                    return r;
                }
                """.formatted(jsCode));
        Value func = context.getBindings("js").getMember("func");
        Value result = func.execute(rValue);
        return result.asString();
    }

}