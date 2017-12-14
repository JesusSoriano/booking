package com.booking.facades;

import com.booking.entities.Notification;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.NotificationType;
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

    public Notification createNotification(NotificationType notificationType, User notificationUser, User actionUser, long objectId, Organisation organisation) {
        Notification notification = new Notification();
        notification.setCreatedDate(new Date());
        notification.setOrganisation(organisation);
        notification.setNotificationType(notificationType);
        notification.setNotificationUser(notificationUser);
        notification.setActiontionUser(actionUser);
        notification.setObjectId(objectId);
        notification.setChecked(Boolean.FALSE);

        create(notification);
        return notification;
    }

    public void createNotificationForAdmins(NotificationType notificationType, List<User> notificationAdmins, User actionUser, long objectId, Organisation organisation) {
        notificationAdmins.forEach((admin) -> {
            createNotification(notificationType, admin, actionUser, objectId, organisation);
        });
    }
    
    public void setNotificationCheck(Notification notification, boolean checked) {
        notification.setChecked(checked);
        edit(notification);
    }
    
    public List<Notification> findAllNotificationsOfUser(User user) {
        return em.createQuery("SELECT n FROM Notification n WHERE n.notificationUser = :user ORDER BY n.createdDate DESC").
                setParameter("user", user).getResultList();
    }
    
    public int findUserUncheckedNotificationsNumber(User user) {
        return em.createQuery("SELECT n FROM Notification n WHERE n.notificationUser = :user AND n.checked = :false").
                setParameter("user", user).
                setParameter("false", false).getResultList().size();
    }
    
    public List<Notification> findAllNotificationsOfTypeOfUser(User user, NotificationType notificationType) {
        return em.createQuery("SELECT n FROM Notification n WHERE n.notificationUser = :user AND n.notificationType = :notificationType ORDER BY n.createdDate DESC").
                setParameter("user", user).
                setParameter("notificationType", notificationType).getResultList();
    }
}
