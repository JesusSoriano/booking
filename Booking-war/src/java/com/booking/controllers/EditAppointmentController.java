package com.booking.controllers;

import com.booking.entities.Appointment;
import com.booking.entities.AppointmentRequest;
import com.booking.entities.Organisation;
import com.booking.entities.Service;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.NotificationType;
import com.booking.enums.RequestStatus;
import com.booking.enums.Role;
import com.booking.enums.Status;
import com.booking.facades.AppointmentFacade;
import com.booking.facades.AppointmentRequestFacade;
import com.booking.facades.AuditFacade;
import com.booking.facades.NotificationFacade;
import com.booking.facades.ServiceFacade;
import com.booking.facades.UserFacade;
import com.booking.util.Constants;
import com.booking.util.FacesUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

@ManagedBean
@ViewScoped
public class EditAppointmentController implements Serializable {

    @EJB
    private AppointmentFacade appointmentFacade;
    @EJB
    private AppointmentRequestFacade appointmentRequestFacade;
    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private UserFacade userFacade;
    @EJB
    private NotificationFacade notificationFacade;
    @EJB
    private AuditFacade auditFacade;

    private List<SelectItem> services;
    private long selectedServiceId;
    private List<SelectItem> allUsers;
    private long selectedUserId;
    private User loggedUser;
    private Organisation organisation;
    private String description;
    private float price;
    private boolean newAppointment;
    private Appointment currentAppointment;
    private List<AppointmentRequest> appointmentRequests;
    private User appointmentUser;
    private String appointmentId;
    private Date startingTime;
    private Date endingTime;
    private Date date;

    public EditAppointmentController() {
    }

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        services = new ArrayList<>();
        serviceFacade.findAllActiveServicesOfOrganisation(organisation).forEach((s) -> {
            services.add(new SelectItem(s.getId(), s.getName()));
        });

        appointmentId = FacesUtil.getParameter("appointment");
        if (appointmentId != null) {
            currentAppointment = appointmentFacade.findAppointmentOfOrganisation(Long.valueOf(appointmentId), organisation);

            if (currentAppointment != null) {
                selectedServiceId = currentAppointment.getService().getId();
                description = currentAppointment.getDescription();
                date = currentAppointment.getDate();
                startingTime = currentAppointment.getStartTime();
                endingTime = currentAppointment.getEndTime();
                price = currentAppointment.getPrice();
                appointmentUser = currentAppointment.getAppointmentUser();

                appointmentRequests = appointmentRequestFacade.findAllRequestsOfAppointment(currentAppointment);
            }
        } else {
            newAppointment = true;
        }
    }

    public String createNewAppointment() {
        try {
            Service selectedService = serviceFacade.find(selectedServiceId);
            Appointment appointment = appointmentFacade.createNewAppointment(selectedService, description, date, startingTime, endingTime, price, organisation);
            FacesUtil.addSuccessMessage("editAppointmentForm:msg", "La cita ha sido creada correctamente.");
            // Audit appointment creation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CREAR_CLASE, loggedUser, ipAddress, appointment.getId(), organisation);
            return "view-appointment.xhtml" + Constants.FACES_REDIRECT + "&amp;appointment=" + appointment.getId();
        } catch (Exception e) {
            FacesUtil.addErrorMessage("editAppointmentForm:msg", "Lo sentimos, no ha sido posible crear la cita.");
            Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
            return "appointments.xhtml" + Constants.FACES_REDIRECT;
        }
    }

    public String updateAppointment() {
        try {
            Service selectedService = serviceFacade.find(selectedServiceId);
            Appointment updatedAppointment = appointmentFacade.updateAppointment(currentAppointment, selectedService, description, date, startingTime, endingTime, price);
            if (updatedAppointment != null) {
//                FacesUtil.addSuccessMessage("editAppointmentForm:msg", "El servicio ha sido actualizado correctamente.");
                return "view-appointment.xhtml" + Constants.FACES_REDIRECT + "&amp;appointment=" + updatedAppointment.getId();
            } else {
                FacesUtil.addErrorMessage("editAppointmentForm:msg", "Lo sentimos, no ha sido posible editar el servicio.");
            }
        } catch (Exception e) {
            FacesUtil.addErrorMessage("editAppointmentForm:msg", "Lo sentimos, no ha sido posible editar el servicio.");
            Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
        }

        String appointmentParam = (appointmentId != null) ? ("appointment=" + appointmentId) : "";
        return "edit-appointment.xhtml" + Constants.FACES_REDIRECT + appointmentParam;
    }

//    public String addParticipant() {
//        User selectedUser = userFacade.find(selectedUserId);
//        if (selectedUser != null) {
//            bookAppointment(selectedUser);
//        } else {
//            FacesUtil.addErrorMessage("viewAppointmentForm:msg", "Error, el usuario no existe.");
//        }
//
//        return viewAppointmentWithParam();
//    }
//
    public String bookAppointmentForUser() {
        return bookAppointment(loggedUser);
    }

    public String bookAppointment(User user) {
        try {
            // Check if the appointment is available
            if (!currentAppointment.isAvailable()) {
                FacesUtil.addErrorMessage("viewAppointmentForm:msg", "Lo sentimos, esta cita no está disponible para solicitud. Por favor, informa a algún administrador del problema.");
            } else {
                // Create the appointment request
                AppointmentRequest newAppointmentRequest = appointmentRequestFacade.createNewAppointmentRequest(currentAppointment, user, "");
                // Set the appointment user, even if the request is pending.
                appointmentFacade.setAppointmentUser(currentAppointment, user);
                appointmentFacade.makeAppointmentUnavailable(currentAppointment);
                String msg = "La cita ha sido solicitada correctamente";
                if (loggedUserIsAdmin()) {
                    acceptAppointmentRequest(newAppointmentRequest);
                    msg = "La cita ha sido reservada correctamente";
                }
                FacesUtil.addSuccessMessage("viewAppointmentForm:msg", msg);

                if (!loggedUserIsAdmin()) {
                    try {
                        // Create user registration notification
                        List<User> admins = userFacade.findAllActiveAdminsOfOrganisation(organisation);
                        notificationFacade.createNotificationForAdmins(NotificationType.SOLICITUD_CITA_ADMIN, admins, loggedUser, currentAppointment.getId(), organisation);
                    } catch (Exception e) {
                        Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
                
                try {
                    // Audit new appointment request
                    String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                    auditFacade.createAudit(AuditType.RESERVAR_CITA, user, ipAddress, newAppointmentRequest.getId(), organisation);

                } catch (Exception e) {
                    Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
                    FacesUtil.addErrorMessage("viewAppointmentForm:msg", msg + ", pero ha habido un problema auditando la solicitud. Por favor, informa a algún administrador del problema.");
                }
            }
        } catch (Exception e) {
            Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("viewAppointmentForm:msg", "Lo sentimos, ha habido un problema al solicitar la cita.");
        }

        return viewAppointmentWithParam();
    }

    public String acceptCurrentAppointmentRequest() {
        AppointmentRequest appointmentRequest = appointmentRequestFacade.findCurrentRequestOfAppointment(currentAppointment);
        if (appointmentRequest != null) {
            return acceptAppointmentRequest(appointmentRequest);
        } else {
            FacesUtil.addErrorMessage("viewAppointmentForm:msg", "Lo sentimos, la solicitud de cita no existe.");
            return viewAppointmentWithParam();
        }
    }

    public String acceptAppointmentRequest(AppointmentRequest appointmentRequest) {
        appointmentRequestFacade.updateRequestStatus(appointmentRequest, RequestStatus.ACCEPTED, "Aceptada por " + loggedUser.getFirstName());

            try {
                if (appointmentRequest.getRequestUser() != null) {
                    // Create appointment acceptation notification
                    notificationFacade.createNotification(NotificationType.CITA_ACEPTADA, appointmentRequest.getRequestUser(), loggedUser, appointmentRequest.getAppointment().getId(), organisation);
                }
            } catch (Exception e) {
                Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
                FacesUtil.addErrorMessage("viewAppointmentForm:msg", "La notificación de la aceptación no se ha podido crear correctamente.");
            }
            
        try {
            // Audit appointment acceptance
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACEPTAR_CITA, currentAppointment.getAppointmentUser(), ipAddress, currentAppointment.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("viewAppointmentForm:msg", "La solicitud de cita ha sido aceptada correctamente, pero ha habido un problema auditando la cancelación. Por favor informa a algún administrador del problema.");
        }

        return viewAppointmentWithParam();
    }

    public String cancelAppointmentBooking() {
        
        boolean pendingRequest = true;
        try {
            // Check if there is a appointment request
            AppointmentRequest appointmentRequest = appointmentRequestFacade.findCurrentRequestOfAppointment(currentAppointment);
            if (appointmentRequest == null) {
                // Find the appointment accepted request
                appointmentRequest = appointmentRequestFacade.findAcceptedRequestOfAppointment(currentAppointment);
                if (appointmentRequest == null) {
                    FacesUtil.addErrorMessage("viewAppointmentForm:msg", "Error, la solicitud o cita no existe.");
                    return viewAppointmentWithParam();
                }
                pendingRequest = false;
            }
            // Make the request status as CANCELLED
            appointmentRequestFacade.updateRequestStatus(appointmentRequest, RequestStatus.CANCELLED, "Cancelado por " + loggedUser.getFirstName());
            appointmentFacade.makeAppointmentAvailable(currentAppointment);
            User currentAppointmentUser = currentAppointment.getAppointmentUser();
            appointmentFacade.setAppointmentUser(currentAppointment, null);
            FacesUtil.addSuccessMessage("viewAppointmentForm:msg", "La solicitud o cita ha sido cancelada correctamente.");

            
            try {
                if (loggedUserIsAdmin()) {
                    if (pendingRequest) {
                        notificationFacade.createNotification(NotificationType.CITA_RECHAZADA, currentAppointmentUser, loggedUser, currentAppointment.getId(), organisation);
                    } else if (!appointmentUser.equals(loggedUser)) {
                        notificationFacade.createNotification(NotificationType.CITA_CANCELADA, currentAppointmentUser, loggedUser, currentAppointment.getId(), organisation);
                    }
                } else {
                    List<User> admins = userFacade.findAllActiveAdminsOfOrganisation(organisation);
                    notificationFacade.createNotificationForAdmins(NotificationType.CITA_SUSPENDIDA_ADMIN, admins, loggedUser, currentAppointment.getId(), organisation);
                }
            } catch (Exception e) {
                Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
                FacesUtil.addErrorMessage("viewAppointmentForm:msg", "La notificación de la suspensión no se ha podido crear correctamente.");
            }
            
            try {
                // Audit appointment cancalation
                String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                auditFacade.createAudit(AuditType.CANCELAR_CITA, currentAppointmentUser, ipAddress, currentAppointment.getId(), organisation);

            } catch (Exception e) {
                Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
                FacesUtil.addErrorMessage("viewAppointmentForm:msg", "La solicitud o cita ha sido cancelada correctamente, pero ha habido un problema auditando la cancelación. Por favor informa a algún administrador del problema.");
            }
        } catch (Exception e) {
            Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("viewAppointmentForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
        }

        return viewAppointmentWithParam();
    }


    public boolean isMyAppointmentBooking() {
        AppointmentRequest acceptedAppointmentRequest = appointmentRequestFacade.findAcceptedRequestOfAppointment(currentAppointment);
        return acceptedAppointmentRequest != null && acceptedAppointmentRequest.getRequestUser().equals(loggedUser);
    }
    
    /**
     * Check if the user have an appointment request of the current appointment
     * or, if the user is an ADMIN, check if there is any.
     *
     * @return boolean
     */
    public boolean isUserAppointmentRequestOrAdminCanSeeRequest() {
        AppointmentRequest currentAppointmentRequest;
        if (loggedUserIsAdmin()) {
            // Find if there is a current request of the appointment
            currentAppointmentRequest = appointmentRequestFacade.findCurrentRequestOfAppointment(currentAppointment);
        } else {
            // Find if there is a current appointment request from the user
            currentAppointmentRequest = appointmentRequestFacade.findCurrentRequestOfAppointmentForUser(currentAppointment, loggedUser);
        }
        return currentAppointmentRequest != null;
    }

    public String activateAppointment() {
        appointmentFacade.activateAppointment(currentAppointment);
        FacesUtil.addSuccessMessage("viewAppointmentForm:msg", "La cita ha sido activada correctamente.");

        try {
            if (currentAppointment.getAppointmentUser() != null) {
                // Create appointment activation notification
                notificationFacade.createNotification(NotificationType.CITA_ACTIVADA, currentAppointment.getAppointmentUser(), loggedUser, currentAppointment.getId(), organisation);
            }
        } catch (Exception e) {
            Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("viewAppointmentForm:msg", "La notificación de la activación no se ha podido crear correctamente.");
        }
        
        try {
            // Audit appointment activation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVAR_SERVICIO, loggedUser, ipAddress, currentAppointment.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
        }
        return viewAppointmentWithParam();
    }

    public String deactivateAppointment() {
        appointmentFacade.deactivateAppointment(currentAppointment);
        FacesUtil.addSuccessMessage("viewAppointmentForm:msg", "La cita ha sido suspendida correctamente.");

        try {
            if (currentAppointment.getAppointmentUser() != null) {
                // Create appointment deactivation notification
                notificationFacade.createNotification(NotificationType.CITA_ACTIVADA, currentAppointment.getAppointmentUser(), loggedUser, currentAppointment.getId(), organisation);
            }
        } catch (Exception e) {
            Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("viewAppointmentForm:msg", "La notificación de la suspensión no se ha podido crear correctamente.");
        }
        
        try {
            // Audit appointment suspention
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.SUSPENDER_SERVICIO, loggedUser, ipAddress, currentAppointment.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
        }
        return viewAppointmentWithParam();
    }

    public String viewAppointmentWithParam() {
        String appointmentParam = (appointmentId != null) ? ("appointment=" + appointmentId) : "";
        return "view-appointment.xhtml" + Constants.FACES_REDIRECT + appointmentParam;
    }

    public String cancelEditAppointment() {
        if (newAppointment) {
            return "appointments.xhtml" + Constants.FACES_REDIRECT;
        } else {
            return viewAppointmentWithParam();
        }
    }

    public boolean bookedAppointmentForUser() {
        return currentAppointment.getAppointmentUser() != null && currentAppointment.getAppointmentUser().equals(loggedUser);
    }

    public List<SelectItem> getServices() {
        return services;
    }

    public List<SelectItem> getAllUsers() {
        return allUsers;
    }

    public Role getUserRole() {
        return loggedUser.getUserRole().getRole();
    }

    public boolean loggedUserIsAdmin() {
        Role userRole = loggedUser.getUserRole().getRole();
        return userRole == Role.ADMIN || userRole == Role.SUPER_ADMIN;
    }

    public boolean isNewAppointment() {
        return newAppointment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getSelectedServiceId() {
        return selectedServiceId;
    }

    public void setSelectedServiceId(long selectedServiceId) {
        this.selectedServiceId = selectedServiceId;
    }

    public long getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(long selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public Appointment getCurrentAppointment() {
        return currentAppointment;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public boolean isPastAppointment() {
        if (currentAppointment != null && currentAppointment.getDate() != null) {
            return currentAppointment.getDate().before(new Date());
        }
        return false;
    }

    public Date getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(Date endingTime) {
        this.endingTime = endingTime;
    }

    public Date getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(Date startingTime) {
        this.startingTime = startingTime;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getAppointmentUser() {
        return appointmentUser;
    }

    public List<AppointmentRequest> getAppointmentRequests() {
        return appointmentRequests;
    }

    public Status getAppointmentStatus() {
        return currentAppointment.getStatus();
    }
}
