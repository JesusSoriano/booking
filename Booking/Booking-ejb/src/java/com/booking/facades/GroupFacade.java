package com.booking.facades;

import com.booking.entities.ActivityGroup;
import com.booking.entities.Organisation;
import com.booking.entities.Service;
import com.booking.enums.Status;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Jesús Soriano
 */
@Stateless
public class GroupFacade extends AbstractFacade<ActivityGroup> {

    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public GroupFacade() {
        super(ActivityGroup.class);
    }

    
    public ActivityGroup createNewGroup(Service service, String name, String description, int maximumUsers, int daysPerWeek, String daysOfTheWeek, Date startTime, Date EndTime, boolean weekly, Organisation organisation) {
        
        ActivityGroup group = new ActivityGroup();
        group.setService(service);
        group.setName(name);
        group.setDescription(description);
        group.setMaximumUsers(maximumUsers);
        group.setDaysPerWeek(daysPerWeek);
        group.setDaysOfWeek(daysOfTheWeek);
        group.setStartTime(startTime);
        group.setEndTime(EndTime);
        group.setWeekly(weekly);
        group.setOrganisation(organisation);
        group.setCreatedDate(new Date());
        group.setStatus(Status.ACTIVATED);
        create(group);
        
        return group;
    }
    
    public ActivityGroup updateGroup(ActivityGroup group, Service service, String name, String description, int maximumUsers, int daysPerWeek, String daysOfTheWeek, Date startTime, Date EndTime, boolean weekly) {
        
        group.setService(service);
        group.setName(name);
        group.setDescription(description);
        group.setMaximumUsers(maximumUsers);
        group.setDaysPerWeek(daysPerWeek);
        group.setDaysOfWeek(daysOfTheWeek);
        group.setStartTime(startTime);
        group.setEndTime(EndTime);
        group.setWeekly(weekly);
        edit(group);
        
        return group;
    }
    
    public void activateGroup(ActivityGroup group) {
        group.setStatus(Status.ACTIVATED);
        edit(group);
    }

    public void deactivateGroup(ActivityGroup group) {
        group.setStatus(Status.SUSPENDED);
        edit(group);
    }

    public List<ActivityGroup> findAllGroupsOfService(Service service, Organisation organisation) {
        return em.createQuery("SELECT a FROM ActivityGroup a WHERE a.service = :service AND a.organisation = :organisation ORDER BY a.name ASC").
                setParameter("service", service).
                setParameter("organisation", organisation).getResultList();
    }

    public List<ActivityGroup> findAllGroupsOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT a FROM ActivityGroup a WHERE a.organisation = :organisation ORDER BY a.name ASC").
                setParameter("organisation", organisation).getResultList();
    }

    public ActivityGroup findGroupOfOrganisation(long groupId, Organisation organisation) {
        return findUniqueResult(em.createQuery("SELECT g FROM ActivityGroup g WHERE g.id = :groupId AND g.organisation = :organisation ORDER BY g.name ASC").
                setParameter("groupId", groupId).
                setParameter("organisation", organisation).getResultList());
    } 
}
