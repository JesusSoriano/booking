/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moneytransfer.validators;

/**
 *
 * @author Patricia
 */
public class PatternValidator {

    private static final String SUB_DOMAIN_PATTERN = "[a-z]*";
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public static boolean isValidSubDomain(String subDomain) {
        if (subDomain == null) {
            return false;
        }
        return subDomain.matches(SUB_DOMAIN_PATTERN);
    }

    public static boolean isValidIp(final String ip) {
        if (ip == null) {
            return false;
        }
        return ip.matches(IPADDRESS_PATTERN);
    }

    public static void main(String[] args) {
//        System.out.println(isValidSubDomain("dcillia."));
//        System.out.println(isValidSubDomain("http://dcillia"));
//        System.out.println(isValidSubDomain("dcillia"));

    }
}
