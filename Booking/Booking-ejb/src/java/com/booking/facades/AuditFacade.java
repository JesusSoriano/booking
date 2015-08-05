package com.booking.facades;

import com.booking.entities.Audit;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import java.util.Date;
import java.util.List;
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

    public Audit createAudit(AuditType action, User user, String ip, long actionObjectId, Organisation organisation) {
        Audit audit = new Audit();
        audit.setCreatedDate(new Date());
        audit.setOrganisation(organisation);
        audit.setAction(action);
        audit.setUser(user);
        audit.setIp(ip);
        audit.setActionObjectId(actionObjectId);

        create(audit);
        return audit;
    }

    public List<Audit> findSuperAdminAudits(Date fromDate, Date toDate, Organisation organisation, AuditType selectedAuditType, User selectedUser) {
        if (selectedUser != null) {
            // TODO: realizados por selectedUser y los que la acción reacae en él
            // TODO: filtrar por selectedUser y selectedAuditType (una misma query con if's)
            return em.createQuery("SELECT a FROM Audit a WHERE a.organisation = :organisation AND a.user.userRole.role = :superAdmin AND a.createdDate BETWEEN :fromDate AND :toDate AND a.action = :selectedAuditType AND a.user = :selectedUser ORDER BY a.createdDate DESC").
                    setParameter("organisation", organisation).
                    setParameter("superAdmin", Role.SUPER_ADMIN).
                    setParameter("fromDate", fromDate).
                    setParameter("toDate", toDate).
                    setParameter("selectedAuditType", selectedAuditType).
                    setParameter("selectedUser", selectedUser).
                    getResultList();
        } else {
            return em.createQuery("SELECT a FROM Audit a WHERE a.organisation = :organisation AND a.user.userRole.role = :superAdmin AND a.createdDate BETWEEN :fromDate AND :toDate AND a.action = :selectedAuditType ORDER BY a.createdDate DESC").
                    setParameter("organisation", organisation).
                    setParameter("superAdmin", Role.SUPER_ADMIN).
                    setParameter("fromDate", fromDate).
                    setParameter("toDate", toDate).
                    setParameter("selectedAuditType", selectedAuditType).
                    getResultList();
        }
    }

    public List<Audit> findAdminAudits(Date fromDate, Date toDate, Organisation organisation, AuditType selectedAuditType, User selectedUser) {
        if (selectedUser != null) {
            // TODO: realizados por selectedUser y los que la acción reacae en él
            return em.createQuery("SELECT a FROM Audit a WHERE a.organisation = :organisation AND a.user.userRole.role = :superAdmin AND a.createdDate BETWEEN :fromDate AND :toDate AND a.action = :selectedAuditType AND a.user = :selectedUser ORDER BY a.createdDate DESC").
                    setParameter("organisation", organisation).
                    setParameter("superAdmin", Role.ADMIN).
                    setParameter("fromDate", fromDate).
                    setParameter("toDate", toDate).
                    setParameter("selectedAuditType", selectedAuditType).
                    setParameter("selectedUser", selectedUser).
                    getResultList();
        } else {
            return em.createQuery("SELECT a FROM Audit a WHERE a.organisation = :organisation AND a.user.userRole.role = :superAdmin AND a.createdDate BETWEEN :fromDate AND :toDate AND a.action = :selectedAuditType ORDER BY a.createdDate DESC").
                    setParameter("organisation", organisation).
                    setParameter("superAdmin", Role.SUPER_ADMIN).
                    setParameter("fromDate", fromDate).
                    setParameter("toDate", toDate).
                    setParameter("selectedAuditType", selectedAuditType).
                    getResultList();
        }
    }

    public List<Audit> findUserAudits(Date fromDate, Date toDate, Organisation organisation, AuditType selectedAuditType, User selectedUser) {
        if (selectedUser != null) {
            // TODO: realizados por selectedUser y los que la acción reacae en él
            return em.createQuery("SELECT a FROM Audit a WHERE a.organisation = :organisation AND a.createdDate BETWEEN :fromDate AND :toDate AND a.action = :selectedAuditType AND a.user = :selectedUser ORDER BY a.createdDate DESC").
                    setParameter("organisation", organisation).
                    setParameter("fromDate", fromDate).
                    setParameter("toDate", toDate).
                    setParameter("selectedAuditType", selectedAuditType).
                    setParameter("selectedUser", selectedUser).
                    getResultList();
        }
        return null;
    }
}
