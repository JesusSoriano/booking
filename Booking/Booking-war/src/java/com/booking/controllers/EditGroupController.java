package com.booking.controllers;

import com.booking.entities.ActivityDay;
import com.booking.entities.ActivityGroup;
import com.booking.entities.Organisation;
import com.booking.entities.Service;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
import com.booking.facades.BookingFacade;
import com.booking.facades.GroupFacade;
import com.booking.facades.ServiceFacade;
import com.booking.facades.UserFacade;
import com.booking.util.Constants;
import com.booking.util.FacesUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import org.primefaces.context.RequestContext;

@ManagedBean
@ViewScoped
public class EditGroupController implements Serializable {

    @EJB
    private GroupFacade groupFacade;
    @EJB
    private BookingFacade bookingFacade;
    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private AuditFacade auditFacade;
    @EJB
    private UserFacade userFacade;

    private List<SelectItem> services;
    private long selectedServiceId;
    private List<SelectItem> allUsers;
    private long selectedUserId;
    private User loggedUser;
    private Organisation organisation;
    private String groupName;
    private String groupDescription;
    private int maximumUsers;
    private int bookedPlaces;
    private int numberOfDays;
    private float price;
    private boolean newGroup;
    private ActivityGroup currentGroup;
    private List<ActivityDay> activityDays; // días de cada actividad. TODO: nueva pantalla para crear los días
    // dependiendo del número?
    private List<User> groupUsers;
    private String groupId;

    public EditGroupController() {
    }

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        services = new ArrayList<>();
        for (Service s : serviceFacade.findAllActiveServicesOfOrganisation(organisation)) {
            services.add(new SelectItem(s.getId(), s.getName()));
        }
        allUsers = new ArrayList<>();
        for (User u : userFacade.findAllActiveAdminsAndClientsOfOrganisation(organisation)) {
            allUsers.add(new SelectItem(u.getId(), u.getFullName()));
        }
        selectedUserId = (long) allUsers.get(0).getValue();

        groupId = FacesUtil.getParameter("group");
        if (groupId != null) {
            currentGroup = groupFacade.findGroupOfOrganisation(Integer.valueOf(groupId), organisation);

            if (currentGroup != null) {
                selectedServiceId = currentGroup.getService().getId();
                groupName = currentGroup.getName();
                groupDescription = currentGroup.getDescription();
                maximumUsers = currentGroup.getMaximumUsers();
                bookedPlaces = currentGroup.getBookedPlaces();
//                startingTime = currentGroup.getStartTime();
//                endingTime = currentGroup.getEndTime();
                numberOfDays = currentGroup.getNumberOfDays();
            }
        } else {
            newGroup = true;
        }

        groupUsers = bookingFacade.findAllBookedUsersOfGroup(currentGroup);
    }

    public String activateGroup(ActivityGroup group) {
        groupFacade.activateGroup(group);
        FacesUtil.addSuccessMessage("groupsForm:msg", "El grupo ha sido activada correctamente.");

        try {
            // Audit group activation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVAR_SERVICIO, loggedUser, ipAddress, group.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(EditGroupController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "groups.xhtml" + Constants.FACES_REDIRECT;
    }

    public String deactivateGroup(ActivityGroup group) {
        groupFacade.deactivateGroup(group);
        FacesUtil.addSuccessMessage("groupsForm:msg", "El grupo ha sido suspendido correctamente.");

        try {
            // Audit group suspention
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.SUSPENDER_SERVICIO, loggedUser, ipAddress, group.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(EditGroupController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "groups.xhtml" + Constants.FACES_REDIRECT;
    }

    public String createNewGroup() {
        RequestContext context = RequestContext.getCurrentInstance();
        try {
            Service selectedService = serviceFacade.find(selectedServiceId);
            ActivityGroup newActivityGroup = groupFacade.createNewGroup(selectedService, groupName, groupDescription, maximumUsers, numberOfDays, price, organisation);
            context.execute("PF('newGroupDialog').hide();");
            FacesUtil.addSuccessMessage("groupsForm:msg", "El nuevo servicio ha sido creado correctamente.");
            // Audit group creation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CREAR_SERVICIO, loggedUser, ipAddress, newActivityGroup.getId(), organisation);
            return "view-group.xhtml" + Constants.FACES_REDIRECT + "&amp;group=" + newActivityGroup.getId();
        } catch (Exception e) {
            FacesUtil.addErrorMessage("groupsForm:msg", "Lo sentimos, no ha sido posible crear el nuevo servicio.");
            Logger.getLogger(EditGroupController.class.getName()).log(Level.SEVERE, null, e);
            return "groups.xhtml" + Constants.FACES_REDIRECT;
        }

    }

    public String updateGroup() {
        try {
            Service selectedService = serviceFacade.find(selectedServiceId);
            ActivityGroup updatedGroup = groupFacade.updateGroup(currentGroup, selectedService, groupName, groupDescription, maximumUsers, numberOfDays, price);
            if (updatedGroup != null) {
//                FacesUtil.addSuccessMessage("groupsForm:msg", "El servicio ha sido actualizado correctamente.");
                return "view-group.xhtml" + Constants.FACES_REDIRECT + "&amp;group=" + updatedGroup.getId();
            } else {
                FacesUtil.addErrorMessage("groupsForm:msg", "Lo sentimos, no ha sido posible editar el servicio.");
            }
        } catch (Exception e) {
            FacesUtil.addErrorMessage("groupsForm:msg", "Lo sentimos, no ha sido posible editar el servicio.");
            Logger.getLogger(EditGroupController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "groups.xhtml" + Constants.FACES_REDIRECT;
    }

    public String bookGroup() {
        try {
            User selectedUser = userFacade.find(selectedUserId);
            if (selectedUser != null) {
                // Create the class user
                bookingFacade.createNewBooking(selectedUser, currentGroup);
                // Add a booked place in the class
                groupFacade.addGroupBooking(currentGroup);

                FacesUtil.addSuccessMessage("viewGroupForm:msg", "La plaza ha sido reservada correctamente.");

                // Audit new booking
                String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                auditFacade.createAudit(AuditType.RESERVAR_CLASE, selectedUser, ipAddress, currentGroup.getId(), organisation);

            } else {
                FacesUtil.addErrorMessage("viewGroupForm:msg", "Error, el usuario no existe.");
            }
        } catch (Exception e) {
            Logger.getLogger(GroupsController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("viewGroupForm:msg", "Lo sentimos, ha habido un problema al reservar la plaza.");
        }

        String groupParam = (groupId != null) ? ("group=" + groupId) : "";
        return "view-group.xhtml" + Constants.FACES_REDIRECT + groupParam;
    }

    public String cancelGroupBooking(User user) {
        try {
            // Create the class user
            if (bookingFacade.removeBooking(user, currentGroup)) {
                // Add a booked place in the class
                groupFacade.removeGroupBooking(currentGroup);
                FacesUtil.addSuccessMessage("viewGroupForm:msg", "La plaza ha sido cancelada correctamente.");

                // Audit booking cancalation
                String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                auditFacade.createAudit(AuditType.CANCELAR_RESERVA, user, ipAddress, currentGroup.getId(), organisation);
            } else {
                FacesUtil.addErrorMessage("viewGroupForm:msg", "Error, la reserva no existe.");
            }
        } catch (Exception e) {
            Logger.getLogger(EditGroupController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("viewGroupForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
        }

        String groupParam = (groupId != null) ? ("group=" + groupId) : "";
        return "view-group.xhtml" + Constants.FACES_REDIRECT + groupParam;
    }

    public List<SelectItem> getServices() {
        return services;
    }

    public List<SelectItem> getAllUsers() {
        return allUsers;
    }

    public Role getUserRole() {
        return loggedUser.getUserRole().getRole();
    }

    public boolean isNewGroup() {
        return newGroup;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public int getMaximumUsers() {
        return maximumUsers;
    }

    public void setMaximumUsers(int maximumUsers) {
        this.maximumUsers = maximumUsers;
    }

    public int getBookedPlaces() {
        return bookedPlaces;
    }

    public long getSelectedServiceId() {
        return selectedServiceId;
    }

    public void setSelectedServiceId(long selectedServiceId) {
        this.selectedServiceId = selectedServiceId;
    }

    public long getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(long selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public ActivityGroup getCurrentGroup() {
        return currentGroup;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public List<User> getGroupUsers() {
        return groupUsers;
    }
}
