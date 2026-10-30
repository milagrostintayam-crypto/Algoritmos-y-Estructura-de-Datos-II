package hospedaje;

import java.io.Serializable;

public class Huesped implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String dni, nombres, telefono;

    public Huesped(String dni, String nombres, String telefono) {
        if (dni == null || !dni.trim().matches("[0-9]{8}"))
            throw new IllegalArgumentException("El DNI debe tener 8 dígitos.");
        if (nombres == null || nombres.trim().isEmpty())
            throw new IllegalArgumentException("Escribe los nombres del huésped.");
        this.dni = dni.trim();
        this.nombres = nombres.trim();
        this.telefono = telefono == null ? "" : telefono.trim();
    }

    public String getDni() {
        return dni;
    }

    public String getNombres() {
        return nombres;
    }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public String toString() {
        return nombres + " · " + dni;
    }
}
