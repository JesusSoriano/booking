package com.booking.controllers;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 *
 * @author Jesús Soriano
 */
@ManagedBean
@RequestScoped
public class DateFormatController {

    private String dateFormatString ;
    private String timeFormatString;
    

    /**
     * Creates a new instance of UserSessionDetecter
     */
    public DateFormatController() {
       dateFormatString = "dd/MM/yyyy";
       timeFormatString = "HH:mm";       
    }

    @PostConstruct
    public void init() {

    }

    public String getDateFormatString() {
        return dateFormatString;
    }

    public void setDateFormatString(String dateFormatString) {
        this.dateFormatString = dateFormatString;
    }

    public String getTimeFormatString() {
        return timeFormatString;
    }

    public void setTimeFormatString(String timeFormatString) {
        this.timeFormatString = timeFormatString;
    }
    
    public String getDateWithTimeFormatString() {
        return dateFormatString + " " + timeFormatString;
    }
    
}
