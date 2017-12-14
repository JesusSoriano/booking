package com.booking.controllers.superAdmin;

import com.booking.entities.ActivityClass;
import com.booking.entities.Appointment;
import com.booking.entities.Notification;
import com.booking.entities.User;
import com.booking.enums.NotificationType;
import com.booking.enums.Role;
import com.booking.facades.AppointmentFacade;
import com.booking.facades.ClassFacade;
import com.booking.facades.DocumentFacade;
import com.booking.facades.NotificationFacade;
import com.booking.facades.UserFacade;
import com.booking.util.FacesUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;

/**
 *
 * @author Jesús Soriano
 */
@ManagedBean
public class NotificationsController implements Serializable {

    @EJB
    private NotificationFacade notificationFacade;
    @EJB
    private UserFacade userFacade;
    @EJB
    private AppointmentFacade appointmentFacade;
    @EJB
    private ClassFacade classFacade;
    @EJB
    private DocumentFacade documentFacade;

    private List<Notification> notificationList;
    private Role userRole;
    private List<SelectItem> notificationTypes;
    private NotificationType selectedNotificationType;
    private User loggedUser;

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        userRole = loggedUser.getUserRole().getRole();

        notificationTypes = new ArrayList<>();
        notificationTypes.add(new SelectItem(null, "Todo"));

        if (loggedUserIsAdmin()) {
            NotificationType.getAllNotificationTypes().forEach((at) -> {
                notificationTypes.add(new SelectItem(at.name(), at.getLabel()));
            });
        } else {
            NotificationType.getNotificationTypesForClient().forEach((at) -> {
                notificationTypes.add(new SelectItem(at.name(), at.getLabel()));
            });
        }

        notificationList = notificationFacade.findAllNotificationsOfUser(loggedUser);
    }

    public void search() {
        if (selectedNotificationType != null) {
            notificationList = notificationFacade.findAllNotificationsOfTypeOfUser(loggedUser, selectedNotificationType);
        } else {
            notificationList = notificationFacade.findAllNotificationsOfUser(loggedUser);
        }
    }

    public String getUserName(Long id) {
        User client = userFacade.find(id);
        if (client != null) {
            return client.getFullName();
        }
        return "";
    }

    public String printNotification(Notification notification) {

        String label = notification.getNotificationType().getLabel();

        switch (notification.getNotificationType().name()) {
            case "REGISTRO_USUARIO_ADMIN":
            case "BAJA_USUARIO_ADMIN":
            case "SOLICITUD_CITA_ADMIN":
            case "CITA_SUSPENDIDA_ADMIN": {
                User actionUser = notification.getActiontionUser();
                if (actionUser != null) {
                    return actionUser.getFullName() + label;
                }
            }
        }

        return label;
    }

    public void takeAction(Notification notification) {
        switch (notification.getNotificationType().name()) {
            case "REGISTRO_USUARIO_ADMIN":
            case "BAJA_USUARIO_ADMIN":
            case "USUARIO_ACTIVADO":
            case "USUARIO_SUSPENDIDO": {
                User actionUser = notification.getActiontionUser();
                if (actionUser != null) {
                    try {
                        FacesUtil.redirectTo("user-profile.xhtml", "&user=" + actionUser.getId());
                    } catch (IOException ex) {
                        Logger.getLogger(NotificationsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            }
            case "SOLICITUD_CITA_ADMIN":
            case "CITA_SUSPENDIDA_ADMIN":
            case "CITA_ACEPTADA":
            case "CITA_RECHAZADA":
            case "CITA_ACTIVADA":
            case "CITA_SUSPENDIDA":
            case "CITA_CANCELADA": {
                Appointment actionAppointment = appointmentFacade.find(notification.getObjectId());
                if (actionAppointment != null) {
                    try {
                        FacesUtil.redirectTo("view-appointment.xhtml", "&appointment=" + actionAppointment.getId());
                    } catch (IOException ex) {
                        Logger.getLogger(NotificationsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            }

            case "CLASE_ACTIVADA":
            case "CLASE_SUSPENDIDA": {
                ActivityClass activityClass = classFacade.find(notification.getObjectId());
                if (activityClass != null) {
                    try {
                        FacesUtil.redirectTo("view-class.xhtml", "&class=" + activityClass.getId());
                    } catch (IOException ex) {
                        Logger.getLogger(NotificationsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            }

            case "ARCHIVO_CREADO":
            case "ARCHIVO_BORRADO": {
                try {
                    FacesUtil.redirectTo("files.xhtml");
                } catch (IOException ex) {
                    Logger.getLogger(NotificationsController.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
        }
    }

    public void setNotificationCheck(Notification notification) {
        notificationFacade.setNotificationCheck(notification, !notification.isChecked());
    }

    public boolean isAdminNotification(NotificationType notificationType) {
        return NotificationType.getAdminNotificationTypes().contains(notificationType);
    }

    public boolean loggedUserIsAdmin() {
        return userRole == Role.ADMIN || userRole == Role.SUPER_ADMIN;
    }

    public List<Notification> getNotificationList() {
        return notificationList;
    }

    public Role getUserRole() {
        return userRole;
    }

    public List<SelectItem> getNotificationTypes() {
        return notificationTypes;
    }

    public NotificationType getSelectedNotificationType() {
        return selectedNotificationType;
    }

    public void setSelectedNotificationType(NotificationType selectedNotificationType) {
        this.selectedNotificationType = selectedNotificationType;
    }
}
