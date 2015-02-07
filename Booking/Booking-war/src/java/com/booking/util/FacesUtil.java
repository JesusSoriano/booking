package com.booking.util;

import com.booking.entities.User;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Jesus Soriano
 */
public class FacesUtil implements Serializable {

    public static ExternalContext getExternalContext() {
        return FacesContext.getCurrentInstance().getExternalContext();
    }

    public static String getPageReferer() {
//        return FacesContext.getCurrentInstance().getViewRoot().getViewId();
        return getRequest().getHeader("referer");
    }

    public static Flash getFlash() {
        return getExternalContext().getFlash();
    }

    public static HttpServletRequest getRequest() {
        return (HttpServletRequest) getExternalContext().getRequest();
    }

    public static HttpSession getHttpSession() {
        return (HttpSession) getRequest().getSession();
    }

    public static void setSessionAttribute(String name, Object value) {
        getHttpSession().setAttribute(name, value);
    }

    public static Object getSessionAttribute(String name) {
        return getHttpSession().getAttribute(name);
    }

    public static void removeSessionAttribute(String name) {
        getHttpSession().removeAttribute(name);
    }

    public static HttpServletResponse getResponse() {
        return (HttpServletResponse) getExternalContext().getResponse();
    }

    public static String getParameter(String key) {
        return getRequest().getParameter(key);
    }

    public static void addErrorMessage(String clientId, String message) {
        FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
        getFlash().setKeepMessages(true);
    }

    public static void addSuccessMessage(String clientId, String message) {
        FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
        getFlash().setKeepMessages(true);
    }

    public static User getCurrentUser() {
        return (User) getHttpSession().getAttribute(Constants.CURRENT_USER);
    }

    public static void redirectTo(String path) throws IOException {
        getExternalContext().redirect(path + Constants.FC); // ?faces-redirect=true
    }

    public static void redirectTo(String path, String params) throws IOException {
        getExternalContext().redirect(path + Constants.FC + params); // ?faces-redirect=true
    }

    public static void addToApplicationContext(String key, Object object) {
        ServletContext servletContext = (ServletContext) FacesUtil.getExternalContext().getContext();
        List<Object> objects = (List<Object>) servletContext.getAttribute(key);

        if (objects == null) {
            objects = new ArrayList<>();
            servletContext.setAttribute(key, objects);
        }
        objects.add(object);
    }

    public static Object getApplicationContextObject(String key) {
        ServletContext servletContext = (ServletContext) FacesUtil.getExternalContext().getContext();
        return servletContext.getAttribute(key);
    }

    public static String getCurrentIPAddress() {
        HttpServletRequest request = getRequest();
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        
        return ipAddress;
    }

}
