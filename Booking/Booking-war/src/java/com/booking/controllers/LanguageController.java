package com.booking.controllers;

import com.booking.entities.Organisation;
import com.booking.language.I18NSupport;
import com.booking.util.FacesUtil;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
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

    private Organisation organisation;
    private String language;
    private Map<String, Locale> supportedLanguages = new LinkedHashMap<>();

    /**
     * Creates a new instance of LanguageController
     */
    public LanguageController() {
    }

    @PostConstruct
    public void init() {

        try {
            organisation = FacesUtil.getCurrentOrganisation();
            supportedLanguages = I18NSupport.getSupportedLanguages();

            // important
            String lan = (String) FacesUtil.getSessionAttribute("language");
            if (lan == null) {
                lan = organisation.getDefaultLanguage() ;
                FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(lan));
            }

            language = lan;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeLanguage (ValueChangeEvent e) {
        language = (String) e.getNewValue();

        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(language));
        FacesUtil.setSessionAttribute("language", language);
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
