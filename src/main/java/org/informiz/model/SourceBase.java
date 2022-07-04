package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
        JOURNALIST("Journalist"),
        SOCIAL_MEDIA("Social Media"),
        STUDY("Scientific Study"),
        SCHOLAR("Scholar"),
        BOOK("Book"),
        POLITICIAN("Political Figure"),
        PERSON("Person"),
        GOVERNMENT("Government Entity"),
        PRIVATE_ORG("Private Organization"),
        NONPROFIT_ORG("Non Profit Organization"),
        BLOG("Blog"),
        WEBSITE("Website");

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
    @NotBlank(message = "Link is mandatory")
    private String link;

    @Enumerated(EnumType.ORDINAL)
    @NotNull(message = "Type is mandatory")
    private SourceType srcType;

    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void edit(SourceBase other) {
        super.edit(other);
        this.setName(other.getName());
        this.setLink(other.getLink());
        this.setSrcType(other.getSrcType());
        this.setDescription(other.getDescription());
    }
}
