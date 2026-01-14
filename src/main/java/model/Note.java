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
public class Note{
    //pr indiquer que c'est une cle primaire et doit etre unique
    @Id
    //pr indiquer que la valeur est auto generee et doit ere croissante auto increment avec mysql
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    //localdatetime stocke sous le fromat 2026-01-14T18:30:00
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    //pour que ce ne soit pas stocke dans la bdd
    @Transient
    //pour indiquer si la note a ete modifiee depuis la dernier sauvegarde
    private boolean dirty = false;
    @Transient
    //lock pour la sunchronisation des acces concurents
    private final ReentrantLock lock = new ReentrantLock();

    //tags est une collection de strings et pas une table et hibernate doit la fetch en meme temps que la note et la stocker dans une table qu'il va creer
    @ElementCollection(fetch = FetchType.EAGER)
    //indique que la table s'appelle note_tags et le join column veut dire que la table note tags va avoir une colonne
    @CollectionTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"))
    //pour dire que la colonne va s'appeler tag
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();


    //format pour affciher les dates
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    //constructeur par defaut pck obligagatoire pour hibernate
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
        //pr mettre a jour la date de modif
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
