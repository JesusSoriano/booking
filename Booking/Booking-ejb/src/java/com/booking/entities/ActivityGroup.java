package com.booking.entities;

import com.booking.enums.Status;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
@Table(name = "groups")
public class ActivityGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "maximum_users")
    private int maximumUsers;
    @Column(name = "booked_places")
    private int bookedPlaces;
    @Column(name = "free_places")
    private int freePlaces;
    @Column(name = "days_a_week")
    private int daysAWeek;
    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation", referencedColumnName = "id")
    private Organisation organisation;
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "activityGroup", cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE})
    private List<GroupSchedule> groupSchedules;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service", referencedColumnName = "id")
    private Service service;

    public ActivityGroup() {
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void setBookedPlaces(int bookedPlaces) {
        this.bookedPlaces = bookedPlaces;
    }

    public int getFreePlaces() {
        return freePlaces;
    }

    public void setFreePlaces(int freePlaces) {
        this.freePlaces = freePlaces;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public List<GroupSchedule> getGroupSchedules() {
        return groupSchedules;
    }

    public void setGroupSchedules(List<GroupSchedule> groupSchedules) {
        this.groupSchedules = groupSchedules;
    }

    public void setDaysAWeek(int daysAWeek) {
        this.daysAWeek = daysAWeek;
    }

    public int getDaysAWeek() {
        return daysAWeek;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
