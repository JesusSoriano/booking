package com.booking.facades;

import com.booking.entities.Announcement;
import com.booking.entities.Organisation;
import com.booking.enums.Status;
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
public class AnnouncementFacade extends AbstractFacade<Announcement> {

    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AnnouncementFacade() {
        super(Announcement.class);
    }

    public Announcement createNewAnnouncement(String title, String text, Organisation organisation) {

        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setText(text);
        announcement.setOrganisation(organisation);
        announcement.setCreatedDate(new Date());
        announcement.setStatus(Status.ACTIVATED);
        create(announcement);

        return announcement;
    }

    public Announcement updateAnnouncement(Announcement announcement, String newTitle, String text, Organisation organisation) {

        announcement.setTitle(newTitle);
        announcement.setText(text);
        edit(announcement);

        return announcement;
    }

    public void activateAnnouncement(Announcement announcement) {
        announcement.setStatus(Status.ACTIVATED);
        edit(announcement);
    }

    public void deactivateAnnouncement(Announcement announcement) {
        announcement.setStatus(Status.SUSPENDED);
        edit(announcement);
    }

    public List<Announcement> findAllAnnouncementsOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT a FROM Announcement a WHERE a.organisation = :organisation ORDER BY a.createdDate DESC").
                setParameter("organisation", organisation).getResultList();
    }

    public List<Announcement> findAllActiveAnnouncementsOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT a FROM Announcement a WHERE a.organisation = :organisation AND a.status = :statusActive ORDER BY a.createdDate DESC").
                setParameter("statusActive", Status.ACTIVATED).
                setParameter("organisation", organisation).getResultList();
    }
}
