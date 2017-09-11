package com.booking.controllers;

import com.booking.entities.User;
import com.booking.util.FacesUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

@ManagedBean
public class HomeController implements Serializable {

    private User currentUser;

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
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getRole() {
        return currentUser.getUserRole().getRole().getName();
    }
    
    public String getUserFullName() {
        if (currentUser != null) {
            return currentUser.getFullName();
        } else {
            return "";
        }
    }

}
