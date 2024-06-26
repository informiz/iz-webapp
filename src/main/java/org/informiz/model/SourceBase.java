package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

@Table(name="source")
@Entity
@JsonView(Utils.Views.EntityDefaultView.class)
@NamedEntityGraph(
        name= SourceBase.SOURCE_DATA,
        attributeNodes={
                @NamedAttributeNode("reviews"),
                @NamedAttributeNode("score"),
                @NamedAttributeNode("srcType")
        })
public final class SourceBase extends ChainCodeEntity implements Serializable {

    static final long serialVersionUID = 3L ;

    public static final String SOURCE_DATA = "source-data";

    /**
     * Validation group for add/edit source through the UI (most fields will not be initialized)
     */
    public interface ExistingSourceFromUI extends ExistingEntityFromUI {}
    public interface NewSourceFromUI {}

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

    @NotBlank(message = "Name is mandatory", groups = {NewSourceFromUI.class, ExistingSourceFromUI.class, Default.class})
    private String name;

    @URL(message = "Please provide a valid link", groups = {NewSourceFromUI.class, ExistingSourceFromUI.class, Default.class})
    @NotBlank(message = "Link is mandatory", groups = {NewSourceFromUI.class, ExistingSourceFromUI.class, Default.class})
    private String link;

    @Enumerated(EnumType.ORDINAL)
    @NotNull(message = "Type is mandatory", groups = {NewSourceFromUI.class, ExistingSourceFromUI.class, Default.class})
    private SourceType srcType;

    //Todo: mach size restriction
    @Size(max = 500, message = "Description must be under 500 characters", groups = {NewSourceFromUI.class, ExistingSourceFromUI.class, Default.class})
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

    public static SourceType[] sourceTypes() {
        return SourceType.values();
    }
}
