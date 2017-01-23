package com.booking.facades;

import com.booking.entities.ActivityGroup;
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

    public Booking createNewBooking(User user, ActivityGroup group) {
        Booking booking = new Booking();
        booking.setGroupUser(user);
        booking.setActivityGroup(group);
        booking.setCreatedDate(new Date());
        create(booking);

        return booking;
    }

    public boolean removeBooking(User user, ActivityGroup group) {
        Booking groupUser = findUniqueResult(em.createQuery("SELECT b FROM Booking b WHERE b.groupUser = :user AND b.activityGroup = :group").
                setParameter("user", user).
                setParameter("group", group).getResultList());

        if (groupUser != null) {
            remove(groupUser);
            return true;
        }
        return false;
    }
    
    public boolean existsBooking(User user, ActivityGroup group) {
        Booking booking = findUniqueResult(em.createQuery("SELECT b FROM Booking b WHERE b.groupUser = :user AND b.activityGroup = :group").
                setParameter("user", user).
                setParameter("group", group).getResultList());
        return (booking != null);
    }
    
    public List<User> findAllBookedUsersOfGroup(ActivityGroup group) {
        return em.createQuery("SELECT b.groupUser FROM Booking b WHERE b.activityGroup = :group").
                setParameter("group", group).getResultList();
    }
}
