package com.booking.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Jesús Soriano
 */
@Entity
@Table(name = "groups")
public class Group implements Serializable {

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
    private String maximumUsers;
    @Column(name = "booked_places")
    private String bookedPlaces;
    @Column(name = "free_places")
    private String freePlaces;
    @Column(name = "days_a_week")
    private int daysAWeek;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation", referencedColumnName = "id")
    private Organisation organisation;
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "group", cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE})
    private List<GroupSchedule> groupSchedules;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service", referencedColumnName = "id")
    private Service service;

    public Group() {
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

    public String getMaximumUsers() {
        return maximumUsers;
    }

    public void setMaximumUsers(String maximumUsers) {
        this.maximumUsers = maximumUsers;
    }

    public String getBookedPlaces() {
        return bookedPlaces;
    }

    public void setBookedPlaces(String bookedPlaces) {
        this.bookedPlaces = bookedPlaces;
    }

    public String getFreePlaces() {
        return freePlaces;
    }

    public void setFreePlaces(String freePlaces) {
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
}
