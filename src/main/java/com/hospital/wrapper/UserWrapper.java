package com.hospital.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserWrapper {


    // UserWrapper user = new com.hospital.wrapper.UserWrapper(1,"abc", "abc@gmail.com","76543","false");
    
    private Integer id;
    private String nombre;
    private String email;
    private String telefono;
    private String estado;
    private String password;

    
    public UserWrapper(Integer id, String nombre, String email, String telefono, String estado,String password) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.estado = estado;
        this.password = password;
    }

   
    
}


