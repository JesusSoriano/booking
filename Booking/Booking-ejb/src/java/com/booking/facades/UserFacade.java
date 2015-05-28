package com.booking.facades;

import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.entities.UserRole;
import com.booking.enums.Role;
import com.booking.enums.Status;
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
public class UserFacade extends AbstractFacade<User> {

    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UserFacade() {
        super(User.class);
    }

    public User createNewUser(String firstname, String lastname, String email,
            String phone, String password, Role userRole, Organisation organisation) {
        User user = new User();
        user.setCreatedDate(new Date());
        user.setOrganisation(organisation);
        user.setFirstName(firstname);
        user.setLastName(lastname);
        user.setEmail(email);
        user.setPassword(password);
        user.setAccountActive(true);
        user.setTermsVersionAccepted(false);
        user.setPhone(phone);

        UserRole role = new UserRole();
        role.setUser(user);
        role.setRole(userRole);
        em.persist(role);

        user.setUserRole(role);
        create(user);
        return user;
    }

    public User setTermsVersionAccepted(User user) {
        user.setTermsVersionAccepted(true);
        edit(user);

        return user;
    }

    public void activateUser(User user) {
        user.setStatus(Status.ACTIVATED);
        edit(user);
    }

    public void deactivateUser(User user) {
        user.setStatus(Status.SUSPENDED);
        edit(user);
    }

    public User findUserByEmail(String email) {
        return findUniqueResult(em.createQuery("SELECT u FROM User u WHERE u.email = :email").
                setParameter("email", email).getResultList());
    }

    public User findUserFromHashId(String hashId) {
        return findUniqueResult(em.createQuery("SELECT u FROM User u WHERE u.hashId = :hashId").
                setParameter("hashId", hashId).getResultList());
    }

    public List<User> findAllClientsForOrganisation() {
        return em.createQuery("SELECT u FROM User u WHERE u.userRole.role = :client").
                setParameter("client", Role.USER).getResultList();
    }
    
    public List<User> findAllAdminsForOrganisation() {
        return em.createQuery("SELECT u FROM User u WHERE u.userRole.role = :client").
                setParameter("client", Role.ADMIN).getResultList();
    }
}
