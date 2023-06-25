package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Table(name = "citation")
@Entity
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
                //includeAllAttributes=true,
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
    public interface CitationFromUI {}

    @NotBlank(message = "Text is mandatory", groups = {CitationFromUI.class, Default.class})
    @Column(length = 500)
    @Size(max = 500)
    private String text;

    @NotBlank(message = "Citations must be sourced", groups = {CitationFromUI.class, Default.class})
    @URL(message = "Please provide a link to the source of the citation")
    private String link;

    @OneToMany(mappedBy = "sourced", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @JsonIgnore
    private Set<SourceRef> sources;

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

    public Set<SourceRef> getSources() {
        return sources;
    }

    public void setSources(Set<SourceRef> sources) {
        this.sources = sources;
    }

    public boolean removeSource(Long srcRefId) {
        List<SourceRef> snapshot = new ArrayList(sources);
        SourceRef ref = snapshot.stream().filter(reference ->
                srcRefId.equals(reference.getId())).findFirst().orElse(null);

        if (ref != null)
            return sources.remove(ref);
        return false;
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
