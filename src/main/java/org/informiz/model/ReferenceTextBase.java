package org.informiz.model;

/*
 * SPDX-License-Identifier: Apache-2.0
 */


import com.fasterxml.jackson.annotation.JsonAutoDetect;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;

/**
 * A data-type managed by the reference-text contract. A reference text consists of:
 * - the text
 * - a locale
 * - the id of the source for the reference (e.g the NASA website)
 * - a link to the source of the reference (e.g the specific page on the NASA website)
 * - the current reliability/confidence score
 * - reviews by fact-checkers
 * Any additional metadata should be saved on a separate CMS
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Table(name="reference")
@Entity
public final class ReferenceTextBase extends ChainCodeEntity implements Serializable {

    static final long serialVersionUID = 1L;

    private String text;

    private Locale locale;

    private String sid;

    private String link;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "confidence", column = @Column(name = "score_confidence"))
    })
    private Score score;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public void edit(ReferenceTextBase other) {
        this.setText(other.getText());
        this.setSid(other.getSid());
        this.setLink(other.getLink());
        this.setLocale(other.getLocale());
        // TODO: allow direct score edit? Calculate new score?
        this.getScore().edit(other.getScore());
    }

}
