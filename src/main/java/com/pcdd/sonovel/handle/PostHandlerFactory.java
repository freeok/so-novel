package com.pcdd.sonovel.handle;

import com.pcdd.sonovel.model.ConfigBean;
import lombok.experimental.UtilityClass;

/**
 * @author pcdd
 * Created at 2024/12/4
 */
@UtilityClass
public class PostHandlerFactory {

    public PostProcessingHandler getHandler(String extName, ConfigBean config) {
        return switch (extName) {
            case "txt" -> new TxtMergeHandler(config);
            case "epub" -> new EpubMergeHandler();
            case "html" -> new HtmlCatalogHandler();
            default -> throw new IllegalArgumentException("Unsupported format: " + extName);
        };
    }
}
