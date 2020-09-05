package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.hibernate.validator.constraints.URL;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Set;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Table(name="citation")
@Entity
public final class CitationBase extends ChainCodeEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @NotBlank(message = "Text is mandatory")
    private String text;

    @NotBlank(message = "Citations must be sourced")
    @URL(message = "Please provided a link to the source of the citation")
    private String link;

    @OneToMany(mappedBy = "sourced", cascade = CascadeType.ALL)
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

    public boolean removeSource(SourceRef src) {
        return sources.remove(src);
    }

    public boolean addSource(SourceRef src) {
        // Source references are considered equal if they have the same well-known source and link.
        // If a similar source-reference exists, this method will replace it
        sources.remove(src);
        return sources.add(src);
    }

    public void edit(CitationBase other) {
        this.setText(other.getText());
        this.setLink(other.getLink());
        this.setLocale(other.getLocale());
        // TODO: allow direct score edit? Calculate new score?
        this.getScore().edit(other.getScore());
    }

}
