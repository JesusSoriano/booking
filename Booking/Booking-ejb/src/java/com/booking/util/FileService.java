/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.booking.util;

import com.booking.entities.Organisation;
import java.io.InputStream;
import java.io.Serializable;

/**
 *
 * @author Jesús Soriano
 */
public interface FileService extends Serializable {

    public void createOrganisationFolders(Organisation organisation);

    public String uploadDocument(String fileName, InputStream inputStream, Organisation organisation) throws Exception;

    public boolean deleteDocument(Organisation organisation, String name);

    public InputStream findDocumentStream(String attachedDocument, Organisation organisation);

    public boolean supportedFileSize(long size);
}
