package Models;

import java.util.Arrays; // Needed for Arrays.fill and hashCode
import java.util.Objects; // Needed for hashCode

/**
 * Clase que representa un estado dentro de un Autómata Finito Determinista (AFD).
 * Contiene un identificador único y su propia fila de la tabla de transiciones.
 * (Nota: La clase AFD principal utiliza una tabla centralizada int[][],
 * por lo que el uso directo de esta clase puede ser limitado o redundante
 * dependiendo de la implementación final en AFD.java).
 */
public class EdoAFD {

    /** Arreglo que almacena los estados destino para cada símbolo (0-255) y el token (256). */
    private int[] trans; // <-- Cambiado a private y nombre corregido a camelCase

    /** Identificador único del estado. */
    private int id;      // <-- Cambiado a private y nombre corregido a camelCase

    /**
     * Constructor por defecto.
     * Inicializa todas las transiciones con -1 (sin transición) y el id con -1.
     */
    public EdoAFD() {
        // Usa camelCase para nombres de variable
        this.trans = new int[257]; // Tamaño 257 para incluir la columna de token/aceptación
        Arrays.fill(this.trans, -1); // Forma más concisa de inicializar
        this.id = -1;
        // El bucle original 'for(int i=0; i<=256 ; i++)' tenía un índice fuera de límites (hasta 256 inclusive)
    }

    /**
     * Constructor que asigna un identificador específico al estado.
     * (Cambiado a public para poder ser llamado desde fuera del paquete si es necesario).
     * @param idEdo Identificador del estado.
     */
    public EdoAFD(int idEdo) {
        // Llama al constructor base para inicializar transiciones a -1
        this();
        // Asigna el ID proporcionado
        this.id = idEdo;
        // El bucle original 'for (int i = 0; i < 256; i++)' no inicializaba la columna 256
    }

    // ----- Getters -----

    /**
     * Obtiene el identificador único del estado.
     * @return El ID del estado.
     */
    public int getId() {
        return id;
    }

    /**
     * Obtiene el arreglo completo de transiciones para este estado.
     * La columna 256 puede usarse para almacenar el token si es un estado de aceptación.
     * @return Una referencia al arreglo de transiciones.
     * (Considerar devolver una copia si se necesita inmutabilidad).
     */
    public int[] getTrans() {
        return trans;
    }

    /**
     * Obtiene el estado destino para un carácter específico.
     * @param c El carácter (se usará su valor ASCII como índice).
     * @return El ID del estado destino, o -1 si no hay transición o carácter fuera de rango.
     */
    public int getTransicion(char c) {
        int index = (int) c;
        if (index >= 0 && index < 256 && index < trans.length) {
            return trans[index];
        }
        return -1;
    }

    /**
     * Obtiene el token asociado a este estado (almacenado en el índice 256).
     * @return El ID del token, o -1 si no es de aceptación o el array es inválido.
     */
    public int getToken() {
         if (256 < trans.length) {
              return trans[256];
         }
         return -1;
    }

    // ----- Setters (Opcionales, añadidos por completitud) -----

    /** Establece el ID del estado. */
    public void setId(int id) {
        this.id = id;
    }

    /** Establece el arreglo completo de transiciones. */
    public void setTrans(int[] trans) {
        // Añadir validación si es necesario (ej. tamaño 257)
        this.trans = trans;
    }

    /** Establece el estado destino para una transición específica. */
    public void setTransicion(char c, int estadoDestino) {
        int index = (int) c;
        if (index >= 0 && index < 257 && index < trans.length) { // Permite índice 256
            trans[index] = estadoDestino;
        } else {
            System.err.println("Advertencia: Carácter/Índice fuera de rango: " + index);
        }
    }

    /** Establece el token para este estado (columna 256). */
     public void setToken(int token) {
         if (256 < trans.length) {
              trans[256] = token;
         } else {
              System.err.println("Advertencia: Array 'trans' no tiene tamaño 257.");
         }
     }

    // ----- Utilidades -----
    @Override
    public String toString() {
        // Representación más útil para debugging
        int tokenVal = getToken();
        String tokenStr = (tokenVal != -1) ? ", token=" + tokenVal : "";
        return "EdoAFD{id=" + id + tokenStr + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdoAFD edoAFD = (EdoAFD) o;
        return id == edoAFD.id; // Igualdad basada solo en ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Hash basado solo en ID
    }
}