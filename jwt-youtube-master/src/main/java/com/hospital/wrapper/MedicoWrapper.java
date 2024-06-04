package com.hospital.wrapper;

import lombok.Data;

@Data

public class MedicoWrapper {
    Integer idMedico;

    String nombreMedico;

    Integer telefonoMedico;

    Integer edadMedico;

    String imagenMedico;

    String status;

    Integer especialidadId;

    String especialidadNombre;

    String especialidadImagen;

    public MedicoWrapper() {

    }

    public MedicoWrapper(Integer idMedico, String nombreMedico, Integer telefonoMedico, Integer edadMedico,
            String imagenMedico, String status, Integer especialidadId, String especialidadNombre,
            String especialidadImagen) {
        this.idMedico = idMedico;
        this.nombreMedico = nombreMedico;
        this.telefonoMedico = telefonoMedico;
        this.edadMedico = edadMedico;
        this.imagenMedico = imagenMedico;
        this.status = status;
        this.especialidadId = especialidadId;
        this.especialidadNombre = especialidadNombre;
        this.especialidadImagen = especialidadImagen;

    }

    public MedicoWrapper(Integer idMedico, String nombreMedico){
        this.idMedico = idMedico;
        this.nombreMedico = nombreMedico;
    }
}
