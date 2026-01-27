package com.example.test_v2.tags;

import java.util.ArrayList;
import java.util.List;

public class TagManager {
    private static final List<String> predefinedTags = new ArrayList<>();

    static {
        predefinedTags.add("Add New Tag");
        predefinedTags.add("Work");
        predefinedTags.add("Personal");
        predefinedTags.add("Urgent");
    }

    public static List<String> getTags() {
        return predefinedTags;
    }

    public static void addTag(String tag) {
        if (!predefinedTags.contains(tag)) {
            predefinedTags.add(tag);
        }
    }

    public static void removeTag(String tag) {
        if (predefinedTags.contains(tag) && !tag.equals("Add New Tag")) {
            predefinedTags.remove(tag);
        }
    }
}
