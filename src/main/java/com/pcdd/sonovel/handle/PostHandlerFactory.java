package com.pcdd.sonovel.handle;

import com.pcdd.sonovel.model.AppConfig;
import lombok.experimental.UtilityClass;

/**
 * @author pcdd
 * Created at 2024/12/4
 */
@UtilityClass
public class PostHandlerFactory {

    public PostProcessingHandler getHandler(String extName, AppConfig config) {
        return switch (extName) {
            case "txt" -> new TxtMergeHandler(config);
            case "epub" -> new EpubMergeHandler();
            case "html" -> new HtmlTocHandler();
            case "pdf" -> new PdfMergeHandler(config);
            default -> throw new IllegalArgumentException("Unsupported format: " + extName);
        };
    }
}