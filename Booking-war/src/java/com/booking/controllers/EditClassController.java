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
import com.booking.facades.ClassDayFacade;
import com.booking.facades.ClassFacade;
import com.booking.facades.ServiceFacade;
import com.booking.facades.UserFacade;
import com.booking.util.Constants;
import com.booking.util.FacesUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
    @EJB
    private ClassDayFacade classDayFacade;

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
    private List<ClassDay> classDays;
    private List<User> classUsers;
    private String classId;

    private Date newDayStartDate;
    private Date newDayEndDate;
    private String newDayDescription;
    private boolean isNewDay;
    private ClassDay selectedClassDay;

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
            currentClass = classFacade.findClassOfOrganisation(Long.valueOf(classId), organisation);

            if (currentClass != null) {
                selectedServiceId = currentClass.getService().getId();
                className = currentClass.getName();
                classDescription = currentClass.getDescription();
                maximumUsers = currentClass.getMaximumUsers();
                bookedPlaces = currentClass.getBookedPlaces();
                numberOfDays = currentClass.getNumberOfDays();
                price = currentClass.getPrice();

                classUsers = bookingFacade.findAllBookedUsersOfClass(currentClass);
                classDays = classDayFacade.findAllDaysOfClass(currentClass);
            }
        } else {
            newClass = true;
        }

        isNewDay = true;
    }

    public String createNewClass() {
        try {
            Service selectedService = serviceFacade.find(selectedServiceId);
            ActivityClass newActivityClass = classFacade.createNewClass(selectedService, className, classDescription, maximumUsers, 0, price, organisation);
            FacesUtil.addSuccessMessage("classesForm:msg", "La clase ha sido creada correctamente.");
            // Audit class creation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CREAR_CLASE, loggedUser, ipAddress, newActivityClass.getId(), organisation);
            return "view-class.xhtml" + Constants.FACES_REDIRECT + "&amp;class=" + newActivityClass.getId();
        } catch (Exception e) {
            FacesUtil.addErrorMessage("classesForm:msg", "Lo sentimos, no ha sido posible crear la clase.");
            Logger.getLogger(EditClassController.class.getName()).log(Level.SEVERE, null, e);
            return "classes.xhtml" + Constants.FACES_REDIRECT;
        }
    }

    public String updateClass() {
        try {
            Service selectedService = serviceFacade.find(selectedServiceId);
            ActivityClass updatedClass = classFacade.updateClass(currentClass, selectedService, className, classDescription, maximumUsers, price);
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

        return viewClassWithParam();
    }

    public String addParticipant() {
        User selectedUser = userFacade.find(selectedUserId);
        if (selectedUser != null) {
            bookClass(selectedUser);
        } else {
            FacesUtil.addErrorMessage("viewClassForm:msg", "Error, el usuario no existe.");
        }

        return viewClassWithParam();
    }

    public String bookClassForUser() {
        return bookClass(loggedUser);
    }

    public String bookClass(User user) {
        try {
            // Check if the booking already exists
            if (bookingFacade.existsBooking(user, currentClass)) {
                FacesUtil.addErrorMessage("viewClassForm:msg", "Esta clase ya ha sido resarvada previamente.");
                // Check if there are free places
            } else if (currentClass.getBookedPlaces() == currentClass.getMaximumUsers()) {
                FacesUtil.addErrorMessage("viewClassForm:msg", "No quedan plazas para esta clase. Puedes revisar periódicamente si queda alguna libre.");
            } else {
                // Create the class user
                bookingFacade.createNewBooking(user, currentClass);
                // Add a booked place in the class
                classFacade.addClassBooking(currentClass);

                FacesUtil.addSuccessMessage("viewClassForm:msg", "La plaza ha sido reservada correctamente.");

                try {
                    // Audit new booking
                    String ipAddress = FacesUtil.getRequest().getRemoteAddr();
                    auditFacade.createAudit(AuditType.RESERVAR_CLASE, user, ipAddress, currentClass.getId(), organisation);

                } catch (Exception e) {
                    Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
                    FacesUtil.addErrorMessage("viewClassForm:msg", "La plaza ha sido reservada, pero ha habido un problema auditando la reserva. Por favor informa a algún administrador del problema.");
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ClassesController.class.getName()).log(Level.SEVERE, null, e);
            FacesUtil.addErrorMessage("viewClassForm:msg", "Lo sentimos, ha habido un problema al reservar la plaza.");
        }

        return viewClassWithParam();
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

        return viewClassWithParam();
    }

    public String activateClassDay(ClassDay classDay) {
        classDayFacade.activateClass(classDay);
        classFacade.addNumberOfDays(currentClass);
        classFacade.updateEndDate(currentClass, classDay.getEndDate());
        return viewClassWithParam();
    }

    public String deactivateClassDay(ClassDay classDay) {
        classDayFacade.deactivateClass(classDay);
        classFacade.removeNumberOfDays(currentClass);
        checkEndDateOfClass(classDay.getEndDate());
        return viewClassWithParam();
    }

    /**
     * Check if the endDate of the Class is the one we just deactivated and
     * assign a new endDate, depending of the rest of classDays.
     *
     * @param endDate The endDate of the deactivated classDay
     */
    private void checkEndDateOfClass(Date endDate) {
        if (endDate.equals(currentClass.getEndDate()) || endDate.after(currentClass.getEndDate())) {
            List<ClassDay> currentClassDays = classDayFacade.findAllActiveDaysOfClass(currentClass);
            int classDaysSize = currentClassDays.size();
            if (classDaysSize > 0) {
                Date lastDate = currentClassDays.get(0).getEndDate();
                for (ClassDay classDay : currentClassDays.subList(1, classDaysSize)) {
                    if (classDay.getEndDate().after(lastDate)) {
                        lastDate = classDay.getEndDate();
                    }
                }
                classFacade.setEndDate(currentClass, lastDate);
            } else {
                classFacade.setEndDate(currentClass, null);
            }
        }
    }

    public String viewClassWithParam() {
        String classParam = (classId != null) ? ("class=" + classId) : "";
        return "view-class.xhtml" + Constants.FACES_REDIRECT + classParam;
    }

    public String cancelEditClass() {
        if (newClass) {
            return "classes.xhtml" + Constants.FACES_REDIRECT;
        } else {
            return viewClassWithParam();
        }
    }

    public void prepareClassDay(ClassDay classDay) {
        selectedClassDay = classDay;
        newDayDescription = classDay.getDescription();
        newDayStartDate = classDay.getStartDate();
        newDayEndDate = classDay.getEndDate();
        isNewDay = false;
    }

    public void prepareNewClassDay() {
        selectedClassDay = null;
        newDayDescription = "";
        newDayStartDate = null;
        newDayEndDate = null;
        isNewDay = true;
    }
    
    public void onDatesChange () {
        //TODO: Revisar
        System.out.println("-------------- ON DATE CHANGE");
        if (newDayEndDate != null && newDayStartDate != null && newDayEndDate.before(newDayStartDate)) {
            newDayEndDate = newDayStartDate;
        }
    }

    public String addClassDay() {
        RequestContext context = RequestContext.getCurrentInstance();
        try {
            if (newDayStartDate == null || newDayEndDate == null) {
                context.execute("PF('newClassDayDialog').hide();");
                FacesUtil.addErrorMessage("viewClassForm:msg", "Introduce las fechas de inicio y fin.");
            } else if (newDayEndDate.before(newDayStartDate)) {
                context.execute("PF('newClassDayDialog').hide();");
                FacesUtil.addErrorMessage("viewClassForm:msg", "La fecha de inicio debe ser anterior a la de fin.");
            } else {
                classDayFacade.createNewClassDay(currentClass, newDayDescription, newDayStartDate, newDayEndDate);
                context.execute("PF('newClassDayDialog').hide();");
                classFacade.updateEndDate(currentClass, newDayEndDate);
                FacesUtil.addSuccessMessage("viewClassForm:msg", "El nuevo día ha sido añadido correctamente.");

                classFacade.addNumberOfDays(currentClass);
            }
        } catch (Exception e) {
            FacesUtil.addErrorMessage("viewClassForm:msg", "Lo sentimos, no ha sido posible añadir el día.");
            Logger
                    .getLogger(ServicesController.class
                            .getName()).log(Level.SEVERE, null, e);
        }

        return viewClassWithParam();
    }

    public String updateClassDay() {
        if (newDayStartDate == null || newDayEndDate == null) {
            FacesUtil.addErrorMessage("newClassDayForm:newDayStartDate", "Introduce las fechas de inicio y fin de la clase.");
            return "";
        }
        try {
            if (selectedClassDay != null) {
                // Save the last date to compare with the class date.
                Date lastEndDate = selectedClassDay.getEndDate();
                if (lastEndDate == null || newDayEndDate.after(lastEndDate)) {
                    lastEndDate = newDayEndDate;
                }
                RequestContext context = RequestContext.getCurrentInstance();
                try {
                    classDayFacade.updateClassDay(selectedClassDay, currentClass, newDayDescription, newDayStartDate, newDayEndDate);
                    context.execute("PF('newClassDayDialog').hide();");
                    checkEndDateOfClass(lastEndDate);
                    FacesUtil.addSuccessMessage("viewClassForm:msg", "El día ha sido actualizado correctamente.");
                } catch (Exception e) {
                    FacesUtil.addErrorMessage("viewClassForm:msg", "Lo sentimos, no ha sido posible actualizar el día.");
                    Logger
                            .getLogger(ServicesController.class
                                    .getName()).log(Level.SEVERE, null, e);
                }
            } else {
                FacesUtil.addErrorMessage("viewClassForm:msg", "Lo sentimos, no ha sido posible actualizar el día.");
            }
        } catch (Exception e) {
            FacesUtil.addErrorMessage("viewClassForm:msg", "Lo sentimos, no ha sido posible actualizar el día.");
            Logger
                    .getLogger(ServicesController.class
                            .getName()).log(Level.SEVERE, null, e);
        }

        return viewClassWithParam();
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

    public List<ClassDay> getClassDays() {
        return classDays;
    }

    public void setNewDayStartDate(Date newDayStartDate) {
        this.newDayStartDate = newDayStartDate;
    }

    public Date getNewDayStartDate() {
        return newDayStartDate;
    }

    public void setNewDayEndDate(Date newDayEndDate) {
        this.newDayEndDate = newDayEndDate;
    }

    public Date getNewDayEndDate() {
        return newDayEndDate;
    }

    public void setNewDayDescription(String newDayDescription) {
        this.newDayDescription = newDayDescription;
    }

    public String getNewDayDescription() {
        return newDayDescription;
    }

    public boolean isIsNewDay() {
        return isNewDay;
    }

    public boolean isPastClass() {
        if (currentClass != null && currentClass.getEndDate() != null) {
            return currentClass.getEndDate().before(new Date());
        }
        return false;
    }
}
