package com.library.management.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    private String username;


    private String password;


    private String email;


    private String fullName;


    private String avatar;


    private String phone;


    private String address;


    private LocalDate dob;


    private Boolean enabled = true;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

}
