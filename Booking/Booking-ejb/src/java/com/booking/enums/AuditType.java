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
    CREATE_SERVICE("ha creado un servicio"),
    SUSPEND_SERVICE("ha suspendido un servicio"),
    ACTIVATE_SERVICE("ha activado un servicio"),
    CREATE_GROUP("ha creado un grupo"),
    SUSPEND_GROUP("ha suspendido un grupo"),
    ACTIVATE_GROUP("ha activado un grupo"),
    SUSPEND_USER("ha suspendido un usuario"),
    ACTIVATE_USER("ha activado un usuario"),
    SUSPEND_ADMIN("ha suspendido un administrador"),
    ACTIVATE_ADMIN("ha activado un administrador"),
    SUSPEND_ORGANISATION("ha suspendido una organización"),
    ACTIVATE_ORGANISATION("ha activado una organización");
    
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
        audits.add(SUSPEND_USER);
        audits.add(ACTIVATE_USER);
        audits.add(SUSPEND_ADMIN);
        audits.add(ACTIVATE_ADMIN);
        audits.add(SUSPEND_ORGANISATION);
        audits.add(ACTIVATE_ORGANISATION);
        audits.add(CREATE_SERVICE);
        audits.add(ACTIVATE_SERVICE);
        audits.add(SUSPEND_SERVICE);
        return audits;
    }

    public static List<AuditType> getAuditTypesForAdmin() {
        List<AuditType> audits = new ArrayList<>();
        audits.add(LOGGED_IN);
        audits.add(LOGGED_OUT);
        audits.add(SIGN_UP);
        audits.add(ACCOUNT_CANCELLED);
        audits.add(SUSPEND_USER);
        audits.add(ACTIVATE_USER);
        audits.add(CREATE_SERVICE);
        audits.add(ACTIVATE_SERVICE);
        audits.add(SUSPEND_SERVICE);
        return audits;
    }
    

    public static List<AuditType> getAuditTypesForClient() {
        List<AuditType> audits = new ArrayList<>();
        audits.add(LOGGED_IN);
        audits.add(LOGGED_OUT);
        audits.add(SIGN_UP);
        audits.add(ACCOUNT_CANCELLED);
        return audits;
    }
    
}
