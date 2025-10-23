package Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.Objects;
import java.util.stream.Collectors; // Asegúrate de usar JDK 8+

/**
 * Clase que representa un Autómata Finito No Determinista (AFN).
 * Implementa construcción de Thompson, asignación de tokens, operaciones
 * destructivas (modifican 'this') y clonación profunda.
 * También almacena una estructura para un 'toString()' representacional.
 */
public class AFN implements Cloneable {

    // ----- Atributos NFA (Estructura real) -----
    protected Set<Estado> estados;
    protected Estado edoInicial;
    protected Set<Character> alfabeto;
    protected Set<Estado> edosAcept;

    // ----- Atributos para toString() Estructural -----
    protected String operador;
    protected List<AFN> subAFNs; // Lista de clones de los AFNs hijos

    // ----- Constructor -----
    public AFN() {
        estados = new HashSet<>();
        alfabeto = new HashSet<>();
        edosAcept = new HashSet<>();
        edoInicial = null;
        subAFNs = new ArrayList<>();
        operador = "";
    }

    // --- Creación de AFNs básicos (CON TOKEN) ---

    public AFN crearBasico(char c, int token) {
        this.estados.clear(); this.alfabeto.clear(); this.edosAcept.clear();
        this.edoInicial = null; this.subAFNs.clear(); this.operador = "";

        Estado e1 = new Estado(); // Usa constructor normal
        Estado e2 = new Estado(); // Usa constructor normal

        // Asume que Estado y Transition están corregidos
        e1.agregarTransicion(new Transition(c, e2));
        e2.setAceptacion(true);
        e2.setToken(token); // Asigna token

        this.edoInicial = e1;
        this.estados.add(e1); this.estados.add(e2);
        this.edosAcept.add(e2); this.alfabeto.add(c);
        return this;
    }

    public AFN crearBasico(char c1, char c2, int token) {
        this.estados.clear(); this.alfabeto.clear(); this.edosAcept.clear();
        this.edoInicial = null; this.subAFNs.clear(); this.operador = "";

        Estado e1 = new Estado(); // Usa constructor normal
        Estado e2 = new Estado();

        e1.agregarTransicion(new Transition(c1, c2, e2));
        e2.setAceptacion(true);
        e2.setToken(token); // Asigna token

        this.edoInicial = e1;
        this.estados.add(e1); this.estados.add(e2);
        this.edosAcept.add(e2);
        if (c1 <= c2) { for (char car = c1; car <= c2; car++) this.alfabeto.add(car); }
        else { System.err.println("Warn: Rango inválido."); }
        return this;
    }

    // --- Operaciones de construcción (Modifican 'this' Y guardan historial) ---

    public AFN unir(AFN f2) {
        if (f2 == null || f2.getEdoInicial() == null) { /* ... validación ... */ return this; }
        if (this == f2) { /* ... validación ... */ return this; }
        if (this.edoInicial == null) { /* ... validación ... */ return this; }

        AFN f1_clon = this.clonar(); AFN f2_clon = f2.clonar(); // Clona para toString

        // Lógica NFA
        Estado ni = new Estado(), nf = new Estado(); nf.setAceptacion(true);
        ni.agregarTransicion(new Transition(this.edoInicial)); // Usa constructor Epsilon
        ni.agregarTransicion(new Transition(f2.getEdoInicial()));
        for(Estado ea:this.edosAcept){ ea.agregarTransicion(new Transition(nf)); ea.setAceptacion(false); }
        for(Estado ea:f2.getEdosAcept()){ ea.agregarTransicion(new Transition(nf)); ea.setAceptacion(false); }
        this.estados.add(ni); this.estados.add(nf); this.estados.addAll(f2.getEstados());
        this.edoInicial = ni; // <-- 'this' se modifica
        this.edosAcept.clear(); this.edosAcept.add(nf); this.alfabeto.addAll(f2.getAlfabeto());

        // Lógica Estructural
        this.operador = "|"; this.subAFNs.clear();
        this.subAFNs.add(f1_clon); this.subAFNs.add(f2_clon);
        return this; // <-- Devuelve 'this' (modificado)
    }

    public AFN concatenar(AFN f2) {
       if (f2 == null || f2.getEdoInicial() == null || f2.getEdoInicial().getTransiciones() == null) { /* ... validación ... */ return this; }
       if (this == f2) { /* ... validación ... */ return this; }
       if (this.edoInicial == null) { /* ... validación ... */ return this; }
       
        AFN f1_clon = this.clonar(); AFN f2_clon = f2.clonar(); // Clona para toString

        // Lógica NFA
        Set<Transition> transIniF2 = f2.getEdoInicial().getTransiciones();
        for(Estado ea:this.edosAcept){
            if(transIniF2 != null) {
                for(Transition t:transIniF2){
                     Transition copiaT = t.esEpsilon() ? new Transition(t.getEdoDestino()) : new Transition(t.getSimbInf(), t.getSimbSup(), t.getEdoDestino());
                     ea.agregarTransicion(copiaT);
                }
            }
            ea.setAceptacion(false);
        }
        f2.getEstados().remove(f2.getEdoInicial());
        this.estados.addAll(f2.getEstados()); this.alfabeto.addAll(f2.getAlfabeto());
        this.edosAcept.clear(); this.edosAcept.addAll(f2.getEdosAcept());

        // Lógica Estructural
        this.operador = "&"; this.subAFNs.clear();
        this.subAFNs.add(f1_clon); this.subAFNs.add(f2_clon);
        return this; // <-- Devuelve 'this' (modificado)
    }

    // --- Cerraduras (Modifican 'this' y guardan historial) ---

    public AFN cerraduraKleene() {
        if (this.edoInicial == null) { return this; }
        AFN this_clon = this.clonar();
        Estado e1=new Estado(), e2=new Estado(); e2.setAceptacion(true);
        for(Estado e:this.edosAcept){ e.agregarTransicion(new Transition(this.edoInicial)); e.agregarTransicion(new Transition(e2)); e.setAceptacion(false); }
        e1.agregarTransicion(new Transition(this.edoInicial)); e1.agregarTransicion(new Transition(e2));
        this.edoInicial=e1; this.edosAcept.clear(); this.edosAcept.add(e2);
        this.estados.add(e1); this.estados.add(e2);
        this.operador = "*"; this.subAFNs.clear(); this.subAFNs.add(this_clon);
        return this;
    }
    public AFN cerraduraPositiva(){
        if (this.edoInicial == null) { return this; }
        AFN this_clon = this.clonar();
        Estado e1=new Estado(), e2=new Estado(); e2.setAceptacion(true);
        for(Estado e:this.edosAcept){ e.agregarTransicion(new Transition(this.edoInicial)); e.agregarTransicion(new Transition(e2)); e.setAceptacion(false); }
        e1.agregarTransicion(new Transition(this.edoInicial));
        this.edoInicial=e1; this.edosAcept.clear(); this.edosAcept.add(e2);
        this.estados.add(e1); this.estados.add(e2);
        this.operador = "+"; this.subAFNs.clear(); this.subAFNs.add(this_clon);
        return this;
    }
     public AFN cerraduraOpcional(){
        if (this.edoInicial == null) { return this; }
        AFN this_clon = this.clonar();
        Estado e1=new Estado(), e2=new Estado(); e2.setAceptacion(true);
        for(Estado e:this.edosAcept){ e.agregarTransicion(new Transition(e2)); e.setAceptacion(false); }
        e1.agregarTransicion(new Transition(this.edoInicial)); e1.agregarTransicion(new Transition(e2));
        this.edoInicial=e1; this.edosAcept.clear(); this.edosAcept.add(e2);
        this.estados.add(e1); this.estados.add(e2);
        this.operador = "?"; this.subAFNs.clear(); this.subAFNs.add(this_clon);
        return this;
    }

    // ----- Operaciones de conjuntos -----
    public Set<Estado> cerraduraEpsilon(Estado e) {
        Set<Estado> c = new HashSet<>(); if (e == null) return c;
        Stack<Estado> p = new Stack<>(); c.add(e); p.push(e);
        while (!p.isEmpty()) {
            Estado e2 = p.pop();
            if (e2.getTransiciones() == null) continue;
            for (Transition t : e2.getTransiciones()) {
                if (t != null && t.esEpsilon() && t.getEdoDestino() != null && !c.contains(t.getEdoDestino())) {
                    c.add(t.getEdoDestino()); p.push(t.getEdoDestino());
                }
            }
        } return c;
    }
    public Set<Estado> cerraduraEpsilon(Set<Estado> estados) {
        Set<Estado> r = new HashSet<>(); if (estados == null) return r;
        for (Estado e : estados) { if(e != null) r.addAll(cerraduraEpsilon(e)); } return r;
    }
    public Set<Estado> mover(Estado e, char c) {
        Set<Estado> r = new HashSet<>(); if (e == null || e.getTransiciones() == null) return r;
        for (Transition t : e.getTransiciones()) {
            if (t != null && t.acepta(c) && t.getEdoDestino() != null) r.add(t.getEdoDestino());
        } return r;
    }
    public Set<Estado> mover(Set<Estado> estados, char c) {
        Set<Estado> r = new HashSet<>(); if (estados == null) return r;
        for (Estado e : estados) { if(e != null) r.addAll(mover(e, c)); } return r;
    }
    public Set<Estado> ira(Estado e, char c) { return cerraduraEpsilon(mover(e, c)); }
    public Set<Estado> ira(Set<Estado> estados, char c) { return cerraduraEpsilon(mover(estados, c)); }

    //----- Getters Públicos -----
    public Set<Estado> getEstados() { return estados; }
    public Estado getEdoInicial() { return edoInicial; }
    public Set<Character> getAlfabeto() { return alfabeto; }
    public Set<Estado> getEdosAcept() { return edosAcept; }
    public String getOperador() { return operador; }
    public List<AFN> getSubAFNs() { return subAFNs; }

    //----- Representación textual (Estructural) -----
    @Override
    public String toString() {
        String currentOperator = (operador != null) ? operador : "";
        if (subAFNs == null || subAFNs.isEmpty()) {
            if (alfabeto == null || alfabeto.isEmpty()) return "[Vacío]";
            String baseStr;
            if (alfabeto.size() == 1) {
                char simbolo = alfabeto.iterator().next();
                baseStr = (simbolo == SimbEsp.EPSILON) ? "ε" : String.valueOf(simbolo);
            } else {
                Optional<Character> minOpt = alfabeto.stream().filter(ch -> ch != SimbEsp.EPSILON).min(Character::compare);
                Optional<Character> maxOpt = alfabeto.stream().filter(ch -> ch != SimbEsp.EPSILON).max(Character::compare);
                if (minOpt.isPresent() && maxOpt.isPresent()) {
                    char min = minOpt.get(); char max = maxOpt.get();
                    baseStr = (min == max) ? String.valueOf(min) : min + "-" + max;
                } else if (alfabeto.contains(SimbEsp.EPSILON)) { baseStr = "ε"; }
                else { baseStr = "?"; }
            }
            if (currentOperator.equals("*") || currentOperator.equals("+") || currentOperator.equals("?")) {
                return "(" + baseStr + ")" + currentOperator;
            }
            return baseStr;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < subAFNs.size(); i++) {
            sb.append(subAFNs.get(i).toString());
            if (i < subAFNs.size() - 1 && !currentOperator.isEmpty()) {
                String opSimbolo = currentOperator.equals("&") ? "." : currentOperator;
                sb.append(" ").append(opSimbolo).append(" ");
            }
        }
        sb.append(")");
        if (currentOperator.equals("*") || currentOperator.equals("+") || currentOperator.equals("?")) {
             sb.append(currentOperator);
        }
        return sb.toString();
    }

    //----- Clonación Profunda -----
     public AFN clonar() {
        try {
            AFN copia = (AFN) super.clone();
            copia.estados = new HashSet<>();
            copia.edosAcept = new HashSet<>();
            copia.alfabeto = (this.alfabeto != null) ? new HashSet<>(this.alfabeto) : new HashSet<>();
            copia.edoInicial = null;
            copia.operador = this.operador;
            copia.subAFNs = new ArrayList<>();
            Map<Estado, Estado> mapaEstados = new HashMap<>();
            if (this.estados == null) return copia;
            for (Estado viejo : this.estados) {
                if (viejo == null) continue;
                Estado nuevo = new Estado(Estado.CREACION_INTERNA);
                nuevo.setAceptacion(viejo.isAceptacion());
                nuevo.setToken(viejo.getToken());
                copia.estados.add(nuevo);
                mapaEstados.put(viejo, nuevo);
                if (this.edoInicial == viejo) copia.edoInicial = nuevo;
                if (this.edosAcept != null && this.edosAcept.contains(viejo)) copia.edosAcept.add(nuevo);
            }
            for (Estado viejo : this.estados) {
                 if (viejo == null) continue;
                Estado nuevoOrigen = mapaEstados.get(viejo);
                if (nuevoOrigen == null || viejo.getTransiciones() == null) continue;
                for (Transition viejaT : viejo.getTransiciones()) {
                    if (viejaT == null) continue;
                    Estado viejoDest = viejaT.getEdoDestino();
                    Estado nuevoDest = mapaEstados.get(viejoDest);
                    if (nuevoDest != null) {
                        Transition nuevaT = viejaT.esEpsilon() ?
                             new Transition(nuevoDest) :
                             new Transition(viejaT.getSimbInf(), viejaT.getSimbSup(), nuevoDest);
                        nuevoOrigen.agregarTransicion(nuevaT);
                    } else if (viejoDest != null && !mapaEstados.containsKey(viejoDest)) {
                        System.err.println("Error clon: Destino ID " + viejoDest.getIdEdo() + " no mapeado.");
                    }
                }
            }
            if (copia.edoInicial == null && this.edoInicial != null) throw new RuntimeException("Clon: EdoInicial no asignado.");
            if (this.subAFNs != null) {
                for (AFN sub : this.subAFNs) {
                    if (sub != null) {
                        copia.subAFNs.add(sub.clonar());
                    }
                }
            }
            return copia;
        } catch (CloneNotSupportedException e) { throw new RuntimeException("Error inesperado clonando AFN.", e); }
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException { return clonar(); }
}