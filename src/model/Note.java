package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

public class Note implements Serializable {

    private Long id;
    private String titre;
    private String contenu;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private Set<String> tags = new HashSet<>();

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Note() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = this.dateCreation;
    }

    public Note(Long id, String titre, String contenu,
                LocalDateTime dateCreation, LocalDateTime dateModification) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.dateCreation = dateCreation != null ? dateCreation : LocalDateTime.now();
        this.dateModification = dateModification != null ? dateModification : this.dateCreation;
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; touch(); }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; touch(); }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; touch(); }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public String getFormattedCreationDate() { return dateCreation.format(FMT); }
    public String getFormattedModificationDate() { return dateModification.format(FMT); }

    public Set<String> getTags() { return tags; }

    public void setTags(Set<String> tags) {
        this.tags = tags == null ? new HashSet<>() : tags;
        touch();
    }

    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            this.tags.add(tag.trim().toLowerCase());
            touch();
        }
    }

    public void removeTag(String tag) {
        if (tag != null) {
            this.tags.remove(tag.toLowerCase());
            touch();
        }
    }

    // Tags as comma-separated string (compatible with la collègue)
    public String getTagsAsString() {
        return String.join(",", tags);
    }

    public void setTagsFromString(String tagsString) {
        this.tags.clear();
        if (tagsString != null && !tagsString.trim().isEmpty()) {
            String[] arr = tagsString.split(",");
            for (String t : arr) {
                if (!t.trim().isEmpty()) addTag(t.trim());
            }
        }
    }

    // rendre touch() public parce que le service en a besoin
    public void touch() { this.dateModification = LocalDateTime.now(); }

    @Override
    public String toString() {
        return "Note [id=" + id + ", titre=" + titre + ", tags=" + tags + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note)) return false;
        Note note = (Note) o;
        return Objects.equals(id, note.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
