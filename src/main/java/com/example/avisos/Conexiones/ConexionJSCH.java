package com.example.avisos.Conexiones;

import java.io.InputStream;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class ConexionJSCH {

    public String ConsultaSSH(String nombre, String servidor, String usuario, String password, String comando) {
        String salida = "";

        try {
            Properties config = new Properties();
            config.put("StricHostKeyChecking", "no");
            config.put("PreferredAuthentications", "password");
            JSch jsch = new JSch();
            Session session = jsch.getSession(usuario, password, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.setTimeout(10000);
            session.connect();

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(comando);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect();

            byte[] tmp = new byte[1024];
            int bandera;

            do {
                int i = in.read(tmp, 0, 1024);
                bandera = 0;
                if (i >= 0) {
                    salida = salida + new String(tmp, 0, i);
                }
                while (in.available() <= 0) {
                    if (channel.isClosed()) {
                        if (in.available() <= 0) {
                            bandera = 1;
                            break;
                        }
                    } else {
                        try {
                            Thread.sleep(1000L);
                        } catch (Exception e) {
                            channel.disconnect();
                            session.disconnect();
                            System.out.println("Error 1 ConsultaSSH: " + e);
                        }
                    }
                }
            } while (bandera != 1);

            channel.disconnect();
            session.disconnect();

        } catch (Exception e) {
            System.out.println("Error 2 ConsultaSSH: " + e);
            salida = "CHECK";
        }

        try {
            Thread.sleep(2000);
            salida = salida.replace("\n", "");
        } catch (Exception e) {
            System.out.println("Error 3 ConsultaSSH: " + e);
            salida = "CHECK";

        }

        return salida;
    }
}
