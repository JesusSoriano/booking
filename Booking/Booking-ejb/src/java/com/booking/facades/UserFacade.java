package com.booking.facades;

import com.booking.entities.User;
import com.booking.entities.UserRole;
import com.booking.enums.Roles;
import java.util.Date;
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
            String phone, String password, Roles userRole) {
        User user = new User();
        user.setCreatedDate(new Date());
        user.setFirstName(firstname);
        user.setLastName(lastname);
        user.setEmail(email);
        user.setPassword(password);
        user.setAccountActive(true);
        user.setTermsVersionAccepted(false);
        user.setPhone(phone);

        UserRole role = new UserRole();
        role.setUser(user);
        role.setUserRole(userRole);
        em.persist(role);

        user.setRole(role);
        create(user);
        return user;
    }
    
    public User setTermsVersionAccepted (User user) {
        user.setTermsVersionAccepted(true);
        edit(user);
        
        return user;
    }

    public User findUserByEmail(String email) {
        return findUniqueResult(em.createQuery("SELECT u FROM User u WHERE u.email = :email").
                setParameter("email", email).getResultList());   
    }
    
    public User findUserFromHashId(String hashId) {
        return findUniqueResult(em.createQuery("SELECT u FROM User u WHERE u.hashId = :hashId").
                setParameter("hashId", hashId).getResultList());
    }
}
