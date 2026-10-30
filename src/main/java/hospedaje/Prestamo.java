package hospedaje;

import java.io.Serializable;

public class Prestamo implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id, reservaId;
    private final String objeto;
    private boolean devuelto;

    public Prestamo(int id, int reservaId, String objeto) {
        this.id = id;
        this.reservaId = reservaId;
        this.objeto = objeto;
    }

    public int getId() {
        return id;
    }

    public int getReservaId() {
        return reservaId;
    }

    public String getObjeto() {
        return objeto;
    }

    public boolean isDevuelto() {
        return devuelto;
    }

    public void devolver() {
        devuelto = true;
    }

    @Override
    public String toString() {
        return "P" + id + " · " + objeto + (devuelto ? " · devuelto" : " · pendiente");
    }
}
