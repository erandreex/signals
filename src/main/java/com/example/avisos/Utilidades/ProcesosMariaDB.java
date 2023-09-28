package com.example.avisos.Utilidades;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.avisos.Conexiones.ConnectionPOOL;
import com.example.avisos.Modelos.ModeloAvisos;

public class ProcesosMariaDB {

    private Generales procesosMariaDB = new Generales();

    public String EjecutaSelectTabla(ModeloAvisos aviso) {

        String comando = aviso.getEjecucion_comando();
        String resultadoTabla = "NO_RETURN_DATA";

        try (Connection mariaDB = ConnectionPOOL.getConnection();
                Statement st = mariaDB.createStatement();) {

            ResultSet rs = st.executeQuery(comando);
            if (rs.first()) {
                resultadoTabla = this.procesosMariaDB.convertirResultadoEnTabla(rs);
            }

        } catch (Exception e) {
            System.out.println("Error EjecutaSelectTablaFinal: " + e);
        }

        return resultadoTabla;

    }

    public List<String> EjecutaSelectSimple(ModeloAvisos aviso) {

        List<String> listaValor = new ArrayList<>();
        String comando = aviso.getEjecucion_comando();

        try (Connection mariaDB = ConnectionPOOL.getConnection();
                Statement st = mariaDB.createStatement();) {

            ResultSet rs = st.executeQuery(comando);
            ResultSetMetaData rsmd = rs.getMetaData();

            if (rsmd.getColumnCount() == 2) {

                while (rs.next()) {
                    listaValor.add(rs.getString(1));
                    listaValor.add(rs.getString(2));
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error EjecutaSelectSimple: " + e);
            listaValor.add("CHECK");
            listaValor.add("CHECK");
        }

        return listaValor;
    }

    public List<String> EjecutaSelectFechaDiferencia(ModeloAvisos aviso) {

        List<String> listaValor = new ArrayList<>();
        String comando = aviso.getEjecucion_comando();

        try (Connection mariaDB = ConnectionPOOL.getConnection();
                Statement st = mariaDB.createStatement();) {

            ResultSet rs = st.executeQuery(comando);
            ResultSetMetaData rsmd = rs.getMetaData();

            if (rsmd.getColumnCount() == 3) {

                while (rs.next()) {
                    listaValor.add(rs.getString(1));
                    listaValor.add(rs.getString(2));
                    listaValor.add(rs.getString(3));
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error EjecutaSelectFecha: " + e);
            listaValor.add("CHECK");
            listaValor.add("CHECK");
            listaValor.add("CHECK");

        }

        return listaValor;
    }

}
