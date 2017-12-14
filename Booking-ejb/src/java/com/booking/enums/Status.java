package com.booking.enums;

/**
 *
 * @author Jesús Soriano
 */
public enum Status {

    ACTIVATED ("Activo/a"),
    SUSPENDED ("Suspendido/a");
    
    private final String label;

    private Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
