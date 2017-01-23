package com.booking.controllers;

import com.booking.entities.ActivityGroup;
import com.booking.entities.Organisation;
import com.booking.entities.Service;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
import com.booking.facades.GroupFacade;
import com.booking.facades.BookingFacade;
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

@ManagedBean
@ViewScoped
public class GroupsController implements Serializable {

    @EJB
    private GroupFacade groupFacade;
    @EJB
    private BookingFacade bookingFacade;
    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private AuditFacade auditFacade;

    private List<ActivityGroup> groups;
    private User loggedUser;
    private Organisation organisation;
    private Service currentService;
    private String serviceId;

    public GroupsController() {
    }

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        serviceId = FacesUtil.getParameter("service");
        if (serviceId != null) {
            currentService = serviceFacade.findServiceOfOrganisation(Integer.valueOf(serviceId), organisation);
            if (currentService != null) {
                groups = groupFacade.findAllGroupsOfService(currentService, organisation);
            } else {
                groups = groupFacade.findAllGroupsOfOrganisation(organisation);
            }
        } else {
            groups = groupFacade.findAllGroupsOfOrganisation(organisation);
        }
    }

    public String activateGroup(ActivityGroup group) {
        groupFacade.activateGroup(group);
        FacesUtil.addSuccessMessage("groupsForm:msg", "El servicio ha sido activado correctamente.");

        try {
            // Audit group activation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVAR_SERVICIO, loggedUser, ipAddress, group.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(GroupsController.class.getName()).log(Level.SEVERE, null, e);
        }

        String serviceParam = (serviceId != null) ? ("service=" + serviceId) : "";
        return "groups.xhtml" + Constants.FACES_REDIRECT + serviceParam;
    }

    public String deactivateGroup(ActivityGroup group) {
        groupFacade.deactivateGroup(group);
        FacesUtil.addSuccessMessage("groupsForm:msg", "El servicio ha sido suspendido correctamente.");

        try {
            // Audit group suspention
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.SUSPENDER_SERVICIO, loggedUser, ipAddress, group.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(GroupsController.class.getName()).log(Level.SEVERE, null, e);
        }

        String serviceParam = (serviceId != null) ? ("service=" + serviceId) : "";
        return "groups.xhtml" + Constants.FACES_REDIRECT + serviceParam;
    }

    public String bookGroup(ActivityGroup group) {
        try {
            // Create the class user
            bookingFacade.createNewBooking(loggedUser, group);
            // Add a booked place in the class
            groupFacade.addGroupBooking(group);

            FacesUtil.addSuccessMessage("groupsForm:msg", "La plaza ha sido reservada correctamente.");
        } catch (Exception e) {
            Logger.getLogger(GroupsController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("groupsForm:msg", "Lo sentimos, ha habido un problema al reservar la plaza.");
        }

        try {
            // Audit group suspention
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.RESERVAR_CLASE, loggedUser, ipAddress, group.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(GroupsController.class.getName()).log(Level.SEVERE, null, e);
        }

        String serviceParam = (serviceId != null) ? ("service=" + serviceId) : "";
        return "groups.xhtml" + Constants.FACES_REDIRECT + serviceParam;
    }

    public String cancelGroupBooking(ActivityGroup group) {
        try {
            // Create the class user
            if (bookingFacade.removeBooking(loggedUser, group)) {
                // Add a booked place in the class
                groupFacade.removeGroupBooking(group);
                FacesUtil.addSuccessMessage("groupsForm:msg", "La plaza ha sido cancelada correctamente.");
            } else {
                FacesUtil.addErrorMessage("groupsForm:msg", "Error, la reserva no existe.");
            }
        } catch (Exception e) {
            Logger.getLogger(GroupsController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("groupsForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
        }

        try {
            // Audit group suspention
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CANCELAR_RESERVA, loggedUser, ipAddress, group.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(GroupsController.class.getName()).log(Level.SEVERE, null, e);
        }

        String serviceParam = (serviceId != null) ? ("service=" + serviceId) : "";
        return "groups.xhtml" + Constants.FACES_REDIRECT + serviceParam;
    }

    public boolean existsBooking(ActivityGroup group) {
        return bookingFacade.existsBooking(loggedUser, group);
    }

    public List<ActivityGroup> getGroups() {
        return groups;
    }

    public Role getUserRole() {
        return loggedUser.getUserRole().getRole();
    }

    public Service getCurrentService() {
        return currentService;
    }
}
