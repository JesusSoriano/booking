package com.booking.controllers;

import com.booking.entities.Document;
import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
import com.booking.facades.DocumentFacade;
import com.booking.facades.UserFacade;
import com.booking.util.BookingProperties;
import com.booking.util.Constants;
import com.booking.util.FacesUtil;
import com.booking.util.FileServiceImpl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import org.primefaces.context.RequestContext;
import org.primefaces.model.UploadedFile;

@ManagedBean
@ViewScoped
public class FilesController implements Serializable {

    @EJB
    private DocumentFacade documentFacade;
    @EJB
    private AuditFacade auditFacade;
    @EJB
    private UserFacade userFacade;

    private List<Document> files;
    private User loggedUser;
    private Organisation organisation;
    private Document selectedDocument;
    private String newFileName;
    private String newFileDescription;
    private User fileUser;
    private boolean isNewFile;
    private List<SelectItem> allUsers;
    private long selectedUserId;
    private UploadedFile file;

    public FilesController() {
    }

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        isNewFile = true;
        files = documentFacade.findAllDocumentsOfOrganisation(organisation);

        allUsers = new ArrayList<>();
        for (User u : userFacade.findAllActiveAdminsAndClientsOfOrganisation(organisation)) {
            allUsers.add(new SelectItem(u.getId(), u.getFullName()));
        }
        selectedUserId = (long) allUsers.get(0).getValue();
    }

    public String createNewFile() {
        if (newFileName.trim().isEmpty()) {
            FacesUtil.addErrorMessage("documentsForm:msg", "Lo sentimos, no ha sido posible crear el nuevo archivo: Introduzca un nombre.");
            return "";
        }
        RequestContext context = RequestContext.getCurrentInstance();
        try {
            User selectedUser = userFacade.find(selectedUserId);
            if (selectedUser != null) {
                Document newFile = documentFacade.uploadDocument(organisation, newFileName, file.getFileName(), newFileDescription, loggedUser, selectedUser, new FileServiceImpl(), file.getInputstream());
                context.execute("PF('newFileDialog').hide();");
                FacesUtil.addSuccessMessage("documentsForm:msg", "El nuevo archivo ha sido creado correctamente.");
                // Audit document creation
                String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                auditFacade.createAudit(AuditType.CREAR_ARCHIVO, loggedUser, ipAddress, newFile.getId(), organisation);
            } else {
                FacesUtil.addErrorMessage("viewClassForm:msg", "Error, el usuario no existe.");
                return "";
            }
        } catch (Exception e) {
            FacesUtil.addErrorMessage("documentsForm:msg", "Lo sentimos, no ha sido posible crear el nuevo archivo.");
            Logger.getLogger(FilesController.class.getName()).log(Level.SEVERE, null, e);
            return "";
        }

        return returnFilesPage();
    }

    public String updateFile() {
        User selectedUser = userFacade.find(selectedUserId);
        if (selectedUser != null && selectedDocument != null) {
            RequestContext context = RequestContext.getCurrentInstance();
            try {
                documentFacade.updateDocument(selectedDocument, newFileName, newFileDescription, selectedUser, organisation);
                context.execute("PF('newFileDialog').hide();");

                FacesUtil.addSuccessMessage("documentsForm:msg", "El archivo ha sido actualizado correctamente.");
            } catch (Exception e) {
                FacesUtil.addErrorMessage("documentsForm:msg", "Lo sentimos, no ha sido posible editar el archivo.");
                Logger.getLogger(FilesController.class.getName()).log(Level.SEVERE, null, e);
                return "";
            }
        } else {
            FacesUtil.addErrorMessage("documentsForm:msg", "Lo sentimos, no ha sido posible editar el archivo.");
        }

        selectedDocument = null;
        return returnFilesPage();
    }

    public String deleteFile(Document document) {
        if (selectedDocument != null) {
            RequestContext context = RequestContext.getCurrentInstance();
            try {
                documentFacade.deleteDocument(selectedDocument);
                context.execute("PF('newFileDialog').hide();");

                FacesUtil.addSuccessMessage("documentsForm:msg", "El archivo ha sido eliminado correctamente.");

                // Audit document deletion
                String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                auditFacade.createAudit(AuditType.ELIMINAR_ARCHIVO, loggedUser, ipAddress, selectedDocument.getId(), organisation);
            } catch (Exception e) {
                FacesUtil.addErrorMessage("documentsForm:msg", "Lo sentimos, no ha sido posible eliminar el archivo.");
                Logger.getLogger(FilesController.class.getName()).log(Level.SEVERE, null, e);
                return "";
            }
        } else {
            FacesUtil.addErrorMessage("documentsForm:msg", "Lo sentimos, no ha sido posible eliminar el archivo.");
        }
        return returnFilesPage();
    }

    public String getFilePath(Document file) {
        try {
            String filePath = BookingProperties.getInstance().getUploadedFilesPath(organisation) + file.getFileName();
            return "file://" + filePath;
        } catch (Exception e) {
            Logger.getLogger(FilesController.class.getName()).log(Level.SEVERE, null, e);
            return "";
        }
    }

    private String returnFilesPage() {
        newFileName = "";
        newFileDescription = "";
        return "files.xhtml" + Constants.FACES_REDIRECT;
    }

    public void prepareFile(Document document) {
        selectedDocument = document;
        newFileName = document.getName();
        newFileDescription = document.getDescription();
        isNewFile = false;
    }

    public void prepareNewFile() {
        selectedDocument = null;
        newFileName = "";
        newFileDescription = "";
        isNewFile = true;
    }

    public List<Document> getFiles() {
        return files;
    }

    public Role getUserRole() {
        return loggedUser.getUserRole().getRole();
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public String getNewFileDescription() {
        return newFileDescription;
    }

    public void setNewFileDescription(String newFileDescription) {
        this.newFileDescription = newFileDescription;
    }

    public boolean isIsNewFile() {
        return isNewFile;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public UploadedFile getFile() {
        return file;
    }

    public boolean loggedUserIsAdmin() {
        Role userRole = loggedUser.getUserRole().getRole();
        return userRole == Role.ADMIN || userRole == Role.SUPER_ADMIN;
    }

    public List<SelectItem> getAllUsers() {
        return allUsers;
    }

    public long getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(long selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public void upload() {
        if (file != null) {
            System.out.println("------------ ARCHIVO SUBIDO: " + file.getFileName());
        } else {
            System.out.println("------------ EMPTY FILE");
        }
    }
}
