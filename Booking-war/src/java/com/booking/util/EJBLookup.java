/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.booking.util;

import javax.naming.InitialContext;

public class EJBLookup {

    public static Object lookUpEJB(Class c) {
        try {
            return new InitialContext().lookup("java:global/Booking/Booking-ejb/" + c.getSimpleName());
        } catch (Exception ex) {
            return null;
        }
    }
}
