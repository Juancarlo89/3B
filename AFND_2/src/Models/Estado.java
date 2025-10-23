package Models;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Estado {
    private int idEdo;
    private boolean esAceptacion;
    private int token;
    private Set<Transition> transiciones;
    private static int contadorEstados = 0;
    public static final int CREACION_INTERNA = -999;

    public Estado() {
        this.idEdo = contadorEstados++;
        this.esAceptacion = false;
        this.token = -1;
        this.transiciones = new HashSet<>();
    }
    public Estado(boolean esAceptacion) {
        this();
        this.esAceptacion = esAceptacion;
    }
    public Estado(int flag) {
        this.esAceptacion = false;
        this.token = -1;
        this.transiciones = new HashSet<>();
        if (flag == CREACION_INTERNA) { this.idEdo = -1; }
        else { this.idEdo = contadorEstados++; }
    }
    public int getIdEdo() { return idEdo; }
    public boolean isAceptacion() { return esAceptacion; }
    public void setAceptacion(boolean esAceptacion) { this.esAceptacion = esAceptacion; }
    public int getToken() { return token; }
    public void setToken(int token) { this.token = token; }
    public Set<Transition> getTransiciones() { return transiciones; }
    public void agregarTransicion(Transition t) {
        if (t != null) { transiciones.add(t); }
        else { System.err.println("Advertencia: Transición nula agregada a " + this.idEdo); }
    }
    @Override
    public String toString() { return "Edo{id=" + idEdo + ", t=" + token + "}"; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return idEdo == ((Estado) o).idEdo;
    }
    @Override
    public int hashCode() { return Objects.hash(idEdo); }
    public static void reiniciarContadorIds() { contadorEstados = 0; }
}