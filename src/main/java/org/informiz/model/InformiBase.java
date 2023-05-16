package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

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
@NamedEntityGraphs({
        @NamedEntityGraph(
                name= InformiBase.INFORMI_PREVIEW,
                attributeNodes={
                        @NamedAttributeNode("reviews"),
                        @NamedAttributeNode("score")
                }
        ),
        @NamedEntityGraph(
                name= InformiBase.INFORMI_DATA,
                attributeNodes={
                        @NamedAttributeNode("reviews"),
                        @NamedAttributeNode("score"),
                        @NamedAttributeNode("references")
                }
        )

})
public final class InformiBase extends FactCheckedEntity implements Serializable {

    static final long serialVersionUID = 3L ;

    public static final String INFORMI_PREVIEW = "informi-with-reviews";
    public static final String INFORMI_DATA = "informi-full-data";
    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Description is mandatory")
    @Column(length = 1500)
    @Size(max = 1500)
    private String description;

    @URL(message = "A valid link to a media file is mandatory")
    private String mediaPath;

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

    public void edit(InformiBase other) {
        super.edit(other);
        this.setName(other.getName());
        this.setDescription(other.getDescription());
        this.setMediaPath(other.getMediaPath());
    }
}
