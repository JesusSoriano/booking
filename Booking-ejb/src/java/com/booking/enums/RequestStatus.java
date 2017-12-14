package com.booking.enums;

/**
 *
 * @author Jesús Soriano
 */
public enum RequestStatus {

    PENDING ("Pendiente"),
    ACCEPTED ("Aceptada"),
    DECLINED ("Rechazada"),
    CANCELLED ("Cancelada");
    
    private final String label;

    private RequestStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
