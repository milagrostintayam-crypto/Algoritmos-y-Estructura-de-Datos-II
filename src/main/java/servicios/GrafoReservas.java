package servicios;

import hospedaje.*;
import java.util.*;

/** Grafo de conflictos: cada reserva es un vértice; una arista impide compartir habitación. */
public class GrafoReservas {
    private final List<Reserva> vertices;
    private final Map<Integer, List<Integer>> adyacencia = new LinkedHashMap<>();

    public GrafoReservas(List<Reserva> reservas) {
        vertices = new ArrayList<>();
        for (Reserva r : reservas)
            if (r.vigente()) {
                vertices.add(r);
                adyacencia.put(r.getId(), new ArrayList<>());
            }
        for (int i = 0; i < vertices.size(); i++)
            for (int j = i + 1; j < vertices.size(); j++) {
                Reserva a = vertices.get(i), b = vertices.get(j);
                if (a.isBanoPrivado() == b.isBanoPrivado() && a.seCruza(b)) {
                    adyacencia.get(a.getId()).add(b.getId());
                    adyacencia.get(b.getId()).add(a.getId());
                }
            }
    }

    public Map<Integer, List<Integer>> getAdyacencia() {
        Map<Integer, List<Integer>> copia = new LinkedHashMap<>();
        adyacencia.forEach((id, vecinos) -> copia.put(id, List.copyOf(vecinos)));
        return Collections.unmodifiableMap(copia);
    }

    /** Colores = números de habitación. Las asignaciones confirmadas permanecen fijas. */
    public Map<Integer, Integer> asignar(List<Habitacion> habitaciones) {
        Map<Integer, Integer> colores = new LinkedHashMap<>();
        List<Reserva> pendientes = new ArrayList<>();
        for (Reserva r : vertices) {
            if (r.getHabitacion() != 0) colores.put(r.getId(), r.getHabitacion());
            else pendientes.add(r);
        }
        for (Reserva r : vertices)
            for (int vecino : adyacencia.get(r.getId()))
                if (colores.containsKey(r.getId())
                        && Objects.equals(colores.get(r.getId()), colores.get(vecino)))
                    throw new IllegalStateException("Hay asignaciones existentes incompatibles.");
        pendientes.sort(
                Comparator.comparingInt((Reserva r) -> adyacencia.get(r.getId()).size()).reversed());
        if (!colorear(0, pendientes, habitaciones, colores))
            throw new IllegalArgumentException(
                    "No hay habitaciones suficientes para todas las solicitudes. Cancela o ajusta una"
                            + " solicitud; no se cambió ninguna asignación.");
        return colores;
    }

    private boolean colorear(
            int posicion,
            List<Reserva> pendientes,
            List<Habitacion> habitaciones,
            Map<Integer, Integer> colores) {
        if (posicion == pendientes.size()) return true;
        Reserva actual = pendientes.get(posicion);
        for (Habitacion h : habitaciones) {
            if (!h.isHabilitada() || !h.isLimpia() || h.isBanoPrivado() != actual.isBanoPrivado()) continue;
            boolean libre = true;
            for (int vecino : adyacencia.get(actual.getId()))
                if (Objects.equals(colores.get(vecino), h.getNumero())) {
                    libre = false;
                    break;
                }
            if (libre) {
                colores.put(actual.getId(), h.getNumero());
                if (colorear(posicion + 1, pendientes, habitaciones, colores)) return true;
                colores.remove(actual.getId());
            }
        }
        return false;
    }
}
