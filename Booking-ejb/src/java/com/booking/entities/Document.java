/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.booking.entities;

import com.booking.enums.DocumentType;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author Jesús Soriano
 */
@Entity
@Table(name = "documents")
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "fileName")
    private String fileName;
    @Column(name = "doc_type")
    private DocumentType docType;
    @Column(name = "created_date")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin", referencedColumnName = "id")
    private User admin;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_user", referencedColumnName = "id")
    private User fileUser;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation", referencedColumnName = "id")
    private Organisation organisation;

    public Document() {
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public DocumentType getDocType() {
        return docType;
    }

    public void setDocType(DocumentType docType) {
        this.docType = docType;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public User getFileUser() {
        return fileUser;
    }

    public void setFileUser(User fileUser) {
        this.fileUser = fileUser;
    }

    @Override
    public String toString() {
        return name + " (" + docType + ")";
    }
}
