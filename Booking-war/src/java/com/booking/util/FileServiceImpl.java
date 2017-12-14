/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.booking.util;

import com.booking.entities.Organisation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Jesús Soriano
 */
public class FileServiceImpl implements FileService {

    private final BookingProperties bookingProperties = BookingProperties.getInstance();
    private static final long MAX_FILE_SIZE = 10485760; // maximum 10 mb
    private static final String ALPHA_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    @Override
    public String uploadDocument(String fileName, InputStream inputStream, Organisation organisation) throws Exception {

        fileName = getCode(5) + "_" + fileName;

        File file = new File(bookingProperties.getUploadedFilesPath(organisation) + fileName);
        OutputStream output = new FileOutputStream(file);
        IOUtils.copy(inputStream, output);
        IOUtils.closeQuietly(output);
        return fileName;
    }

    @Override
    public boolean deleteDocument(Organisation organisation, String name) {
        try {
            File file = new File(bookingProperties.getUploadedFilesPath(organisation) + name);
            return file.delete();
        } catch (Exception e) {
            Logger.getLogger(FileServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    @Override
    public boolean supportedFileSize(long size) {
        return size <= MAX_FILE_SIZE;
    }

    @Override
    public InputStream findDocumentStream(String fileName, Organisation organisation) {
        try {
            String docPath = bookingProperties.getUploadedFilesPath(organisation);
            return new FileInputStream(new File(docPath, fileName));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void createOrganisationFolders(Organisation organisation) {

        try {
            BookingProperties tempBookingProperties = BookingProperties.getInstance();
            String rootPath = tempBookingProperties.getProjectRootPath();

            System.out.println("\n");
            System.out.println("Creating organisation folders " + organisation.getDomainName());
            System.out.println("Root path: " + rootPath);
            // Main Organisation folder
            File mainFolder = new File(rootPath, organisation.getDomainName());
            System.out.println("Main folder created: " + mainFolder.mkdirs());

            // Organisation uploaded files folder
            File uploadedFilesFolder = new File(mainFolder, "uploadedFiles");
            System.out.println("Resources folder created: " + uploadedFilesFolder.mkdir());

        } catch (Exception e) {
            Logger.getLogger(FileServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    public static String getCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int x = 0; x < length; x++) {
            sb.append(ALPHA_NUM.charAt((int) (Math.random() * ALPHA_NUM.length())));
        }
        return shuffle(sb.toString());
    }

    private static String shuffle(String input) {
        List<Character> characters = new ArrayList<>();
        for (char c : input.toCharArray()) {
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(input.length());
        while (!characters.isEmpty()) {
            int randPicker = (int) (Math.random() * characters.size());
            output.append(characters.remove(randPicker));
        }
        return output.toString();
    }
}
