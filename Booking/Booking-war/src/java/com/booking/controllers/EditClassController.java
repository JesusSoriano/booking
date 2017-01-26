package com.booking.controllers;

import com.booking.entities.ClassDay;
import com.booking.entities.ActivityClass;
import com.booking.entities.Organisation;
import com.booking.entities.Service;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AuditFacade;
import com.booking.facades.BookingFacade;
import com.booking.facades.ClassFacade;
import com.booking.facades.ServiceFacade;
import com.booking.facades.UserFacade;
import com.booking.util.Constants;
import com.booking.util.FacesUtil;
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

@ManagedBean
@ViewScoped
public class EditClassController implements Serializable {

    @EJB
    private ClassFacade classFacade;
    @EJB
    private BookingFacade bookingFacade;
    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private AuditFacade auditFacade;
    @EJB
    private UserFacade userFacade;

    private List<SelectItem> services;
    private long selectedServiceId;
    private List<SelectItem> allUsers;
    private long selectedUserId;
    private User loggedUser;
    private Organisation organisation;
    private String className;
    private String classDescription;
    private int maximumUsers;
    private int bookedPlaces;
    private int numberOfDays;
    private float price;
    private boolean newClass;
    private ActivityClass currentClass;
    private List<ClassDay> activityDays; // días de cada actividad. TODO: nueva pantalla para crear los días
    // dependiendo del número?
    private List<User> classUsers;
    private String classId;

    public EditClassController() {
    }

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        services = new ArrayList<>();
        for (Service s : serviceFacade.findAllActiveServicesOfOrganisation(organisation)) {
            services.add(new SelectItem(s.getId(), s.getName()));
        }
        allUsers = new ArrayList<>();
        for (User u : userFacade.findAllActiveAdminsAndClientsOfOrganisation(organisation)) {
            allUsers.add(new SelectItem(u.getId(), u.getFullName()));
        }
        selectedUserId = (long) allUsers.get(0).getValue();

        classId = FacesUtil.getParameter("class");
        if (classId != null) {
            currentClass = classFacade.findClassOfOrganisation(Integer.valueOf(classId), organisation);

            if (currentClass != null) {
                selectedServiceId = currentClass.getService().getId();
                className = currentClass.getName();
                classDescription = currentClass.getDescription();
                maximumUsers = currentClass.getMaximumUsers();
                bookedPlaces = currentClass.getBookedPlaces();
//                startingTime = currentClass.getStartTime();
//                endingTime = currentClass.getEndTime();
                numberOfDays = currentClass.getNumberOfDays();
            }
        } else {
            newClass = true;
        }

        classUsers = bookingFacade.findAllBookedUsersOfClass(currentClass);
    }

    public String activateClass(ActivityClass activityClass) {
        classFacade.activateClass(activityClass);
        FacesUtil.addSuccessMessage("classesForm:msg", "El grupo ha sido activada correctamente.");

        try {
            // Audit class activation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.ACTIVAR_SERVICIO, loggedUser, ipAddress, activityClass.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(EditClassController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "classes.xhtml" + Constants.FACES_REDIRECT;
    }

    public String deactivateClass(ActivityClass activityClass) {
        classFacade.deactivateClass(activityClass);
        FacesUtil.addSuccessMessage("classesForm:msg", "El grupo ha sido suspendido correctamente.");

        try {
            // Audit class suspention
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.SUSPENDER_SERVICIO, loggedUser, ipAddress, activityClass.getId(), organisation);
        } catch (Exception e) {
            Logger.getLogger(EditClassController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "classes.xhtml" + Constants.FACES_REDIRECT;
    }

    public String createNewClass() {
        RequestContext context = RequestContext.getCurrentInstance();
        try {
            Service selectedService = serviceFacade.find(selectedServiceId);
            ActivityClass newActivityClass = classFacade.createNewClass(selectedService, className, classDescription, maximumUsers, numberOfDays, price, organisation);
            context.execute("PF('newClassDialog').hide();");
            FacesUtil.addSuccessMessage("classesForm:msg", "El nuevo servicio ha sido creado correctamente.");
            // Audit class creation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CREAR_SERVICIO, loggedUser, ipAddress, newActivityClass.getId(), organisation);
            return "view-class.xhtml" + Constants.FACES_REDIRECT + "&amp;class=" + newActivityClass.getId();
        } catch (Exception e) {
            FacesUtil.addErrorMessage("classesForm:msg", "Lo sentimos, no ha sido posible crear el nuevo servicio.");
            Logger.getLogger(EditClassController.class.getName()).log(Level.SEVERE, null, e);
            return "classes.xhtml" + Constants.FACES_REDIRECT;
        }

    }

    public String updateClass() {
        try {
            Service selectedService = serviceFacade.find(selectedServiceId);
            ActivityClass updatedClass = classFacade.updateClass(currentClass, selectedService, className, classDescription, maximumUsers, numberOfDays, price);
            if (updatedClass != null) {
//                FacesUtil.addSuccessMessage("classesForm:msg", "El servicio ha sido actualizado correctamente.");
                return "view-class.xhtml" + Constants.FACES_REDIRECT + "&amp;class=" + updatedClass.getId();
            } else {
                FacesUtil.addErrorMessage("classesForm:msg", "Lo sentimos, no ha sido posible editar el servicio.");
            }
        } catch (Exception e) {
            FacesUtil.addErrorMessage("classesForm:msg", "Lo sentimos, no ha sido posible editar el servicio.");
            Logger.getLogger(EditClassController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "classes.xhtml" + Constants.FACES_REDIRECT;
    }

    public void addParticipant() {
        User selectedUser = userFacade.find(selectedUserId);
        if (selectedUser != null) {
            bookClass(selectedUser);
        } else {
            FacesUtil.addErrorMessage("viewClassForm:msg", "Error, el usuario no existe.");
        }
    }
    
    public String bookClassForUser() {
        return bookClass(loggedUser);
    }

    public String bookClass(User user) {
        try {
            // Create the class user
            bookingFacade.createNewBooking(user, currentClass);
            // Add a booked place in the class
            classFacade.addClassBooking(currentClass);

            FacesUtil.addSuccessMessage("viewClassForm:msg", "La plaza ha sido reservada correctamente.");
        } catch (Exception e) {
            Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("viewClassForm:msg", "Lo sentimos, ha habido un problema al reservar la plaza.");
        }
        
        try {
            // Audit new booking
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.RESERVAR_CLASE, user, ipAddress, currentClass.getId(), organisation);

        } catch (Exception e) {
            Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("viewClassForm:msg", "La plaza ha sido reservada, pero ha habido un problema auditando la reserva. Por favor informa a algún administrador del problema.");
        }

        String classParam = (classId != null) ? ("class=" + classId) : "";
        return "view-class.xhtml" + Constants.FACES_REDIRECT + classParam;
    }

    public String cancelBookingForUser() {
        return cancelClassBooking(loggedUser);
    }
    
    public String cancelClassBooking(User user) {
        try {
            // Create the class user
            if (bookingFacade.removeBooking(user, currentClass)) {
                // Add a booked place in the class
                classFacade.removeClassBooking(currentClass);
                FacesUtil.addSuccessMessage("viewClassForm:msg", "La reserva ha sido cancelada correctamente.");

                // Audit booking cancalation
                String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                auditFacade.createAudit(AuditType.CANCELAR_RESERVA, user, ipAddress, currentClass.getId(), organisation);
            } else {
                FacesUtil.addErrorMessage("viewClassForm:msg", "Error, la reserva no existe.");
            }
        } catch (Exception e) {
            Logger.getLogger(EditClassController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("viewClassForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
        }

        String classParam = (classId != null) ? ("class=" + classId) : "";
        return "view-class.xhtml" + Constants.FACES_REDIRECT + classParam;
    }
    
    public boolean existsBooking() {
        return bookingFacade.existsBooking(loggedUser, currentClass);
    }

    public List<SelectItem> getServices() {
        return services;
    }

    public List<SelectItem> getAllUsers() {
        return allUsers;
    }

    public Role getUserRole() {
        return loggedUser.getUserRole().getRole();
    }

    public boolean isNewClass() {
        return newClass;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassDescription() {
        return classDescription;
    }

    public void setClassDescription(String classDescription) {
        this.classDescription = classDescription;
    }

    public int getMaximumUsers() {
        return maximumUsers;
    }

    public void setMaximumUsers(int maximumUsers) {
        this.maximumUsers = maximumUsers;
    }

    public int getBookedPlaces() {
        return bookedPlaces;
    }

    public long getSelectedServiceId() {
        return selectedServiceId;
    }

    public void setSelectedServiceId(long selectedServiceId) {
        this.selectedServiceId = selectedServiceId;
    }

    public long getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(long selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public ActivityClass getCurrentClass() {
        return currentClass;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public List<User> getClassUsers() {
        return classUsers;
    }
}
