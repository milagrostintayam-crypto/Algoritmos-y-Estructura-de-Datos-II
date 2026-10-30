package hospedaje;
import java.io.Serializable;
import java.time.LocalDateTime;

/** Reserva o estadía de un huésped. Los importes se guardan en céntimos. */
public class Reserva implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Estado { PENDIENTE, CONFIRMADA, ALOJADA, FINALIZADA, CANCELADA }
    private final int id;
    private final Huesped huesped;
    private final boolean banoPrivado;
    private final LocalDateTime ingreso, salida;
    private final long totalHabitacion;
    private long pagado;
    private int habitacion;
    private Estado estado = Estado.PENDIENTE;
    public Reserva(int id, Huesped huesped, boolean banoPrivado, LocalDateTime ingreso, LocalDateTime salida, long totalHabitacion) {
        this.id=id; this.huesped=huesped; this.banoPrivado=banoPrivado; this.ingreso=ingreso; this.salida=salida; this.totalHabitacion=totalHabitacion;
    }
    public int getId(){return id;} public Huesped getHuesped(){return huesped;} public boolean isBanoPrivado(){return banoPrivado;}
    public LocalDateTime getIngreso(){return ingreso;} public LocalDateTime getSalida(){return salida;} public long getTotalHabitacion(){return totalHabitacion;}
    public long getPagado(){return pagado;} public int getHabitacion(){return habitacion;} public Estado getEstado(){return estado;}
    public void agregarPago(long monto){if(monto<0) throw new IllegalArgumentException("El pago no puede ser negativo."); pagado+=monto;}
    public void asignar(int numero){if(numero<=0) throw new IllegalArgumentException("Habitación inválida."); habitacion=numero; estado=Estado.CONFIRMADA;}
    public void setEstado(Estado estado){this.estado=estado;}
    public boolean vigente(){return estado==Estado.PENDIENTE || estado==Estado.CONFIRMADA || estado==Estado.ALOJADA;}
    public boolean seCruza(Reserva otra){return ingreso.toLocalDate().isBefore(otra.salida.toLocalDate()) && otra.ingreso.toLocalDate().isBefore(salida.toLocalDate());}
    @Override public String toString(){return "R"+id+" - "+huesped.getNombres()+" ("+estado+")";}
}
