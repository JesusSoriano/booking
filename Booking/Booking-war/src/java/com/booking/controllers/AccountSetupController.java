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
import com.booking.util.StringsUtil;
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
 * @author Jesús Soriano
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
    private String firstLastName;
    private String secondLastName;
    private String email;
    private String phone;
    private String password;
    private String confirmPassword;
    private String addressLine;
    private String addressLine2;
    private String city;
    private String country;
    private String postcode;

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
            if (StringsUtil.isNotNullNotEmpty(firstName)) {
                FacesUtil.addErrorMessage("registrationForm", "Introduce tu nombre");
            }
            if (StringsUtil.isNotNullNotEmpty(firstLastName)) {
                FacesUtil.addErrorMessage("registrationForm", "Introduce tu primer apellido");
            }
            
            // Remove start and end white spaces of email
            email = email.trim();
            if (StringsUtil.isNotNullNotEmpty(email)) {
                FacesUtil.addErrorMessage("registrationForm", "Introduce tu email");
            }
            
            if (StringsUtil.isNotNullNotEmpty(password)) {
                FacesUtil.addErrorMessage("registrationForm", "Introduce contraseña");
            }
            if (StringsUtil.isNotNullNotEmpty(confirmPassword)) {
                FacesUtil.addErrorMessage("registrationForm", "Confirma tu contraseña");
            }
            if (!password.equals(confirmPassword)) {
                FacesUtil.addErrorMessage("registrationForm:confirmPassword", "Las contraseñas no coinciden.");
                return "";
            }
            
            // Password encryption
            password = PBKDF2HashGenerator.createHash(password);
            
            Organisation organisation = FacesUtil.getCurrentOrganisation();
            User newUser = userFacade.createNewUser(firstName, firstLastName, secondLastName, email, password, phone, 
                    addressLine, addressLine2, city, country, postcode, Role.USER, organisation);

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

    public String getFirstLastName() {
        return firstLastName;
    }

    public void setFirstLastName(String firstLastName) {
        this.firstLastName = firstLastName;
    }

    public String getSecondLastName() {
        return secondLastName;
    }

    public void setSecondLastName(String secondLastName) {
        this.secondLastName = secondLastName;
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

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

}
