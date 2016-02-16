package com.booking.entities;

import java.io.Serializable;
import javax.persistence.Basic;
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
@Table(name = "group_schedules")
public class GroupSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_group", referencedColumnName = "id")
    private ActivityGroup activityGroup;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule", referencedColumnName = "id")
    private Schedule schedule;

    public GroupSchedule() {
    }

    public long getId() {
        return id;
    }

    public ActivityGroup getActivityGroup() {
        return activityGroup;
    }

    public void setActivityGroup(ActivityGroup activityGroup) {
        this.activityGroup = activityGroup;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
}
