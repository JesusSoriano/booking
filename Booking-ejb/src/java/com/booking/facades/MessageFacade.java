package com.booking.facades;

import com.booking.entities.Message;
import com.booking.entities.User;
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
public class MessageFacade extends AbstractFacade<Message> {

    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager entityManager;
    
    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    public MessageFacade() {
        super(Message.class);
    }

    public Message sendMessage(String subject, String body, User receiver, User sender) {
        Message message = new Message();
        message.setSubject(subject);
        message.setBody(body);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSentDate(new Date());
        create(message);
        
        return message;
    }
    
    public void setReadMessage (Message message, boolean read) {
        message.setStatus(read);
        edit(message);
    }

    public List<Message> findAllReceivedMessages(User user) {
        return (List<Message>) entityManager.createQuery("SELECT m FROM Message m WHERE m.receiver = :receiver ORDER BY m.sentDate DESC").
                setParameter("receiver", user).getResultList();
    }

    public List<Message> findAllSentMessages(User user) {
        return (List<Message>) entityManager.createQuery("SELECT m FROM Message m WHERE m.sender = :sender ORDER BY m.sentDate DESC").
                setParameter("sender", user).getResultList();
    }

    public List<Message> getUnreadMessages(User currentUser) {
        return entityManager.createQuery("SELECT m FROM Message m WHERE m.receiver = :receiver AND m.status=false ORDER BY m.sentDate DESC").
                setParameter("receiver", currentUser).getResultList();
    }
}
