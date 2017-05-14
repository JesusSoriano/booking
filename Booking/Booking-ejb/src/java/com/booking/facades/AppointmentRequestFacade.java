package com.booking.facades;

import com.booking.entities.Appointment;
import com.booking.entities.AppointmentRequest;
import com.booking.entities.User;
import com.booking.enums.RequestStatus;
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

    public AppointmentRequest findCurrentRequestOfAppointment(Appointment appointment) {
        return findUniqueResult(em.createQuery("SELECT a FROM AppointmentRequest a WHERE a.appointment = :appointment AND a.status = :pending ORDER BY a.createdDate ASC").
                setParameter("pending", RequestStatus.PENDING).
                setParameter("appointment", appointment).getResultList());
    }

    public AppointmentRequest findCurrentRequestOfAppointmentForUser(Appointment appointment, User user) {
        return findUniqueResult(em.createQuery("SELECT a FROM AppointmentRequest a WHERE a.appointment = :appointment AND a.status = :pending AND a.requestUser = :user ORDER BY a.createdDate ASC").
                setParameter("pending", RequestStatus.PENDING).
                setParameter("appointment", appointment).
                setParameter("user", user).getResultList());
    }

    public AppointmentRequest findAcceptedRequestOfAppointmentForUser(Appointment appointment, User user) {
        return findUniqueResult(em.createQuery("SELECT a FROM AppointmentRequest a WHERE a.appointment = :appointment AND a.status = :accepted AND a.requestUser = :user ORDER BY a.createdDate ASC").
                setParameter("accepted", RequestStatus.ACCEPTED).
                setParameter("appointment", appointment).
                setParameter("user", user).getResultList());
    }

    public boolean existsRequest(User user, Appointment appointment) {
        List<AppointmentRequest> appointmentRequests = em.createQuery("SELECT a FROM AppointmentRequest a WHERE a.requestUser = :user AND a.appointment = :appointment").
                setParameter("user", user).
                setParameter("appointment", appointment).getResultList();
        for (AppointmentRequest ar : appointmentRequests) {
            if (ar.getStatus().equals(RequestStatus.ACCEPTED) || ar.getStatus().equals(RequestStatus.PENDING)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRequestAvailable(Appointment appointment) {
        AppointmentRequest appointmentRequest = findUniqueResult(em.createQuery("SELECT a FROM AppointmentRequest a WHERE a.appointment = :appointment AND a.status = :pending ORDER BY a.createdDate ASC").
                setParameter("pending", RequestStatus.PENDING).
                setParameter("appointment", appointment).getResultList());
        
        return appointmentRequest == null;
    }
}
