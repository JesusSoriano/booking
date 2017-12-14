package com.booking.facades;

import com.booking.entities.Audit;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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

    public List<Audit> findAllAudits(Date fromDate, Date toDate, Organisation organisation, AuditType selectedAuditType, User selectedUser, Role role) {

        boolean visibleUser = false;
        String sqlString = "SELECT a FROM Audit a WHERE a.organisation = :organisation AND a.createdDate BETWEEN :fromDate AND :toDate";
        if (selectedAuditType != null) {
            sqlString += " AND a.action = :selectedAuditType";
        }
        if (selectedUser != null) {
        // Check if selected user is visible by the user regarding the user role (just in case user id is manually introduced in the url)
            visibleUser = true;
            Role userRole = selectedUser.getUserRole().getRole();
            if (userRole != Role.USER && userRole != role) {
                if (userRole == Role.SUPER_ADMIN) {
                    visibleUser = false;
                }
            }
        }
        if (visibleUser) {
            sqlString += " AND a.user = :selectedUser";
        } else {
            sqlString += " AND a.user.userRole.role IN :roleList";
        }
        sqlString += " ORDER BY a.createdDate DESC";

        Query sqlQuery = em.createQuery(sqlString).
                setParameter("organisation", organisation).
                setParameter("fromDate", fromDate).
                setParameter("toDate", toDate);
        if (selectedAuditType != null) {
            sqlQuery.setParameter("selectedAuditType", selectedAuditType);
        }
        if (visibleUser) {
            sqlQuery.setParameter("selectedUser", selectedUser);
        } else {
            List<Role> roleList = new ArrayList<>();
            roleList.add(Role.USER);
            if (!role.equals(Role.USER)) {
                roleList.add(Role.ADMIN);
            }
            if (role.equals(Role.SUPER_ADMIN)) {
                roleList.add(Role.SUPER_ADMIN);
            }
            sqlQuery.setParameter("roleList", roleList);
        }

        return sqlQuery.getResultList();
    }
}
