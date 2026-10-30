package servicios;

import hospedaje.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Reglas del hospedaje. La vista no modifica directamente las listas. */
public class HotelService implements Serializable {
    private static final long serialVersionUID = 2L;
    private final List<Habitacion> habitaciones = new ArrayList<>();
    private final List<Reserva> reservas = new ArrayList<>();
    private final List<Cochera> cocheras = new ArrayList<>();
    private final List<Prestamo> prestamos = new ArrayList<>();
    private int siguienteReserva = 1, siguienteCochera = 1, siguientePrestamo = 1;
    private transient Clock reloj;

    public HotelService() {
        this(Clock.system(ZoneId.of("America/Lima")));
    }

    public HotelService(Clock reloj) {
        this.reloj = reloj;
        for (int n = 101; n <= 105; n++) habitaciones.add(new Habitacion(n, n >= 104));
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now(reloj).withSecond(0).withNano(0);
    }

    public List<Habitacion> getHabitaciones() {
        return Collections.unmodifiableList(habitaciones);
    }

    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }

    public List<Cochera> getCocheras() {
        return Collections.unmodifiableList(cocheras);
    }

    public List<Prestamo> getPrestamos() {
        return Collections.unmodifiableList(prestamos);
    }

    public Habitacion habitacion(int numero) {
        return habitaciones.stream()
                .filter(h -> h.getNumero() == numero)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Habitación inexistente."));
    }

    public Reserva reserva(int id) {
        return reservas.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reserva inexistente."));
    }

    public Reserva ocupante(int numero) {
        return reservas.stream()
                .filter(r -> r.getHabitacion() == numero && r.getEstado() == Reserva.Estado.ALOJADA)
                .findFirst()
                .orElse(null);
    }

    public String estado(int numero) {
        Habitacion h = habitacion(numero);
        if (ocupante(numero) != null) return "Ocupada";
        if (!h.isHabilitada()) return "Bloqueada";
        if (!h.isLimpia()) return "Por limpiar";
        return "Disponible ahora";
    }

    public static long monto(String texto) {
        try {
            long valor =
                    new BigDecimal(texto.trim().replace(',', '.')).movePointRight(2).longValueExact();
            if (valor < 0) throw new ArithmeticException();
            return valor;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Importe inválido. Usa 0 o un valor con hasta dos decimales.");
        }
    }

    public static String soles(long centimos) {
        return "S/ " + BigDecimal.valueOf(centimos, 2).toPlainString();
    }

    public static long noches(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null || !fin.isAfter(inicio))
            throw new IllegalArgumentException("La salida debe ser posterior al ingreso.");
        long n = ChronoUnit.DAYS.between(inicio.toLocalDate(), fin.toLocalDate());
        if (n < 1 || n > 365)
            throw new IllegalArgumentException(
                    "Registra entre 1 y 365 noches. La salida debe ser otro día.");
        return n;
    }

    public static long cotizar(boolean privado, LocalDateTime inicio, LocalDateTime fin) {
        long n = noches(inicio, fin), total = 0;
        for (int i = 0; i < n; i++) {
            DayOfWeek dia = inicio.toLocalDate().plusDays(i).getDayOfWeek();
            total +=
                    ((privado ? 40 : 30) + (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY ? 10 : 0))
                            * 100L;
        }
        return total;
    }

    private Reserva crear(
            Huesped h, boolean privado, LocalDateTime inicio, LocalDateTime fin, long adelanto) {
        if (h == null) throw new IllegalArgumentException("Completa los datos del huésped.");
        long total = cotizar(privado, inicio, fin);
        if (adelanto < 0 || adelanto > total)
            throw new IllegalArgumentException("El adelanto debe estar entre cero y el total.");
        Reserva r = new Reserva(siguienteReserva, h, privado, inicio, fin, total);
        r.agregarPago(adelanto);
        return r;
    }

    public Reserva solicitar(
            Huesped h, boolean privado, LocalDateTime inicio, LocalDateTime fin, long adelanto) {
        if (inicio.isBefore(ahora()))
            throw new IllegalArgumentException("Una reserva futura no puede comenzar en el pasado.");
        Reserva r = crear(h, privado, inicio, fin, adelanto);
        reservas.add(r);
        siguienteReserva++;
        return r;
    }

    private void comprobarHabitacion(Reserva nueva, int numero) {
        Habitacion h = habitacion(numero);
        if (!h.isHabilitada() || !h.isLimpia())
            throw new IllegalArgumentException("La habitación debe estar habilitada y limpia.");
        if (ocupante(numero) != null)
            throw new IllegalArgumentException("La habitación todavía tiene un huésped alojado.");
        for (Reserva r : reservas)
            if (r.getId() != nueva.getId()
                    && r.vigente()
                    && r.getHabitacion() == numero
                    && r.seCruza(nueva))
                throw new IllegalArgumentException(
                        "Las fechas coinciden con la reserva R" + r.getId() + " de esta habitación.");
    }

    public Reserva registrarIngreso(
            Huesped h, int numero, LocalDateTime inicio, LocalDateTime fin, long adelanto) {
        if (!inicio.toLocalDate().equals(ahora().toLocalDate())
                || inicio.isAfter(ahora())
                || !fin.isAfter(ahora()))
            throw new IllegalArgumentException(
                    "Para ingreso inmediato, usa una hora de hoy que ya haya llegado. Para fechas futuras usa"
                            + " Reservas.");
        Reserva r = crear(h, habitacion(numero).isBanoPrivado(), inicio, fin, adelanto);
        comprobarHabitacion(r, numero);
        r.asignar(numero);
        r.setEstado(Reserva.Estado.ALOJADA);
        reservas.add(r);
        siguienteReserva++;
        return r;
    }

    public int asignarPendientes() {
        GrafoReservas grafo = new GrafoReservas(reservas);
        Map<Integer, Integer> propuesta = grafo.asignar(habitaciones);
        int cantidad = 0;
        // Solo se confirma después de encontrar una solución completa.
        for (Reserva r : reservas)
            if (r.getEstado() == Reserva.Estado.PENDIENTE) {
                r.asignar(propuesta.get(r.getId()));
                cantidad++;
            }
        return cantidad;
    }

    public void llegada(int id) {
        Reserva r = reserva(id);
        if (r.getEstado() != Reserva.Estado.CONFIRMADA)
            throw new IllegalArgumentException("Selecciona una reserva confirmada.");
        if (ahora().isBefore(r.getIngreso()) || !ahora().isBefore(r.getSalida()))
            throw new IllegalArgumentException("La llegada debe estar dentro del horario reservado.");
        comprobarHabitacion(r, r.getHabitacion());
        r.setEstado(Reserva.Estado.ALOJADA);
    }

    public long total(int id) {
        return reserva(id).getTotalHabitacion()
                + cocheras.stream()
                .filter(c -> c.getReservaId() == id)
                .mapToLong(Cochera::getImporte)
                .sum();
    }

    public long saldo(int id) {
        return total(id) - reserva(id).getPagado();
    }

    public void pagar(int id, long monto) {
        Reserva r = reserva(id);
        if ((r.getEstado() != Reserva.Estado.CONFIRMADA && r.getEstado() != Reserva.Estado.ALOJADA)
                || monto <= 0
                || monto > saldo(id))
            throw new IllegalArgumentException(
                    "El pago debe ser positivo y no superar el saldo de una estadía vigente.");
        r.agregarPago(monto);
    }

    public long cancelar(int id) {
        Reserva r = reserva(id);
        if (r.getEstado() != Reserva.Estado.PENDIENTE && r.getEstado() != Reserva.Estado.CONFIRMADA)
            throw new IllegalArgumentException("Solo puedes cancelar reservas pendientes o confirmadas.");
        // Se conserva el adelanto como registro histórico; al cancelar debe devolverse al huésped.
        r.setEstado(Reserva.Estado.CANCELADA);
        return r.getPagado();
    }

    private Reserva alojada(int id) {
        Reserva r = reserva(id);
        if (r.getEstado() != Reserva.Estado.ALOJADA)
            throw new IllegalArgumentException("La reserva debe tener un huésped alojado.");
        return r;
    }

    public void salida(int id) {
        Reserva r = alojada(id);
        if (saldo(id) > 0)
            throw new IllegalArgumentException("Queda por pagar " + soles(saldo(id)) + ".");
        if (prestamos.stream().anyMatch(p -> p.getReservaId() == id && !p.isDevuelto()))
            throw new IllegalArgumentException("Primero registra la devolución de los préstamos.");
        if (cocheras.stream().anyMatch(c -> c.getReservaId() == id && c.isActivo()))
            throw new IllegalArgumentException("Primero registra el retiro del vehículo.");
        r.setEstado(Reserva.Estado.FINALIZADA);
        habitacion(r.getHabitacion()).setLimpia(false);
    }

    public void marcarLimpia(int numero) {
        if (ocupante(numero) != null)
            throw new IllegalArgumentException(
                    "Registra la salida antes de marcar la habitación limpia.");
        habitacion(numero).setLimpia(true);
    }

    public void cambiarHabilitada(int numero) {
        Habitacion h = habitacion(numero);
        if (h.isHabilitada()
                && reservas.stream().anyMatch(r -> r.vigente() && r.getHabitacion() == numero))
            throw new IllegalArgumentException("La habitación tiene una estadía o una reserva asignada.");
        h.setHabilitada(!h.isHabilitada());
    }

    public Prestamo prestar(int id, String objeto) {
        alojada(id);
        if (objeto == null || objeto.trim().isEmpty())
            throw new IllegalArgumentException("Escribe el objeto prestado.");
        Prestamo p = new Prestamo(siguientePrestamo++, id, objeto.trim());
        prestamos.add(p);
        return p;
    }

    public void devolver(int id) {
        Prestamo p =
                prestamos.stream()
                        .filter(x -> x.getId() == id)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Selecciona un préstamo."));
        if (p.isDevuelto()) throw new IllegalArgumentException("El préstamo ya está devuelto.");
        p.devolver();
    }

    public Cochera estacionar(int reservaId, String placa, Cochera.Tipo tipo) {
        Reserva r = alojada(reservaId);
        if (!r.getSalida().isAfter(ahora()))
            throw new IllegalArgumentException(
                    "La estadía ya venció; regulariza la salida antes de registrar otro vehículo.");
        String normal = placa == null ? "" : placa.trim().toUpperCase(Locale.ROOT);
        if (!normal.matches("[A-Z0-9-]{3,12}") || tipo == null)
            throw new IllegalArgumentException("Revisa la placa y el tipo de vehículo.");
        for (Cochera c : cocheras)
            if (c.isActivo() && (c.getPlaca().equals(normal) || c.getReservaId() == reservaId))
                throw new IllegalArgumentException(
                        "La placa o esta habitación ya tiene un vehículo en cochera.");
        int plaza = 0;
        for (int i = 1; i <= 5; i++) {
            final int n = i;
            if (cocheras.stream().noneMatch(c -> c.isActivo() && c.getPlaza() == n)) {
                plaza = i;
                break;
            }
        }
        if (plaza == 0) throw new IllegalArgumentException("Las cinco plazas están ocupadas.");
        long n =
                Math.max(1, ChronoUnit.DAYS.between(ahora().toLocalDate(), r.getSalida().toLocalDate()));
        Cochera c =
                new Cochera(
                        siguienteCochera++,
                        plaza,
                        reservaId,
                        normal,
                        tipo,
                        n * tipo.getTarifa() * 100L,
                        ahora());
        cocheras.add(c);
        return c;
    }

    public void retirarVehiculo(int id) {
        Cochera c =
                cocheras.stream()
                        .filter(x -> x.getId() == id)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Selecciona un vehículo."));
        if (!c.isActivo()) throw new IllegalArgumentException("El vehículo ya fue retirado.");
        c.retirar(ahora());
    }

    public void guardar(Path archivo) throws IOException {
        Path destino = archivo.toAbsolutePath();
        Files.createDirectories(destino.getParent());
        Path temporal = Files.createTempFile(destino.getParent(), "hospedaje-", ".tmp");
        try {
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temporal))) {
                out.writeObject(this);
            }
            try {
                Files.move(
                        temporal, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    public static HotelService cargar(Path archivo) throws IOException {
        if (!Files.exists(archivo)) return new HotelService();
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(archivo))) {
            in.setObjectInputFilter(
                    ObjectInputFilter.Config.createFilter(
                            "maxdepth=40;maxrefs=100000;maxbytes=20000000;hospedaje.*;servicios.HotelService;java.base/*;!*"));
            HotelService hotel = (HotelService) in.readObject();
            hotel.reloj = Clock.system(ZoneId.of("America/Lima"));
            return hotel;
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("El archivo de datos no es compatible.", e);
        }
    }
}
