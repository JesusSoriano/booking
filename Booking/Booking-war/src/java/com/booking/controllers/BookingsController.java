package com.booking.controllers;

import com.booking.entities.ActivityClass;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
import com.booking.facades.BookingFacade;
import com.booking.facades.ClassFacade;
import com.booking.facades.UserFacade;
import com.booking.util.Constants;
import com.booking.util.FacesUtil;
import java.io.Serializable;
import java.util.Calendar;
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
import javax.faces.event.ActionEvent;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

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
    private AuditFacade auditFacade;

    private User bookingsUser;
    private String userId;
    private List<ActivityClass> classes;

    // Variables for the Calendar
    private ScheduleModel eventModel;
    private ScheduleModel lazyEventModel; 
    private ScheduleEvent event = new DefaultScheduleEvent();
    /**
     * Creates a new instance of UserProfileController
     */
    public BookingsController() {
    }

    @PostConstruct
    public void init() {

        Organisation organisation = FacesUtil.getCurrentOrganisation();

        User loggedUser = FacesUtil.getCurrentUser();
        userId = FacesUtil.getParameter("user");
        if (userId != null && (loggedUser.getUserRole().getRole() == Role.ADMIN || loggedUser.getUserRole().getRole() == Role.SUPER_ADMIN)) {
            bookingsUser = userFacade.findUserOfOrganisation(Integer.valueOf(userId), organisation);
        } else {
            bookingsUser = loggedUser;
        }

        if (bookingsUser != null) {
            classes = bookingFacade.findAllClassesOfUser(bookingsUser);
        } else {
            classes = bookingFacade.findAllClassesOfUser(loggedUser);
        }
        
        // Load schedule
        eventModel = new DefaultScheduleModel();
        for (ActivityClass g:classes) {
            eventModel.addEvent(new DefaultScheduleEvent(g.getName(), getRandomDate(Calendar.getInstance().getTime()), getRandomDate(Calendar.getInstance().getTime())));
        }
        
    }

    private Date getRandomDate(Date base) {
        Calendar date = Calendar.getInstance();
        date.setTime(base);
        date.add(Calendar.DATE, ((int) (Math.random()*30)) + 1);    //set random day of month
         
        return date.getTime();
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

        String userParam = (userId != null) ? ("user=" + userId) : "";
        return "my-bookings.xhtml" + Constants.FACES_REDIRECT + userParam;
    }

    public List<ActivityClass> getClasses() {
        return classes;
    }
    
    // Events functions
    
    public ScheduleModel getEventModel() {
        return eventModel;
    }
    
    public ScheduleEvent getEvent() {
        return event;
    }
 
    public void setEvent(ScheduleEvent event) {
        this.event = event;
    }
     
    public void addEvent(ActionEvent actionEvent) {
        if(event.getId() == null)
            eventModel.addEvent(event);
        else
            eventModel.updateEvent(event);
         
        event = new DefaultScheduleEvent();
    }
     
    public void deleteEvent(ActionEvent actionEvent) {
        if(event.getId() != null) {
            // TODO: añadir a la BD
            eventModel.deleteEvent(event);
        }
         
        event = new DefaultScheduleEvent();
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
