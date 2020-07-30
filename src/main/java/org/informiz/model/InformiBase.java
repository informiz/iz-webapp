package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A data-type managed by the informi contract, representing a media file (e.g an infographics):
 * - the informi's name
 * - a description
 * - a path to the media file
 * - the current reliability/confidence score
 * - reviews by fact-checkers
 * Any additional metadata should be saved on a separate CMS
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Table(name="informi")
@Entity
public final class InformiBase extends ChainCodeEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Description is mandatory")
    private String description;

    @NotBlank(message = "Media file/link is mandatory")
    private String mediaPath;

    @Transient
    private MultipartFile file;

    @NotNull(message = "Locale is mandatory")
    private Locale locale;

/*
    // TODO: why can't I specify `referencedColumnName="entity_id"` ??
    @ElementCollection
    @CollectionTable(name = "informi_claim",
            joinColumns = @JoinColumn(name = "informi_id"))
    @Column(name = "claim_id")
    private Set<String> claims = new HashSet<>();
*/

    @OneToMany(mappedBy = "reviewed", cascade = CascadeType.ALL)
    protected Set<Reference> references;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String link) {
        this.mediaPath = link;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Set<Reference> getReferences() {
        return references;
    }

    public void setReferences(Set<Reference> references) {
        this.references = references;
    }

    public boolean addReference(Reference ref) {
        return references.add(ref);
    }

    public boolean removeReference(Reference ref) {
        return references.remove(ref);
    }

    public Reference getReference(@NotNull Reference ref) {
        // TODO: more efficient way?
        Reference found = references.stream().filter(reference ->
                ref.equals(reference)).findFirst().orElse(null);
        return found;
    }

    public void edit(InformiBase other) {
        this.setName(other.getName());
        this.setDescription(other.getDescription());
        this.setMediaPath(other.getMediaPath());
        this.setLocale(other.getLocale());
        // TODO: allow direct score edit? Calculate new score?
        this.getScore().edit(other.getScore());
    }
}
