
package com.booking.facades;

import com.booking.entities.Appointment;
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
public class AppointmentFacade extends AbstractFacade<Appointment> {
    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AppointmentFacade() {
        super(Appointment.class);
    }
    
    public Appointment createNewAppointment(Service service, String description, Date date, Date startingTime, Date endingTime, float price, Organisation organisation) {
        Appointment appointment = new Appointment();
        appointment.setCreatedDate(new Date());
        appointment.setService(service);
        appointment.setDate(date);
        appointment.setStartTime(startingTime);
        appointment.setEndTime(endingTime);
        appointment.setDescription(description);
        appointment.setPrice(price);
        appointment.setAvailable(true);
        appointment.setOrganisation(organisation);
        appointment.setStatus(Status.ACTIVATED);
        create(appointment);
        
        return appointment;
    }
    
    public Appointment updateAppointment(Appointment appointment, Service service, String description, Date date, Date startingTime, Date endingTime, float price) {
        appointment.setService(service);
        appointment.setDate(date);
        appointment.setStartTime(startingTime);
        appointment.setEndTime(endingTime);
        appointment.setDescription(description);
        appointment.setPrice(price);
        edit(appointment);
        
        return appointment;
    }
    
    public Appointment duplicateAppointment(Appointment appointment) {
        Appointment duplicatedAppointment = new Appointment();
        duplicatedAppointment.setService(appointment.getService());
        duplicatedAppointment.setCreatedDate(new Date());
        duplicatedAppointment.setDate(appointment.getDate());
        duplicatedAppointment.setStartTime(appointment.getStartTime());
        duplicatedAppointment.setEndTime(appointment.getEndTime());
        duplicatedAppointment.setDescription(appointment.getDescription());
        duplicatedAppointment.setPrice(appointment.getPrice());
        duplicatedAppointment.setAvailable(true);
        duplicatedAppointment.setStatus(Status.ACTIVATED);
        duplicatedAppointment.setOrganisation(appointment.getOrganisation());
        create(duplicatedAppointment);
        
        return duplicatedAppointment;
    }

    public void activateAppointment(Appointment appointment) {
        appointment.setStatus(Status.ACTIVATED);
        edit(appointment);
    }

    public void deactivateAppointment(Appointment appointment) {
        appointment.setStatus(Status.SUSPENDED);
        edit(appointment);
    }

    public void makeAppointmentAbailable(Appointment appointment) {
        appointment.setAvailable(true);
        edit(appointment);
    }

    public void makeAppointmentUnabailable(Appointment appointment) {
        appointment.setAvailable(false);
        edit(appointment);
    }
    
    
    public Appointment findAppointmentOfOrganisation(long appointmentId, Organisation organisation) {
        return findUniqueResult(em.createQuery("SELECT a FROM Appointment a WHERE a.id = :appointmentId AND a.organisation = :organisation ORDER BY a.date ASC").
                setParameter("appointmentId", appointmentId).
                setParameter("organisation", organisation).getResultList());
    }
    
    public List<Appointment> findAllCurrentAppointmentsOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT a FROM Appointment a WHERE a.date > :today AND a.organisation = :organisation ORDER BY a.date ASC").
                setParameter("today", new Date()).
                setParameter("organisation", organisation).getResultList();
    }
    
    public List<Appointment> findAllPastAppointmentsOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT a FROM Appointment a WHERE a.date < :today AND a.organisation = :organisation ORDER BY a.date ASC").
                setParameter("today", new Date()).
                setParameter("organisation", organisation).getResultList();
    }
    
    public List<Appointment> findAllCurrentAppointmentsOfService(Service service, Organisation organisation) {
        return em.createQuery("SELECT a FROM Appointment a WHERE a.service = :service AND a.date > :today AND a.organisation = :organisation ORDER BY a.date ASC").
                setParameter("service", service).
                setParameter("today", new Date()).
                setParameter("organisation", organisation).getResultList();
    }
    
    public List<Appointment> findAllPastAppointmentsOfService(Service service, Organisation organisation) {
        return em.createQuery("SELECT a FROM Appointment a WHERE a.service = :service AND a.date < :today AND a.organisation = :organisation ORDER BY a.date ASC").
                setParameter("service", service).
                setParameter("today", new Date()).
                setParameter("organisation", organisation).getResultList();
    }
}
