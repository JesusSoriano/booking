package com.booking.entities;

import com.booking.enums.RequestStatus;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Jesús Soriano
 */
@Entity
@Table(name = "appointment_requests")
public class AppointmentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_user", referencedColumnName = "id")
    private User requestUser;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment", referencedColumnName = "id")
    private Appointment appointment;
    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
    @Column(name = "comments")
    private String comments;
    @Column(name = "status_comment")
    private String statusComment;

    public AppointmentRequest() {
    }

    public long getId() {
        return id;
    }

    public User getRequestUser() {
        return requestUser;
    }

    public void setRequestUser(User requestUser) {
        this.requestUser = requestUser;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getStatusComment() {
        return statusComment;
    }

    public void setStatusComment(String statusComment) {
        this.statusComment = statusComment;
    }
}
