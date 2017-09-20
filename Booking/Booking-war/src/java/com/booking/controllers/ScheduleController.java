/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.booking.controllers;

import com.booking.entities.ActivityClass;
import com.booking.entities.Appointment;
import com.booking.entities.AppointmentRequest;
import com.booking.entities.Booking;
import com.booking.entities.ClassDay;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.RequestStatus;
import com.booking.enums.Role;
import com.booking.facades.AppointmentFacade;
import com.booking.facades.AppointmentRequestFacade;
import com.booking.facades.AuditFacade;
import com.booking.facades.BookingFacade;
import com.booking.facades.ClassDayFacade;
import com.booking.facades.ClassFacade;
import com.booking.util.FacesUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

/**
 *
 * @author Jesús Soriano
 */
@ManagedBean
@ViewScoped
public class ScheduleController implements Serializable {

    @EJB
    private BookingFacade bookingFacade;
    @EJB
    private ClassDayFacade classDayFacade;
    @EJB
    private ClassFacade classFacade;
    @EJB
    private AuditFacade auditFacade;
    @EJB
    private AppointmentRequestFacade appointmentRequestFacade;
    @EJB
    private AppointmentFacade appointmentFacade;

    private Organisation organisation;
    private User loggedUser;

    // Variables for the schedule
    private ScheduleModel eventModel;
    private ScheduleModel lazyEventModel;
    private ScheduleEvent event = new DefaultScheduleEvent();

    public static class ScheduleElement {

        private ClassDay classDay;
        private Appointment appointment;
        private final boolean dataAsAppointment;

        public ScheduleElement(ClassDay classDay) {
            this.classDay = classDay;
            this.dataAsAppointment = false;
        }

        public ScheduleElement(Appointment appointment) {
            this.appointment = appointment;
            this.dataAsAppointment = true;
        }

        public ClassDay getClassDay() {
            return classDay;
        }

        public Appointment getAppointment() {
            return appointment;
        }

        public boolean isDataAsAppointment() {
            return dataAsAppointment;
        }
    }

    @PostConstruct
    public void init() {
        organisation = FacesUtil.getCurrentOrganisation();
        loggedUser = FacesUtil.getCurrentUser();

        // Load schedule
        eventModel = new DefaultScheduleModel();
        List<ClassDay> classDays = classDayFacade.findAllActiveDaysOfOrganisation(organisation);
        for (ClassDay day : classDays) {
            ScheduleElement scheduleElement = new ScheduleElement(day);
            DefaultScheduleEvent eventDay = new DefaultScheduleEvent(day.getActivityClass().getService().getName() + ": " + day.getActivityClass().getName(), day.getStartDate(), day.getEndDate(), scheduleElement);
            String eventClass = "";
            if (day.getEndDate().before(new Date())) {
                eventClass = "pastEvent";
            } else if (existsBooking(day.getActivityClass())) {
                eventClass = "bookedEvent";
            } else if (day.getActivityClass().getBookedPlaces() == day.getActivityClass().getMaximumUsers()) {
                eventClass = "allBookedEvent";
            }
            eventDay.setStyleClass(eventClass + " classEvent");
            eventModel.addEvent(eventDay);
        }

        List<Appointment> appointments = appointmentFacade.findAllActiveAppointmentsOfOrganisation(organisation);
        for (Appointment appointment : appointments) {
            ScheduleElement scheduleElement = new ScheduleElement(appointment);
            DefaultScheduleEvent eventAppointment = new DefaultScheduleEvent(appointment.getDescription().isEmpty() ? ("Cita " + appointment.getService().getName()) : (appointment.getService().getName() + ": " + appointment.getDescription()), getStartingDateOfAppointment(appointment), getEndingDateOfAppointment(appointment), scheduleElement);
            String eventClass = "";
            if (getEndingDateOfAppointment(appointment).before(new Date())) {
                eventClass = "pastAppointment";
            } else if (isMyAppointmentBooking(appointment)) {
                eventClass = "bookedAppointment";
            } else if (!appointment.isAvailable()) {
                eventClass = "allBookedAppointment";
            } else {
                eventClass = "availableAppointment";
            }
            eventAppointment.setStyleClass(eventClass + " appointmentEvent");
            eventModel.addEvent(eventAppointment);
        }
    }

    private Date getStartingDateOfAppointment(Appointment appointment) {
        return new Date(appointment.getDate().getTime() + appointment.getStartTime().getTime());
    }

    private Date getEndingDateOfAppointment(Appointment appointment) {
        return new Date(appointment.getDate().getTime() + appointment.getEndTime().getTime());
    }

    public void bookClass(ActivityClass activityClass) {
        try {
            // Check if the booking already exists
            if (bookingFacade.existsBooking(loggedUser, activityClass)) {
                FacesUtil.addErrorMessage("scheduleForm:msg", "Esta clase ya ha sido resarvada previamente.");
                // Check if there are free places
            } else if (activityClass.getBookedPlaces() == activityClass.getMaximumUsers()) {
                FacesUtil.addErrorMessage("scheduleForm:msg", "No quedan plazas para esta clase. Puedes revisar periódicamente si queda alguna libre.");
            } else {
                // Create the booking
                Booking booking = bookingFacade.createNewBooking(loggedUser, activityClass);
                if (booking != null) {
                    // Add a booked place in the class
                    classFacade.addClassBooking(activityClass);

                    FacesUtil.addSuccessMessage("scheduleForm:msg", "La plaza ha sido reservada correctamente.");

                    try {
                        // Audit class booking
                        String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                        auditFacade.createAudit(AuditType.RESERVAR_CLASE, loggedUser, ipAddress, activityClass.getId(), organisation);
                    } catch (Exception e) {
                        Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("scheduleForm:msg", "Lo sentimos, ha habido un problema al reservar la plaza.");
        }

        try {
            FacesUtil.redirectTo("calendar.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cancelClassBooking(ActivityClass activityClass) {
        try {
            // Remove booking
            if (bookingFacade.removeBooking(loggedUser, activityClass)) {
                // Remove a booked place in the class
                classFacade.removeClassBooking(activityClass);
                FacesUtil.addSuccessMessage("scheduleForm:msg", "La reserva ha sido cancelada correctamente.");
            } else {
                FacesUtil.addErrorMessage("scheduleForm:msg", "Error, la reserva no existe.");
            }
        } catch (Exception e) {
            Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("scheduleForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
        }

        try {
            // Audit booking cancelation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CANCELAR_RESERVA, loggedUser, ipAddress, activityClass.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, e);
        }

        try {
            FacesUtil.redirectTo("calendar.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, ex);
        }
//        return "calendar.xhtml" + Constants.FACES_REDIRECT;
    }

    public void bookAppointment(Appointment appointment) {
        try {
            // Check if the appointment is available
            if (!appointment.isAvailable()) {
                FacesUtil.addErrorMessage("scheduleForm:msg", "Lo sentimos, esta cita no está disponible para solicitud. Por favor, informa a algún administrador del problema.");
            } else {
                // Create the appointment request
                AppointmentRequest newAppointmentRequest = appointmentRequestFacade.createNewAppointmentRequest(appointment, loggedUser, "");
                // Set the appointment user, even if the request is pending.
                appointmentFacade.setAppointmentUser(appointment, loggedUser);
                appointmentFacade.makeAppointmentUnavailable(appointment);
                String msg = "La cita ha sido solicitada correctamente";
                if (loggedUserIsAdmin()) {
                    appointmentRequestFacade.updateRequestStatus(newAppointmentRequest, RequestStatus.ACCEPTED, "");
                    msg = "La cita ha sido reservada correctamente";
                }

                FacesUtil.addSuccessMessage("scheduleForm:msg", msg);

                try {
                    // Audit new appointment request
                    String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                    auditFacade.createAudit(AuditType.RESERVAR_CITA, loggedUser, ipAddress, newAppointmentRequest.getId(), organisation);

                } catch (Exception e) {
                    Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, e);
                    FacesUtil.addErrorMessage("scheduleForm:msg", msg + ", pero ha habido un problema auditando la solicitud. Por favor informa a algún administrador del problema.");
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("scheduleForm:msg", "Lo sentimos, ha habido un problema al solicitar la cita.");
        }

        try {
            FacesUtil.redirectTo("calendar.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cancelAppointmentBooking(Appointment appointment) {
        try {
            // Check if there is a appointment request
            AppointmentRequest appointmentRequest = appointmentRequestFacade.findCurrentRequestOfAppointment(appointment);
            if (appointmentRequest == null) {
                // Find the appointment request
                appointmentRequest = appointmentRequestFacade.findAcceptedRequestOfAppointment(appointment);
                if (appointmentRequest == null) {
                    FacesUtil.addErrorMessage("scheduleForm:msg", "Error, la solicitud o cita no existe.");

                    try {
                        FacesUtil.redirectTo("calendar.xhtml");
                    } catch (IOException ex) {
                        Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            // Make the request status as CANCELLED
            appointmentRequestFacade.updateRequestStatus(appointmentRequest, RequestStatus.CANCELLED, "Cancelado por " + loggedUser.getFirstName());
            appointmentFacade.makeAppointmentAvailable(appointment);
            appointmentFacade.deleteAppointmentUser(appointment);
            FacesUtil.addSuccessMessage("scheduleForm:msg", "La solicitud o cita ha sido cancelada correctamente.");

            try {
                // Audit appointment cancalation
                String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                auditFacade.createAudit(AuditType.CANCELAR_CITA, appointment.getAppointmentUser(), ipAddress, appointment.getId(), organisation);
            } catch (Exception e) {
                Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, e);
                FacesUtil.addErrorMessage("scheduleForm:msg", "La solicitud o cita ha sido cancelada correctamente, pero ha habido un problema auditando la cancelación. Por favor informa a algún administrador del problema.");
            }
        } catch (Exception e) {
            Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("scheduleForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
        }

        try {
            FacesUtil.redirectTo("calendar.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(ScheduleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isMyAppointmentBooking(Appointment appointment) {
        AppointmentRequest acceptedAppointmentRequest = appointmentRequestFacade.findAcceptedRequestOfAppointment(appointment);
        return acceptedAppointmentRequest != null && acceptedAppointmentRequest.getRequestUser().equals(loggedUser);
    }

    public boolean isUserAppointmentRequest(Appointment appointment) {
        return appointmentRequestFacade.findCurrentRequestOfAppointmentForUser(appointment, loggedUser) != null;
    }

    public boolean loggedUserIsAdmin() {
        Role userRole = loggedUser.getUserRole().getRole();
        return userRole == Role.ADMIN || userRole == Role.SUPER_ADMIN;
    }

    public boolean existsBooking(ActivityClass activityClass) {
        return bookingFacade.existsBooking(loggedUser, activityClass);
    }

    public ScheduleModel getEventModel() {
        return eventModel;
    }

    public ScheduleModel getLazyEventModel() {
        return lazyEventModel;
    }

    public ScheduleEvent getEvent() {
        return event;
    }

    public void onEventSelect(SelectEvent selectEvent) {
        event = (ScheduleEvent) selectEvent.getObject();
    }

    public void onDateSelect(SelectEvent selectEvent) {
        event = new DefaultScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject());
    }

    public void onEventMove(ScheduleEntryMoveEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event moved", "Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());

        addMessage(message);
    }

    public void onEventResize(ScheduleEntryResizeEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event resized", "Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());

        addMessage(message);
    }

    private void addMessage(FacesMessage message) {
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
}
