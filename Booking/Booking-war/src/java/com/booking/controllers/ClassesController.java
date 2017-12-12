package com.booking.controllers;

import com.booking.entities.ActivityClass;
import com.booking.entities.Booking;
import com.booking.entities.Organisation;
import com.booking.entities.Service;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.NotificationType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
import com.booking.facades.ClassFacade;
import com.booking.facades.BookingFacade;
import com.booking.facades.NotificationFacade;
import com.booking.facades.ServiceFacade;
import com.booking.facades.UserFacade;
import com.booking.util.Constants;
import com.booking.util.FacesUtil;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class ClassesController implements Serializable {

    @EJB
    private ClassFacade classFacade;
    @EJB
    private BookingFacade bookingFacade;
    @EJB
    private UserFacade userFacade;
    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private NotificationFacade notificationFacade;
    @EJB
    private AuditFacade auditFacade;

    private List<ActivityClass> classes;
    private List<ActivityClass> pastClasses;
    private User loggedUser;
    private Organisation organisation;
    private Service currentService;
    private String serviceId;

    public ClassesController() {
    }

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        serviceId = FacesUtil.getParameter("service");
        if (serviceId != null) {
            currentService = serviceFacade.findServiceOfOrganisation(Integer.valueOf(serviceId), organisation);
            if (currentService != null) {
                classes = classFacade.findAllCurrentClassesOfService(currentService, organisation);
                pastClasses = classFacade.findAllPastActiveClassesOfService(currentService, organisation);
            } else {
                classes = classFacade.findAllCurrentClassesOfOrganisation(organisation);
                pastClasses = classFacade.findAllPastClassesOfOrganisation(organisation);
            }
        } else {
            classes = classFacade.findAllCurrentClassesOfOrganisation(organisation);
            pastClasses = classFacade.findAllPastClassesOfOrganisation(organisation);
        }
    }

    public String activateClass(ActivityClass activityClass) {
        classFacade.activateClass(activityClass);
        FacesUtil.addSuccessMessage("classesForm:msg", "El servicio ha sido activado correctamente.");

        try {
            // Create class activation notification
            List<User> classUsers = bookingFacade.findAllBookedUsersOfClass(activityClass);
            classUsers.forEach((classUser) -> {
                notificationFacade.createNotification(NotificationType.CLASE_ACTIVADA, classUser, loggedUser, activityClass.getId(), organisation);
            });
        } catch (Exception e) {
            Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("classesForm:msg", "La notificación de la activación no se ha podido crear correctamente.");
        }

        try {
            // Audit class activation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVAR_SERVICIO, loggedUser, ipAddress, activityClass.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
        }

        return classesWithParam();
    }

    public String deactivateClass(ActivityClass activityClass) {
        classFacade.deactivateClass(activityClass);
        FacesUtil.addSuccessMessage("classesForm:msg", "El servicio ha sido suspendido correctamente.");

        try {
            // Create class deactivation notification
            List<User> classUsers = bookingFacade.findAllBookedUsersOfClass(activityClass);
            classUsers.forEach((classUser) -> {
                notificationFacade.createNotification(NotificationType.CLASE_SUSPENDIDA, classUser, loggedUser, activityClass.getId(), organisation);
            });
        } catch (Exception e) {
            Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("classesForm:msg", "La notificación de la suspensión no se ha podido crear correctamente.");
        }

        try {
            // Audit class suspention
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.SUSPENDER_SERVICIO, loggedUser, ipAddress, activityClass.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
        }

        return classesWithParam();
    }

    public String bookClass(ActivityClass activityClass) {
        try {
            // Check if the booking already exists
            if (bookingFacade.existsBooking(loggedUser, activityClass)) {
                FacesUtil.addErrorMessage("classesForm:msg", "Esta clase ya ha sido resarvada previamente.");
                // Check if there are free places
            } else if (activityClass.getBookedPlaces() == activityClass.getMaximumUsers()) {
                FacesUtil.addErrorMessage("classesForm:msg", "No quedan plazas para esta clase. Puedes revisar periódicamente si queda alguna libre.");
            } else {
                // Create the booking
                Booking booking = bookingFacade.createNewBooking(loggedUser, activityClass);
                if (booking != null) {
                    // Add a booked place in the class
                    classFacade.addClassBooking(activityClass);

                    FacesUtil.addSuccessMessage("classesForm:msg", "La plaza ha sido reservada correctamente.");

                    if (activityClass.getBookedPlaces() == activityClass.getMaximumUsers()) {
                        try {
                            // Create full class notification
                            List<User> admins = userFacade.findAllActiveAdminsOfOrganisation(organisation);
                            notificationFacade.createNotificationForAdmins(NotificationType.CLASE_COMPLETA_ADMIN, admins, loggedUser, activityClass.getId(), organisation);
                        } catch (Exception e) {
                            Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }

                    try {
                        // Audit class booking
                        String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                        auditFacade.createAudit(AuditType.RESERVAR_CLASE, loggedUser, ipAddress, activityClass.getId(), organisation);
                    } catch (Exception e) {
                        Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("classesForm:msg", "Lo sentimos, ha habido un problema al reservar la plaza.");
        }

        return classesWithParam();
    }

    public String cancelClassBooking(ActivityClass activityClass) {
        try {
            // Remove booking
            if (bookingFacade.removeBooking(loggedUser, activityClass)) {
                // Remove a booked place in the class
                classFacade.removeClassBooking(activityClass);
                FacesUtil.addSuccessMessage("classesForm:msg", "La reserva ha sido cancelada correctamente.");
            } else {
                FacesUtil.addErrorMessage("classesForm:msg", "Error, la reserva no existe.");
            }
        } catch (Exception e) {
            Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("classesForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
        }

        try {
            // Audit booking cancelation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CANCELAR_RESERVA, loggedUser, ipAddress, activityClass.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
        }

        return classesWithParam();
    }

    public String duplicateClass(ActivityClass activityClass) {
        try {
            ActivityClass duplicatedActivityClass = classFacade.duplicateClass(activityClass);
            FacesUtil.addSuccessMessage("classesForm:msg", "La clase ha sido duplicada correctamente.");
            // Audit class duplicate
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.DUPLICAR_CLASE, loggedUser, ipAddress, duplicatedActivityClass.getId(), organisation);
        } catch (Exception e) {
            FacesUtil.addErrorMessage("classesForm:msg", "Lo sentimos, no ha sido posible duplicar la clase.");
            Logger.getLogger(EditClassController.class.getName()).log(Level.SEVERE, null, e);
        }

        return classesWithParam();
    }

    private String classesWithParam() {
        String serviceParam = (serviceId != null) ? ("service=" + serviceId) : "";
        return "classes.xhtml" + Constants.FACES_REDIRECT + serviceParam;
    }

    public boolean existsBooking(ActivityClass activityClass) {
        return bookingFacade.existsBooking(loggedUser, activityClass);
    }

    public List<ActivityClass> getClasses() {
        return classes;
    }

    public Role getUserRole() {
        return loggedUser.getUserRole().getRole();
    }

    public Service getCurrentService() {
        return currentService;
    }

    public List<ActivityClass> getPastClasses() {
        return pastClasses;
    }
}
