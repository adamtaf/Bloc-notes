package model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@Entity
@Table(name = "notes")
public class Note implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    @Transient
    private boolean dirty = false;
    @Transient
    private final ReentrantLock lock = new ReentrantLock();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "tag")
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


    public ReentrantLock getLock() {
        return lock;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; touch(); }


    public String getTitle() { return title; }
    public String getContent() { return content; }

    public void setTitle(String title) {
        this.title = title;
        this.dirty = true;
        touch();
    }

    public void setContent(String content) {
        this.content = content;
        this.dirty = true;
        touch();
    }


    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }

    public String getFormattedCreationDate() { return dateCreation.format(FMT); }
    public String getFormattedModificationDate() { return dateModification.format(FMT); }

    public Set<String> getTags() { return tags; }

    public void setTags(Set<String> tags) {
        this.tags.clear();
        if (tags != null) this.tags.addAll(tags);
        this.dirty = true;
        touch();
    }

    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }

    public String getTagsAsString() {
        return  String.join(", ", tags);
    }

    public void setTagsFromString(String s) {
        tags.clear();
        if (s == null || s.isBlank()) return;

        String[] parts = s.split(",");
        for (String t : parts) {
            tags.add(t.trim());
        }
    }


    public void touch() {
        this.dateModification = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return title + " (" + id + ")";
    }



}
