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
import javax.faces.bean.SessionScoped;
import javax.servlet.http.HttpServletRequest;

@ManagedBean
@SessionScoped
public class TermsAndConditionsController implements Serializable {

    @EJB
    private UserFacade userFacade;

    private boolean tncAccepted;
    private User currentUser;
    private boolean tNcCheckVisible;

    /**
     * Creates a new instance of TermsAndConditionsController
     */
    public TermsAndConditionsController() {
    }

    @PostConstruct
    public void init() {
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

    /**
     * Sets the terms & conditions as accepted by the user and goes to the
     * user's home page
     */
    public void TNCDone() {
        String outcome = "";
        if (tNcCheckVisible) {
            if (tncAccepted) {
                if (currentUser != null) {

                    // login
                    HttpServletRequest request = FacesUtil.getRequest();
                    try {
                        request.logout();
                    } catch (Exception ex) {
                        Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    try {
                        request.getSession(true);
                        request.login(currentUser.getEmail(), currentUser.getPassword());
                        FacesUtil.setSessionAttribute(Constants.CURRENT_USER, currentUser);
                    } catch (Exception ex) {
                        Logger.getLogger(TermsAndConditionsController.class.getName()).log(Level.SEVERE, null, ex);
                        FacesUtil.addErrorMessage("TNCForm", "Lo sentimos, no ha sido posible iniciar sesión en este momento. Contáctanos si el problema persiste.");
                        return;
                    }

                    outcome = homePage();
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
            System.out.println("----- redirect?: " + outcome);
            FacesUtil.redirectTo(outcome);
            System.out.println("----- redirected");
        } catch (IOException ex) {
            Logger.getLogger(TermsAndConditionsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String homePage() {
        Role userRole = currentUser.getUserRole().getRole();
        switch (userRole) {
            case USER: {
                return "/client/home.xhtml";
            }
            case ADMIN: {
                return "/admin/home.xhtml";
            }
            case SUPER_ADMIN: {
                return "/super-admin/home.xhtml";
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
