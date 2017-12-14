package com.booking.controllers;

import com.booking.entities.ActivityClass;
import com.booking.entities.Appointment;
import com.booking.entities.AppointmentRequest;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.NotificationType;
import com.booking.enums.RequestStatus;
import com.booking.enums.Role;
import com.booking.enums.Status;
import com.booking.facades.AppointmentFacade;
import com.booking.facades.AppointmentRequestFacade;
import com.booking.facades.AuditFacade;
import com.booking.facades.BookingFacade;
import com.booking.facades.ClassFacade;
import com.booking.facades.NotificationFacade;
import com.booking.facades.UserFacade;
import com.booking.util.FacesUtil;
import com.booking.util.StringsUtil;
import java.io.IOException;
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
public class UserProfileController implements Serializable {

    @EJB
    private UserFacade userFacade;
    @EJB
    private BookingFacade bookingFacade;
    @EJB
    private ClassFacade classFacade;
    @EJB
    private AppointmentFacade appointmentFacade;
    @EJB
    private AppointmentRequestFacade appointmentRequestFacade;
    @EJB
    private NotificationFacade notificationFacade;
    @EJB
    private AuditFacade auditFacade;

    private User profileUser;
    private String firstName;
    private String firstLastName;
    private String secondLastName;
    private String email;
    private String phone;
    private String addressLine;
    private String addressLine2;
    private String city;
    private String country;
    private String postcode;
    private String userId;
    private boolean suspended;
    private List<ActivityClass> classes;
    private List<Appointment> appointments;
    private User loggedUser;
    private Organisation organisation;

    /**
     * Creates a new instance of UserProfileController
     */
    public UserProfileController() {
    }

    @PostConstruct
    public void init() {

        organisation = FacesUtil.getCurrentOrganisation();

        loggedUser = FacesUtil.getCurrentUser();
        userId = FacesUtil.getParameter("user");
        if (userId != null && (loggedUser.getUserRole().getRole() == Role.ADMIN || loggedUser.getUserRole().getRole() == Role.SUPER_ADMIN)) {
            profileUser = userFacade.findUserOfOrganisation(Integer.valueOf(userId), organisation);
        } else {
            profileUser = loggedUser;
        }

        if (profileUser != null) {
            firstName = profileUser.getFirstName();
            firstLastName = profileUser.getFirstLastName();
            secondLastName = profileUser.getSecondLastName();
            email = profileUser.getEmail();
            phone = profileUser.getPhone();
            addressLine = profileUser.getAddress().getAddressLine();
            addressLine2 = profileUser.getAddress().getAddressLine2();
            city = profileUser.getAddress().getCity();
            country = profileUser.getAddress().getCountry();
            postcode = profileUser.getAddress().getPostcode();
            suspended = profileUser.getStatus().equals(Status.SUSPENDED);

            classes = bookingFacade.findAllCurrentClassesOfUser(profileUser);
            appointments = appointmentFacade.findAllCurrentAppointmentsOfUser(profileUser, organisation);
        }
    }

    public void saveProfile() {

        if (StringsUtil.isNotNullNotEmpty(firstName)) {
            FacesUtil.addErrorMessage("user-profileForm", "Introduce tu nombre");
        }
        if (StringsUtil.isNotNullNotEmpty(firstLastName)) {
            FacesUtil.addErrorMessage("user-profileForm", "Introduce tu primer apellido");
        }

        // Remove start and end white spaces of email
        email = email.trim();
        if (StringsUtil.isNotNullNotEmpty(email)) {
            FacesUtil.addErrorMessage("user-profileForm", "Introduce tu email");
        }

        userFacade.editUserProfile(profileUser, firstName, firstLastName, secondLastName, email,
                phone, addressLine, addressLine2, city, country, postcode);
        try {
            FacesUtil.redirectTo("user-profile.xhtml", "&user=" + profileUser.getId());
        } catch (IOException ex) {
            Logger.getLogger(UserProfileController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void goToProfileEdition() {
        try {
            FacesUtil.redirectTo("edit-user.xhtml", "&user=" + profileUser.getId());
        } catch (IOException ex) {
            Logger.getLogger(UserProfileController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cancelProfileEdition() {
        try {
            FacesUtil.redirectTo("user-profile.xhtml", "&user=" + profileUser.getId());
        } catch (IOException ex) {
            Logger.getLogger(UserProfileController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cancelClassBooking(ActivityClass activityClass) {
        try {
            // Create the class user
            if (bookingFacade.removeBooking(profileUser, activityClass)) {
                // Add a booked place in the class
                classFacade.removeClassBooking(activityClass);
                FacesUtil.addSuccessMessage("user-profileForm:msg", "La plaza ha sido cancelada correctamente.");
            } else {
                FacesUtil.addErrorMessage("user-profileForm:msg", "Error, la reserva no existe.");
            }
        } catch (Exception e) {
            Logger.getLogger(UserProfileController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("user-profileForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
        }

        try {
            // Audit booking cancelation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CANCELAR_RESERVA, profileUser, ipAddress, activityClass.getId(), activityClass.getOrganisation());
        } catch (Exception e) {
            Logger.getLogger(UserProfileController.class.getName()).log(Level.SEVERE, null, e);
        }

        profileWithParams();
    }

    public void cancelAppointmentBooking(Appointment appointment) {
        try {
            boolean pendingRequest = true;
            // Check if there is a appointment request
            AppointmentRequest appointmentRequest = appointmentRequestFacade.findCurrentRequestOfAppointment(appointment);
            if (appointmentRequest == null) {
                // Find the appointment request
                appointmentRequest = appointmentRequestFacade.findAcceptedRequestOfAppointment(appointment);
                if (appointmentRequest == null) {
                    FacesUtil.addErrorMessage("user-profileForm:msg", "Error, la solicitud o cita no existe.");
                    profileWithParams();
                }
                pendingRequest = false;
            }
            // Make the request status as CANCELLED
            appointmentRequestFacade.updateRequestStatus(appointmentRequest, RequestStatus.CANCELLED, "Cancelado por " + loggedUser.getFirstName());
            appointmentFacade.makeAppointmentAvailable(appointment);
            User appointmentUser = appointment.getAppointmentUser();
            appointmentFacade.deleteAppointmentUser(appointment);
            FacesUtil.addSuccessMessage("user-profileForm:msg", "La solicitud o cita ha sido cancelada correctamente.");

            try {
                if (loggedUserIsAdmin()) {
                    if (pendingRequest) {
                        notificationFacade.createNotification(NotificationType.CITA_RECHAZADA, appointmentUser, loggedUser, appointment.getId(), organisation);
                    } else if (!appointmentUser.equals(loggedUser)) {
                        notificationFacade.createNotification(NotificationType.CITA_CANCELADA, appointmentUser, loggedUser, appointment.getId(), organisation);
                    }
                } else {
                    List<User> admins = userFacade.findAllActiveAdminsOfOrganisation(organisation);
                    notificationFacade.createNotificationForAdmins(NotificationType.CITA_SUSPENDIDA_ADMIN, admins, loggedUser, appointment.getId(), organisation);
                }
            } catch (Exception e) {
                Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
                FacesUtil.addErrorMessage("user-profileForm:msg", "La notificación de la suspensión no se ha podido crear correctamente.");
            }

            try {
                // Audit appointment cancalation
                String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                auditFacade.createAudit(AuditType.CANCELAR_CITA, appointment.getAppointmentUser(), ipAddress, appointment.getId(), organisation);
            } catch (Exception e) {
                Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
                FacesUtil.addErrorMessage("user-profileForm:msg", "La solicitud o cita ha sido cancelada correctamente, pero ha habido un problema auditando la cancelación. Por favor informa a algún administrador del problema.");
            }
        } catch (Exception e) {
            Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("user-profileForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
        }

        profileWithParams();
    }

    public boolean isUserAppointmentRequestOrAdminCanSeeRequest(Appointment appointment) {
        AppointmentRequest currentAppointmentRequest;
        if (loggedUserIsAdmin()) {
            // Find if there is a current request of the appointment
            currentAppointmentRequest = appointmentRequestFacade.findCurrentRequestOfAppointment(appointment);
        } else {
            // Find if there is a current appointment request from the user
            currentAppointmentRequest = appointmentRequestFacade.findCurrentRequestOfAppointmentForUser(appointment, loggedUser);
        }
        return currentAppointmentRequest != null;
    }

    private void profileWithParams() {
        String userParam = (userId != null) ? ("&user=" + userId) : "";
        try {
            FacesUtil.redirectTo("user-profile.xhtml", userParam);
        } catch (IOException ex) {
            Logger.getLogger(UserProfileController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean loggedUserIsAdmin() {
        Role userRole = loggedUser.getUserRole().getRole();
        return userRole == Role.ADMIN || userRole == Role.SUPER_ADMIN;
    }

    public boolean loggedUserIsNotProfileUser() {
        return (userId != null);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstLastName() {
        return firstLastName;
    }

    public void setFirstLastName(String firstLastName) {
        this.firstLastName = firstLastName;
    }

    public String getSecondLastName() {
        return secondLastName;
    }

    public void setSecondLastName(String secondLastName) {
        this.secondLastName = secondLastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public List<ActivityClass> getClasses() {
        return classes;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public User getProfileUser() {
        return profileUser;
    }
}
