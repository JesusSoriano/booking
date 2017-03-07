
package com.booking.facades;

import com.booking.entities.ActivityClass;
import com.booking.entities.ClassDay;
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
public class ClassDayFacade extends AbstractFacade<ClassDay> {
    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ClassDayFacade() {
        super(ClassDay.class);
    }
    
    
    
    public ClassDay createNewClassDay (ActivityClass activityClass, String description, Date startDate, Date endDate) {
        
        ClassDay classDay = new ClassDay();
        classDay.setActivityClass(activityClass);
        classDay.setDescription(description);
        classDay.setStatus(Status.ACTIVATED);
        classDay.setStartDate(startDate);
        classDay.setEndDate(endDate);
        create(classDay);
        
        return classDay;
    }
    
    public ClassDay updateClassDay (ClassDay classDay, ActivityClass activityClass, String description, Date startDate, Date endDate) {
        
        classDay.setActivityClass(activityClass);
        classDay.setDescription(description);
        classDay.setStartDate(startDate);
        classDay.setEndDate(endDate);
        edit(classDay);
        
        return classDay;
    }
    
    public void activateClass(ClassDay classDay) {
        classDay.setStatus(Status.ACTIVATED);
        edit(classDay);
    }
    
    public void deactivateClass(ClassDay classDay) {
        classDay.setStatus(Status.SUSPENDED);
        edit(classDay);
    }

    
    public List<ClassDay> findAllDaysOfClass(ActivityClass activityClass) {
        return em.createQuery("SELECT d FROM ClassDay d WHERE d.activityClass = :activityClass ORDER BY d.startDate ASC").
                setParameter("activityClass", activityClass).getResultList();
    }
    
    public List<ClassDay> findAllActiveDaysOfClass(ActivityClass activityClass) {
        return em.createQuery("SELECT d FROM ClassDay d WHERE d.activityClass = :activityClass AND d.status = :activeStatus ORDER BY d.startDate ASC").
                setParameter("activityClass", activityClass).
                setParameter("activeStatus", Status.ACTIVATED).getResultList();
    }
}
