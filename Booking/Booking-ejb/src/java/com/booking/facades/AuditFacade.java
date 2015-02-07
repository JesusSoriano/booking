package com.booking.facades;

import com.booking.entities.Audit;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import java.util.Date;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Jesus Soriano
 */
@Stateless
public class AuditFacade extends AbstractFacade<Audit> {

    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AuditFacade() {
        super(Audit.class);
    }

    public Audit createAudit (AuditType action, User user, String ip) {
        Audit audit = new Audit();
        audit.setCreatedDate(new Date());
        audit.setAction(action);
        audit.setUser(user);
        audit.setIp(ip);
        
        create(audit);
        return audit;
    }
}
