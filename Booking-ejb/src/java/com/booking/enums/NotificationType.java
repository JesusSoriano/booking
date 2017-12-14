package com.booking.enums;

import java.util.List;
import java.util.ArrayList;

/**
 * This enumeration defines the type of notification.
 *
 * @author Jesus Soriano
 */
public enum NotificationType {

    // Administrators notifications
    REGISTRO_USUARIO_ADMIN(" se ha registrado en el sistema."),
    BAJA_USUARIO_ADMIN(" se ha dado de baja del sistema."),
    SOLICITUD_CITA_ADMIN(" ha solicitado una cita."),
    CITA_SUSPENDIDA_ADMIN(" ha suspendido una cita."),
    CLASE_COMPLETA_ADMIN("Esta clase se ha quedado sin plazas."),
    
    // Clients notifications
    USUARIO_ACTIVADO("Los administradores han activado tu perfil."),
    USUARIO_SUSPENDIDO("Los administradores han suspendido tu perfil."),
    CLASE_ACTIVADA("Los administradores han activado una clase en la que tenías plaza."),
    CLASE_SUSPENDIDA("Los administradores han suspendido una clase en la que tenías plaza."),
    CITA_ACEPTADA("Los administradores han aceptado tu solicitud de reserva."),
    CITA_RECHAZADA("Los administradores han rechazado tu solicitud de reserva."),
    CITA_ACTIVADA("Los administradores han activado una cita que tenías reservada."),
    CITA_SUSPENDIDA("Los administradores han suspendido una cita que tenías reservada."),
    CITA_CANCELADA("Los administradores han cancelado la reserva de una cita que tenías adjudicada."),
    ARCHIVO_CREADO("Los administradores han añadido un archivo."),
    ARCHIVO_BORRADO("Los administradores han eliminado un archivo.");
    
    private final String label;

    private NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static List<NotificationType> getAllNotificationTypes() {
        List<NotificationType> notifications = new ArrayList<>();
        notifications.add(REGISTRO_USUARIO_ADMIN);
        notifications.add(BAJA_USUARIO_ADMIN);
        notifications.add(SOLICITUD_CITA_ADMIN);
        notifications.add(CITA_SUSPENDIDA_ADMIN);
        notifications.add(CLASE_COMPLETA_ADMIN);
        notifications.add(USUARIO_SUSPENDIDO);
        notifications.add(USUARIO_ACTIVADO);
        notifications.add(CLASE_SUSPENDIDA);
        notifications.add(CLASE_ACTIVADA);
        notifications.add(CITA_ACEPTADA);
        notifications.add(CITA_RECHAZADA);
        notifications.add(CITA_SUSPENDIDA);
        notifications.add(CITA_ACTIVADA);
        notifications.add(CITA_CANCELADA);
        notifications.add(ARCHIVO_CREADO);
        notifications.add(ARCHIVO_BORRADO);
        return notifications;
    }

    public static List<NotificationType> getAdminNotificationTypes() {
        List<NotificationType> notifications = new ArrayList<>();
        notifications.add(REGISTRO_USUARIO_ADMIN);
        notifications.add(BAJA_USUARIO_ADMIN);
        notifications.add(SOLICITUD_CITA_ADMIN);
        notifications.add(CITA_SUSPENDIDA_ADMIN);
        notifications.add(CLASE_COMPLETA_ADMIN);
        return notifications;
    }

    public static List<NotificationType> getNotificationTypesForClient() {
        List<NotificationType> notifications = new ArrayList<>();
        notifications.add(USUARIO_ACTIVADO);
        notifications.add(USUARIO_SUSPENDIDO);
        notifications.add(CLASE_ACTIVADA);
        notifications.add(CLASE_SUSPENDIDA);
        notifications.add(CITA_ACEPTADA);
        notifications.add(CITA_RECHAZADA);
        notifications.add(CITA_ACTIVADA);
        notifications.add(CITA_SUSPENDIDA);
        notifications.add(CITA_CANCELADA);
        notifications.add(ARCHIVO_CREADO);
        notifications.add(ARCHIVO_BORRADO);
        return notifications;
    }
    
}
