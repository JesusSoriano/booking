package com.booking.enums;

/**
 *
 * @author Jesús Soriano
 */
public enum DocumentType {

    PERSONAL_TRAINIG ("Entrenamiento Personal"),
    DIET ("Dieta"),
    OTHER ("Otro");
    
    private final String label;

    private DocumentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
