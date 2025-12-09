package com.bolsadeideas.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bolsadeideas.springboot.reactor.app.models.Comentarios;
import com.bolsadeideas.springboot.reactor.app.models.Usuario;
import com.bolsadeideas.springboot.reactor.app.models.UsuarioComentarios;

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
		ejemploContraPresion();
		
		
	}
	
	public void ejemploContraPresion() { 
		
		Flux.range(1, 10)
		.log()
		.limitRate(5)
		.subscribe(/* new Subscriber<Integer>() {
			
			private Subscription s;
			
			private Integer limite = 5;
			
			private Integer consumido = 0;

			@Override
			public void onSubscribe(Subscription s) {
				this.s = s;
				s.request(limite);
				
				
			}

			@Override
			public void onNext(Integer t) {
			   log.info(t.toString());
			   consumido++;
			   if(consumido == limite) { 
				   consumido = 0;
				   s.request(limite);
			   }
			}

			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}
		}*/);
		
	}
	
	// intervalor
	public void ejemploIntervaloDesdeCreate() { 
		Flux.create(emitter -> { 
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				
				private Integer contador = 0;
				
				@Override
				public void run() {
				 emitter.next(++contador);
					if(contador == 10) { 
						timer.cancel();
						emitter.complete();
					}
					
					if(contador == 5) { 
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5"));
					}
					
				}
			}, 1000,1000);
		})
		.subscribe(next -> log.info(next.toString()) ,
				e -> log.error(e.getMessage()) , 
				() -> log.info("Hemos terminado"));
	}
	
	
			// ejemploIntervaloInfinitoPrueba
	public void ejemploIntervaloInfinito() throws InterruptedException { 
		
		CountDownLatch latch = new CountDownLatch(1);
		
		Flux.interval(Duration.ofSeconds(1))
		.doOnTerminate(latch::countDown)
		.flatMap( i -> { 
			if( i >= 5) { 
				return Flux.error(new InterruptedException("Solo hasta 5"));
			}
			return Flux.just(i);
		})
		.map(i -> "Hola " + i)
		.retry(2)
		.subscribe(s -> log.info(s) , e -> log.error(e.getMessage()));
		
		latch.await();
		
	}
	
	
	 // intervalos de segundos 
	
	   public void ejemploDelayElements()   { 
		   Flux<Integer> rango = Flux.range(1, 12)
		   .delayElements(Duration.ofSeconds(1))
		   .doOnNext(i -> log.info(i.toString()));
		   
		   rango.blockLast();
		   
	   }
	
	
	
	 // intervalos de segundos 
	   public void ejemploInterval() { 
		   Flux<Integer> rango = Flux.range(1, 12);
		   Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		   
		   rango.zipWith(retraso , (ra , re) -> ra )
		   .doOnNext( i -> log.info(i.toString()))
		   .blockLast();
	   }
	
	
	//usando range
		public void ejemploZipWithRangos() { 
		 
			Flux.just(1,2,3,4)
			.map(v -> (v * 2))
			.zipWith(Flux.range(0, 4) , (uno , dos) -> String.format("Primer Flux: %d , Segundo Flux: %d", uno, dos) )
			.subscribe(texto -> log.info(texto));
						
		}
	
	
	
	// convertir usario&COmentarios con zipWith forma 2da sin BI
			public void ejemploUsuarioComentariosZipWithForma2() { 
				Mono<Usuario> UsuarioMono = Mono.fromCallable(() -> new Usuario("marct" , "Churs"));
				Mono<Comentarios> ComentarioUsuarioMono = Mono.fromCallable(() -> {  
					Comentarios comentarios =  new Comentarios();
					comentarios.addComentarios("Hola marcct , que tal !");
					comentarios.addComentarios("mñn se juega ajedrez");			
					comentarios.addComentarios("estare practicando ahora");
					return comentarios;
				});
				
				Mono<UsuarioComentarios> usuarioConComentarios = 
					UsuarioMono.zipWith(ComentarioUsuarioMono)
					.map(tuple -> { 
						Usuario u = tuple.getT1();
						Comentarios c = tuple.getT2();
						return new UsuarioComentarios(u, c);
					});
				
				usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
				
			}
	
	
	// convertir usario&COmentarios con zipWith forma BI
		public void ejemploUsuarioComentariosZipWith() { 
			Mono<Usuario> UsuarioMono = Mono.fromCallable(() -> new Usuario("marct" , "Churs"));
			Mono<Comentarios> ComentarioUsuarioMono = Mono.fromCallable(() -> {  
				Comentarios comentarios =  new Comentarios();
				comentarios.addComentarios("Hola marcct , que tal !");
				comentarios.addComentarios("mñn se juega ajedrez");			
				comentarios.addComentarios("estare practicando ahora");
				return comentarios;
			});
			
			Mono<UsuarioComentarios> usuarioConComentarios = 
				UsuarioMono.zipWith(ComentarioUsuarioMono , (u,cu) -> new UsuarioComentarios(u, cu));
			
			usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
			
		}
	
	
	
	// convertidor de usuarios&Comentarios 
	public void ejemploUsuarioComentariosFlatMap() { 
		Mono<Usuario> UsuarioMono = Mono.fromCallable(() -> new Usuario("marct" , "Churs"));
		Mono<Comentarios> ComentarioUsuarioMono = Mono.fromCallable(() -> {  
			Comentarios comentarios =  new Comentarios();
			comentarios.addComentarios("Hola marcct , que tal !");
			comentarios.addComentarios("mñn se juega ajedrez");			
			comentarios.addComentarios("estare practicando ahora");
			return comentarios;
		});
		
		UsuarioMono.flatMap(u -> ComentarioUsuarioMono.
				map(c -> new UsuarioComentarios(u, c) ) 
		).subscribe(uc -> log.info(uc.toString()));
		
	}
	
	
	//convertir de Flux a Mono basico
	
	public void ejemploCollectList() throws Exception {
		
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario( "Andres" ,"Fultaño"));
		usuariosList.add(new Usuario(  "Pedro", "Torres"));
		usuariosList.add(new Usuario ("Maria", "esmeralda" ));
		usuariosList.add(new Usuario("Juan", "pelarez"));
		usuariosList.add(new Usuario("Marcos","kistom "));
		usuariosList.add(new Usuario("Marcos", "lopez"));
		
		
		
          Flux.fromIterable(usuariosList)
          .collectList()
          .subscribe(lista -> { 
        	   lista.forEach(item -> log.info(item.toString()));
          });
          
	}
	
	
	//ejemploToString
	public void ejemploToString() throws Exception {
		
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario( "Andres" ,"Fultaño"));
		usuariosList.add(new Usuario(  "Pedro", "Torres"));
		usuariosList.add(new Usuario ("Maria", "esmeralda" ));
		usuariosList.add(new Usuario("Juan", "pelarez"));
		usuariosList.add(new Usuario("Marcos","kistom "));
		usuariosList.add(new Usuario("Marcos", "lopez"));
		
		
		
          Flux.fromIterable(usuariosList)
          	.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()) )
          	.flatMap(nombre -> { 
          		if(nombre.contains("marcos".toUpperCase()) ) { 
				return Mono.just(nombre);
			}else {
				return Mono.empty();
			}
		}) 
            .map(usuario2 -> {
    			return usuario2.toLowerCase();
    		  })  
		.subscribe( u -> log.info(u.toString() ));
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
