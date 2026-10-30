package hospedaje;

import java.io.Serializable;

public class Habitacion implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int numero;
    private final boolean banoPrivado;
    private boolean limpia = true, habilitada = true;

    public Habitacion(int numero, boolean banoPrivado) {
        this.numero = numero;
        this.banoPrivado = banoPrivado;
    }

    public int getNumero() {
        return numero;
    }

    public boolean isBanoPrivado() {
        return banoPrivado;
    }

    public String getTipo() {
        return banoPrivado ? "Baño privado" : "Baño compartido";
    }

    public int getTarifaBase() {
        return banoPrivado ? 40 : 30;
    }

    public boolean isLimpia() {
        return limpia;
    }

    public boolean isHabilitada() {
        return habilitada;
    }

    public void setLimpia(boolean limpia) {
        this.limpia = limpia;
    }

    public void setHabilitada(boolean habilitada) {
        this.habilitada = habilitada;
    }

    @Override
    public String toString() {
        return numero + " — " + getTipo();
    }
}
