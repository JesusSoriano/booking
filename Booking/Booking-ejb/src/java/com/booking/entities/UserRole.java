package com.booking.entities;

import com.booking.enums.Roles;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Jesus Soriano
 */
@Entity
@Table(name = "user_roles")
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "role_name")
    @Enumerated(EnumType.STRING)
    private Roles userRole;
    @OneToOne
    @JoinColumn(name = "email", referencedColumnName = "email")
    private User user;

    public UserRole() {
    }

    public long getId() {
        return id;
    }

    public Roles getUserRole() {
        return userRole;
    }

    public void setUserRole(Roles userRole) {
        this.userRole = userRole;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserRole other = (UserRole) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return "[ " + id + " - " + userRole + " ]";
    }
}
