package com.booking.controllers;

import com.booking.entities.Appointment;
import com.booking.entities.AppointmentRequest;
import com.booking.entities.Organisation;
import com.booking.entities.Service;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AppointmentFacade;
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
//        try {
//            // Check if the appointment is already booked
//            if (appointment.getAppointmentUser() != null) {
//                FacesUtil.addErrorMessage("appointmentsForm:msg", "Esta cita ya ha sido resarvada previamente.");
//            } else {
//                // Create the request
//                AppointmentRequest appointmentRequest = appointmentFacade.createNewAppointmentRequest(loggedUser, appointment);
//                if (appointmentRequest != null) {
//                    // Add a booked place in the appointment
//                    appointmentFacade.addAppointmentUser(appointment, loggedUser);
//
//                    FacesUtil.addSuccessMessage("appointmentsForm:msg", "La solicitud de cita ha sido enviada correctamente.");
//
//                    try {
//                        // Audit appointment booking
//                        String ipAddress = FacesUtil.getRequest().getRemoteAddr();
//                        auditFacade.createAudit(AuditType.RESERVAR_CLASE, loggedUser, ipAddress, appointment.getId(), organisation);
//                    } catch (Exception e) {
//                        Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
//            FacesUtil.addErrorMessage("appointmentsForm:msg", "Lo sentimos, ha habido un problema al solicitar la cita.");
//        }

        return appointmentsWithParam();
    }

    public String cancelAppointmentBooking(Appointment appointment) {
//        try {
//            // Remove booking
//            if (appointmentFacade.cancelAppointmentBooking(loggedUser, appointment)) {
//                // Remove a booked place in the appointment
//                appointmentFacade.cancelAppointmentBooking(appointment);
//                FacesUtil.addSuccessMessage("appointmentsForm:msg", "La reserva ha sido cancelada correctamente.");
//            } else {
//                FacesUtil.addErrorMessage("appointmentsForm:msg", "Error, la reserva no existe.");
//            }
//        } catch (Exception e) {
//            Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
//            FacesUtil.addErrorMessage("appointmentsForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
//        }
//
//        try {
//            // Audit booking cancelation
//            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
//            auditFacade.createAudit(AuditType.CANCELAR_RESERVA, loggedUser, ipAddress, appointment.getId(), organisation);
//        } catch (Exception e) {
//            Logger.getLogger(AppointmentsController.class.getName()).log(Level.SEVERE, null, e);
//        }

        return appointmentsWithParam();
    }

    public String duplicateAppointment(Appointment appointment) {
        try {
            Appointment duplicatedAppointment = appointmentFacade.duplicateAppointment(appointment);
            FacesUtil.addSuccessMessage("appointmentsForm:msg", "La clase ha sido duplicada correctamente.");
            // Audit appointment duplicate
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.DUPLICAR_CLASE, loggedUser, ipAddress, duplicatedAppointment.getId(), organisation);
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

    public boolean bookedAppointmentForUser(Appointment appointment) {
        return appointment.getAppointmentUser() != null && appointment.getAppointmentUser() == loggedUser;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public Role getUserRole() {
        return loggedUser.getUserRole().getRole();
    }

    public Service getCurrentService() {
        return currentService;
    }

    public List<Appointment> getPastAppointments() {
        return pastAppointments;
    }
}
