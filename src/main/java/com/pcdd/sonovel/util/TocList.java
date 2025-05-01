package com.pcdd.sonovel.util;

import com.pcdd.sonovel.model.Chapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 解决目录章节乱序，同时去重章节名
 */
public class TocList extends ArrayList<Chapter> {

    private final Map<String, Chapter> chapterMap = new HashMap<>();

    @Override
    public boolean add(Chapter chapter) {
        // 如果已有相同 title，先删除
        Chapter existing = chapterMap.put(chapter.getTitle(), chapter);
        if (existing != null) {
            super.remove(existing);
        }
        return super.add(chapter);
    }

}