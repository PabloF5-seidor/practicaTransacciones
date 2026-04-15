package com.example.practicaTransacciones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class Async {
    @Bean("transaccionExecutor")
    public Executor transaccionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
  // Detecta las CPUs disponibles en la máquina
        int cpus = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(cpus);    // Hilos base siempre activos
        // Máximo de hilos en momentos de pico
        executor.setMaxPoolSize(cpus * 2);
        // Cola capacidad
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("trans-exec-"); // Prefijo para identificar los hilos en los logs
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // Si la cola está llena, el hilo que llamó ejecuta la tarea él mismo

        executor.initialize();
        return executor;
    }

}
