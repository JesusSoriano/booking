/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.booking.util;

import com.booking.entities.Organisation;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jesús Soriano
 */
public class BookingProperties {

    private static final BookingProperties BOOKING_PROPERTIES = new BookingProperties();
    private static final String OS = System.getProperty("os.name");
    private static final String USER_HOME_PATH = System.getProperty("user.home");
    private static String ROOT_PATH;

    static {
        try {
            // localhost development purpose
            if (OS.toLowerCase().startsWith("windows")) {
                ROOT_PATH = USER_HOME_PATH + "\\project_files\\booking\\";
            } else if (OS.contains("OS X")) {
                ROOT_PATH = USER_HOME_PATH + "/project_files/booking/";
            } else {
                // using System.getProperty("user.name") because if wildfly running as root
                // it gives path for user.home is /root/ not /home/root/ or something below way would work all user types
                String path = "/home/" + System.getProperty("user.name");
                ROOT_PATH = path + "/project_files/booking/";
            }

            File pathFolder = new File(ROOT_PATH);
            if (!pathFolder.exists()) {
                pathFolder.mkdirs();
            }
        } catch (Throwable e) {
            Logger.getLogger(BookingProperties.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public String getProjectRootPath() {
        return ROOT_PATH;
    }

    public String getUploadedFilesPath(Organisation organisation) {
        String organisationPath = getProjectRootPath() + organisation.getDomainName() + File.separator + "uploadedFiles" + File.separator;
        File organisationPathFolder = new File(organisationPath);
        if (!organisationPathFolder.exists()) {
            organisationPathFolder.mkdirs();
        }

        return organisationPath;
    }

    public static BookingProperties getInstance() {
        return BOOKING_PROPERTIES;
    }
}
