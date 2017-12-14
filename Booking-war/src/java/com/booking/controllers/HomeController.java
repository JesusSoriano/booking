package com.booking.controllers;

import com.booking.entities.Announcement;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.facades.AnnouncementFacade;
import com.booking.facades.NotificationFacade;
import com.booking.util.FacesUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;

@ManagedBean
public class HomeController implements Serializable {

    @EJB
    private AnnouncementFacade announcementFacade;
    @EJB
    private NotificationFacade notificationFacade;
    
    private User currentUser;
    private List<Announcement> announcements;
    int notificationsNumber;

    /**
     * Creates a new instance of HomeController
     */
    public HomeController() {
    }

    @PostConstruct
    public void init() {

        currentUser = FacesUtil.getCurrentUser();

        try {
            if (currentUser == null) {
                FacesUtil.redirectTo("/login.xhtml");
            }

        } catch (IOException ioEx) {
            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ioEx);
        }
        
        notificationsNumber = notificationFacade.findUserUncheckedNotificationsNumber(currentUser);
        
        Organisation organisation = FacesUtil.getCurrentOrganisation();
        announcements = announcementFacade.findAllActiveAnnouncementsOfOrganisation(organisation);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getRole() {
        return currentUser.getUserRole().getRole().getName();
    }
    
    public String getUserName() {
        if (currentUser != null) {
            return currentUser.getFirstName();
        } else {
            return "";
        }
    }
    
    public String getUserFullName() {
        if (currentUser != null) {
            return currentUser.getFullName();
        } else {
            return "";
        }
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    public int getNotificationsNumber() {
        return notificationsNumber;
    }
}
