package com.booking.controllers;

import com.booking.entities.Organisation;
import com.booking.entities.User;
import com.booking.facades.UserFacade;
import com.booking.util.FacesUtil;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

/**
 *
 * @author Jesús Soriano
 */
@ManagedBean
@SessionScoped
public class LanguageController implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @EJB
    private UserFacade userFacade;

    private String language;
    private static final Map<String, Locale> supportedLanguages;

    static {
        supportedLanguages = new LinkedHashMap<>();
        supportedLanguages.put("Español", new Locale("es"));
        supportedLanguages.put("English", Locale.ENGLISH);
    }

    private Organisation organisation;

    /**
     * Creates a new instance of LanguageController
     */
    public LanguageController() {
    }

    @PostConstruct
    public void init() {

        try {
            organisation = FacesUtil.getCurrentOrganisation();

            // important
            String lan = (String) FacesUtil.getSessionAttribute("language");
            if (lan == null) {
                lan = organisation.getDefaultLanguage();
                FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(lan));
            }

            language = lan;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeLanguage(ValueChangeEvent e) {
        language = (String) e.getNewValue();
        User loggedUser = FacesUtil.getCurrentUser();
        
        if (loggedUser != null) {
            userFacade.changeUserLanguage(loggedUser, language);
        }

        FacesUtil.setSessionAttribute("language", language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(language));        
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Map<String, Locale> getSupportedLanguages() {
        return supportedLanguages;
    }
}
