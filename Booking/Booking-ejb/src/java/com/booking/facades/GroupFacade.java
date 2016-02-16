package com.booking.facades;

import com.booking.entities.ActivityGroup;
import com.booking.entities.Organisation;
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

    
    public ActivityGroup createNewGroup(String name, String description, int maximumUsers, int daysPerWeek, Organisation organisation) {
        
        ActivityGroup group = new ActivityGroup();
        group.setName(name);
        group.setDescription(description);
        group.setMaximumUsers(maximumUsers);
        group.setDaysAWeek(daysPerWeek);
        group.setFreePlaces(maximumUsers);
        group.setOrganisation(organisation);
        group.setCreatedDate(new Date());
        group.setStatus(Status.ACTIVATED);
        create(group);
        
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

    public List<ActivityGroup> findAllGroupsOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT a FROM ActivityGroup a WHERE a.organisation = :organisation ORDER BY a.name ASC").
                setParameter("organisation", organisation).getResultList();
    }
}
