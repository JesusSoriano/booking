package com.booking.facades;

import com.booking.entities.Address;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.entities.UserRole;
import com.booking.enums.Role;
import com.booking.enums.Status;
import java.util.ArrayList;
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

    public User createNewUser(String firstname, String firstLastName, String secondLastName, String email,
            String password, String phone, String addressLine, String addressLine2, String city, String country,
            String postcode, Role userRole, Organisation organisation) {

        Address address = new Address();
        address.setAddressLine(addressLine);
        address.setAddressLine2(addressLine2);
        address.setCity(city);
        address.setCountry(country);
        address.setPostcode(postcode);
        em.persist(address);

        User user = new User();
        user.setCreatedDate(new Date());
        user.setOrganisation(organisation);
        user.setFirstName(firstname);
        user.setFirstLastName(firstLastName);
        user.setSecondLastName(secondLastName);
        user.setEmail(email);
        user.setPassword(password);
        user.setStatus(Status.SUSPENDED);
        user.setPhone(phone);
        user.setAddress(address);

        UserRole role = new UserRole();
        role.setUser(user);
        role.setRole(userRole);
        em.persist(role);

        user.setUserRole(role);
        create(user);
        return user;
    }

    public void editUserProfile(User user, String firstName, String firstLastName, String secondLastName, String email,
            String phone, String addressLine, String addressLine2, String city, String country, String postcode) {
        
        user.setFirstName(firstName);
        user.setFirstLastName(firstLastName);
        user.setSecondLastName(secondLastName);
        user.setPhone(phone);
        user.getAddress().setAddressLine(addressLine);
        user.getAddress().setAddressLine2(addressLine2);
        user.getAddress().setCity(city);
        user.getAddress().setCountry(country);
        user.getAddress().setPostcode(postcode);
        
        edit(user);
    }

    public void activateUser(User user) {
        user.setStatus(Status.ACTIVATED);
        edit(user);
    }

    public void deactivateUser(User user) {
        user.setStatus(Status.SUSPENDED);
        edit(user);
    }

    public void changeUserLanguage(User user, String language) {
        user.setApplicationLanguage(language);
        edit(user);
    }

    public List<User> findAllUsersOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT u FROM User u WHERE u.organisation = :organisation ORDER BY u.firstName ASC, u.firstLastName ASC").
                setParameter("organisation", organisation).getResultList();
    }

    public User findUserOfOrganisation(long userId, Organisation organisation) {
        return findUniqueResult(em.createQuery("SELECT u FROM User u WHERE u.id = :userId AND u.organisation = :organisation ORDER BY u.firstName ASC, u.firstLastName ASC").
                setParameter("userId", userId).
                setParameter("organisation", organisation).getResultList());
    }

    public User findUserByEmail(String email) {
        return findUniqueResult(em.createQuery("SELECT u FROM User u WHERE u.email = :email").
                setParameter("email", email).getResultList());
    }

    public User findUserOfOrganisationByEmail(String email, Organisation organisation) {
        return findUniqueResult(em.createQuery("SELECT u FROM User u WHERE u.email = :email AND u.organisation = :organisation").
                setParameter("email", email).
                setParameter("organisation", organisation).getResultList());
    }

   /* public User findUserFromHashId(String hashId, Organisation organisation) {
        return findUniqueResult(em.createQuery("SELECT u FROM User u WHERE u.hashId = :hashId").
                setParameter("hashId", hashId).getResultList());
    } */

    public List<User> findAllClientsOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT u FROM User u WHERE u.organisation = :organisation AND u.userRole.role = :client ORDER BY u.firstName ASC, u.firstLastName ASC").
                setParameter("organisation", organisation).
                setParameter("client", Role.USER).getResultList();
    }

    public List<User> findAllAdminsOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT u FROM User u WHERE u.organisation = :organisation AND u.userRole.role = :client ORDER BY u.firstName ASC, u.firstLastName ASC").
                setParameter("organisation", organisation).
                setParameter("client", Role.ADMIN).getResultList();
    }

    public List<User> findAllActiveAdminsOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT u FROM User u WHERE u.organisation = :organisation AND u.userRole.role = :admin AND u.status = :activated ORDER BY u.firstName ASC, u.firstLastName ASC").
                setParameter("organisation", organisation).
                setParameter("admin", Role.ADMIN).
                setParameter("activated", Status.ACTIVATED).getResultList();
    }

    public List<User> findAllAdminsAndClientsOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT u FROM User u WHERE u.organisation = :organisation AND u.userRole.role IN :users ORDER BY u.firstName ASC, u.firstLastName ASC").
                setParameter("organisation", organisation).
                setParameter("users", new ArrayList<Role>() {
                    {
                        add(Role.ADMIN);
                        add(Role.USER);
                    }
                }).getResultList();
    }

    public List<User> findAllActiveAdminsAndClientsOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT u FROM User u WHERE u.organisation = :organisation AND u.userRole.role IN :users AND u.status = :activated ORDER BY u.firstName ASC, u.firstLastName ASC").
                setParameter("organisation", organisation).
                setParameter("users", new ArrayList<Role>() {
                    {
                        add(Role.ADMIN);
                        add(Role.USER);
                    }
                }).
                setParameter("activated", Status.ACTIVATED).getResultList();
    }
}
