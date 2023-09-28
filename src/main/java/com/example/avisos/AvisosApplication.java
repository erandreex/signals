package com.example.avisos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

import com.example.avisos.Controladores.ControladorAvisos;

@SpringBootApplication
@EnableAsync
@Profile("!test")
public class AvisosApplication implements CommandLineRunner {

	@Autowired
	ControladorAvisos controladorAvisos;

	public static void main(String[] args) {
		SpringApplication.run(AvisosApplication.class, args);
	}

	public void run(String... args) throws Exception {
		this.controladorAvisos.ValidaAvisos();
	}

}
