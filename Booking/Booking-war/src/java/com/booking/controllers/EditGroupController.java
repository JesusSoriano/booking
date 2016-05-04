package com.booking.controllers;

import com.booking.entities.ActivityGroup;
import com.booking.entities.Organisation;
import com.booking.entities.Service;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
import com.booking.facades.GroupFacade;
import com.booking.facades.ServiceFacade;
import com.booking.util.Constants;
import com.booking.util.FacesUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    private ServiceFacade serviceFacade;
    @EJB
    private AuditFacade auditFacade;

    private List<SelectItem> services;
    private long selectedServiceId;
    private User loggedUser;
    private Organisation organisation;
    private String groupName;
    private String groupDescription;
    private int maximumUsers;
    private int bookedPlaces;
    private int daysPerWeek;
    private String daysOfWeek;
    private Date startingTime;
    private Date endingTime;
    private boolean weekly;
    private boolean newGroup;
    private ActivityGroup currentGroup;
    private List<Integer> weekDays;

    public EditGroupController() {
    }

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        services = new ArrayList<>();
        for (Service s : serviceFacade.findAllServicesOfOrganisation(organisation)) {
            services.add(new SelectItem(s.getId(), s.getName()));
        }
        weekDays = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        weekly = true;

        String groupId = FacesUtil.getParameter("group");
        if (groupId != null && (loggedUser.getUserRole().getRole() == Role.ADMIN || loggedUser.getUserRole().getRole() == Role.SUPER_ADMIN)) {
            currentGroup = groupFacade.findGroupOfOrganisation(Integer.valueOf(groupId), organisation);

            if (currentGroup != null) {
                selectedServiceId = currentGroup.getService().getId();
                groupName = currentGroup.getName();
                groupDescription = currentGroup.getDescription();
                maximumUsers = currentGroup.getMaximumUsers();
                bookedPlaces = currentGroup.getBookedPlaces();
                daysPerWeek = currentGroup.getDaysPerWeek();
                daysOfWeek = currentGroup.getDaysOfWeek();
                startingTime = currentGroup.getStartTime();
                endingTime = currentGroup.getEndTime();
                weekly = currentGroup.isWeekly();
            }
        } else {
            newGroup = true;
        }
    }

    public String activateGroup(ActivityGroup group) {
        groupFacade.activateGroup(group);
        FacesUtil.addSuccessMessage("groupsForm:msg", "El grupo ha sido activada correctamente.");

        try {
            // Audit group activation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVATE_SERVICE, loggedUser, ipAddress, group.getId(), organisation);
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
            auditFacade.createAudit(AuditType.SUSPEND_SERVICE, loggedUser, ipAddress, group.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(EditGroupController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "groups.xhtml" + Constants.FACES_REDIRECT;
    }

    public String createNewGroup() {
        RequestContext context = RequestContext.getCurrentInstance();
        try {
            Service selectedService = serviceFacade.find(selectedServiceId);
            ActivityGroup newActivityGroup = groupFacade.createNewGroup(selectedService, groupName, groupDescription, maximumUsers, daysPerWeek, daysOfWeek, startingTime, endingTime, weekly, organisation);
            context.execute("PF('newGroupDialog').hide();");
            FacesUtil.addSuccessMessage("groupsForm:msg", "El nuevo servicio ha sido creado correctamente.");
            // Audit group creation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CREATE_SERVICE, loggedUser, ipAddress, newActivityGroup.getId(), organisation);
            return "view-group.xhtml" + Constants.FACES_REDIRECT + "&amp;group=" + newActivityGroup.getId();
        } catch (Exception e) {
            FacesUtil.addErrorMessage("groupsForm:msg", "Lo sentimos, no ha sido posible crear el nuevo servicio.");
            Logger.getLogger(EditGroupController.class.getName()).log(Level.SEVERE, null, e);
            return "groups.xhtml" + Constants.FACES_REDIRECT;
        }

    }

    public String updateGroup() {
        RequestContext context = RequestContext.getCurrentInstance();
        try {
            Service selectedService = serviceFacade.find(selectedServiceId);
            ActivityGroup updatedGroup = groupFacade.updateGroup(currentGroup, selectedService, groupName, groupDescription, maximumUsers, daysPerWeek, daysOfWeek, startingTime, endingTime, weekly);
            context.execute("PF('newGroupDialog').hide();");
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

    public List<SelectItem> getServices() {
        return services;
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

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public int getDaysPerWeek() {
        return daysPerWeek;
    }

    public void setDaysPerWeek(int daysPerWeek) {
        this.daysPerWeek = daysPerWeek;
    }

    public boolean isWeekly() {
        return weekly;
    }

    public void setWeekly(boolean weekly) {
        this.weekly = weekly;
    }

    public Date getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(Date endingTime) {
        this.endingTime = endingTime;
    }

    public Date getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(Date startingTime) {
        this.startingTime = startingTime;
    }

    public long getSelectedServiceId() {
        return selectedServiceId;
    }

    public void setSelectedServiceId(long selectedServiceId) {
        this.selectedServiceId = selectedServiceId;
    }

    public ActivityGroup getCurrentGroup() {
        return currentGroup;
    }

    public List<Integer> getWeekDays() {
        return weekDays;
    }
}
