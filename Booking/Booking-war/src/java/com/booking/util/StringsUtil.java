/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.booking.util;

/**
 *
 * @author Jesús Soriano
 */
public class StringsUtil {
    
    public static boolean isNotNullNotEmpty (String string) {
        string = string.trim();
        return (string == null || string.isEmpty());
    }
}
