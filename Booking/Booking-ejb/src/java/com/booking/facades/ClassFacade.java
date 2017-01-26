package com.booking.facades;

import com.booking.entities.ActivityClass;
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
public class ClassFacade extends AbstractFacade<ActivityClass> {

    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ClassFacade() {
        super(ActivityClass.class);
    }

    
    public ActivityClass createNewClass(Service service, String name, String description, int maximumUsers, int numberOfDays, float price, Organisation organisation) {
        
        ActivityClass activityClass = new ActivityClass();
        activityClass.setService(service);
        activityClass.setName(name);
        activityClass.setDescription(description);
        activityClass.setMaximumUsers(maximumUsers);
        activityClass.setNumberOfDays(numberOfDays);
        activityClass.setPrice(price);
        activityClass.setOrganisation(organisation);
        activityClass.setCreatedDate(new Date());
        activityClass.setStatus(Status.ACTIVATED);
        create(activityClass);
        
        return activityClass;
    }
    
    public ActivityClass updateClass(ActivityClass activityClass, Service service, String name, String description, int maximumUsers, int numberOfDays, float price) {
        
        activityClass.setService(service);
        activityClass.setName(name);
        activityClass.setDescription(description);
        activityClass.setMaximumUsers(maximumUsers);
        activityClass.setNumberOfDays(numberOfDays);
        activityClass.setPrice(price);
        edit(activityClass);
        
        return activityClass;
    }
    
    public void activateClass(ActivityClass activityClass) {
        activityClass.setStatus(Status.ACTIVATED);
        edit(activityClass);
    }

    public void deactivateClass(ActivityClass activityClass) {
        activityClass.setStatus(Status.SUSPENDED);
        edit(activityClass);
    }

    public void addClassBooking(ActivityClass activityClass) {
        activityClass.setBookedPlaces(activityClass.getBookedPlaces() + 1);
        edit(activityClass);
    }
    
    public void removeClassBooking(ActivityClass activityClass) {
        activityClass.setBookedPlaces(activityClass.getBookedPlaces() - 1);
        edit(activityClass);
    }

    public List<ActivityClass> findAllClassesOfService(Service service, Organisation organisation) {
        return em.createQuery("SELECT a FROM ActivityClass a WHERE a.service = :service AND a.organisation = :organisation ORDER BY a.name ASC").
                setParameter("service", service).
                setParameter("organisation", organisation).getResultList();
    }

    public List<ActivityClass> findAllActiveClassesOfService(Service service, Organisation organisation) {
        return em.createQuery("SELECT a FROM ActivityClass a WHERE a.service = :service AND a.status = :activeStatus AND a.organisation = :organisation ORDER BY a.name ASC").
                setParameter("service", service).
                setParameter("activeStatus", Status.ACTIVATED).
                setParameter("organisation", organisation).getResultList();
    }

    public int findNumberOfActiveClassesOfService(Service service, Organisation organisation) {
        return findAllActiveClassesOfService(service, organisation).size();
    }
    
    public List<ActivityClass> findAllClassesOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT a FROM ActivityClass a WHERE a.organisation = :organisation ORDER BY a.name ASC").
                setParameter("organisation", organisation).getResultList();
    }

    public List<ActivityClass> findAllActiveClassesOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT a FROM ActivityClass a WHERE a.status = :activeStatus AND a.organisation = :organisation ORDER BY a.name ASC").
                setParameter("activeStatus", Status.ACTIVATED).
                setParameter("organisation", organisation).getResultList();
    }

    public ActivityClass findClassOfOrganisation(long classId, Organisation organisation) {
        return findUniqueResult(em.createQuery("SELECT g FROM ActivityClass g WHERE g.id = :classId AND g.organisation = :organisation ORDER BY g.name ASC").
                setParameter("classId", classId).
                setParameter("organisation", organisation).getResultList());
    } 
}
