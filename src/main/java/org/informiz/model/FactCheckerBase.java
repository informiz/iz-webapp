package org.informiz.model;

import org.hibernate.validator.constraints.URL;
import org.informiz.auth.AuthUtils;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;


@Table(name="fact_checker")
@Entity
public class FactCheckerBase extends ChainCodeEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "confidence", column = @Column(name = "score_confidence"))
    })
    @Valid
    private Score score;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @Email(message = "Please provide a valid email address")
    private String email;

    @URL(message = "Please provide a valid profile-link")
    private String link;

    private Boolean active;

    public FactCheckerBase() {}

    public FactCheckerBase(String name, String email, String link) {
        this(name, email, link, true);
    }

    public FactCheckerBase(String name, String email, String link, Boolean active) {
        this.name = name;
        this.email = email;
        this.link = link;
        this.active = active;
        this.score = new Score();
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void edit(FactCheckerBase other) {
        this.setEmail(other.getEmail());
        this.setLink(other.getLink());
        this.setName(other.getName());
        // TODO: allow direct score edit? Calculate new score?
        this.getScore().edit(other.getScore());
    }

    @PrePersist
    private void generateCryptoMaterial() {
        // TODO: use entity-id instead
        AuthUtils.generateCryptoMaterial(this.getEmail());
    }
}
