/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moneytransfer.validators;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *validate the supplied email that it is of the correct pattern.
 * @author Mamadou D
 */
@FacesValidator(value = "emailValidator")
public class EmailValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String emailProvided = (String) value;
        if (!(isValidEmailAddress(emailProvided))) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Invalid Email",
                    "Invalid Email");
            throw new ValidatorException(message);
        }
    }

    private boolean isValidEmailAddress(String email) {
        //String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w-]+\\.)+[\\w-]+[\\w-]$";
        return email.matches(EMAIL_REGEX);
    }
}
