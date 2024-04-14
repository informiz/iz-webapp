package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

@Table(name = "citation")
@Entity
@JsonView(Utils.Views.EntityDefaultView.class)
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = CitationBase.CITATION_PREVIEW,
                attributeNodes = {
                        @NamedAttributeNode("reviews"),
                        @NamedAttributeNode("score")
                }
        ),
        @NamedEntityGraph(
                name = CitationBase.CITATION_DATA,
                attributeNodes = {
                        @NamedAttributeNode("reviews"),
                        @NamedAttributeNode("score"),
                        @NamedAttributeNode("sources")
                }
        )
})
public final class CitationBase extends ChainCodeEntity implements Serializable {

    static final long serialVersionUID = 3L;
    public static final String CITATION_PREVIEW = "citation-with-reviews";
    public static final String CITATION_DATA = "citation-full-data";

    /**
     * Validation group for add/edit citation through the UI (most fields will not be initialized)
     */
    public interface ExistingCitationFromUI extends ExistingEntityFromUI {}
    public interface NewCitationFromUI {}
    @NotBlank(message = "Text is mandatory", groups = {NewCitationFromUI.class, ExistingCitationFromUI.class, Default.class})
    @Column(length = 500)
    @Size(max = 500, groups = {NewCitationFromUI.class, ExistingCitationFromUI.class, Default.class})
    private String text;

    @NotBlank(message = "Citations must be sourced", groups = {NewCitationFromUI.class, ExistingCitationFromUI.class, Default.class})
    @URL(message = "Please provide a link to the source of the citation", groups = {NewCitationFromUI.class, ExistingCitationFromUI.class, Default.class})
    private String link;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean addSource(SourceRef src) {
        // Source references are considered equal if they have the same well-known source and link.
        // If a similar source-reference exists, this method will replace it
        sources.remove(src);
        return sources.add(src);
    }

    public void edit(CitationBase other) {
        super.edit(other);
        this.setText(other.getText());
        this.setLink(other.getLink());
    }

}
