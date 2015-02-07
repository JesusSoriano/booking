package com.booking.enums;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jesus Soriano
 */
public enum Roles {

    CLIENT("Cliente"),
    ADMIN("Administrador");

    private final String name;

    private Roles(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public static List<Roles> getAllRoles() {
        List<Roles> roles = new ArrayList<>();
        roles.add(CLIENT);
        roles.add(ADMIN);
        return roles;
    }

}
