
package com.booking.facades;

import com.booking.entities.Appointment;
import com.booking.entities.AppointmentRequest;
import com.booking.entities.Organisation;
import com.booking.entities.Service;
import com.booking.entities.User;
import com.booking.enums.RequestStatus;
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
public class AppointmentRequestFacade extends AbstractFacade<AppointmentRequest> {
    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AppointmentRequestFacade() {
        super(AppointmentRequest.class);
    }
    
    public AppointmentRequest createNewAppointmentRequest(Appointment appointment, User requestUser, String comments) {
        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setCreatedDate(new Date());
        appointmentRequest.setAppointment(appointment);
        appointmentRequest.setRequestUser(requestUser);
        appointmentRequest.setComments(comments);
        appointmentRequest.setStatus(RequestStatus.PENDING);
        create(appointmentRequest);
        
        return appointmentRequest;
    }
    
    public AppointmentRequest updateRequestStatus(AppointmentRequest appointmentRequest, RequestStatus status, String statusComment) {
        appointmentRequest.setStatus(status);
        appointmentRequest.setStatusComment(statusComment);
        edit(appointmentRequest);
        
        return appointmentRequest;
    }
    
    public List<AppointmentRequest> findAllRequestsOfAppointment(Appointment appointment) {
        return em.createQuery("SELECT a FROM AppointmentRequest a WHERE a.appointment = :appointment ORDER BY a.createdDate ASC").
                setParameter("appointment", appointment).getResultList();
    }
}
