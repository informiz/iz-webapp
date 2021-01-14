package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.hibernate.validator.constraints.URL;
import org.informiz.ctrl.entity.EntityWithReferences;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
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
public final class InformiBase extends ChainCodeEntity implements Serializable, EntityWithReferences {

    static final long serialVersionUID = 1L;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Description is mandatory")
    @Column(length = 1500)
    @Size(max = 1500)
    private String description;

    @URL(message = "A valid link to a media file is mandatory")
    private String mediaPath;

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

    public Set<Reference> getReferences() {
        return references;
    }

    public void setReferences(Set<Reference> references) {
        this.references = references;
    }

    public void edit(InformiBase other) {
        super.edit(other);
        this.setName(other.getName());
        this.setDescription(other.getDescription());
        this.setMediaPath(other.getMediaPath());
    }
}
