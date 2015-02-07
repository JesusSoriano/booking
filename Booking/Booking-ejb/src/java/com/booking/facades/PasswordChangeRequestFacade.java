package com.booking.facades;

import com.booking.entities.PasswordChangeRequest;
import com.booking.entities.User;
import java.util.Date;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Jesus Soriano
 */
@Stateless
public class PasswordChangeRequestFacade extends AbstractFacade<PasswordChangeRequest> {

    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PasswordChangeRequestFacade() {
        super(PasswordChangeRequest.class);
    }

    public PasswordChangeRequest createNewPasswordChangeRequest(User user) {
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setCreatedDate(new Date());
        passwordChangeRequest.setUser(user);

        create(passwordChangeRequest);
        return passwordChangeRequest;
    }

    public PasswordChangeRequest findRequestFromHashId(String hashId) {
        return findUniqueResult(em.createQuery("SELECT p FROM PasswordChangeRequest p WHERE p.hashId = :hashId").
                setParameter("hashId", hashId).getResultList());
    }

    public PasswordChangeRequest findLastRequestFromUser(User user) {
        return findUniqueResult(em.createQuery("SELECT p FROM PasswordChangeRequest p WHERE p.user = :user ORDER BY p.createdDate DESC").
                setParameter("user", user).getResultList());
    }
    
    public void setExpiredRequest (PasswordChangeRequest passwordChangeRequest) {
        passwordChangeRequest.setExpired(true);
        edit(passwordChangeRequest);
    }
}
