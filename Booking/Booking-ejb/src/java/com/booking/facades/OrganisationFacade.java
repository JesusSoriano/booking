package com.booking.facades;

import com.booking.entities.Address;
import com.booking.entities.Organisation;
import com.booking.enums.Status;
import java.util.Date;
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

    public Organisation createNewOrganisation(String name, String email, String addressLine,
            String addressLine2, String city, String country, String postcode,
            String phone, String fax, String styleFile) {
        
        Address address = new Address();
        address.setAddressLine(addressLine);
        address.setAddressLine2(addressLine2);
        address.setCity(city);
        address.setCountry(country);
        address.setPostcode(postcode);
        em.merge(address);
        
        Organisation organisation = new Organisation();
        organisation.setName(name);
        organisation.setEmail(email);
        organisation.setAddress(address);
        organisation.setPhone(phone);
        organisation.setFax(fax);
        organisation.setStyleFile(styleFile);
        organisation.setCreatedDate(new Date());
        create(organisation);
        
        return organisation;
    }

    public void activateOrganisation(Organisation organisation) {
        organisation.setStatus(Status.ACTIVATED);
    }

    public void deactivateOrganisation(Organisation organisation) {
        organisation.setStatus(Status.SUSPENDED);
    }
    
    public Organisation getDefaultOrganisation() {
        // Única organización de momento
        return findUniqueResult(em.createQuery("SELECT o FROM Organisation o").getResultList());
    }

}
