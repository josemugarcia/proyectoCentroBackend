package com.hospital.POJO;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@NamedQuery(name = "User.findByEmailId",query = "select u from User u where u.email=:email")


@NamedQuery(name = "User.getAllUser", query = "select new com.hospital.wrapper.UserWrapper(u.id,u.nombre,u.email,u.telefono,u.estado,u.password) from User u where u.rol= 'user'")

@NamedQuery(name = "User.updateStatus", query = "update User u set u.estado=:estado where u.id=:id" )

@NamedQuery(name = "User.getAdmin", query = "select new com.hospital.wrapper.UserWrapper(u.id, u.nombre, u.email, u.telefono, u.estado, u.password) from User u where u.rol = 'admin'")






@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "estado")
    private String estado;

    @Column(name = "rol")
    private String rol;
    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Cita> citas;
    

   
}
