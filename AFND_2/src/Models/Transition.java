package Models;

import java.util.Objects;
import java.util.Set;

public class Transition {
    private final char simbInf;
    private final char simbSup;
    private Estado edoDestino;

    private Transition(char cInf, char cSup, Estado e, boolean checkRange) {
        if (checkRange && cInf > cSup) {
             throw new IllegalArgumentException("Rango inválido: '" + cInf + "' > '" + cSup + "'.");
        }
        this.simbInf = cInf;
        this.simbSup = cSup;
        this.edoDestino = Objects.requireNonNull(e, "El estado destino no puede ser null.");
    }
     public Transition(Estado e) {
         this(SimbEsp.EPSILON, SimbEsp.EPSILON, e, false);
     }
    public Transition(char c, Estado e) {
        this(c, c, e, false);
        if (c == SimbEsp.EPSILON) {
             throw new IllegalArgumentException("Usar el constructor Transition(Estado e) para Epsilon.");
        }
    }
    public Transition(char cInf, char cSup, Estado e) {
        this(cInf, cSup, e, true);
        if (cInf == SimbEsp.EPSILON || cSup == SimbEsp.EPSILON) {
            throw new IllegalArgumentException("Usar el constructor Transition(Estado e) para Epsilon.");
        }
    }
    public char getSimbInf() { return simbInf; }
    public char getSimbSup() { return simbSup; }
    public Estado getEdoDestino() { return edoDestino; }
    public void setEdoDestino(Estado edoDestino) {
        this.edoDestino = Objects.requireNonNull(edoDestino, "El estado destino no puede ser null.");
    }
    public boolean acepta(char c) {
        if (this.esEpsilon()) return false;
        return c >= simbInf && c <= simbSup;
    }
    public boolean esEpsilon() {
        return this.simbInf == SimbEsp.EPSILON && this.simbSup == SimbEsp.EPSILON;
    }
    @Override
    public String toString() {
        String destID = (edoDestino != null) ? String.valueOf(edoDestino.getIdEdo()) : "?";
        String destinoStr = "-> Edo" + destID;
        if (esEpsilon()) return "[ε]" + destinoStr;
        if (simbInf == simbSup) return "['" + simbInf + "']" + destinoStr;
        return "['" + simbInf + "'-'" + simbSup + "']" + destinoStr;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transition that = (Transition) o;
        return simbInf == that.simbInf && simbSup == that.simbSup && Objects.equals(edoDestino, that.edoDestino);
    }
    @Override
    public int hashCode() { return Objects.hash(simbInf, simbSup, edoDestino); }
}