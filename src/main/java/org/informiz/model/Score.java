package org.informiz.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.io.Serializable;

@Embeddable
public class Score implements Serializable {

    static final long serialVersionUID = 3L ;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Float reliability;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Float confidence;

    public Score() {
        setReliability(0.9f);
        setConfidence(0.5f);
    }

    public Score(float reliability, float confidence) {
        setReliability(reliability);
        setConfidence(confidence);
    }

    public Float getReliability() {
        return reliability;
    }

    public void setReliability(Float reliability) {
        this.reliability = reliability;
    }

    public Float getConfidence() {
        return confidence;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    public void edit(Score other) {
        this.setReliability(other.getReliability());
        this.setConfidence(other.getConfidence());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Score other = (Score) obj;

        return this.reliability.equals(other.reliability) && this.confidence.equals(other.confidence);
    }

    // TODO: make comparable?

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        return String.format("{ \"reliability\": %.2f, \"confidence\": %.2f }", reliability, confidence);
    }

}
