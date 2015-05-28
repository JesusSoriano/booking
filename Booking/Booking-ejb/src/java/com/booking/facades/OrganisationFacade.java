package com.booking.facades;

import com.booking.entities.Organisation;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Jesús Soriano
 */
@Stateless
public class OrganisationFacade extends AbstractFacade<Organisation> {
    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public OrganisationFacade() {
        super(Organisation.class);
    }
    
    public Organisation getDefaultOrganisation () {
        // Única organización de momento
        return findUniqueResult(em.createQuery("SELECT o FROM Organisation o").getResultList());
    }
    
}
