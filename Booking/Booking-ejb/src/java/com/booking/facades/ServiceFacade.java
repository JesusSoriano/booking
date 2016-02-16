package com.booking.facades;

import com.booking.entities.Organisation;
import com.booking.entities.Service;
import com.booking.enums.Status;
import com.booking.exceptions.ServiceAlreadyExistsException;
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

//        if (findServiceByName(name, organisation) != null) {
//            throw new ServiceAlreadyExistsException("Lo sentimos, no ha sido posible crear el nuevo servicio: Nombre existente.");
//        }

        Service service = new Service();
        service.setName(name);
        service.setDescription(description);
        service.setOrganisation(organisation);
        service.setCreatedDate(new Date());
        service.setStatus(Status.ACTIVATED);
        create(service);

        return service;
    }

    public Service updateService(String oldName, String newName, String description, Organisation organisation) {
        System.out.println("----- UPDATE");
        
        Service service = findServiceByName(oldName, organisation);
        System.out.println("----- service: " + service);
        if (service != null) {
        System.out.println("----- service name: " + service.getName());
            service.setName(newName);
        System.out.println("----- service new name: " + service.getName());
            service.setDescription(description);
            em.merge(service);
        }

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

    public Service findServiceByName(String serviceName, Organisation organisation) {
        return findUniqueResult(em.createQuery("SELECT s FROM Service s WHERE s.organisation = :organisation AND s.name = :serviceName ORDER BY s.name ASC").
                setParameter("organisation", organisation).
                setParameter("serviceName", serviceName).getResultList());
    }
}
