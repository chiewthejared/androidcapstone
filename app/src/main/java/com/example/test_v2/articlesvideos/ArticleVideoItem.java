package com.example.test_v2.articlesvideos;

public class ArticleVideoItem {
    private String title;
    private String description;
    private String link;
    private String type; // "article" or "video"

    public ArticleVideoItem(String title, String description, String link, String type) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.type = type;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLink() { return link; }
    public String getType() { return type; }
}
