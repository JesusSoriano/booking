package com.booking.enums;

import java.util.List;
import java.util.ArrayList;

/**
 * This enumeration defines the type of audit record.
 *
 * @author Jesus Soriano
 */
public enum AuditType {

    INICIO_SESION("ha entrado en el sistema"),
    CERRAR_SESION("ha salido del sistema"),
    REGISTRO("se ha registrado"),
    CANCELAR_CUENTA("se ha dado de baja"),
    CREAR_SERVICIO("ha creado un servicio"),
    SUSPENDER_SERVICIO("ha suspendido un servicio"),
    ACTIVAR_SERVICIO("ha activado un servicio"),
    CREAR_CLASE("ha creado una clase"),
    DUPLICAR_CLASE("ha duplicado una clase"),
    SUSPENDER_CLASE("ha suspendido una clase"),
    ACTIVAR_CLASE("ha activado una clase"),
    RESERVAR_CLASE("ha reservado una clase"),
    CANCELAR_RESERVA("ha cancelado la reserva de una clase"),
    CREAR_CITA("ha creado una cita"),
    DUPLICAR_CITA("ha duplicado una cita"),
    SUSPENDER_CITA("ha suspendido una cita"),
    ACTIVAR_CITA("ha activado una cita"),
    RESERVAR_CITA("ha reservado una cita"),
    CANCELAR_CITA("ha cancelado la reserva de una cita"),
    ACEPTAR_CITA("ha aceptado la reserva de una cita"),
    CREAR_ARCHIVO("ha creado un archivo"),
    ELIMINAR_ARCHIVO("ha eliminado un archivo"),
    SUSPENDER_USUARIO("ha suspendido un usuario"),
    ACTIVAR_USUARIO("ha activado un usuario"),
    SUSPENDER_ADMIN("ha suspendido un administrador"),
    ACTIVAR_ADMIN("ha activado un administrador"),
    SUSPENDER_ORGANIZACION("ha suspendido una organización"),
    ACTIVAR_ORGANIZACION("ha activado una organización");
    
    private final String label;

    private AuditType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static List<AuditType> getAllAuditTypes() {
        List<AuditType> audits = new ArrayList<>();
        audits.add(INICIO_SESION);
        audits.add(CERRAR_SESION);
        audits.add(REGISTRO);
        audits.add(CANCELAR_CUENTA);
        audits.add(CREAR_SERVICIO);
        audits.add(ACTIVAR_SERVICIO);
        audits.add(SUSPENDER_SERVICIO);
        audits.add(CREAR_CLASE);
        audits.add(DUPLICAR_CLASE);
        audits.add(SUSPENDER_CLASE);
        audits.add(ACTIVAR_CLASE);
        audits.add(RESERVAR_CLASE);
        audits.add(CANCELAR_RESERVA);
        audits.add(CREAR_CITA);
        audits.add(DUPLICAR_CITA);
        audits.add(SUSPENDER_CITA);
        audits.add(ACTIVAR_CITA);
        audits.add(RESERVAR_CITA);
        audits.add(CANCELAR_CITA);
        audits.add(CREAR_ARCHIVO);
        audits.add(ELIMINAR_ARCHIVO);
        audits.add(SUSPENDER_USUARIO);
        audits.add(ACTIVAR_USUARIO);
        audits.add(SUSPENDER_ADMIN);
        audits.add(ACTIVAR_ADMIN);
        audits.add(SUSPENDER_ORGANIZACION);
        audits.add(ACTIVAR_ORGANIZACION);
        return audits;
    }

    public static List<AuditType> getAuditTypesForAdmin() {
        List<AuditType> audits = new ArrayList<>();
        audits.add(INICIO_SESION);
        audits.add(CERRAR_SESION);
        audits.add(REGISTRO);
        audits.add(CANCELAR_CUENTA);
        audits.add(CREAR_SERVICIO);
        audits.add(ACTIVAR_SERVICIO);
        audits.add(SUSPENDER_SERVICIO);
        audits.add(CREAR_CLASE);
        audits.add(DUPLICAR_CLASE);
        audits.add(SUSPENDER_CLASE);
        audits.add(ACTIVAR_CLASE);
        audits.add(RESERVAR_CLASE);
        audits.add(CANCELAR_RESERVA);
        audits.add(CREAR_CITA);
        audits.add(DUPLICAR_CITA);
        audits.add(SUSPENDER_CITA);
        audits.add(ACTIVAR_CITA);
        audits.add(RESERVAR_CITA);
        audits.add(CANCELAR_CITA);
        audits.add(CREAR_ARCHIVO);
        audits.add(ELIMINAR_ARCHIVO);
        audits.add(SUSPENDER_USUARIO);
        audits.add(ACTIVAR_USUARIO);
        audits.add(SUSPENDER_ADMIN);
        audits.add(ACTIVAR_ADMIN);
        return audits;
    }
    

    public static List<AuditType> getAuditTypesForClient() {
        List<AuditType> audits = new ArrayList<>();
        audits.add(INICIO_SESION);
        audits.add(CERRAR_SESION);
        audits.add(REGISTRO);
        audits.add(CANCELAR_CUENTA);
        audits.add(RESERVAR_CLASE);
        audits.add(CANCELAR_RESERVA);
        audits.add(RESERVAR_CITA);
        audits.add(CANCELAR_CITA);
        return audits;
    }
    
}
