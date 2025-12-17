package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;


public class Note implements Serializable {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private Set<String> tags = new HashSet<>();

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Note() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = this.dateCreation;
    }

    public Note(Long id, String title, String content,
                LocalDateTime dateCreation, LocalDateTime dateModification) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; touch(); }


    public String getTitle() { return title; }
    public String getContent() { return content; }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }


    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }

    public String getFormattedCreationDate() { return dateCreation.format(FMT); }
    public String getFormattedModificationDate() { return dateModification.format(FMT); }

    public Set<String> getTags() { return tags; }

    public void setTags(Set<String> tags) {
        this.tags = tags != null ? tags : new HashSet<>();
        touch();
    }

    public void addTag(String tag) {

    }

    public void removeTag(String tag) {

    }


    public String getTagsAsString() {
        return  String.join(", ", tags);
    }

    public void setTagsFromString(String s) {
        return;
    }

    public void touch() {
        this.dateModification = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return title + " (" + id + ")";
    }
}
