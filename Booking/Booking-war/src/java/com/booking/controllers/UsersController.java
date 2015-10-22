package com.booking.controllers;

import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
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

@ManagedBean
public class UsersController implements Serializable {

    @EJB
    private UserFacade userFacade;
    @EJB
    private AuditFacade auditFacade;

    private List<User> users;
    private List<User> admins;
    private User loggedUser;
    private Organisation organisation;

    public UsersController() {
    }
    
    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        users = userFacade.findAllClientsOfOrganisation(organisation);
        admins = userFacade.findAllAdminsOfOrganisation(organisation);
    }

    public String activateUser(User user, String pageName) {
        userFacade.activateUser(user);
        FacesUtil.addSuccessMessage(pageName + "Form:msg", "El usuario ha sido activado correctamente.");

        try {
            // Registrar activación de usuario
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(pageName.equals("admins") ? AuditType.ACTIVATE_ADMIN : AuditType.ACTIVATE_USER, loggedUser, ipAddress, user.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(UsersController.class.getName()).log(Level.SEVERE, null, e);
        }

        return pageName + ".xhtml" + Constants.FACES_REDIRECT;
    }

    public String deactivateUser(User user, String pageName) {
        userFacade.deactivateUser(user);
        FacesUtil.addSuccessMessage(pageName + "Form:msg", "El usuario ha sido suspendido correctamente.");

        try {
            // Registrar suspensión de usuario
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(pageName.equals("admins") ? AuditType.SUSPEND_ADMIN : AuditType.SUSPEND_USER, loggedUser, ipAddress, user.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(UsersController.class.getName()).log(Level.SEVERE, null, e);
        }

        return pageName + ".xhtml" + Constants.FACES_REDIRECT;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<User> getAdmins() {
        return admins;
    }
    
    public Role getUserRole() {
        return loggedUser.getUserRole().getRole();
    }
}
