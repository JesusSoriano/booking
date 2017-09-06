package com.booking.facades;

import com.booking.entities.Document;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.DocumentType;
import com.booking.util.FileService;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Jesús Soriano
 */
@Stateless
public class DocumentFacade extends AbstractFacade<Document> {

    @PersistenceContext(unitName = "Booking-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DocumentFacade() {
        super(Document.class);
    }

    public Document uploadDocument(Organisation organisation, String name, String fileName, String description, 
            User admin, FileService fileService, InputStream inputStream) {

        try {
            fileName = fileService.uploadDocument(fileName, inputStream, organisation);
            return createNewDocument(name, description, fileName, admin, organisation);
        } catch (Exception e) {
                Logger.getLogger(DocumentFacade.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public Document createNewDocument(String name, String description, String fileName, User admin, Organisation organisation) {

        Document document = new Document();
        document.setName(name);
        document.setDescription(description);
        document.setFileName(fileName);
        document.setAdmin(admin);
        document.setOrganisation(organisation);
        document.setCreatedDate(new Date());
        create(document);

        return document;
    }

    public Document updateDocument(Document document, String newName, String description, Organisation organisation) {

        document.setName(newName);
        document.setDescription(description);
        edit(document);

        return document;
    }


    public void deleteDocument(Document document) {
        remove(document);
    }
    
    public List<Document> findAllDocumentsOfOrganisation(Organisation organisation) {
        return em.createQuery("SELECT d FROM Document d WHERE d.organisation = :organisation ORDER BY d.name ASC").
                setParameter("organisation", organisation).getResultList();
    }

    public Document findDocumentOfOrganisation(long documentId, Organisation organisation) {
        return findUniqueResult(em.createQuery("SELECT d FROM Document d WHERE d.id = :documentId AND d.organisation = :organisation ORDER BY d.name ASC").
                setParameter("documentId", documentId).
                setParameter("organisation", organisation).getResultList());
    }

    public Document findDocumentsByType(DocumentType docType, Organisation organisation) {
        return findUniqueResult(em.createQuery("SELECT d FROM Document d WHERE d.organisation = :organisation AND d.docType = :docType ORDER BY d.name ASC").
                setParameter("organisation", organisation).
                setParameter("docType", docType).getResultList());
    }
}
