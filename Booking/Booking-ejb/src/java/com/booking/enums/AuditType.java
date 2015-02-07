package com.booking.enums;

import java.util.List;
import java.util.ArrayList;

/**
 * This enumeration defines the type of audit record.
 *
 * @author Jesus Soriano
 */
public enum AuditType {

    LOGGED_IN("ha entrado en el sistema"),
    LOGGED_OUT("ha salido del sistema"),
    SIGN_UP("se ha registrado"),
    ACCOUNT_CANCELLED("se ha dado de baja"),
    BOOK_TRX("ha reservado hora para TRX"),
    BOOK_MASSAGE("ha reservado hora para masaje"),
    BOOK_DIET("ha reservado hora para dieta"),
    BOOK_PILATES("ha reservado hora para pilates");
    
    private final String label;

    private AuditType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static List<AuditType> getAllAuditTypes() {
        List<AuditType> audits = new ArrayList<>();
        audits.add(LOGGED_IN);
        audits.add(LOGGED_OUT);
        audits.add(SIGN_UP);
        audits.add(ACCOUNT_CANCELLED);
        audits.add(BOOK_TRX);
        audits.add(BOOK_PILATES);
        audits.add(BOOK_MASSAGE);
        audits.add(BOOK_DIET);
        return audits;
    }
    
}
