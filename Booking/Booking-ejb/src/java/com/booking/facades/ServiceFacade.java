package com.booking.facades;

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
public class ServiceFacade extends AbstractFacade<Service> {
    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ServiceFacade() {
        super(Service.class);
    }
    
    public Service createNewService(String name, String description, Organisation organisation) {
        
        Service service = new Service();
        service.setName(name);
        service.setDescription(description);
        service.setOrganisation(organisation);
        service.setCreatedDate(new Date());
        service.setStatus(Status.ACTIVATED);
        create(service);
        
        return service;
    }
    
    public void activateService(Service service) {
        service.setStatus(Status.ACTIVATED);
        edit(service);
    }

    public void deactivateService(Service service) {
        service.setStatus(Status.SUSPENDED);
        edit(service);
    }

    public List<Service> findAllServicesOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT s FROM Service s WHERE s.organisation = :organisation ORDER BY s.name ASC").
                setParameter("organisation", organisation).getResultList();
    }
}
