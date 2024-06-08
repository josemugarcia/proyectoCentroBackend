package com.hospital.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import com.hospital.POJO.User;
import com.hospital.wrapper.UserWrapper;

public interface UserDao extends JpaRepository<User,Integer> {

    User findByEmailId(@Param("email") String email);
    List<UserWrapper> getAllUser();


    
    List<UserWrapper> getAdmin();


    @Transactional
    @Modifying
    Integer updateStatus(@Param("estado") String estado, @Param("id") Integer id);

    User findByEmail(String email);

} 
