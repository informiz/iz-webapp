package org.informiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.Default;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;


@Table(name="fact_checker")
@Entity
@JsonView(Utils.Views.EntityDefaultView.class)
@NamedEntityGraph(
        name= FactCheckerBase.FACT_CHECKER_DATA,
        attributeNodes={
                @NamedAttributeNode("reviews"),
                @NamedAttributeNode("score")
        })
public final class FactCheckerBase extends ChainCodeEntity implements Serializable {

    public static final String FACT_CHECKER_DATA = "fact-checker-data";

    static final long serialVersionUID = 3L ;

    @NotBlank(message = "Name is mandatory")
    private String name;

    /**
     * Validation group for add/edit fact-checker through the UI (most fields will not be initialized)
     */
    public interface FactCheckerFromUI {}

    @Email(message = "Please provide a valid email address", groups = {FactCheckerFromUI.class, Default.class})
    private String email; // TODO: encrypt in DB?

    @URL(message = "Please provide a valid profile-link", groups = {FactCheckerFromUI.class, Default.class})
    private String link;

    public FactCheckerBase() {}

    public FactCheckerBase(String name, String email, String link) {
        this.name = name;
        this.email = email;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public String getEmail() {
        return email;
    } // exclude when serializing to json

    @JsonProperty
    public void setEmail(String email) {
        this.email = email;
    } // require when deserializing

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void edit(FactCheckerBase other) {
        super.edit(other);
        this.setEmail(other.getEmail());
        this.setLink(other.getLink());
        this.setName(other.getName());
    }
}
