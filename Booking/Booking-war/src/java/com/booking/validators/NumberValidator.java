/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.booking.validators;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author Anoop
 */
@FacesValidator(value = "numberValidator")
public class NumberValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String phoneProvided = (String) value;
        if (!(validatePhone(phoneProvided))) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Invalid Phone",
                    "");
            throw new ValidatorException(message);
        }
    }

    public boolean validatePhone(String str) {

        //It can't contain only numbers if it's null or empty...
       

        for (int i = 0; i < str.length(); i++) {
            if(str.charAt(0)=='+')
            {
                if (i + 1 < str.length()) {
                    if (!Character.isDigit(str.charAt(i + 1))) {
                        return false;
                    }
                }
            }
            //If we find a non-digit character we return false.
            else if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return true;

    }
}

