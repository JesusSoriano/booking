package com.booking.facades;

import com.booking.entities.Notification;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.NotificationType;
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
public class NotificationFacade extends AbstractFacade<Notification> {

    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public NotificationFacade() {
        super(Notification.class);
    }

    public Notification createNotification(NotificationType notificationType, User notificationUser, long objectId, Organisation organisation) {
        Notification notification = new Notification();
        notification.setCreatedDate(new Date());
        notification.setOrganisation(organisation);
        notification.setNotificationType(notificationType);
        notification.setNotificationUser(notificationUser);
        notification.setObjectId(objectId);

        create(notification);
        return notification;
    }
    
    public List<Notification> findAllNotificationsOfUser(User user) {
        return em.createQuery("SELECT n FROM Notification n WHERE n.notificationUser = :user").
                setParameter("user", user).getResultList();
    }
    
    public List<Notification> findAllNotificationsOfTypeOfUser(User user, NotificationType notificationType) {
        return em.createQuery("SELECT n FROM Notification n WHERE n.notificationUser = :user AND n.notificationType = :notificationType").
                setParameter("user", user).
                setParameter("notificationType", notificationType).getResultList();
    }
}
