package com.booking.controllers;

import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.enums.Role;
import com.booking.facades.UserFacade;
import com.booking.util.FacesUtil;
import com.booking.util.StringsUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class UserProfileController implements Serializable {

    @EJB
    private UserFacade userFacade;

    private User profileUser;
    private String firstName;
    private String firstLastName;
    private String secondLastName;
    private String email;
    private String phone;
    private String addressLine;
    private String addressLine2;
    private String city;
    private String country;
    private String postcode;

    /**
     * Creates a new instance of UserProfileController
     */
    public UserProfileController() {
    }

    @PostConstruct
    public void init() {

        Organisation organisation = FacesUtil.getCurrentOrganisation();

        User logedUser = FacesUtil.getCurrentUser();
        String userId = FacesUtil.getParameter("user");
        if (userId != null && (logedUser.getUserRole().getRole() == Role.ADMIN || logedUser.getUserRole().getRole() == Role.SUPER_ADMIN)) {
            profileUser = userFacade.findUserOfOrganisation(Integer.valueOf(userId), organisation);
        } else {
            profileUser = logedUser;
        }

        if (profileUser != null) {
            firstName = profileUser.getFirstName();
            firstLastName = profileUser.getFirstLastName();
            secondLastName = profileUser.getSecondLastName();
            email = profileUser.getEmail();
            phone = profileUser.getPhone();
            addressLine = profileUser.getAddress().getAddressLine();
            addressLine2 = profileUser.getAddress().getAddressLine2();
            city = profileUser.getAddress().getCity();
            country = profileUser.getAddress().getCountry();
            postcode = profileUser.getAddress().getPostcode();
        }
    }

    public void saveProfile() {

        if (StringsUtil.isNotNullNotEmpty(firstName)) {
            FacesUtil.addErrorMessage("profileForm", "Introduce tu nombre");
        }
        if (StringsUtil.isNotNullNotEmpty(firstLastName)) {
            FacesUtil.addErrorMessage("profileForm", "Introduce tu primer apellido");
        }

        // Remove start and end white spaces of email
        email = email.trim();
        if (StringsUtil.isNotNullNotEmpty(email)) {
            FacesUtil.addErrorMessage("profileForm", "Introduce tu email");
        }

        userFacade.editUserProfile(profileUser, firstName, firstLastName, secondLastName, email,
                phone, addressLine, addressLine2, city, country, postcode);
        try {
            FacesUtil.redirectTo("user-profile.xhtml", "&user=" + profileUser.getId());
        } catch (Exception ex) {
            Logger.getLogger(UserProfileController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void goToProfileEdition() {
        try {
            FacesUtil.redirectTo("edit-user.xhtml", "&user=" + profileUser.getId());
        } catch (IOException ex) {
            Logger.getLogger(UserProfileController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cancelProfileEdition() {
        try {
            FacesUtil.redirectTo("user-profile.xhtml", "&user=" + profileUser.getId());
        } catch (IOException ex) {
            Logger.getLogger(UserProfileController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstLastName() {
        return firstLastName;
    }

    public void setFirstLastName(String firstLastName) {
        this.firstLastName = firstLastName;
    }

    public String getSecondLastName() {
        return secondLastName;
    }

    public void setSecondLastName(String secondLastName) {
        this.secondLastName = secondLastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }
}
