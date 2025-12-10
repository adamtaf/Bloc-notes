package model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Note {
    private Long id;
    private String titre;
    private String contenu;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private Set<String> tags = new HashSet<>();

    public Note() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = this.dateCreation;
    }

    public Note(Long id, String titre, String contenu, Set<String> tags) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        if (tags != null) this.tags = tags;
        this.dateCreation = LocalDateTime.now();
        this.dateModification = this.dateCreation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; touch(); }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; touch(); }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; touch(); }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; touch(); }

    public void addTag(String tag) { this.tags.add(tag); touch(); }
    public void removeTag(String tag) { this.tags.remove(tag); touch(); }

    private void touch() { this.dateModification = LocalDateTime.now(); }

    @Override
    public String toString() {
        return "Note{" + "id=" + id + ", titre='" + titre + '\'' + ", tags=" + tags + '}';
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
