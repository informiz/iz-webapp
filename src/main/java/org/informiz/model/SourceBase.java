package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Table(name="source")
@Entity
public final class SourceBase extends ChainCodeEntity implements Serializable {

    static final long serialVersionUID = 1L;

    // TODO: additional details per source-type?
    public enum SourceType {
        UNIVERSITY("University"),
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

    public void edit(SourceBase other) {
        this.setName(other.getName());
        this.setLink(other.getLink());
        this.setSrcType(other.getSrcType());
        this.setReviews(other.getReviews()); // TODO: move to parent edit method, proper copy
        // TODO: allow direct score edit? Calculate new score?
        this.getScore().edit(other.getScore());
    }
}
