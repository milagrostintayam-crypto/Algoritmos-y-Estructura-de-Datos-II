package hospedaje;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Cochera implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Tipo {
        CARRO("Carro", 10),
        TORITO("Torito / mototaxi", 5),
        MOTO("Moto lineal", 5);
        private final String nombre;
        private final int tarifa;

        Tipo(String nombre, int tarifa) {
            this.nombre = nombre;
            this.tarifa = tarifa;
        }

        public int getTarifa() {
            return tarifa;
        }

        @Override
        public String toString() {
            return nombre + " · S/ " + tarifa + " por noche";
        }
    }

    private final int id, plaza, reservaId;
    private final String placa;
    private final Tipo tipo;
    private final long importe;
    private final LocalDateTime ingreso;
    private LocalDateTime retiro;

    public Cochera(
            int id,
            int plaza,
            int reservaId,
            String placa,
            Tipo tipo,
            long importe,
            LocalDateTime ingreso) {
        this.id = id;
        this.plaza = plaza;
        this.reservaId = reservaId;
        this.placa = placa;
        this.tipo = tipo;
        this.importe = importe;
        this.ingreso = ingreso;
    }

    public int getId() {
        return id;
    }

    public int getPlaza() {
        return plaza;
    }

    public int getReservaId() {
        return reservaId;
    }

    public String getPlaca() {
        return placa;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public long getImporte() {
        return importe;
    }

    public LocalDateTime getIngreso() {
        return ingreso;
    }

    public LocalDateTime getRetiro() {
        return retiro;
    }

    public boolean isActivo() {
        return retiro == null;
    }

    public void retirar(LocalDateTime fecha) {
        retiro = fecha;
    }
}
