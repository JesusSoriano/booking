package com.booking.facades;

import com.booking.entities.ActivityClass;
import com.booking.entities.Booking;
import com.booking.entities.User;
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
public class BookingFacade extends AbstractFacade<Booking> {

    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public BookingFacade() {
        super(Booking.class);
    }

    public Booking createNewBooking(User user, ActivityClass activityClass) {
        if (existsBooking(user, activityClass)) {
            return null;
        }
        Booking booking = new Booking();
        booking.setClassUser(user);
        booking.setActivityClass(activityClass);
        booking.setCreatedDate(new Date());
        create(booking);

        return booking;
    }

    public boolean removeBooking(User user, ActivityClass activityClass) {
        Booking userBooking = findUniqueResult(em.createQuery("SELECT b FROM Booking b WHERE b.classUser = :user AND b.activityClass = :activityClass").
                setParameter("user", user).
                setParameter("activityClass", activityClass).getResultList());

        if (userBooking != null) {
            remove(userBooking);
            return true;
        }
        return false;
    }

    public boolean existsBooking(User user, ActivityClass activityClass) {
        Booking booking = findUniqueResult(em.createQuery("SELECT b FROM Booking b WHERE b.classUser = :user AND b.activityClass = :activityClass").
                setParameter("user", user).
                setParameter("activityClass", activityClass).getResultList());
        return (booking != null);
    }

    public List<User> findAllBookedUsersOfClass(ActivityClass activityClass) {
        return em.createQuery("SELECT b.classUser FROM Booking b WHERE b.activityClass = :activityClass ORDER BY b.classUser.firstName, b.classUser.firstLastName ASC").
                setParameter("activityClass", activityClass).getResultList();
    }

    public List<ActivityClass> findAllCurrentClassesOfUser(User user) {
        return em.createQuery("SELECT b.activityClass FROM Booking b WHERE b.classUser = :user AND b.activityClass.endDate > :today ORDER BY b.activityClass.endDate ASC").
                setParameter("today", new Date()).
                setParameter("user", user).getResultList();
    }

    public List<ActivityClass> findAllPastClassesOfUser(User user) {
        return em.createQuery("SELECT b.activityClass FROM Booking b WHERE b.classUser = :user AND b.activityClass.endDate < :today ORDER BY b.activityClass.endDate ASC").
                setParameter("today", new Date()).
                setParameter("user", user).getResultList();
    }
}
