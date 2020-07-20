package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A data-type managed by the source contract, representing a source for references (e.g the NASA website):
 * - the id of the source
 * - the source's name
 * - the current reliability/confidence score
 * - reviews by fact-checkers
 * Any additional metadata should be saved on a separate CMS
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Table(name="source")
@Entity
public final class SourceBase extends ChainCodeEntity implements Serializable {

    static final long serialVersionUID = 1L;

    // TODO: additional details per source-type?
    public enum SourceType {
        NEWS("News Outlet"),
        SOCIAL_MEDIA("Social Media"),
        STUDY("Scientific Study"),
        BOOK("Book"),
        PERSON("Person"),
        GOVERNMENT("Government Entity"),
        PRIVATE_ORG("Private Organization"),
        BLOG("Blog");

        private final String displayValue;

        private SourceType(String displayValue) {
            this.displayValue = displayValue;
        }

        public String getDisplayValue() {
            return displayValue;
        }

    }

    @NotBlank(message = "Name is mandatory")
    private String name;

    @URL(message = "Please provide a valid link")
    private String link;

    @Enumerated(EnumType.ORDINAL)
    private SourceType srcType;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "confidence", column = @Column(name = "score_confidence"))
    })
    @Valid
    private Score score;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public SourceType getSrcType() {
        return srcType;
    }

    public void setSrcType(SourceType srcType) {
        this.srcType = srcType;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public void edit(SourceBase other) {
        this.setName(other.getName());
        this.setLink(other.getLink());
        this.setReviews(other.getReviews()); // TODO: move to parent edit method, proper copy
        // TODO: allow direct score edit? Calculate new score?
        this.getScore().edit(other.getScore());
    }
}
