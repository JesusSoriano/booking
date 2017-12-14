package com.booking.enums;

/**
 *
 * @author Jesús Soriano
 */
public enum Role {

    USER("Usuario"),
    ADMIN("Administrador"),
    SUPER_ADMIN("Superadministrador del sistema");

    private final String name;

    private Role(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isAdmin(Role role) {
        return role == ADMIN || role == SUPER_ADMIN;
    }
}
