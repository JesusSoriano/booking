package com.booking.controllers;

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
        supportedLanguages.put("Français", Locale.FRENCH);
    }

    /**
     * Creates a new instance of LanguageController
     */
    public LanguageController() {
    }

    @PostConstruct
    public void init() {
        // Default language
        // TODO: get the language depending on the organisation (url based)
        language = "es";
    }

    public void changeLanguage(ValueChangeEvent e) {
        language = (String) e.getNewValue();
        User loggedUser = FacesUtil.getCurrentUser();

        if (loggedUser != null) {
            userFacade.changeUserLanguage(loggedUser, language);
        }
        setLocale();
    }

    public void getUserApplicationLanguage() {
        User loggedUser = FacesUtil.getCurrentUser();

        if (loggedUser != null) {
            String userLanguage = loggedUser.getApplicationLanguage();

            if (userLanguage != null) {
                language = userLanguage;
                setLocale();
            }
        }
    }

    private void setLocale() {
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
