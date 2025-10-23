package Controllers;

import Models.*;
import java.util.ArrayList;
import java.util.List;


public class DatosAFD {
    
    private List<String> tokens = new ArrayList<>(); 
          
    // Convierte varios AFN en un solo AFD
    public AFD convertirAFNsSeleccionados(List<AFN> listaAFNs, List<String> tokens) {
        // 1️⃣ Crear un nuevo AFN combinado vacío
        AFN combinado = new AFN();
        Estado nuevoInicial = new Estado();
        combinado.edoInicial = nuevoInicial;

        // Unir todos los AFN seleccionados bajo un solo inicial con transiciones ε
        for (AFN a : listaAFNs) {
            nuevoInicial.transiciones.add(new Transition(SimbEsp.EPSILON, a.edoInicial));

            // Fusionar los estados y el alfabeto de cada AFN
            combinado.estados.addAll(a.estados);
            combinado.alfabeto.addAll(a.alfabeto);
            combinado.alfabeto.addAll(a.alfabeto);

            // Marcar los estados de aceptación con su token
            for (Estado e : a.edosAcept) {
                e.token = tokens.get(listaAFNs.indexOf(a)); // Asigna el token al AFN correspondiente
            }
        
            combinado.edosAcept.addAll(a.edosAcept);
        }

        combinado.estados.add(nuevoInicial);

        // Llama al método de AFD
        AFD afdFinal = new AFD().AFNtoAFD(combinado);

        // Guarda los Tokens en el AFD
        afdFinal.setTokens(tokens);

        return afdFinal;
    }
    
}
