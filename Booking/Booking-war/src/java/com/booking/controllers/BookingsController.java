package com.booking.controllers;

import com.booking.entities.ActivityClass;
import com.booking.entities.Appointment;
import com.booking.entities.AppointmentRequest;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.RequestStatus;
import com.booking.enums.Role;
import com.booking.facades.AppointmentFacade;
import com.booking.facades.AppointmentRequestFacade;
import com.booking.facades.AuditFacade;
import com.booking.facades.BookingFacade;
import com.booking.facades.ClassFacade;
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
public class BookingsController implements Serializable {

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
    private AuditFacade auditFacade;

    private User bookingsUser;
    private User loggedUser;
    private String userId;
    private Organisation organisation;
    private List<ActivityClass> classes;
    private List<ActivityClass> pastClasses;
    private List<Appointment> appointments;
    private List<Appointment> pastAppointments;

    /**
     * Creates a new instance of BookingsController
     */
    public BookingsController() {
    }

    @PostConstruct
    public void init() {

        organisation = FacesUtil.getCurrentOrganisation();

        loggedUser = FacesUtil.getCurrentUser();
        userId = FacesUtil.getParameter("user");
        if (userId != null && loggedUserIsAdmin()) {
            bookingsUser = userFacade.findUserOfOrganisation(Integer.valueOf(userId), organisation);
        }
        if (bookingsUser == null) {
            bookingsUser = loggedUser;
        }

        classes = bookingFacade.findAllCurrentClassesOfUser(bookingsUser);
        pastClasses = bookingFacade.findAllPastClassesOfUser(bookingsUser);
        appointments = appointmentFacade.findAllCurrentAppointmentsOfUser(bookingsUser, organisation);
        pastAppointments = appointmentFacade.findAllPastAppointmentsOfUser(bookingsUser, organisation);
    }

    public String cancelClassBooking(ActivityClass activityClass) {
        try {
            // Create the class user
            if (bookingFacade.removeBooking(bookingsUser, activityClass)) {
                // Add a booked place in the class
                classFacade.removeClassBooking(activityClass);
                FacesUtil.addSuccessMessage("myBookingsForm:msg", "La reserva ha sido cancelada correctamente.");
            } else {
                FacesUtil.addErrorMessage("myBookingsForm:msg", "Error, la reserva no existe.");
            }
        } catch (Exception e) {
            Logger.getLogger(BookingsController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("myBookingsForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
        }

        try {
            // Audit booking cancelation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CANCELAR_RESERVA, bookingsUser, ipAddress, activityClass.getId(), activityClass.getOrganisation());
        } catch (Exception e) {
            Logger.getLogger(BookingsController.class.getName()).log(Level.SEVERE, null, e);
        }

        return myBookingsWithParam();
    }

    public String cancelAppointmentBooking(Appointment appointment) {
        try {
            // Check if there is a appointment request
            AppointmentRequest appointmentRequest = appointmentRequestFacade.findCurrentRequestOfAppointment(appointment);
            if (appointmentRequest == null) {
                // Find the appointment request
                appointmentRequest = appointmentRequestFacade.findAcceptedRequestOfAppointment(appointment);
                if (appointmentRequest == null) {
                    FacesUtil.addErrorMessage("myBookingsForm:msg", "Error, la solicitud o cita no existe.");
                    return myBookingsWithParam();
                }
            }
            // Make the request status as CANCELLED
            appointmentRequestFacade.updateRequestStatus(appointmentRequest, RequestStatus.CANCELLED, "Cancelado por " + loggedUser.getFirstName());
            appointmentFacade.makeAppointmentAvailable(appointment);
            appointmentFacade.deleteAppointmentUser(appointment);
            FacesUtil.addSuccessMessage("myBookingsForm:msg", "La solicitud o cita ha sido cancelada correctamente.");

            try {
                // Audit appointment cancalation
                String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                auditFacade.createAudit(AuditType.CANCELAR_CITA, appointment.getAppointmentUser(), ipAddress, appointment.getId(), organisation);
            } catch (Exception e) {
                Logger.getLogger(BookingsController.class.getName()).log(Level.SEVERE, null, e);
                FacesUtil.addErrorMessage("myBookingsForm:msg", "La solicitud o cita ha sido cancelada correctamente, pero ha habido un problema auditando la cancelación. Por favor informa a algún administrador del problema.");
            }
        } catch (Exception e) {
            Logger.getLogger(BookingsController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("myBookingsForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
        }

        return myBookingsWithParam();
    }

    public String activateAppointment(Appointment appointment) {
        appointmentFacade.activateAppointment(appointment);
        FacesUtil.addSuccessMessage("myBookingsForm:msg", "La cita ha sido activado correctamente.");

        try {
            // Audit appointment activation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVAR_SERVICIO, loggedUser, ipAddress, appointment.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(BookingsController.class.getName()).log(Level.SEVERE, null, e);
        }

        return myBookingsWithParam();
    }

    public String deactivateAppointment(Appointment appointment) {
        appointmentFacade.deactivateAppointment(appointment);
        FacesUtil.addSuccessMessage("myBookingsForm:msg", "La cita ha sido suspendido correctamente.");

        try {
            // Audit appointment suspention
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.SUSPENDER_SERVICIO, loggedUser, ipAddress, appointment.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(BookingsController.class.getName()).log(Level.SEVERE, null, e);
        }

        return myBookingsWithParam();
    }

    public boolean isMyAppointmentBooking(Appointment appointment) {
        AppointmentRequest acceptedAppointmentRequest = appointmentRequestFacade.findAcceptedRequestOfAppointment(appointment);
        return acceptedAppointmentRequest != null && acceptedAppointmentRequest.getRequestUser().equals(bookingsUser);
    }

    public boolean loggedUserIsAdmin() {
        Role userRole = loggedUser.getUserRole().getRole();
        return userRole == Role.ADMIN || userRole == Role.SUPER_ADMIN;
    }

    private String myBookingsWithParam() {
        String userParam = (userId != null) ? ("user=" + userId) : "";
        return "my-bookings.xhtml" + Constants.FACES_REDIRECT + userParam;
    }

    public List<ActivityClass> getClasses() {
        return classes;
    }

    public List<ActivityClass> getPastClasses() {
        return pastClasses;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public List<Appointment> getPastAppointments() {
        return pastAppointments;
    }
}
