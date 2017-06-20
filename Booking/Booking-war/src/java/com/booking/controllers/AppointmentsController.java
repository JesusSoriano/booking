package com.booking.controllers;

import com.booking.entities.Appointment;
import com.booking.entities.AppointmentRequest;
import com.booking.entities.Organisation;
import com.booking.entities.Service;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.RequestStatus;
import com.booking.enums.Role;
import com.booking.facades.AppointmentFacade;
import com.booking.facades.AppointmentRequestFacade;
import com.booking.facades.AuditFacade;
import com.booking.facades.ServiceFacade;
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
public class AppointmentsController implements Serializable {

    @EJB
    private AppointmentFacade appointmentFacade;
    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private AuditFacade auditFacade;
    @EJB
    private AppointmentRequestFacade appointmentRequestFacade;

    private List<Appointment> appointments;
    private List<Appointment> pastAppointments;
    private User loggedUser;
    private Organisation organisation;
    private Service currentService;
    private String serviceId;

    public AppointmentsController() {
    }

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        serviceId = FacesUtil.getParameter("service");
        if (serviceId != null) {
            currentService = serviceFacade.findServiceOfOrganisation(Integer.valueOf(serviceId), organisation);
            if (currentService != null) {
                appointments = appointmentFacade.findAllCurrentAppointmentsOfService(currentService, organisation);
                pastAppointments = appointmentFacade.findAllPastAppointmentsOfService(currentService, organisation);
            } else {
                appointments = appointmentFacade.findAllCurrentAppointmentsOfOrganisation(organisation);
                pastAppointments = appointmentFacade.findAllPastAppointmentsOfOrganisation(organisation);
            }
        } else {
            appointments = appointmentFacade.findAllCurrentAppointmentsOfOrganisation(organisation);
            pastAppointments = appointmentFacade.findAllPastAppointmentsOfOrganisation(organisation);
        }
    }

    public String activateAppointment(Appointment appointment) {
        appointmentFacade.activateAppointment(appointment);
        FacesUtil.addSuccessMessage("appointmentsForm:msg", "La cita ha sido activado correctamente.");

        try {
            // Audit appointment activation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVAR_SERVICIO, loggedUser, ipAddress, appointment.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
        }

        return appointmentsWithParam();
    }

    public String deactivateAppointment(Appointment appointment) {
        appointmentFacade.deactivateAppointment(appointment);
        FacesUtil.addSuccessMessage("appointmentsForm:msg", "La cita ha sido suspendido correctamente.");

        try {
            // Audit appointment suspention
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.SUSPENDER_SERVICIO, loggedUser, ipAddress, appointment.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
        }

        return appointmentsWithParam();
    }

    public String bookAppointment(Appointment appointment) {
        try {
            // Check if the appointment is available
            if (!appointment.isAvailable()) {
                FacesUtil.addErrorMessage("appointmentsForm:msg", "Lo sentimos, esta cita no está disponible para solicitud. Por favor, informa a algún administrador del problema.");
            } else {
                // Create the appointment request
                AppointmentRequest newAppointmentRequest = appointmentRequestFacade.createNewAppointmentRequest(appointment, loggedUser, "");
                // Set the appointment user, even if the request is pending.
                appointmentFacade.setAppointmentUser(appointment, loggedUser);
                appointmentFacade.makeAppointmentUnabailable(appointment);
                String msg = "La cita ha sido solicitada correctamente";
                if (loggedUserIsAdmin()) {
                    appointmentRequestFacade.updateRequestStatus(newAppointmentRequest, RequestStatus.ACCEPTED, "Cita reservada por " + loggedUser.getFirstName());
                    msg = "La cita ha sido reservada correctamente";
                }

                FacesUtil.addSuccessMessage("appointmentsForm:msg", msg);

                try {
                    // Audit new appointment request
                    String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                    auditFacade.createAudit(AuditType.RESERVAR_CITA, loggedUser, ipAddress, newAppointmentRequest.getId(), organisation);

                } catch (Exception e) {
                    Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
                    FacesUtil.addErrorMessage("appointmentsForm:msg", msg + ", pero ha habido un problema auditando la solicitud. Por favor informa a algún administrador del problema.");
                }
            }
        } catch (Exception e) {
            Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("appointmentsForm:msg", "Lo sentimos, ha habido un problema al solicitar la cita.");
        }

        return appointmentsWithParam();
    }

    public String cancelAppointmentBooking(Appointment appointment) {
        try {
            // Check if there is a appointment request
            AppointmentRequest appointmentRequest = appointmentRequestFacade.findCurrentRequestOfAppointment(appointment);
            if (appointmentRequest == null) {
                // Find the appointment request
                appointmentRequest = appointmentRequestFacade.findAcceptedRequestOfAppointment(appointment);
                if (appointmentRequest == null) {
                    FacesUtil.addErrorMessage("appointmentsForm:msg", "Error, la solicitud o cita no existe.");
                    return appointmentsWithParam();
                }
            }
            // Make the request status as CANCELLED
            appointmentRequestFacade.updateRequestStatus(appointmentRequest, RequestStatus.CANCELLED, "Cancelado por " + loggedUser.getFirstName());
            appointmentFacade.makeAppointmentAbailable(appointment);
            appointmentFacade.deleteAppointmentUser(appointment);
            FacesUtil.addSuccessMessage("appointmentsForm:msg", "La solicitud o cita ha sido cancelada correctamente.");

            try {
                // Audit appointment cancalation
                String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                auditFacade.createAudit(AuditType.CANCELAR_CITA, appointment.getAppointmentUser(), ipAddress, appointment.getId(), organisation);
            } catch (Exception e) {
                Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
                FacesUtil.addErrorMessage("appointmentsForm:msg", "La solicitud o cita ha sido cancelada correctamente, pero ha habido un problema auditando la cancelación. Por favor informa a algún administrador del problema.");
            }
        } catch (Exception e) {
            Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("appointmentsForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
        }

        return appointmentsWithParam();
    }

    public String acceptAppointmentBooking(Appointment appointment) {
        try {
            // Check if there is a appointment request
            AppointmentRequest appointmentRequest = appointmentRequestFacade.findCurrentRequestOfAppointment(appointment);
            if (appointmentRequest == null) {
                FacesUtil.addErrorMessage("appointmentsForm:msg", "Error, la solicitud o cita no existe.");
                return appointmentsWithParam();
            }
            // Make the request status as ACCEPTED
            appointmentRequestFacade.updateRequestStatus(appointmentRequest, RequestStatus.ACCEPTED, "Aceptado por " + loggedUser.getFirstName());
            FacesUtil.addSuccessMessage("appointmentsForm:msg", "La solicitud de cita ha sido aceptada correctamente.");

            try {
                // Audit appointment acceptance
                String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                auditFacade.createAudit(AuditType.ACEPTAR_CITA, appointment.getAppointmentUser(), ipAddress, appointment.getId(), organisation);
            } catch (Exception e) {
                Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
                FacesUtil.addErrorMessage("appointmentsForm:msg", "La solicitud de cita ha sido aceptada correctamente, pero ha habido un problema auditando la cancelación. Por favor informa a algún administrador del problema.");
            }
        } catch (Exception e) {
            Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("appointmentsForm:msg", "Lo sentimos, ha habido un problema al aceptar la reserva.");
        }

        return appointmentsWithParam();
    }

    public String checkStatus(Appointment appointment) {
        // TODO: Check the stage of the status of the appoinment request
        return appointment.getStatus().getLabel();
    }

    public String duplicateAppointment(Appointment appointment) {
        try {
            Appointment duplicatedAppointment = appointmentFacade.duplicateAppointment(appointment);
            FacesUtil.addSuccessMessage("appointmentsForm:msg", "La clase ha sido duplicada correctamente.");
            // Audit appointment duplicate
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.DUPLICAR_CITA, loggedUser, ipAddress, duplicatedAppointment.getId(), organisation);
        } catch (Exception e) {
            FacesUtil.addErrorMessage("appointmentsForm:msg", "Lo sentimos, no ha sido posible duplicar la clase.");
            Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
        }

        return appointmentsWithParam();
    }

    private String appointmentsWithParam() {
        String serviceParam = (serviceId != null) ? ("service=" + serviceId) : "";
        return "appointments.xhtml" + Constants.FACES_REDIRECT + serviceParam;
    }

    public boolean isMyAppointmentBooking(Appointment appointment) {
        AppointmentRequest acceptedAppointmentRequest = appointmentRequestFacade.findAcceptedRequestOfAppointment(appointment);
        return acceptedAppointmentRequest != null && acceptedAppointmentRequest.getRequestUser().equals(loggedUser);
    }

    /**
     * Check if the user have an appointment request or, if the user is an
     * ADMIN, check if there is any.
     *
     * @param appointment the Appointment to check if there is any request
     * @return boolean
     */
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

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public Role getUserRole() {
        return loggedUser.getUserRole().getRole();
    }

    public boolean loggedUserIsAdmin() {
        Role userRole = loggedUser.getUserRole().getRole();
        return userRole == Role.ADMIN || userRole == Role.SUPER_ADMIN;
    }

    public Service getCurrentService() {
        return currentService;
    }

    public List<Appointment> getPastAppointments() {
        return pastAppointments;
    }
}
