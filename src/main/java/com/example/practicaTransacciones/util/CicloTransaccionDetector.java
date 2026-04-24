package com.example.practicaTransacciones.util;

import com.example.practicaTransacciones.domain.Transaccion;
import com.example.practicaTransacciones.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CicloTransaccionDetector {

    private final TransaccionRepository transaccionRepository;

    public ResultadoCiclo detectarCiclo() {
        // Obtener todas las transacciones del último día
        LocalDateTime desde = LocalDateTime.now().minusDays(1);
        List<Transaccion> transacciones = transaccionRepository.findTransaccionesUltimoDia(desde);

        log.info("Analizando {} transacciones del último día en busca de ciclos", transacciones.size());

        // Construir el grafo como mapa de adyacencia
        // cuentaOrigen -> lista de cuentasDestino
        // Complejidad O(E) donde E = número de transacciones
        Map<String, List<String>> grafo = construirGrafo(transacciones);

        // Ejecutar DFS iterativo para detectar ciclos
        // Complejidad O(V + E) donde V = cuentas, E = transacciones
        return dfsIterativo(grafo);
    }

    private Map<String, List<String>> construirGrafo(List<Transaccion> transacciones) {
        Map<String, List<String>> grafo = new HashMap<>();

        for (Transaccion t : transacciones) {
            String origen  = t.getCuentaOrigen();
            String destino = t.getCuentaDestino();

            // Si la cuenta origen no está en el grafo la añade con lista vacía
            grafo.putIfAbsent(origen, new ArrayList<>());
            // Si la cuenta destino no está en el grafo la añade con lista vacía
            // para que también sea visitada en el DFS
            grafo.putIfAbsent(destino, new ArrayList<>());
            // Añade la arista origen -> destino
            grafo.get(origen).add(destino);
        }

        log.debug("Grafo construido con {} nodos (cuentas) y {} aristas (transacciones)",
                grafo.size(), transacciones.size());
        return grafo;
    }

    private ResultadoCiclo dfsIterativo(Map<String, List<String>> grafo) {
        // Nodos completamente procesados (todos sus vecinos visitados)
        Set<String> visitados = new HashSet<>();
        // Nodos en el camino actual del DFS -> si aparece uno ya en este set -> ciclo
        Set<String> enPila    = new HashSet<>();
        // Cuentas involucradas en el ciclo si se detecta
        List<String> cuentasCiclo = new ArrayList<>();

        // Iterar sobre todos los nodos del grafo
        // Necesario porque el grafo puede no ser conexo
        for (String nodoInicial : grafo.keySet()) {
            if (visitados.contains(nodoInicial)) {
                continue; // Ya procesado, saltar
            }

            // Pila del DFS: cada entrada es [nodo, índice del vecino a visitar]
            Deque<int[]> pila = new ArrayDeque<>();
            // Lista de nodos en el encamino actual para reconstruir el ciclo
            List<String> encamino = new ArrayList<>();
            // Mapa de nodo -> índice en el encamino
            Map<String, Integer> indiceCamino = new HashMap<>();
            // Permite encontrar dónde empieza el ciclo en O

            // Convertir nodos a índices para la pila
            List<String> nodos = new ArrayList<>(grafo.keySet());
            Map<String, Integer> indiceNodo = new HashMap<>();
            // Convierte nombres de cuenta a índices numéricos
            // Necesario porque la pila almacena int[] no Strings
            for (int i = 0; i < nodos.size(); i++) {
                indiceNodo.put(nodos.get(i), i);
            }

            int idxInicial = indiceNodo.get(nodoInicial);
            // Empuja el nodo inicial con índice de vecino = 0
            pila.push(new int[]{idxInicial, 0});
            encamino.add(nodoInicial);
            indiceCamino.put(nodoInicial, 0);
            enPila.add(nodoInicial);

            while (!pila.isEmpty()) {
                int[] top     = pila.peek();
                // Mira el tope sin sacarlo
                // top[0] = nodo actual, top[1] = vecino que toca visitar
                String nodo   = nodos.get(top[0]);
                List<String> vecinos = grafo.getOrDefault(nodo, Collections.emptyList());

                // Obtiene el siguiente vecino e incrementa el índice
                // La próxima iteración visitará el siguiente vecino
                if (top[1] < vecinos.size()) {
                    // Hay vecinos por visitar
                    String vecino = vecinos.get(top[1]++);

                    // El vecino ya está en el camino actual
                    // Significa que llegamos a un nodo ya visitado
                    if (enPila.contains(vecino)) {
                        // Ciclo detectado: vecino ya está en el encamino actual
                        log.warn("Ciclo detectado en el grafo de transacciones");
                        int idx = indiceCamino.get(vecino);
                        // Extrae solo las cuentas que forman el ciclo
                        cuentasCiclo.addAll(encamino.subList(idx, encamino.size()));
                        cuentasCiclo.add(vecino); // cierra el ciclo
                        // Reconstruye las cuentas que forman el ciclo
                        log.warn("Cuentas involucradas en el ciclo: {}", cuentasCiclo);
                        return new ResultadoCiclo(true, cuentasCiclo);//Boolean + lista cuentas

                    } else if (!visitados.contains(vecino)) {
                        // Vecino no visitado -> explorar
                        int idxVecino = indiceNodo.get(vecino);
                        pila.push(new int[]{idxVecino, 0});
                        encamino.add(vecino);
                        indiceCamino.put(vecino, encamino.size() - 1);
                        enPila.add(vecino);
                    }

                } else {
                    // Todos los vecinos visitados -> retroceder
                    pila.pop();
                    visitados.add(nodo);
                    enPila.remove(nodo);
                    encamino.remove(encamino.size() - 1);
                    indiceCamino.remove(nodo);
                }
            }
        }

        log.info("No se detectaron ciclos en el grafo de transacciones");
        return new ResultadoCiclo(false, Collections.emptyList());//Lista vacia y boolean false
    }

    // Record inmutable para encapsular el resultado del algoritmo
    public record ResultadoCiclo(
            boolean tieneCiclo,          // true si existe ciclo
            List<String> cuentas         // cuentas involucradas en el ciclo
    ) {}
}