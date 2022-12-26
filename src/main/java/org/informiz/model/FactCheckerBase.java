package org.informiz.model;

import org.hibernate.validator.constraints.URL;
import org.informiz.auth.AuthUtils;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.function.Consumer;


@Table(name="fact_checker")
@Entity
public class FactCheckerBase extends ChainCodeEntity implements Serializable {

    static final long serialVersionUID = 1L;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @Email(message = "Please provide a valid email address")
    private String email;

    @URL(message = "Please provide a valid profile-link")
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

    public void edit(FactCheckerBase other) {
        super.edit(other);
        this.setEmail(other.getEmail());
        this.setLink(other.getLink());
        this.setName(other.getName());
    }

    protected Consumer<InformizEntity> onCreateConsumer() {
        Consumer<InformizEntity> consumer = super.onCreateConsumer();
        return entity -> {
            consumer.accept(entity);
            // TODO: use entity-id instead
            AuthUtils.generateCryptoMaterial(this.getEmail());
        };
    }
}
