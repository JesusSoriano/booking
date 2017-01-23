package com.booking.controllers;

import com.booking.entities.Organisation;
import com.booking.entities.PasswordChangeRequest;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.enums.Status;
import com.booking.facades.AuditFacade;
import com.booking.facades.PasswordChangeRequestFacade;
import com.booking.facades.UserFacade;
import com.booking.util.Constants;
import com.booking.util.FacesUtil;
import com.booking.security.PBKDF2HashGenerator;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@ManagedBean
@ViewScoped
public class LoginController implements Serializable {

//    @EJB
//    private MailService mailService;
    @EJB
    private UserFacade userFacade;
    @EJB
    private AuditFacade auditFacade;
    @EJB
    private PasswordChangeRequestFacade passwordChangeRequestFacade;

    private String email;
    private String password;
    private User currentUser;
    private Organisation organisation;

    /**
     * Creates a new instance of LoginController
     */
    public LoginController() {
    }

    @PostConstruct
    public void init() {
        organisation = FacesUtil.getCurrentOrganisation();
    }

    public void userLogin() {
        String outcome = "";
        try {
            // remove start and end white spaces of email
            email = email.trim();

            currentUser = userFacade.findUserOfOrganisationByEmail(email, organisation);

            if (currentUser == null) {
                FacesUtil.addErrorMessage("loginForm", "Inicio de sesión fallido, inténtalo de nuevo.");
                return;
            }

            if (Status.SUSPENDED.equals(currentUser.getOrganisation().getStatus())) {
                FacesUtil.addErrorMessage("loginForm", "Inicio de sesión fallido, organización suspendida.");
                return;
            }

//            if (currentUser.isLoginBlock()) {
//                FacesUtil.addErrorMessage("loginForm", "For security reasons, your account has been blocked. Please, contact your system admin for more info.");
//                return;
//            }
            boolean isValidPassword;
            try {
                isValidPassword = PBKDF2HashGenerator.validatePassword(password, currentUser.getPassword());
            } catch (NumberFormatException | NoSuchAlgorithmException | InvalidKeySpecException nfEx) {
                FacesUtil.addErrorMessage("loginForm", "Inicio de sesión fallido, inténtalo de nuevo.");
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, nfEx);
                return;
            }

            if (!isValidPassword) {
//                addLoginTry();
                FacesUtil.addErrorMessage("loginForm", "Login fallido, inténtalo de nuevo.");
                return;
            }

//            if (currentUser.getLoginTries() > 0) {
//                currentUser.setLoginTries(0);
//                userFacade.edit(currentUser);
//            }
            if (Status.SUSPENDED.equals(currentUser.getStatus())) {
                FacesUtil.addErrorMessage("loginForm", "Tu cuenta no está activa. Consúltanos para más información.");
                return;
            }

            if (!currentUser.isTermsVersionAccepted()) {
                FacesUtil.setSessionAttribute(Constants.CURRENT_USER, currentUser);
                FacesUtil.redirectTo("terms-and-conditions.xhtml");
                return;
            }

            HttpServletRequest request = FacesUtil.getRequest();
            try {
                request.logout();
            } catch (Exception ex) {
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                request.getSession(true);
                request.login(email, currentUser.getPassword());
                FacesUtil.setSessionAttribute(Constants.CURRENT_USER, currentUser);
            } catch (Exception ex) {
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
                FacesUtil.addErrorMessage("loginForm", "Lo sentimos, no ha sido posible iniciar sesión en este momento. Contáctanos si el problema persiste.");
                return;
            }

            try {
                String ipAddress = FacesUtil.getCurrentIPAddress();
                auditFacade.createAudit(AuditType.INICIO_SESION, currentUser, ipAddress, currentUser.getId(), organisation);
            } catch (Exception e) {
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, e);
            }

            outcome = homePage();

        } catch (Exception e) {
            FacesUtil.addErrorMessage("loginForm", "Lo sentimos, no ha sido posible iniciar sesión en este momento. Contáctanos si el problema persiste.");
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, e);
            return;
        }

        try {
            FacesUtil.redirectTo(outcome);
        } catch (IOException ioEx) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ioEx);
        }
    }

//    private void addLoginTry() {
//        currentUser.setLoginTries(currentUser.getLoginTries() + 1);
//        if (currentUser.getLoginTries() > 4) {
//            currentUser.setLoginBlock(true);
//            FacesUtil.addErrorMessage("loginForm", "For security reasons, your account has been blocked. Please, contact your the system admin for more info.");
//            return;
//        }
//        userFacade.edit(currentUser);
//    }
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

    public void performLogout() {
        String redirectPage = "/login.xhtml";

        try {
            String ipAddress = FacesUtil.getCurrentIPAddress();
            User loggedUser = FacesUtil.getCurrentUser();
            auditFacade.createAudit(AuditType.CERRAR_SESION, loggedUser, ipAddress, loggedUser.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, e);
        }

        try {
            HttpServletRequest request = FacesUtil.getRequest();
            request.logout();
            request.getSession(false).invalidate();
        } catch (ServletException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            FacesUtil.redirectTo(redirectPage);
        } catch (IOException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void forgotPassword() throws IOException {
        User user = userFacade.findUserByEmail(email);
        if (user == null) {
            FacesUtil.addErrorMessage("forgotPassword:email", "Usuario no válido o no dado de alta.");
        } else {
            try {
                // create a PasswordChangeRequest
                PasswordChangeRequest passwordChangeRequest = passwordChangeRequestFacade.createNewPasswordChangeRequest(user);
                // set an encryption id to the request
                passwordChangeRequest.setHashId(PBKDF2HashGenerator.createHash(passwordChangeRequest.getId().toString()));
                passwordChangeRequestFacade.edit(passwordChangeRequest);

                // send the email with the encrypted id
                // mailService.sendPasswordResetEmail(user);
                try {
                    FacesUtil.redirectTo("info.xhtml", "&info=password-reset");
                } catch (IOException ex) {
                    Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                FacesUtil.addErrorMessage("forgotPassword:email", "Lo sentimos, no se ha podido enviar la petición en este momento. Inténtalo de nuevo más tarde.");
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getStyleFilePath() {
        return "/resources/style/" + organisation.getStyleFile();
    }

    public String getLogoPath() {
        return "/resources/images/" + organisation.getLogo();
    }

    public String getIconPath() {
        return "/resources/images/" + organisation.getIcon();
    }

    public String getOrganisationName() {
        return organisation.getName();
    }
}
