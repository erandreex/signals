package com.example.avisos.Threads;

import com.example.avisos.Modelos.ModeloAvisos;
import com.example.avisos.Utilidades.Generales;

public class ThreadValidaAviso extends Thread {

    private ModeloAvisos aviso;

    public ThreadValidaAviso(ModeloAvisos aviso) {
        this.aviso = aviso;
    }

    public void run() {
        System.out.println("se ejecuta " + this.aviso.getConfig_nombre());
        Generales generales = new Generales();

        boolean ejecutaAviso = true;

        try {
            ejecutaAviso = generales.EjecutaAlarma(this.aviso);

            if (ejecutaAviso) {
                this.aviso.setRespuesta_estado("Activa");
            } else {
                this.aviso.setRespuesta_estado("Inactiva");
            }

            generales.actualizaInfoPostEjecucion(this.aviso);

        } catch (Exception e) {
            System.out.println("Error aqui en 123: " + e);
        }
    }

}
