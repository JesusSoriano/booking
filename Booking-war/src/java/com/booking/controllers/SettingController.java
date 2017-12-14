package com.booking.controllers;

import com.booking.entities.User;
import com.booking.facades.UserFacade;
import com.booking.security.PBKDF2HashGenerator;
import com.booking.util.FacesUtil;
import com.booking.util.StringsUtil;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class SettingController implements Serializable {

    @EJB
    private UserFacade userFacade;
    private User currentUser;
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

    /**
     * Creates a new instance of SettingController
     */
    public SettingController() {
    }

    @PostConstruct
    public void init() {
        currentUser = FacesUtil.getCurrentUser();
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setNewPassword() {
        if (!newPassword.equals(confirmPassword)) {
            FacesUtil.addErrorMessage("settings", "Las contraseñas no coinciden. Inténtalo de nuevo.");
            return;
        }

        if (StringsUtil.isNotNullNotEmpty(newPassword)) {
            FacesUtil.addErrorMessage("settings", "Introduce contraseña");
            return;
        }
        if (StringsUtil.notSecure(newPassword)) {
            FacesUtil.addErrorMessage("settings", "La contraseña debe contener al menos 6 caracteres, incluyendo una letra mayúscula y una minúscula.");
            return;
        }

        if (newPassword.equals(currentPassword)) {
            FacesUtil.addErrorMessage("settings", "Tu nueva contraseña debe ser diferente a la anterior.");
            return;
        }

        try {
            newPassword = PBKDF2HashGenerator.createHash(newPassword);
            currentUser.setPassword(newPassword);
            userFacade.edit(currentUser);
            FacesUtil.addSuccessMessage("settings", "Tu contraseña ha sido actualizada correctamente.");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            FacesUtil.addErrorMessage("settings", "Lo sentimos, no ha sido posible actualizar tu contraseña.");
            Logger.getLogger(SettingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
