package org.informiz.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

// TODO: Create interface in charge of ledger serde

@Table(name="fact_checker")
@Entity
public class FactCheckerBase {

    private static ObjectMapper mapper = new ObjectMapper();

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    // Fact-checker's id on the ledger
    @JsonProperty("fcid")
    private String entityId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "confidence", column = @Column(name = "score_confidence"))
    })
    private Score score;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @Email(message = "Please provide a valid email address")
    @JsonIgnore
    private String email;

    @URL(message = "Please provide a valid profile-link")
    @JsonIgnore
    private String link; // TODO: add to ledger entity?

    public FactCheckerBase() {}

    public FactCheckerBase(String name, String email, String link) {
        // TODO: validate?
        this.name = name;
        this.email = email;
        this.link = link;
    }

    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
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

    // TODO: is this how I want to compare fact-checkers?
    @Override
    public int hashCode() {
        return this.entityId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        FactCheckerBase other = (FactCheckerBase) obj;

        return this.entityId.equals(other.entityId);
    }

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize fact-checker", e);
        }
    }
}
