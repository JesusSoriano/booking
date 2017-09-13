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

    public ActivityClass duplicateClass(ActivityClass activityClass) {

        ActivityClass duplicatedClass = new ActivityClass();
        duplicatedClass.setService(activityClass.getService());
        duplicatedClass.setName(activityClass.getName());
        duplicatedClass.setDescription(activityClass.getDescription());
        duplicatedClass.setMaximumUsers(activityClass.getMaximumUsers());
        duplicatedClass.setNumberOfDays(0);
        duplicatedClass.setPrice(activityClass.getPrice());
        duplicatedClass.setOrganisation(activityClass.getOrganisation());
        duplicatedClass.setCreatedDate(new Date());
        duplicatedClass.setStatus(Status.ACTIVATED);
        create(duplicatedClass);

        return duplicatedClass;
    }

    public ActivityClass updateClass(ActivityClass activityClass, Service service, String name, String description, int maximumUsers, float price) {

        activityClass.setService(service);
        activityClass.setName(name);
        activityClass.setDescription(description);
        activityClass.setMaximumUsers(maximumUsers);
        activityClass.setPrice(price);
        edit(activityClass);

        return activityClass;
    }

    public void addNumberOfDays(ActivityClass activityClass) {
        activityClass.setNumberOfDays(activityClass.getNumberOfDays() + 1);
        edit(activityClass);
    }

    public void removeNumberOfDays(ActivityClass activityClass) {
        int numberOfDays = activityClass.getNumberOfDays();
        if (numberOfDays > 0) {
            activityClass.setNumberOfDays(numberOfDays - 1);
            edit(activityClass);
        }
    }

    public void updateEndDate(ActivityClass activityClass, Date endDate) {
        if (activityClass.getEndDate() == null || activityClass.getEndDate().before(endDate)) {
            activityClass.setEndDate(endDate);
            edit(activityClass);
        }
    }

    public void setEndDate(ActivityClass activityClass, Date endDate) {
            activityClass.setEndDate(endDate);
            edit(activityClass);
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

    public List<ActivityClass> findAllCurrentClassesOfService(Service service, Organisation organisation) {
        return em.createQuery("SELECT a FROM ActivityClass a WHERE a.service = :service AND a.endDate > :today AND a.organisation = :organisation OR a.service = :service AND a.endDate IS NULL AND a.organisation = :organisation ORDER BY a.endDate ASC").
                setParameter("service", service).
                setParameter("today", new Date()).
                setParameter("organisation", organisation).getResultList();
    }

    public List<ActivityClass> findAllPastActiveClassesOfService(Service service, Organisation organisation) {
        // Take all classes
        return em.createQuery("SELECT a FROM ActivityClass a WHERE a.service = :service AND a.status = :activeStatus AND a.endDate < :today AND a.organisation = :organisation ORDER BY a.endDate ASC").
                setParameter("service", service).
                setParameter("today", new Date()).
                setParameter("activeStatus", Status.ACTIVATED).
                setParameter("organisation", organisation).getResultList();
    }

    public List<ActivityClass> findAllActiveCurrentClassesOfService(Service service, Organisation organisation) {
        return em.createQuery("SELECT a FROM ActivityClass a WHERE a.service = :service AND a.status = :activeStatus AND a.endDate > :today AND a.organisation = :organisation ORDER BY a.endDate ASC").
                setParameter("service", service).
                setParameter("activeStatus", Status.ACTIVATED).
                setParameter("today", new Date()).
                setParameter("organisation", organisation).getResultList();
    }

    public int findNumberOfActiveCurrentClassesOfService(Service service, Organisation organisation) {
        return findAllActiveCurrentClassesOfService(service, organisation).size();
    }

    public List<ActivityClass> findAllCurrentClassesOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT a FROM ActivityClass a WHERE a.organisation = :organisation AND a.endDate > :today OR a.organisation = :organisation AND a.endDate IS NULL ORDER BY a.endDate ASC").
                setParameter("today", new Date()).
                setParameter("organisation", organisation).getResultList();
    }

    public List<ActivityClass> findAllPastClassesOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT a FROM ActivityClass a WHERE a.organisation = :organisation AND a.endDate < :today ORDER BY a.endDate ASC").
                setParameter("today", new Date()).
                setParameter("organisation", organisation).getResultList();
    }

    public List<ActivityClass> findAllActiveClassesOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT a FROM ActivityClass a WHERE a.status = :activeStatus AND a.organisation = :organisation ORDER BY a.endDate ASC").
                setParameter("activeStatus", Status.ACTIVATED).
                setParameter("organisation", organisation).getResultList();
    }

    public ActivityClass findClassOfOrganisation(long classId, Organisation organisation) {
        return findUniqueResult(em.createQuery("SELECT a FROM ActivityClass a WHERE a.id = :classId AND a.organisation = :organisation ORDER BY a.endDate ASC").
                setParameter("classId", classId).
                setParameter("organisation", organisation).getResultList());
    }
}
