package com.booking.controllers;

import com.booking.entities.ActivityGroup;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
import com.booking.facades.GroupFacade;
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
public class GroupsController implements Serializable {

    @EJB
    private GroupFacade groupFacade;
    @EJB
    private AuditFacade auditFacade;

    private List<ActivityGroup> groups;
    private User loggedUser;
    private Organisation organisation;
    private String newGroupName;
    private String newGroupDescription;
    private int newGroupMaxUsers;
    private int newGroupDaysPerWeek;

    public GroupsController() {
    }

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        groups = groupFacade.findAllGroupsOfOrganisation(organisation);
    }

    public String activateGroup(ActivityGroup group) {
        groupFacade.activateGroup(group);
        FacesUtil.addSuccessMessage("groupsForm:msg", "El servicio ha sido activada correctamente.");

        try {
            // Audit group activation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVATE_SERVICE, loggedUser, ipAddress, group.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(GroupsController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "groups.xhtml" + Constants.FACES_REDIRECT;
    }

    public String deactivateGroup(ActivityGroup group) {
        groupFacade.deactivateGroup(group);
        FacesUtil.addSuccessMessage("groupsForm:msg", "El servicio ha sido suspendido correctamente.");

        try {
            // Audit group suspention
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.SUSPEND_SERVICE, loggedUser, ipAddress, group.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(GroupsController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "groups.xhtml" + Constants.FACES_REDIRECT;
    }

    public String createNewGroup() {
        RequestContext context = RequestContext.getCurrentInstance();
        try {
            ActivityGroup newGroup = groupFacade.createNewGroup(newGroupName, newGroupDescription, newGroupMaxUsers, newGroupDaysPerWeek, organisation);
            context.execute("PF('newGroupDialog').hide();");
            FacesUtil.addSuccessMessage("groupsForm:msg", "El nuevo servicio ha sido creado correctamente.");
            // Audit group creation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CREATE_SERVICE, loggedUser, ipAddress, newGroup.getId(), organisation);
        } catch (Exception e) {
            FacesUtil.addErrorMessage("groupsForm:msg", "Lo sentimos, no ha sido posible crear el nuevo servicio.");
            Logger.getLogger(GroupsController.class.getName()).log(Level.SEVERE, null, e);
            return "";
        }

        newGroupName = "";
        newGroupDescription = "";
        return "groups.xhtml" + Constants.FACES_REDIRECT;
    }

    public void prepareGroup(ActivityGroup group) {
        newGroupName = group.getName();
        newGroupDescription = group.getDescription();
    }

    public void prepareNewGroup() {
    }

    public List<ActivityGroup> getGroups() {
        return groups;
    }

    public Role getUserRole() {
        return loggedUser.getUserRole().getRole();
    }

    public String getNewGroupName() {
        return newGroupName;
    }

    public void setNewGroupName(String newGroupName) {
        this.newGroupName = newGroupName;
    }

    public String getNewGroupDescription() {
        return newGroupDescription;
    }

    public void setNewGroupDescription(String newGroupDescription) {
        this.newGroupDescription = newGroupDescription;
    }

    public int getNewGroupMaxUsers() {
        return newGroupMaxUsers;
    }

    public int getNewGroupDaysPerWeek() {
        return newGroupDaysPerWeek;
    }

    public void setNewGroupMaxUsers(int newGroupMaxUsers) {
        this.newGroupMaxUsers = newGroupMaxUsers;
    }

    public void setNewGroupDaysPerWeek(int newGroupDaysPerWeek) {
        this.newGroupDaysPerWeek = newGroupDaysPerWeek;
    }
}
