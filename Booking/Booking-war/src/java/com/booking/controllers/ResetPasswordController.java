package com.booking.controllers;

import com.booking.entities.PasswordChangeRequest;
import com.booking.entities.User;
import com.booking.facades.PasswordChangeRequestFacade;
import com.booking.facades.UserFacade;
import com.booking.util.FacesUtil;
import com.booking.security.PBKDF2HashGenerator;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class ResetPasswordController implements Serializable {

    @EJB
    private UserFacade userFacade;
    @EJB
    private PasswordChangeRequestFacade passwordChangeRequestFacade;

    private String password;
    private String confirmPassword;
    private User currentUser;
    private PasswordChangeRequest passwordChangeRequest;

    /**
     * Creates a new instance of ResetPasswordController
     */
    public ResetPasswordController() {
    }

    @PostConstruct
    public void init() {
        String requestId = FacesUtil.getParameter("token");
        boolean valid = false;

        passwordChangeRequest = passwordChangeRequestFacade.findRequestFromHashId(requestId);
        if (passwordChangeRequest != null && passwordChangeRequest.getUser() != null && !passwordChangeRequest.isExpired()) {
            // check if the password change request was done more than 60 minutes ago
            long requestDateTime = passwordChangeRequest.getCreatedDate().getTime();
            Date expirationTime = new Date(requestDateTime + (60 * 60000)); // 60 minutes expiration time (60000 milliseconds in a minute)
            if (new Date().after(expirationTime)) {
                passwordChangeRequestFacade.setExpiredRequest(passwordChangeRequest);
            } else {
                valid = true;
                currentUser = passwordChangeRequest.getUser();
            }
        }

        if (!valid) {
            try {
                FacesUtil.redirectTo("info.xhtml", "&info=invalid-link");
            } catch (IOException ioE) {
                Logger.getLogger(ResetPasswordController.class.getName()).log(Level.SEVERE, null, ioE);
            }
        }
    }

    public void resetPassword() {
        try {
            if (passwordChangeRequest.isExpired()) {
                FacesUtil.addErrorMessage("resetPassword", "The reset passsword request has expired.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                FacesUtil.addErrorMessage("resetPassword", "Passwords don't match. Please, enter them again.");
                return;
            }

            // Password encryption
            password = PBKDF2HashGenerator.createHash(password);
            currentUser.setPassword(password);
            userFacade.edit(currentUser);

            passwordChangeRequestFacade.setExpiredRequest(passwordChangeRequest);

            try {
                FacesUtil.redirectTo("info.xhtml", "&info=valid-reset");
            } catch (IOException ioE) {
                Logger.getLogger(ResetPasswordController.class.getName()).log(Level.SEVERE, null, ioE);
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(ResetPasswordController.class.getName()).log(Level.SEVERE, null, ex);
            FacesUtil.addErrorMessage("registrationForm", "Please, try again later");
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

}
