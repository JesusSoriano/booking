package com.booking.controllers.superAdmin;

import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
import com.booking.facades.OrganisationFacade;
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
public class OrganisationsController implements Serializable {

    @EJB
    private OrganisationFacade organisationFacade;
    @EJB
    private AuditFacade auditFacade;

    private List<Organisation> organisations;
    private User logedUser;

    @PostConstruct
    public void init() {
        logedUser = FacesUtil.getCurrentUser();

        organisations = organisationFacade.findAll();
    }

    public String activateOrganisation(Organisation organisation) {
        organisationFacade.activateOrganisation(organisation);
        FacesUtil.addSuccessMessage("usersForm:msg", "La organización ha sido activada correctamente.");

        try {
            // Registrar activación de la organización
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVATE_ORGANISATION, logedUser, ipAddress, organisation);
        } catch (Exception e) {
            Logger.getLogger(OrganisationsController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "organisations.xhtml" + Constants.FACES_REDIRECT;
    }

    public String deactivateOrganisation(Organisation organisation) {
        organisationFacade.deactivateOrganisation(organisation);
        FacesUtil.addSuccessMessage("usersForm:msg", "La organización ha sido suspendida correctamente.");

        try {
            // Registrar suspensión de la organización
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.SUSPEND_ORGANISATION, logedUser, ipAddress, organisation);
        } catch (Exception e) {
            Logger.getLogger(OrganisationsController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "organisations.xhtml" + Constants.FACES_REDIRECT;
    }

    public List<Organisation> getOrganisations() {
        return organisations;
    }
    
    public Role getUserRole() {
        return logedUser.getUserRole().getRole();
    }
}
