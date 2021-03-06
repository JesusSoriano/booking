package com.booking.controllers;

import com.booking.entities.Service;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.exceptions.ServiceAlreadyExistsException;
import com.booking.facades.AppointmentFacade;
import com.booking.facades.AuditFacade;
import com.booking.facades.ClassFacade;
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
import javax.faces.bean.ViewScoped;
import org.primefaces.context.RequestContext;

@ManagedBean
@ViewScoped
public class ServicesController implements Serializable {

    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private ClassFacade classFacade;
    @EJB
    private AuditFacade auditFacade;
    @EJB
    private AppointmentFacade appointmentsFacade;

    private List<Service> services;
    private User loggedUser;
    private Role userRole;
    private Organisation organisation;
    private Service selectedService;
    private String newServiceName;
    private String newServiceDescription;
    private boolean isNewService;

    public ServicesController() {
    }

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        userRole = loggedUser.getUserRole().getRole();
        organisation = FacesUtil.getCurrentOrganisation();

        isNewService = true;
        services = serviceFacade.findAllServicesOfOrganisation(organisation);
    }

    public String activateService(Service service) {
        serviceFacade.activateService(service);
        FacesUtil.addSuccessMessage("servicesForm:msg", "El servicio ha sido activado correctamente.");

        try {
            // Audit service activation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVAR_SERVICIO, loggedUser, ipAddress, service.getId(), organisation);
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
            auditFacade.createAudit(AuditType.SUSPENDER_SERVICIO, loggedUser, ipAddress, service.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(ServicesController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "services.xhtml" + Constants.FACES_REDIRECT;
    }

    public String createNewService() {
        if (newServiceName.trim().isEmpty()) {
            FacesUtil.addErrorMessage("servicesForm:msg", "Lo sentimos, no ha sido posible crear el nuevo servicio: Introduzca un nombre.");
            return "";
        }
        RequestContext context = RequestContext.getCurrentInstance();
        try {
            Service newService = serviceFacade.createNewService(newServiceName, newServiceDescription, organisation);
            context.execute("PF('newServiceDialog').hide();");
            FacesUtil.addSuccessMessage("servicesForm:msg", "El nuevo servicio ha sido creado correctamente.");
            // Audit service creation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CREAR_SERVICIO, loggedUser, ipAddress, newService.getId(), organisation);
        } catch (ServiceAlreadyExistsException e) {
            FacesUtil.addErrorMessage("servicesForm:msg", e.getMessage());
            Logger.getLogger(ServicesController.class.getName()).log(Level.SEVERE, null, e);
            return "";
        } catch (Exception e) {
            FacesUtil.addErrorMessage("servicesForm:msg", "Lo sentimos, no ha sido posible crear el nuevo servicio.");
            Logger.getLogger(ServicesController.class.getName()).log(Level.SEVERE, null, e);
            return "";
        }

        newServiceName = "";
        newServiceDescription = "";
        return "services.xhtml" + Constants.FACES_REDIRECT;
    }

    public String updateService() {
        if (selectedService != null) {
            RequestContext context = RequestContext.getCurrentInstance();
            try {
                serviceFacade.updateService(selectedService, newServiceName, newServiceDescription, organisation);
                context.execute("PF('newServiceDialog').hide();");

                FacesUtil.addSuccessMessage("servicesForm:msg", "El servicio ha sido actualizado correctamente.");
            } catch (ServiceAlreadyExistsException e) {
                FacesUtil.addErrorMessage("servicesForm:msg", e.getMessage());
                Logger.getLogger(ServicesController.class.getName()).log(Level.SEVERE, null, e);
                return "";
            } catch (Exception e) {
                FacesUtil.addErrorMessage("servicesForm:msg", "Lo sentimos, no ha sido posible editar el servicio.");
                Logger.getLogger(ServicesController.class.getName()).log(Level.SEVERE, null, e);
                return "";
            }
        } else {
            FacesUtil.addErrorMessage("servicesForm:msg", "Lo sentimos, no ha sido posible editar el servicio.");
        }

        selectedService = null;
        newServiceName = "";
        newServiceDescription = "";
        return "services.xhtml" + Constants.FACES_REDIRECT;
    }

    public void prepareService(Service service) {
        selectedService = service;
        newServiceName = service.getName();
        newServiceDescription = service.getDescription();
        isNewService = false;
    }

    public void prepareNewService() {
        selectedService = null;
        newServiceName = "";
        newServiceDescription = "";
        isNewService = true;
    }

    public boolean loggedUserIsAdmin() {
        return userRole == Role.ADMIN || userRole == Role.SUPER_ADMIN;
    }

    public List<Service> getServices() {
        return services;
    }

    public Role getUserRole() {
        return userRole;
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

    public boolean isIsNewService() {
        return isNewService;
    }

    public int getNumberOfClasses(Service service) {
        return classFacade.findNumberOfActiveCurrentClassesOfService(service, organisation);
    }

    public int getNumberOfAppointments(Service service) {
        return appointmentsFacade.findNumberOfActiveCurrentAppointmentsOfService(service, organisation);
    }
}
