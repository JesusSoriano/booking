/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.booking.language;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;


public class I18NSupport extends ResourceBundle {

    private static final Map<String, Locale> supportedLanguages = new LinkedHashMap<>();

    static {
        supportedLanguages.put("Español", new Locale("es"));
        supportedLanguages.put("English", Locale.ENGLISH);
    }

    protected static final String BUNDLE_NAME = "com.booking.language.text";
    protected static final String BUNDLE_EXTENSION = "properties";
    protected static final Control UTF8_CONTROL = new UTF8Control();

    public I18NSupport() {
        setParent(ResourceBundle.getBundle(BUNDLE_NAME,
                FacesContext.getCurrentInstance().getViewRoot().getLocale(), UTF8_CONTROL));
    }

    @Override
    protected Object handleGetObject(String key) {
        return parent.getObject(key);
    }

    @Override
    public Enumeration getKeys() {
        return parent.getKeys();
    }

    protected static class UTF8Control extends Control {

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            // The below code is copied from default Control#newBundle() implementation.
            // Only the PropertyResourceBundle line is changed to read the file as UTF-8.
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
//            System.out.println(baseName + " " + locale);
//            System.out.println("bundleName- " + bundleName);
//            System.out.println("resourceName- " + resourceName);
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }

    public static Map<String, Locale> getSupportedLanguages() {
        return supportedLanguages;
    }
}
