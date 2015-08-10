package com.booking.controllers;

import com.booking.entities.Audit;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
import com.booking.facades.UserFacade;
import com.booking.util.DateService;
import com.booking.util.FacesUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;

/**
 *
 * @author Jesús Soriano
 */
@ManagedBean
public class AuditsController implements Serializable {

    @EJB
    private UserFacade usersFacade;
    @EJB
    private AuditFacade auditFacade;

    private List<Audit> auditList;
    private Role userRole;
    private Organisation organisation;
    private List<SelectItem> users;
    long selectedUserId;
    private List<SelectItem> auditTypes;
    AuditType selectedAuditType;
    private Date fromDate;
    private Date toDate;

    @PostConstruct
    public void init() {
        User loggedUser = FacesUtil.getCurrentUser();
        userRole = loggedUser.getUserRole().getRole();
        organisation = FacesUtil.getCurrentOrganisation();

        users = new ArrayList<>();
        users.add(new SelectItem(-1, "All"));
        auditTypes = new ArrayList<>();
        auditTypes.add(new SelectItem(null, "All"));

        if (userRole == Role.SUPER_ADMIN) {
            for (User u : usersFacade.findAllUsersOfOrganisation(organisation)) {
                users.add(new SelectItem(u.getId(), u.getFullName()));
            }

            for (AuditType at : AuditType.getAllAuditTypes()) {
                auditTypes.add(new SelectItem(at.name(), at.name()));
            }
        } else if (userRole == Role.ADMIN) {
            for (User u : usersFacade.findAllAdminsAndClientsOfOrganisation(organisation)) {
                users.add(new SelectItem(u.getId(), u.getFullName()));
            }

            for (AuditType at : AuditType.getAuditTypesForAdmin()) {
                auditTypes.add(new SelectItem(at.name(), at.name()));
            }
        } else {
            selectedUserId = loggedUser.getId();

            for (AuditType at : AuditType.getAuditTypesForClient()) {
                auditTypes.add(new SelectItem(at.name(), at.name()));
            }
        }

        fromDate = new Date();
        toDate = new Date();
        
        String userID = FacesUtil.getParameter("id");
        if (userID != null) {
            selectedUserId = Long.valueOf(userID);
            fromDate = DateService.getDaysEarlier(new Date(), 7);
            search();
        }
    }

    public void search() {

        fromDate = DateService.getDawnDay(fromDate);
        toDate = DateService.getMidnightDay(toDate);

        User selectedUser = null;
        if (selectedUserId >= 0) {
            selectedUser = usersFacade.findUserOfOrganisation(selectedUserId, organisation);
        }

        auditList = auditFacade.findAllAudits(fromDate, toDate, organisation, selectedAuditType, selectedUser, userRole);

    }

    public List<Audit> getAuditList() {
        return auditList;
    }

    public Role getUserRole() {
        return userRole;
    }

    public List<SelectItem> getUsers() {
        return users;
    }

    public long getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(long selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public List<SelectItem> getAuditTypes() {
        return auditTypes;
    }

    public AuditType getSelectedAuditType() {
        return selectedAuditType;
    }

    public void setSelectedAuditType(AuditType selectedAuditType) {
        this.selectedAuditType = selectedAuditType;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

}
