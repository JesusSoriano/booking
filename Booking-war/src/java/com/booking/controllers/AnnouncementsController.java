package com.booking.controllers;

import com.booking.entities.Announcement;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
import com.booking.facades.AnnouncementFacade;
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
public class AnnouncementsController implements Serializable {

    @EJB
    private AnnouncementFacade announcementFacade;
    @EJB
    private AuditFacade auditFacade;

    private List<Announcement> announcements;
    private User loggedUser;
    private Organisation organisation;
    private Announcement selectedAnnouncement;
    private String newAnnouncementTitle;
    private String newAnnouncementText;
    private boolean isNewAnnouncement;

    public AnnouncementsController() {
    }

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        isNewAnnouncement = true;
        announcements = announcementFacade.findAllAnnouncementsOfOrganisation(organisation);
    }

    public String activateAnnouncement(Announcement announcement) {
        announcementFacade.activateAnnouncement(announcement);
        FacesUtil.addSuccessMessage("announcementsForm:msg", "El comunicado ha sido activado correctamente.");

        try {
            // Audit announcement activation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVAR_SERVICIO, loggedUser, ipAddress, announcement.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(AnnouncementsController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "announcements.xhtml" + Constants.FACES_REDIRECT;
    }

    public String deactivateAnnouncement(Announcement announcement) {
        announcementFacade.deactivateAnnouncement(announcement);
        FacesUtil.addSuccessMessage("announcementsForm:msg", "El comunicado ha sido suspendido correctamente.");

        try {
            // Audit announcement suspention
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.SUSPENDER_SERVICIO, loggedUser, ipAddress, announcement.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(AnnouncementsController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "announcements.xhtml" + Constants.FACES_REDIRECT;
    }

    public String createNewAnnouncement() {
        if (newAnnouncementTitle.trim().isEmpty()) {
            FacesUtil.addErrorMessage("announcementsForm:msg", "Lo sentimos, no ha sido posible crear el nuevo comunicado: Introduzca un nombre.");
            return "";
        }
        RequestContext context = RequestContext.getCurrentInstance();
        try {
            Announcement newAnnouncement = announcementFacade.createNewAnnouncement(newAnnouncementTitle, newAnnouncementText, organisation);
            context.execute("PF('newAnnouncementDialog').hide();");
            FacesUtil.addSuccessMessage("announcementsForm:msg", "El nuevo comunicado ha sido creado correctamente.");
            // Audit announcement creation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CREAR_SERVICIO, loggedUser, ipAddress, newAnnouncement.getId(), organisation);
        } catch (Exception e) {
            FacesUtil.addErrorMessage("announcementsForm:msg", "Lo sentimos, no ha sido posible crear el nuevo comunicado.");
            Logger.getLogger(AnnouncementsController.class.getName()).log(Level.SEVERE, null, e);
            return "";
        }

        newAnnouncementTitle = "";
        newAnnouncementText = "";
        return "announcements.xhtml" + Constants.FACES_REDIRECT;
    }

    public String updateAnnouncement() {
        if (selectedAnnouncement != null) {
            RequestContext context = RequestContext.getCurrentInstance();
            try {
                announcementFacade.updateAnnouncement(selectedAnnouncement, newAnnouncementTitle, newAnnouncementText, organisation);
                context.execute("PF('newAnnouncementDialog').hide();");

                FacesUtil.addSuccessMessage("announcementsForm:msg", "El comunicado ha sido actualizado correctamente.");
            } catch (Exception e) {
                FacesUtil.addErrorMessage("announcementsForm:msg", "Lo sentimos, no ha sido posible editar el comunicado.");
                Logger.getLogger(AnnouncementsController.class.getName()).log(Level.SEVERE, null, e);
                return "";
            }
        } else {
            FacesUtil.addErrorMessage("announcementsForm:msg", "Lo sentimos, no ha sido posible editar el comunicado.");
        }

        selectedAnnouncement = null;
        newAnnouncementTitle = "";
        newAnnouncementText = "";
        return "announcements.xhtml" + Constants.FACES_REDIRECT;
    }

    public void prepareAnnouncement(Announcement announcement) {
        selectedAnnouncement = announcement;
        newAnnouncementTitle = announcement.getTitle();
        newAnnouncementText = announcement.getText();
        isNewAnnouncement = false;
    }

    public void prepareNewAnnouncement() {
        selectedAnnouncement = null;
        newAnnouncementTitle = "";
        newAnnouncementText = "";
        isNewAnnouncement = true;
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    public Role getUserRole() {
        return loggedUser.getUserRole().getRole();
    }

    public String getNewAnnouncementTitle() {
        return newAnnouncementTitle;
    }

    public void setNewAnnouncementTitle(String newAnnouncementTitle) {
        this.newAnnouncementTitle = newAnnouncementTitle;
    }

    public String getNewAnnouncementText() {
        return newAnnouncementText;
    }

    public void setNewAnnouncementText(String newAnnouncementText) {
        this.newAnnouncementText = newAnnouncementText;
    }

    public boolean isIsNewAnnouncement() {
        return isNewAnnouncement;
    }
}
