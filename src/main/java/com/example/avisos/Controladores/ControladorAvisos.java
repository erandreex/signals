package com.example.avisos.Controladores;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.avisos.Modelos.ModeloAvisos;
import com.example.avisos.Modelos.ModeloAvisosActivos;
import com.example.avisos.Threads.ThreadValidaAviso;
import com.example.avisos.Utilidades.Generales;

@Component
public class ControladorAvisos {

    public void ValidaAvisos() {

        try {
            List<ModeloAvisosActivos> listaActivas = new ArrayList<>();
            ModeloAvisosActivos avisoInicio = new ModeloAvisosActivos();

            avisoInicio.setId("Inicio");
            avisoInicio.setFecha(Instant.now().getEpochSecond());
            listaActivas.add(avisoInicio);

            while (true) {
                Generales generales = new Generales();
                List<ModeloAvisos> listaAvisos = generales.ConsultaAvisos();
                try {
                    for (ModeloAvisos aviso : listaAvisos) {

                        boolean existeAviso = false;
                        ModeloAvisosActivos avisoActivo = new ModeloAvisosActivos();
                        avisoActivo.setId(aviso.getId());
                        avisoActivo.setFecha(Instant.now().toEpochMilli());

                        for (ModeloAvisosActivos activo : listaActivas) {

                            if (aviso.getId().equalsIgnoreCase(activo.getId())) {

                                Long tiempo = generales.calcularTiempo(aviso.getConfig_intervalo(),
                                        aviso.getConfig_valor());

                                Long diferencia = Instant.now().toEpochMilli() - activo.getFecha();

                                System.out.println("Tiempo: " + tiempo);
                                System.out.println("Diferencia: " + diferencia);

                                if (diferencia >= tiempo) {
                                    // Abrir hilo
                                    new ThreadValidaAviso(aviso).start();
                                    activo.setFecha(Instant.now().toEpochMilli());
                                }
                                existeAviso = true;
                                break;
                            }

                        }

                        if (!existeAviso && !aviso.getId().equals("Inicio")) {
                            // Abrir hilo
                            new ThreadValidaAviso(aviso).start();
                            listaActivas.add(avisoActivo);
                            Thread.sleep(500);
                        }

                    }
                } catch (Exception e) {
                    System.out.println("Error Inciando el hilo " + e);
                }

                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                    System.out.println("Error esperando el main " + e);
                }
            }

        } catch (Exception e) {
            System.out.println("Error obteniendo los resultados " + e);
        }

    }
}
