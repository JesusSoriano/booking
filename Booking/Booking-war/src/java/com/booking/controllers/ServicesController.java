package com.booking.controllers;

import com.booking.entities.Service;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
import com.booking.facades.ServiceFacade;
import com.booking.util.Constants;
import com.booking.util.FacesUtil;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import org.primefaces.context.RequestContext;

@ManagedBean
public class ServicesController implements Serializable {

    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private AuditFacade auditFacade;

    private List<Service> services;
    private User loggedUser;
    private Organisation organisation;
    private String newServiceName;
    private String newServiceDescription;

    public ServicesController() {
    }

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        services = serviceFacade.findAllServicesOfOrganisation(organisation);
    }

    public String activateService(Service service) {
        serviceFacade.activateService(service);
        FacesUtil.addSuccessMessage("servicesForm:msg", "El servicio ha sido activada correctamente.");

        try {
            // Audit service activation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVATE_SERVICE, loggedUser, ipAddress, service.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(ServicesController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "services.xhtml" + Constants.FACES_REDIRECT;
    }

    public String deactivateService(Service service) {
        serviceFacade.deactivateService(service);
        FacesUtil.addSuccessMessage("servicesForm:msg", "El servicio ha sido suspendido correctamente.");

        try {
            // Audit service suspention
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.SUSPEND_SERVICE, loggedUser, ipAddress, service.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(ServicesController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "services.xhtml" + Constants.FACES_REDIRECT;
    }

    public String createNewService() {
        RequestContext context = RequestContext.getCurrentInstance();
        try {
            Service newService = serviceFacade.createNewService(newServiceName, newServiceDescription, organisation);
            context.execute("PF('newServiceDialog').hide();");
            FacesUtil.addSuccessMessage("servicesForm:msg", "El nuevo servicio ha sido creado correctamente.");
            // Audit service creation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CREATE_SERVICE, loggedUser, ipAddress, newService.getId(), organisation);
        } catch (Exception e) {
            FacesUtil.addErrorMessage("servicesForm:msg", "Lo sentimos, no ha sido posible crear el nuevo servicio.");
            Logger.getLogger(ServicesController.class.getName()).log(Level.SEVERE, null, e);
            return "";
        }

        newServiceName = "";
        newServiceDescription = "";
        return "services.xhtml" + Constants.FACES_REDIRECT;
    }

    public void prepareService(Service service) {
        newServiceName = service.getName();
        newServiceDescription = service.getDescription();
    }

    public void prepareNewService() {
    }

    public List<Service> getServices() {
        return services;
    }

    public Role getUserRole() {
        return loggedUser.getUserRole().getRole();
    }

    public String getNewServiceName() {
        return newServiceName;
    }

    public void setNewServiceName(String newServiceName) {
        this.newServiceName = newServiceName;
    }

    public String getNewServiceDescription() {
        return newServiceDescription;
    }

    public void setNewServiceDescription(String newServiceDescription) {
        this.newServiceDescription = newServiceDescription;
    }
}