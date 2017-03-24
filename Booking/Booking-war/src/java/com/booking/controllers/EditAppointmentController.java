package com.booking.controllers;

import com.booking.entities.Appointment;
import com.booking.entities.AppointmentRequest;
import com.booking.entities.Organisation;
import com.booking.entities.Service;
import com.booking.entities.User;
import com.booking.enums.AuditType;
import com.booking.enums.Role;
import com.booking.facades.AppointmentFacade;
import com.booking.facades.AppointmentRequestFacade;
import com.booking.facades.AuditFacade;
import com.booking.facades.ServiceFacade;
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

@ManagedBean
@ViewScoped
public class EditAppointmentController implements Serializable {

    @EJB
    private AppointmentFacade appointmentFacade;
    @EJB
    private AppointmentRequestFacade appointmentRequestFacade;
    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private AuditFacade auditFacade;

    private List<SelectItem> services;
    private long selectedServiceId;
    private List<SelectItem> allUsers;
    private long selectedUserId;
    private User loggedUser;
    private Organisation organisation;
    private String description;
    private float price;
    private boolean newAppointment;
    private Appointment currentAppointment;
    private List<AppointmentRequest> appointmentRequests;
    private User appointmentUser;
    private String appointmentId;
    private Date startingTime;
    private Date endingTime;
    private Date date;

    public EditAppointmentController() {
    }

    @PostConstruct
    public void init() {
        loggedUser = FacesUtil.getCurrentUser();
        organisation = FacesUtil.getCurrentOrganisation();

        services = new ArrayList<>();
        for (Service s : serviceFacade.findAllActiveServicesOfOrganisation(organisation)) {
            services.add(new SelectItem(s.getId(), s.getName()));
        }
        
        appointmentId = FacesUtil.getParameter("appointment");
        if (appointmentId != null) {
            currentAppointment = appointmentFacade.findAppointmentOfOrganisation(Long.valueOf(appointmentId), organisation);

            if (currentAppointment != null) {
                selectedServiceId = currentAppointment.getService().getId();
                description = currentAppointment.getDescription();
                date = currentAppointment.getDate();
                startingTime = currentAppointment.getStartTime();
                endingTime = currentAppointment.getEndTime();
                price = currentAppointment.getPrice();
                appointmentUser = currentAppointment.getAppointmentUser();
                
                appointmentRequests = appointmentRequestFacade.findAllRequestsOfAppointment(currentAppointment);
            }
        } else {
            newAppointment = true;
        }
    }

    public String createNewAppointment() {
        try {
            Service selectedService = serviceFacade.find(selectedServiceId);
            Appointment appointment = appointmentFacade.createNewAppointment(selectedService, description, date, startingTime, endingTime, price, organisation);
            FacesUtil.addSuccessMessage("editAppointmentForm:msg", "La cita ha sido creada correctamente.");
            // Audit appointment creation
            String ipAddress = FacesUtil.getRequest().getRemoteAddr();
            auditFacade.createAudit(AuditType.CREAR_CLASE, loggedUser, ipAddress, appointment.getId(), organisation);
            return "view-appointment.xhtml" + Constants.FACES_REDIRECT + "&amp;appointment=" + appointment.getId();
        } catch (Exception e) {
            FacesUtil.addErrorMessage("editAppointmentForm:msg", "Lo sentimos, no ha sido posible crear la cita.");
            Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
            return "appointments.xhtml" + Constants.FACES_REDIRECT;
        }
    }

    public String updateAppointment() {
        try {
            Service selectedService = serviceFacade.find(selectedServiceId);
            Appointment updatedAppointment = appointmentFacade.updateAppointment(currentAppointment, selectedService, description, date, startingTime, endingTime, price);
            if (updatedAppointment != null) {
//                FacesUtil.addSuccessMessage("editAppointmentForm:msg", "El servicio ha sido actualizado correctamente.");
                return "view-appointment.xhtml" + Constants.FACES_REDIRECT + "&amp;appointment=" + updatedAppointment.getId();
            } else {
                FacesUtil.addErrorMessage("editAppointmentForm:msg", "Lo sentimos, no ha sido posible editar el servicio.");
            }
        } catch (Exception e) {
            FacesUtil.addErrorMessage("editAppointmentForm:msg", "Lo sentimos, no ha sido posible editar el servicio.");
            Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
        }

        String appointmentParam = (appointmentId != null) ? ("appointment=" + appointmentId) : "";
        return "edit-appointment.xhtml" + Constants.FACES_REDIRECT + appointmentParam;
    }

//    public String addParticipant() {
//        User selectedUser = userFacade.find(selectedUserId);
//        if (selectedUser != null) {
//            bookAppointment(selectedUser);
//        } else {
//            FacesUtil.addErrorMessage("viewAppointmentForm:msg", "Error, el usuario no existe.");
//        }
//
//        return viewAppointmentWithParam();
//    }
//
    public String bookAppointmentForUser() {
        return bookAppointment(loggedUser);
    }

    public String bookAppointment(User user) {
//        try {
//            // Check if the booking already exists
//            if (appointmentFacade.existsBooking(user, currentAppointment)) {
//                FacesUtil.addErrorMessage("viewAppointmentForm:msg", "Esta cita ya ha sido resarvada previamente.");
//                // Check if there are free places
//            } else if (currentAppointment.getBookedPlaces() == currentAppointment.getMaximumUsers()) {
//                FacesUtil.addErrorMessage("viewAppointmentForm:msg", "No quedan plazas para esta cita. Puedes revisar periódicamente si queda alguna libre.");
//            } else {
//                // Create the appointments user
//                appointmentFacade.createNewBooking(user, currentAppointment);
//                // Add a booked place in the appointments
//                appointmentFacade.addAppointmentBooking(currentAppointment);
//
//                FacesUtil.addSuccessMessage("viewAppointmentForm:msg", "La plaza ha sido reservada correctamente.");
//
//                try {
//                    // Audit new booking
//                    String ipAddress = FacesUtil.getRequest().getRemoteAddr();
//                    auditFacade.createAudit(AuditType.RESERVAR_CLASE, user, ipAddress, currentAppointment.getId(), organisation);
//
//                } catch (Exception e) {
//                    Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
//                    FacesUtil.addErrorMessage("viewAppointmentForm:msg", "La plaza ha sido reservada, pero ha habido un problema auditando la reserva. Por favor informa a algún administrador del problema.");
//                }
//            }
//        } catch (Exception e) {
//            Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
//            FacesUtil.addErrorMessage("viewAppointmentForm:msg", "Lo sentimos, ha habido un problema al reservar la plaza.");
//        }
//
        return viewAppointmentWithParam();
    }

    public String cancelBookingForUser() {
        return cancelAppointmentBooking(loggedUser);
    }

    public String cancelAppointmentBooking(User user) {
//        try {
//            // Create the appointments user
//            if (appointmentFacade.removeBooking(user, currentAppointment)) {
//                // Add a booked place in the appointments
//                appointmentFacade.removeAppointmentBooking(currentAppointment);
//                FacesUtil.addSuccessMessage("viewAppointmentForm:msg", "La reserva ha sido cancelada correctamente.");
//
//                // Audit booking cancalation
//                String ipAddress = FacesUtil.getRequest().getRemoteAddr();
//                auditFacade.createAudit(AuditType.CANCELAR_RESERVA, user, ipAddress, currentAppointment.getId(), organisation);
//            } else {
//                FacesUtil.addErrorMessage("viewAppointmentForm:msg", "Error, la reserva no existe.");
//            }
//        } catch (Exception e) {
//            Logger.getLogger(EditAppointmentController.class.getName()).log(Level.SEVERE, null, e);
//            FacesUtil.addErrorMessage("viewAppointmentForm:msg", "Lo sentimos, ha habido un problema al cancelar la reserva.");
//        }

        return viewAppointmentWithParam();
    }

    public String viewAppointmentWithParam() {
        String appointmentParam = (appointmentId != null) ? ("appointment=" + appointmentId) : "";
        return "view-appointment.xhtml" + Constants.FACES_REDIRECT + appointmentParam;
    }

    public String cancelEditAppointment() {
        if (newAppointment) {
            return "appointments.xhtml" + Constants.FACES_REDIRECT;
        } else {
            return viewAppointmentWithParam();
        }
    }

    public boolean bookedAppointmentForUser() {
        return currentAppointment.getAppointmentUser() != null && currentAppointment.getAppointmentUser() == loggedUser;
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

    public boolean isNewAppointment() {
        return newAppointment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Appointment getCurrentAppointment() {
        return currentAppointment;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public boolean isPastAppointment() {
        if (currentAppointment != null && currentAppointment.getDate()!= null) {
            return currentAppointment.getDate().before(new Date());
        }
        return false;
    }

    public Date getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(Date endingTime) {
        this.endingTime = endingTime;
    }

    public Date getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(Date startingTime) {
        this.startingTime = startingTime;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getAppointmentUser() {
        return appointmentUser;
    }

    public List<AppointmentRequest> getAppointmentRequests() {
        return appointmentRequests;
    }
}
