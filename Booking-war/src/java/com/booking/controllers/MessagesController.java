package com.booking.controllers;

import com.booking.entities.Message;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.facades.MessageFacade;
import com.booking.facades.UserFacade;
import com.booking.util.FacesUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

/**
 *
 * @author Jesús Soriano
 */
@ManagedBean
@ViewScoped
public class MessagesController implements Serializable {

    @EJB
    private UserFacade userFacade;
    @EJB
    private MessageFacade messageFacade;

    private List<Message> allReceivedMessages;
    private List<Message> allSentMessages;
    private int allUnreadMessagesCount;
    private List<SelectItem> allUsersItems;
    private String receiverEmail;
    private String subject;
    private String messageBody;
    private User loggedUser;

    /**
     * Creates a new instance of MessagesController
     */
    public MessagesController() {
    }

    @PostConstruct
    public void postInitialize() {

        loggedUser = FacesUtil.getCurrentUser();
        allUsersItems = new ArrayList<>();
        Organisation organisation = FacesUtil.getCurrentOrganisation();

        userFacade.findAllUsersOfOrganisation(organisation).forEach((user) -> {
            if (!loggedUser.equals(user)) {
                allUsersItems.add(new SelectItem(user.getEmail(), user.getFullName()));
            }
        });

        String receiverParam = FacesUtil.getParameter("user");
        if (receiverParam != null) {
            receiverEmail = userFacade.find(Long.valueOf(receiverParam)).getEmail();
        }

        String messageId = FacesUtil.getParameter("reply");
        if (messageId != null) {
            Message replyMsg = messageFacade.find(Long.valueOf(messageId));
            if (replyMsg != null) {
                receiverEmail = replyMsg.getSender().getEmail();
                subject = "Re: " + replyMsg.getSubject();
                messageBody = "<br/><br/><div style='color: #656565'>-------------------------------------------------------<br/><br/>" + replyMsg.getBody() + "</div>";
            }
        }

        FacesUtil.removeSessionAttribute("messageSess");
        FacesUtil.removeSessionAttribute("mailbox");
    }

    public String sendMessage() {
        User receiver = userFacade.findUserByEmail(receiverEmail);
        Message message = messageFacade.sendMessage(subject, messageBody, receiver, loggedUser);
        FacesUtil.setSessionAttribute("messageSess", message);
        FacesUtil.setSessionAttribute("mailbox", "sent");
        return "view-message.xhtml?faces-redirect=true";
    }

    public String cancelMessage() {
        return "mailbox.xhtml?faces-redirect=true";
    }

    public String viewMessage(Message message, boolean read, String mailbox) {
        messageFacade.setReadMessage(message, read);
        FacesUtil.setSessionAttribute("messageSess", message);
        FacesUtil.setSessionAttribute("mailbox", mailbox);
        return "view-message.xhtml?faces-redirect=true";
    }

    public int getAllUnreadMessagesCount() {
        allUnreadMessagesCount = messageFacade.getUnreadMessages(loggedUser).size();
        return allUnreadMessagesCount;
    }

    public List<SelectItem> getAllUsersItems() {
        return allUsersItems;
    }

    public List<Message> getAllReceivedMessages() {
        allReceivedMessages = messageFacade.findAllReceivedMessages(loggedUser);
        return allReceivedMessages;
    }

    public List<Message> getAllSentMessages() {
        allSentMessages = messageFacade.findAllSentMessages(loggedUser);
        return allSentMessages;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
