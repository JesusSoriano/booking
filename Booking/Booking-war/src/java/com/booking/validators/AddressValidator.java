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
@FacesValidator(value = "addressValidator")
public class AddressValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String name = (String) value;
        if (!(isValidName(name))) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Invalid Address",
                    "Invalid Address");
            throw new ValidatorException(message);
        }
    }

    private boolean isValidName(String email) {
        if(!email.isEmpty() || email.length()>0){
        //String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        String ADDRESS_REGEX = "^[a-zA-Z0-9#,\\s]+$";
        return email.matches(ADDRESS_REGEX);
    }
     else {
    return true;}

    }
}
