/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moneytransfer.validators;

/**
 *
 * @author Anita
 */
public final class DoubleValidator {

    public static boolean isValidDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
