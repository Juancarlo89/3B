package Models;

import java.util.Objects; // Para validación de nulls
import java.util.Stack;

/**
 * Clase de análisis léxico (lexer) que procesa una cadena de entrada usando un AFD.
 * Genera tokens y lexemas basados en la tabla de transición del AFD,
 * siguiendo el principio de "maximal munch" (coincidencia más larga).
 */
public class AnalizLexico {

    // --- Atributos Internos ---
    private int token;                     // Último token encontrado
    private String lexema;                 // Último lexema encontrado
    private String cadenaSigma;            // Cadena de entrada completa
    private AFD automataFD;                // Autómata a utilizar

    // --- Estado Interno del Analizador ---
    private int edoActual;                 // Estado actual en el AFD
    private int edoTransicion;             // Estado al que se transita
    private boolean pasoPorEdoAcept;       // Indica si se pasó por un estado de aceptación
    private int iniLexema;                 // Índice de inicio del lexema actual
    private int finLexema;                 // Índice de fin del último lexema válido encontrado
    private int indiceCaracterActual;      // Posición actual en cadenaSigma
    private char caracterActual;           // Carácter actual siendo procesado
    
    // Pila para soportar backtracking (ej. undoToken) si el parser lo requiere
    private Stack<Integer> pila;

    // ============================================
    // Constructores
    // ============================================

    /**
     * Constructor por defecto (Analizador no inicializado).
     * Se debe llamar a setSigma() y setAFD() antes de usar yylex().
     */
    public AnalizLexico() {
        cadenaSigma = "";
        automataFD = null;
        pila = new Stack<>();
        resetState(); // Inicializa el resto de variables de estado
    }

    /**
     * Constructor principal.
     * @param sigma La cadena de entrada a analizar (no debe ser null).
     * @param afd El Autómata Finito Determinista a usar (no debe ser null, y su tabla interna tampoco).
     * @throws NullPointerException si sigma o afd son null.
     * @throws IllegalArgumentException si la tabla del AFD es null.
     */
    public AnalizLexico(String sigma, AFD afd) {
        // Validación de entradas nulas (Java 7+)
        this.cadenaSigma = Objects.requireNonNull(sigma, "La cadena de entrada (sigma) no puede ser null.");
        this.automataFD = Objects.requireNonNull(afd, "El AFD no puede ser null.");
        
        if (automataFD.getTablaAFD() == null) {
            throw new IllegalArgumentException("El AFD proporcionado no tiene una tabla de transiciones inicializada.");
        }
        
        this.pila = new Stack<>();
        resetState(); // Inicializa el resto de variables de estado
    }
    
    /** Resetea el estado interno del analizador a sus valores iniciales. */
    private void resetState() {
        pasoPorEdoAcept = false;
        iniLexema = 0;
        finLexema = -1;
        indiceCaracterActual = 0;
        token = -1;
        lexema = "";
        if (pila != null) { // Asegurarse de que la pila exista antes de limpiarla
            pila.clear();
        }
    }


    // ============================================
    // Métodos principales
    // ============================================

    /**
     * Obtiene el siguiente token de la cadena de entrada.
     * Avanza el estado interno del analizador.
     * @return El ID del token encontrado (definido en SimbEsp), SimbEsp.FIN si se llegó al final,
     * o SimbEsp.ERROR si se encontró un carácter no reconocido.
     */
    public int yylex() {
        // Validar estado inicial
        if (automataFD == null || automataFD.getTablaAFD() == null) {
             System.err.println("Error: Analizador Léxico no inicializado con un AFD válido.");
             lexema = ""; // Asegurar que lexema no sea null
             token = SimbEsp.ERROR;
             return SimbEsp.ERROR;
        }
        
        int[][] tabla = automataFD.getTablaAFD(); // Obtener la tabla una vez

        while (true) { // Bucle externo para manejar SimbEsp.OMITIR
            // Guarda la posición actual para posible backtracking con undoToken()
            pila.push(indiceCaracterActual); 

            // --- Comprobar fin de cadena ---
            if (indiceCaracterActual >= cadenaSigma.length()) {
                lexema = "";
                token = SimbEsp.FIN;
                return SimbEsp.FIN;
            }

            // --- Reiniciar búsqueda para el nuevo lexema ---
            iniLexema = indiceCaracterActual;
            edoActual = 0; // Siempre empezamos en el estado 0 del AFD
            pasoPorEdoAcept = false;
            finLexema = -1;
            token = -1;
            lexema = "";

            // --- Simular el AFD (Bucle interno - Maximal Munch) ---
            while (indiceCaracterActual < cadenaSigma.length()) {

                caracterActual = cadenaSigma.charAt(indiceCaracterActual);
                int asciiValor = (int) caracterActual;

                // Validar que el carácter esté dentro del rango esperado por la tabla (0-255)
                // y que el estado actual sea válido.
                if (edoActual < 0 || edoActual >= tabla.length || asciiValor < 0 || asciiValor >= 256) {
                    break; // Salir del bucle interno, estado inválido o carácter fuera de rango
                }

                // Obtener el siguiente estado de la tabla
                edoTransicion = tabla[edoActual][asciiValor];

                // Si hay una transición válida (no es -1)
                if (edoTransicion != -1) {
                    edoActual = edoTransicion; // Moverse al siguiente estado

                    // Comprobar si el NUEVO estado es de aceptación (columna 256)
                    // Validar límites de edoActual de nuevo por seguridad
                    if (edoActual >= 0 && edoActual < tabla.length && tabla[edoActual][256] != -1) {
                        pasoPorEdoAcept = true;
                        token = tabla[edoActual][256]; // Guardar el token de este estado
                        finLexema = indiceCaracterActual; // Guardar la posición del ÚLTIMO carácter aceptado
                    }
                    
                    indiceCaracterActual++; // Avanzar al siguiente carácter de la entrada

                } else {
                    // No hay transición para el caracterActual desde edoActual
                    break; // Salir del bucle interno
                }
            } // Fin del bucle interno (while indiceCaracterActual < cadenaSigma.length())

            // --- Determinar el resultado de la búsqueda ---

            // Si NUNCA pasamos por un estado de aceptación
            if (!pasoPorEdoAcept) {
                // Error léxico: el carácter en iniLexema no puede iniciar ningún token
                indiceCaracterActual = iniLexema + 1; // Avanzamos un carácter
                // Asegurarse de no exceder la longitud de la cadena
                if(indiceCaracterActual > cadenaSigma.length()) {
                     lexema = cadenaSigma.substring(iniLexema);
                } else {
                     lexema = cadenaSigma.substring(iniLexema, indiceCaracterActual);
                }
                token = SimbEsp.ERROR;
                return token; // Devolver el error
            }

            // Si SÍ pasamos por un estado de aceptación (Maximal Munch)
            // El lexema válido es desde iniLexema hasta finLexema (inclusive)
            lexema = cadenaSigma.substring(iniLexema, finLexema + 1);
            // La siguiente búsqueda empezará DESPUÉS de este lexema
            indiceCaracterActual = finLexema + 1;

            // Comprobar si el token encontrado debe ser omitido
            if (token == SimbEsp.OMITIR) {
                continue; // Vuelve al inicio del bucle `while (true)`
            } else {
                return token; // Devolver el token encontrado
            }
        } // Fin del bucle externo (while true)
    }

    // ============================================
    // Métodos auxiliares (Getters y Setters)
    // ============================================

    /**
     * Retrocede el estado del analizador a la posición donde empezó
     * la última llamada a yylex().
     * @return true si se pudo retroceder, false si la pila estaba vacía.
     */
    public boolean undoToken() {
        if (pila.isEmpty()) {
            return false;
        }
        indiceCaracterActual = pila.pop(); 
        token = -1; // Resetear token/lexema
        lexema = "";
        return true;
    }

    /**
     * Establece una nueva cadena de entrada para analizar, reseteando el estado interno.
     * @param sigma La nueva cadena (no debe ser null).
     */
    public void setSigma(String sigma) {
        cadenaSigma = Objects.requireNonNull(sigma, "La cadena de entrada (sigma) no puede ser null.");
        resetState(); // Resetea la posición, pila, etc.
    }

    /**
     * Devuelve la porción de la cadena de entrada que aún no ha sido analizada.
     * @return La subcadena restante, o "" si ya se llegó al final.
     */
    public String cadenaPorAnalizar() {
        if (cadenaSigma == null || indiceCaracterActual >= cadenaSigma.length()) {
             return "";
        }
        return cadenaSigma.substring(indiceCaracterActual);
    }

    /**
     * Permite cambiar el AFD utilizado por el analizador.
     * Se recomienda resetear la cadena (llamar a setSigma) después de esto.
     * @param afd El nuevo AFD (no debe ser null y debe tener tabla).
     */
    public void setAFD(AFD afd) {
        this.automataFD = Objects.requireNonNull(afd, "El AFD no puede ser null.");
         if (automataFD.getTablaAFD() == null) {
            throw new IllegalArgumentException("El AFD proporcionado no tiene una tabla de transiciones inicializada.");
        }
        // No resetea 'sigma', pero sí la pila y el estado, asumiendo
        // que se podría querer re-analizar la misma cadena con otro AFD.
        resetState(); 
        cadenaSigma = ""; // Opcional: forzar a que se establezca una nueva sigma
    }

    /** Devuelve el último lexema encontrado por yylex(). */
    public String getLexema() {
        return lexema;
    }

    /** Devuelve el último ID de token encontrado por yylex(). */
    public int getToken() {
        return token;
    }

    /** Devuelve el índice actual (la posición del siguiente carácter a leer). */
    public int getIndice() {
        return indiceCaracterActual;
    }
}