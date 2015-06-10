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
    private User logedUser;
    private Organisation organisation;

    public UsersController() {
    }
    
    @PostConstruct
    public void init() {
        logedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        users = userFacade.findAllClientsOfOrganisation(organisation);
        admins = userFacade.findAllAdminsOfOrganisation(organisation);
    }

    public String viewPayCenterAdminDetails(User user) {
        FacesUtil.setSessionAttribute("agent", user);
        return "user-profile.xhtml" + Constants.FACES_REDIRECT;
    }

    public String activateUser(User user) {
        userFacade.activateUser(user);
        FacesUtil.addSuccessMessage("usersForm:msg", "El usuario ha sido activado correctamente.");

        try {
            // Registrar activación de usuario
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVATE_USER, logedUser, ipAddress, organisation);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "users.xhtml" + Constants.FACES_REDIRECT;
    }

    public String deactivateUser(User user) {
        userFacade.deactivateUser(user);
        FacesUtil.addSuccessMessage("usersForm:msg", "El usuario ha sido suspendido correctamente.");

        try {
            // Registrar suspensión de usuario
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.SUSPEND_USER, logedUser, ipAddress, organisation);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "users.xhtml" + Constants.FACES_REDIRECT;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<User> getAdmins() {
        return admins;
    }
    
    public Role getUserRole() {
        return logedUser.getUserRole().getRole();
    }
}
