package com.booking.entities;

import com.booking.enums.NotificationType;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author Jesus Soriano
 */
@Entity
@Table(name = "notifications")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "notification_type")
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    @Column(name = "created_date")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdDate;
    /* User that gets the notification */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_user", referencedColumnName = "id")
    private User notificationUser;
    /* User that causes the notification. 
    Example: Admin that cancel an appointment or user that request one. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_user", referencedColumnName = "id")
    private User actiontionUser;
    @Column(name = "object_id")
    private long objectId;
    @Column(name = "checked")
    private Boolean checked;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation", referencedColumnName = "id")
    private Organisation organisation;

    public long getId() {
        return id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public User getNotificationUser() {
        return notificationUser;
    }

    public void setNotificationUser(User notificationUser) {
        this.notificationUser = notificationUser;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public Boolean isChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public User getActiontionUser() {
        return actiontionUser;
    }

    public void setActiontionUser(User actiontionUser) {
        this.actiontionUser = actiontionUser;
    }
}
