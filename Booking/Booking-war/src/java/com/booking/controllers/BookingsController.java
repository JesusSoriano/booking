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
    private AuditFacade auditFacade;

    private User bookingsUser;
    private String userId;
    private List<ActivityClass> classes;
    private List<ActivityClass> pastClasses;

    /**
     * Creates a new instance of BookingsController
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
            classes = bookingFacade.findAllCurrentClassesOfUser(bookingsUser);
            pastClasses = bookingFacade.findAllPastClassesOfUser(bookingsUser);
        } else {
            classes = bookingFacade.findAllCurrentClassesOfUser(loggedUser);
            pastClasses = bookingFacade.findAllPastClassesOfUser(loggedUser);
        }
        
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

    public List<ActivityClass> getPastClasses() {
        return pastClasses;
    }
}
