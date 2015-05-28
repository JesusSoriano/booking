package com.booking.controllers;

import com.booking.entities.User;
import com.booking.enums.Role;
import com.booking.facades.UserFacade;
import com.booking.util.Constants;
import com.booking.util.FacesUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;

@ManagedBean
@ViewScoped
public class TermsAndConditionsController implements Serializable {

    @EJB
    private UserFacade userFacade;

    private boolean tncAccepted;
    private User currentUser;
    private boolean tNcCheckVisible;

    /**
     * Creates a new instance of LoginController
     */
    public TermsAndConditionsController() {
    }

    @PostConstruct
    public void init() {
        /* 
         tncAccepted is used to enable/disable the terms and conditions button,
         so if the page is manually typed in the browser it'll enabled and redirect to login.xhtml
         */
        currentUser = FacesUtil.getCurrentUser();
        tNcCheckVisible = currentUser != null;
        tncAccepted = !tNcCheckVisible;

        HttpServletRequest request = FacesUtil.getRequest();
        try {
            request.logout();
            request.getSession().invalidate();
        } catch (Exception ex) {
            Logger.getLogger(TermsAndConditionsController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void TNCDone() {
        String outcome = "";
        if (isTNcCheckVisible()) {
            if (tncAccepted) {
                if (currentUser != null) {
                    currentUser = userFacade.findUserByEmail(currentUser.getEmail());
                    currentUser.setTermsVersionAccepted(true);
                    userFacade.edit(currentUser);

                    // login
                    HttpServletRequest request = FacesUtil.getRequest();
                    try {
                        request.login(currentUser.getEmail(), currentUser.getPassword());
                    } catch (Exception ex) {
                        Logger.getLogger(TermsAndConditionsController.class.getName()).log(Level.SEVERE, null, ex);
                        FacesUtil.addErrorMessage("TNCForm", "Lo sentimos, no ha sido posible iniciar sesión en este momento. Contáctanos si el problema persiste.");
                        return;
                    }

                    outcome = homePage();
                    FacesUtil.setSessionAttribute(Constants.CURRENT_USER, currentUser);
                } else {
                    FacesUtil.addErrorMessage("TNCForm", "Lo sentimos, no ha sido posible iniciar sesión en este momento. Contáctanos si el problema persiste.");
                }
            } else {
                FacesUtil.addErrorMessage("TNCForm", "Haz click en la casilla para aceptar los términos y condiciones.");
            }
        } else {
            outcome = "/login.xhtml";
        }

        try {
            FacesUtil.redirectTo(outcome);
        } catch (IOException ex) {
            Logger.getLogger(TermsAndConditionsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String homePage() {
        Role userRole = currentUser.getUserRole().getRole();
        switch (userRole) {
            case ADMIN: {
                return "/admin/home.xhtml";
            }
            case USER: {
                return "/client/home.xhtml";
            }
            default: {
                return "";
            }
        }
    }

    public boolean isTncAccepted() {
        return tncAccepted;
    }

    public void setTncAccepted(boolean tncAccepted) {
        this.tncAccepted = tncAccepted;
    }

    public boolean isTNcCheckVisible() {
        return tNcCheckVisible;
    }
}
