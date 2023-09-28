package com.example.avisos.Utilidades;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.example.avisos.Conexiones.ConexionJSCH;
import com.example.avisos.Conexiones.ConnectionPOOL;
import com.example.avisos.Modelos.ModeloAvisos;
import com.example.avisos.Modelos.ModeloSelectTabla;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class Generales {

    public List<ModeloAvisos> ConsultaAvisos() {

        List<ModeloAvisos> listaAvisos = new ArrayList<>();
        String query = "{CALL admin_avisos.sp_admin_avisos_consultas(?,?,?)}";

        try (Connection mariaDB = ConnectionPOOL.getConnection();
                CallableStatement cst = mariaDB.prepareCall(query)) {
            cst.setString(1, "Q");
            cst.setString(2, "QTAA");
            cst.setString(3, null);

            ResultSet rs = cst.executeQuery();
            while (rs.next()) {
                ModeloAvisos res = new ModeloAvisos();
                res.setId(rs.getString("aal_id"));
                res.setConfig_tipo(rs.getString("aal_config_tipo"));
                res.setConfig_nombre(rs.getString("aal_config_nombre"));
                res.setConfig_intervalo(rs.getString("aal_config_intervalo"));
                res.setConfig_valor(rs.getString("aal_config_valor"));
                res.setConfig_mensaje(rs.getString("aal_config_mensaje"));
                res.setMeta_fecha_creacion(rs.getString("aal_meta_fecha_creacion"));
                res.setMeta_ultima_ejecucion(rs.getString("aal_meta_ultima_ejecucion"));
                res.setEjecucion_tipo(rs.getString("aal_ejecucion_tipo"));
                res.setEjecucion_servidor(rs.getString("aal_ejecucion_servidor"));
                res.setEjecucion_comando(rs.getString("aal_ejecucion_comando"));
                res.setRespuesta_estado(rs.getString("aal_respuesta_estado"));
                res.setRespuesta_config(rs.getString("aal_respuesta_config"));
                res.setRespuesta_cantidad_col(rs.getString("aal_respuesta_cantidad_col"));
                res.setRespuesta_tipo(rs.getString("aal_respuesta_tipo"));
                res.setRespuesta_comparacion(rs.getString("aal_respuesta_comparacion"));
                res.setRespuesta_esperada(rs.getString("aal_respuesta_comparacion"));
                res.setCorreo_destinatarios(rs.getString("aal_respuesta_comparacion"));
                res.setAccion_tipo(rs.getString("aal_accion_tipo"));
                res.setAccion_componente_id(rs.getString("aal_accion_compenente_id"));
                res.setBandera_accion(rs.getString("aal_bandera_accion"));
                res.setBandera_enviar_correo(rs.getString("aal_bandera_enviar_correo"));
                res.setBandera_activo(rs.getString("aal_bandera_activo"));
                listaAvisos.add(res);
            }

        } catch (Exception e) {
            System.out.println("Error en la obtencion de avisos QTAA");
        }

        return listaAvisos;
    }

    public Long calcularTiempo(String intervalo, String intervalo_valor) {

        int resp = 0;
        int valor = Integer.parseInt(intervalo_valor);
        switch (intervalo) {
            case "segundos":
                resp = 1000 * valor;
                break;
            case "minutos":
                resp = 1000 * 60 * valor;
                break;
            case "horas":
                resp = 1000 * 60 * 60 * valor;
                break;
            case "dias":
                resp = 1000 * 60 * 60 * 24 * valor;
                break;
            default:
                resp = 1000 * valor;
                break;
        }

        return Long.valueOf(resp);

    }

    public Boolean EjecutaAlarma(ModeloAvisos aviso) {

        Boolean respAlarmas = false;
        String validaResp = null;
        String obtenido = "";
        String tablaRespuesta = "";
        String ultimaFechaCorreo = "NO";
        ProcesosMariaDB procesosMariaDB = new ProcesosMariaDB();
        ConexionJSCH conexionJSCH = new ConexionJSCH();
        String respuesta_config = aviso.getRespuesta_config();

        List<String> listaTemporal = new ArrayList<>();

        try {

            switch (aviso.getEjecucion_tipo().toLowerCase()) {
                case "unix":
                    obtenido = conexionJSCH.ConsultaSSH(aviso.getConfig_nombre(), aviso.getEjecucion_servidor(), "user",
                            "password", aviso.getEjecucion_comando());
                    break;

                case "mariadb":

                    if (aviso.getRespuesta_config().equalsIgnoreCase("Simple")) {
                        listaTemporal = procesosMariaDB.EjecutaSelectSimple(aviso);
                        obtenido = listaTemporal.get(1);
                    }

                    if (aviso.getRespuesta_config().equalsIgnoreCase("Diferencia")) {
                        listaTemporal = procesosMariaDB.EjecutaSelectFechaDiferencia(aviso);
                        ultimaFechaCorreo = listaTemporal.get(1);
                        obtenido = listaTemporal.get(2);
                    }

                    if (aviso.getRespuesta_config().equalsIgnoreCase("Tabla")) {
                        tablaRespuesta = procesosMariaDB.EjecutaSelectTabla(aviso);
                    }

                    break;

                default:
                    break;
            }

            if (!tablaRespuesta.equalsIgnoreCase("NO_RETURN_DATA")) {
                respAlarmas = true;
                System.out.println(tablaRespuesta);
            }

            if (respuesta_config.equalsIgnoreCase("Simple")) {
                respAlarmas = comparacion(aviso.getRespuesta_tipo(), aviso.getRespuesta_comparacion(),
                        aviso.getRespuesta_esperada(), obtenido);
            }

            if (respuesta_config.equalsIgnoreCase("Diferencia")) {
                respAlarmas = comparacion(aviso.getRespuesta_tipo(), aviso.getRespuesta_comparacion(),
                        aviso.getRespuesta_esperada(), obtenido);
            }

        } catch (Exception e) {
            System.out.println("Aqui errororororro" + e);
        }

        return respAlarmas;
    }

    public boolean comparacion(String respuesta_tipo, String respuesta_comparacion, String resp_esperada,
            String valor_obtenido) {

        Boolean resp = true;

        if (respuesta_tipo.equalsIgnoreCase("int")) {

            if ("igual que".equalsIgnoreCase(respuesta_comparacion)) {
                if (Integer.parseInt(valor_obtenido) != Integer.parseInt(resp_esperada)) {
                    resp = false;
                }
            }

            if ("menor que".equalsIgnoreCase(respuesta_comparacion)) {
                if (Integer.parseInt(valor_obtenido) > Integer.parseInt(resp_esperada)) {
                    resp = false;
                }
            }

            if ("mayor que".equalsIgnoreCase(respuesta_comparacion)) {
                if (Integer.parseInt(valor_obtenido) < Integer.parseInt(resp_esperada)) {
                    resp = false;
                }
            }

            if ("distinto que".equalsIgnoreCase(respuesta_comparacion)) {
                if (Integer.parseInt(valor_obtenido) == Integer.parseInt(resp_esperada)) {
                    resp = false;
                }
            }

            if ("menor o igual que".equalsIgnoreCase(respuesta_comparacion)) {
                if (Integer.parseInt(valor_obtenido) >= Integer.parseInt(resp_esperada)) {
                    resp = false;
                }
            }

            if ("mayor o igual que".equalsIgnoreCase(respuesta_comparacion)) {
                if (Integer.parseInt(valor_obtenido) <= Integer.parseInt(resp_esperada)) {
                    resp = false;
                }
            }
        }

        if (respuesta_tipo.equalsIgnoreCase("string")) {

            if ("igual que".equalsIgnoreCase(respuesta_comparacion)) {
                if (!valor_obtenido.equalsIgnoreCase(resp_esperada)) {
                    resp = false;
                }
            }

            if ("like".equalsIgnoreCase(respuesta_comparacion)) {
                if (!valor_obtenido.contains(resp_esperada)) {
                    resp = false;
                }
            }

        }

        return resp;

    }

    public void PrepararCorreo(ModeloAvisos aviso, String tabla_respuesta, String obtenido, String diferencia) {

        String from = "ersolis021@gmail.com";
        String para = aviso.getCorreo_destinatarios();
        String asunto = "";
        String mensaje = aviso.getConfig_mensaje();
        String correo_mensaje = "";
        String host = "";

        mensaje = mensaje.replace("\n", "<br>");
        mensaje = mensaje.replace("\t", "&nbsp; &nbsp; &nbsp; ");

        if ("Alerta".equalsIgnoreCase(aviso.getConfig_tipo())) {
            asunto = "Alerta - " + aviso.getConfig_nombre();
        }

        if ("Informacion".equalsIgnoreCase(aviso.getConfig_tipo())) {
            asunto = "Informacion - " + aviso.getConfig_nombre();
        }

        if ("Aviso".equalsIgnoreCase(aviso.getConfig_tipo())) {
            asunto = "Aviso - " + aviso.getConfig_nombre();
        }

        if ("Notificacion".equalsIgnoreCase(aviso.getConfig_tipo())) {
            asunto = "Notificacion - " + aviso.getConfig_nombre();
        }

        if ("Simple".equalsIgnoreCase(aviso.getRespuesta_config())) {
            correo_mensaje = "Favor validar" + "<br><br>" + mensaje;
        }

        if ("Diferencia".equalsIgnoreCase(aviso.getRespuesta_config())) {
            correo_mensaje = "El ultimo registro fue: " + diferencia + "<br><br>" + mensaje;
        }

        if ("Tabla".equalsIgnoreCase(aviso.getRespuesta_config())) {
            correo_mensaje = mensaje + "<br><br>" + tabla_respuesta;
        }

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        Session session = Session.getDefaultInstance(properties);

        try {

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(para));
            message.setSubject(asunto);
            message.setText(correo_mensaje);
            MimeMultipart multipart = new MimeMultipart("related");
            BodyPart messageBodyPart = new MimeBodyPart();
            String htmlText = correo_mensaje;
            messageBodyPart.setContent(htmlText, "text/html");
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);

            Transport.send(message);

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    public void actualizaInfoPostEjecucion(ModeloAvisos aviso) {

        String query = "{call admin_avisos.sp_admin_avisos_ejecucion(?,?,?,?)}";

        try (Connection mariaDB = ConnectionPOOL.getConnection();
                CallableStatement cst = mariaDB.prepareCall(query)) {

            System.out.println(aviso.getRespuesta_estado());
            cst.setString(1, "U");
            cst.setString(2, "UEA");
            cst.setString(3, aviso.getId());
            cst.setString(4, aviso.getRespuesta_estado());
            cst.execute();

        } catch (Exception e) {
            System.out.println("Error actualizaInfoPostEjecucion: " + e);

        }

    }

    public String convertirResultadoEnTabla(ResultSet rs) {

        String tabla_respuesta = "<table style= 'border-collapse: collapse; width: 100%;'>";

        try {

            ResultSetMetaData rsmd = rs.getMetaData();
            int cant_columnas = rsmd.getColumnCount();

            for (int i = 1; i < cant_columnas; i++) {
                tabla_respuesta += "<th style='text-align: left; background-color: #003d76; color: white;'>"
                        + rsmd.getColumnLabel(i) + "</th>";
            }

            tabla_respuesta += "<tr style='border:solid 1px black'>";

            while (rs.next()) {

                for (int i = 1; i < cant_columnas; i++) {
                    tabla_respuesta += "<td style=' text-align: left; padding: 8px; border-bottom: 1px solid #ddd;'>"
                            + rs.getString(i) + "</td>";
                }

                tabla_respuesta = tabla_respuesta + "</tr>";
            }

            tabla_respuesta += "</table>";

        } catch (Exception e) {
            // TODO: handle exception
        }

        return tabla_respuesta;

    }

}
