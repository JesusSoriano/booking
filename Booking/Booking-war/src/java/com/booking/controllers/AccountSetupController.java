package com.booking.controllers;

import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
import com.booking.facades.UserFacade;
import com.booking.util.Constants;
import com.booking.util.FacesUtil;
import com.booking.security.PBKDF2HashGenerator;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Jesus Soriano
 */
@ManagedBean
@ViewScoped
public class AccountSetupController implements Serializable {

//    @EJB
//    private MailService mailService;
    @EJB
    private UserFacade userFacade;
    @EJB
    private AuditFacade auditFacade;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private String confirmPassword;

    /**
     * Creates a new instance of AccountSetupController
     */
    public AccountSetupController() {
    }

    @PostConstruct
    public void init() {
    }

    public String userRegistration() {

        try {
            if (!password.equals(confirmPassword)) {
                FacesUtil.addErrorMessage("registrationForm:confirmPassword", "Las contraseñas no coinciden.");
                return "";
            }

            Organisation organisation = FacesUtil.getCurrentOrganisation();
            
            // Password encryption
            password = PBKDF2HashGenerator.createHash(password);
            User newUser = userFacade.createNewUser(firstName, lastName, email, phone, password, Role.USER, organisation);

            try {
                HttpServletRequest request = FacesUtil.getRequest();
                request.logout();
                request.login(newUser.getEmail(), password);
            } catch (Exception ex) {
                Logger.getLogger(AccountSetupController.class.getName()).log(Level.SEVERE, null, ex);
                FacesUtil.addErrorMessage("registrationForm", "El usuario ha sido registrado, pero el inicio de sesión ha fallado. Inténtelo de nuevo desde la página de login.");
                return "";
            }

            //TODO: Send registration completed email?
//            try {
//                mailService.sendUserRegistrationMail(newUser);
//            } catch (Exception ex) {
//                Logger.getLogger(AccountSetupController.class.getName()).log(Level.SEVERE, null, ex);
//            }
            try {
                String ipAddress = FacesUtil.getCurrentIPAddress();
                auditFacade.createAudit(AuditType.SIGN_UP, newUser, ipAddress, organisation);
            } catch (Exception e) {
                Logger.getLogger(AccountSetupController.class.getName()).log(Level.SEVERE, null, e);
            }

            FacesUtil.setSessionAttribute(Constants.CURRENT_USER, newUser);
            return "terms-and-conditions.xhtml";
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(AccountSetupController.class.getName()).log(Level.SEVERE, null, ex);
            FacesUtil.addErrorMessage("registrationForm", "Lo sentimos, ha habido un error. Inténtelo de nuevo más tarde.");
            return "";
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
