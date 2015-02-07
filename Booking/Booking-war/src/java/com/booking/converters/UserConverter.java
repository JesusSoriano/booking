package com.booking.converters;

import com.booking.entities.User;
import com.booking.facades.UserFacade;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Jesús Soriano
 */
@FacesConverter(value = "userConverter")
public class UserConverter implements Converter {

    @EJB
    UserFacade userFacade;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        User user = null;
        try {
            user = userFacade.findUserByEmail(value);
        } catch (Exception ex) {
            Logger.getLogger(UserConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return user;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value == null ? "" : value.toString();
    }
}
