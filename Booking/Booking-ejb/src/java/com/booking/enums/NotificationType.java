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
    REGISTRO_USUARIO(" se ha registrado en el sistema."),
    BAJA_USUARIO(" se ha dado de baja del sistema."),
    SOLICITUD_RESERVA(" ha solicitado una cita"),
    RESERVA_SUSPENDIDA(" ha suspendido una cita"),
    
    // Clients notifications
    USUARIO_ACTIVADO("Los administradores han activado tu perfil."),
    USUARIO_SUSPENDIDO("Los administradores han suspendido tu perfil."),
    CLASE_ACTIVADA("ha activado un servicio"),
    CLASE_SUSPENDIDA("Los administradores han suspendido una clase en la que tenías plaza."),
    RESERVA_ACEPTADA("Los administradores han aceptado tu solicitud de reserva."),
    RESERVA_RECHAZADA("Los administradores han rechazado tu solicitud de reserva."),
    CITA_ACTIVADA("Los administradores han activado una cita que tenías reservada."),
    CITA_SUSPENDIDA("Los administradores han suspendido una cita que tenías reservada."),
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
        notifications.add(REGISTRO_USUARIO);
        notifications.add(BAJA_USUARIO);
        notifications.add(SOLICITUD_RESERVA);
        notifications.add(RESERVA_SUSPENDIDA);
        notifications.add(USUARIO_SUSPENDIDO);
        notifications.add(USUARIO_ACTIVADO);
        notifications.add(CLASE_SUSPENDIDA);
        notifications.add(CLASE_ACTIVADA);
        notifications.add(RESERVA_ACEPTADA);
        notifications.add(RESERVA_RECHAZADA);
        notifications.add(CITA_SUSPENDIDA);
        notifications.add(CITA_ACTIVADA);
        notifications.add(ARCHIVO_CREADO);
        notifications.add(ARCHIVO_BORRADO);
        return notifications;
    }

    public static List<NotificationType> getAdminNotificationTypes() {
        List<NotificationType> notifications = new ArrayList<>();
        notifications.add(REGISTRO_USUARIO);
        notifications.add(BAJA_USUARIO);
        notifications.add(SOLICITUD_RESERVA);
        notifications.add(RESERVA_SUSPENDIDA);
        return notifications;
    }

    public static List<NotificationType> getNotificationTypesForClient() {
        List<NotificationType> notifications = new ArrayList<>();
        notifications.add(USUARIO_ACTIVADO);
        notifications.add(USUARIO_SUSPENDIDO);
        notifications.add(CLASE_ACTIVADA);
        notifications.add(CLASE_SUSPENDIDA);
        notifications.add(RESERVA_ACEPTADA);
        notifications.add(RESERVA_RECHAZADA);
        notifications.add(CITA_ACTIVADA);
        notifications.add(CITA_SUSPENDIDA);
        notifications.add(ARCHIVO_CREADO);
        notifications.add(ARCHIVO_BORRADO);
        return notifications;
    }
    
}
