package com.booking.controllers;

import com.booking.util.FacesUtil;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

@ManagedBean
public class InfoController implements Serializable {
    
    private String infoType;
    
    /**
     * Creates a new instance of InfoController
     */
    public InfoController() {
    }

    @PostConstruct
    public void init() {
        infoType = (String) FacesUtil.getParameter("info");
    }


    public String getInfoType() {
        return infoType;
    }

}
