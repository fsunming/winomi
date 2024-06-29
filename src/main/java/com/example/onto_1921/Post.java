package com.example.onto_1921;

public class Post {
    private String postId;
    private String title;
    private String content;
    private String authorId;

    public Post(String postId, String title, String content, String authorId) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
    }

    public  String getPostId() { return postId; }
    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
    public String getAuthorId() {
        return authorId;
    }
}
