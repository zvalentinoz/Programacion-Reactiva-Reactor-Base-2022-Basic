package com.bolsadeideas.springboot.reactor.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bolsadeideas.springboot.reactor.app.models.Usuario;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	//importamos un Logger para ver mejor los datos 
	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploFlatMap();
		
		
	}
	
	//ejemploFlatMap
	
	public void ejemploFlatMap() throws Exception {
		
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Fultaño");
		usuariosList.add("Pedro Torres");
		usuariosList.add("Maria esmeralda");
		usuariosList.add("Juan pelarez");
		usuariosList.add("Marcos kistom ");
		usuariosList.add("Marcos lopez");
		
		
		
          Flux.fromIterable(usuariosList)
          	.map(nomb -> new Usuario(nomb.split(" ")[0].toUpperCase(), nomb.split(" ")[1].toUpperCase() ))
          	.flatMap(usuario -> { 
          		if(usuario.getNombre().equalsIgnoreCase("marcos") ) { 
				return Mono.just(usuario);
			}else {
				return Mono.empty();
			}
		}) 
		  .map(usuario2 -> {
			String nombre = usuario2.getNombre().toLowerCase();
			usuario2.setNombre(nombre);
		  	return usuario2;
		  })    
		.subscribe( u -> log.info(u.toString() ));
	}
	
	
	//metodo EjemploIterable
	public void ejemploIterable() throws Exception {
		
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Fultaño");
		usuariosList.add("Pedro Torres");
		usuariosList.add("Maria esmeralda");
		usuariosList.add("Juan pelarez");
		usuariosList.add("Marcos kistom ");
		usuariosList.add("Ernesto lopez");
		
		
		
		Flux<String> nombres = Flux.fromIterable(usuariosList);
				/* Flux.just("Andres Fultaño","Pedro Torres","Maria esmeralda" , 
				"Juan pelarez","Marcos kistom ","Ernesto lopez"); */
		
			Flux<Usuario> Usuarios = nombres.map(nomb -> new Usuario(nomb.split(" ")[0].toUpperCase(), nomb.split(" ")[1].toUpperCase()  ))
				.filter(n -> n.getNombre().toLowerCase().equals("maria") )
				.doOnNext(usuario -> { 
					if(usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacios");
					}
				 System.out.println( usuario.getNombre().concat(" ").concat(usuario.getApellido()) );	 
	})   
		.map(usuario2 -> {
			String nombre = usuario2.getNombre().toLowerCase();
		  usuario2.setNombre(nombre);
		  return usuario2;
	}); 
	      
			Usuarios.subscribe(e -> log.info(e.toString()) ,
				errors -> log.error(errors.getMessage()) ,
				new Runnable() {
					
					@Override
					public void run() {
					
					 log.info("Ha finalizado la ejecucion observable con exito!");	
					}
					
				});
		
		
	}
	
	
	
	
	
}
