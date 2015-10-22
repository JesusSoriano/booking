package com.booking.facades;

import com.booking.entities.GroupSchedule;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Jesús Soriano
 */
@Stateless
public class GroupScheduleFacade extends AbstractFacade<GroupSchedule> {

    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public GroupScheduleFacade() {
        super(GroupSchedule.class);
    }

}
