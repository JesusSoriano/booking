/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.booking.controllers;

import com.booking.entities.ActivityClass;
import com.booking.entities.Booking;
import com.booking.entities.ClassDay;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.facades.AuditFacade;
import com.booking.facades.BookingFacade;
import com.booking.facades.ClassDayFacade;
import com.booking.facades.ClassFacade;
import com.booking.util.FacesUtil;
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

    private Organisation organisation;
    private User loggedUser;
    
    // Variables for the schedule
    private ScheduleModel eventModel;
    private ScheduleModel lazyEventModel;
    private ScheduleEvent event = new DefaultScheduleEvent();

    @PostConstruct
    public void init() {
        organisation = FacesUtil.getCurrentOrganisation();
        loggedUser = FacesUtil.getCurrentUser();

        // Load schedule
        eventModel = new DefaultScheduleModel();
        List<ClassDay> classDays = classDayFacade.findAllActiveDaysOfOrganisation(organisation);
        for (ClassDay day : classDays) {
            DefaultScheduleEvent eventDay = new DefaultScheduleEvent(day.getActivityClass().getName(), day.getStartDate(), day.getEndDate(), day);
            String eventClass = "";
            if (day.getEndDate().before(new Date())) {
                eventClass = "pastEvent";
            } else if (existsBooking(day.getActivityClass())) {
                eventClass = "bookedEvent";
            } else if (day.getActivityClass().getBookedPlaces() == day.getActivityClass().getMaximumUsers()) {
                eventClass = "allBookedEvent";
            }
            eventDay.setStyleClass(eventClass);
            eventModel.addEvent(eventDay);
        }
    }
    
    
    public String bookClass(ActivityClass activityClass) {
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
                        Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("scheduleForm:msg", "Lo sentimos, ha habido un problema al reservar la plaza.");
        }
        
        return "calendar.xhtml";
    }

    public String cancelClassBooking(ActivityClass activityClass) {
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
            Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("scheduleForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
        }

        try {
            // Audit booking cancelation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CANCELAR_RESERVA, loggedUser, ipAddress, activityClass.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return "calendar.xhtml";
    }
    
    public boolean existsBooking (ActivityClass activityClass) {
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
