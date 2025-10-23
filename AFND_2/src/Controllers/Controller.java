package Controllers;

// Importar todos los Modelos y Vistas
import Models.*;
import Views.*;
import Models.AnalizLexico;
import Models.SimbEsp;     // <-- Añade esta
import Models.AFD;

// Imports de Java Swing y AWT
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Imports de Java Util
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap; // Para el par (ID, Token) de la vista AFD
import java.util.stream.Collectors; // Para Java 8+
import java.util.function.Consumer; // Para Java 8+

/**
 * El Controlador Principal (Cerebro) de la aplicación.
 * Conecta las Vistas con los Modelos, maneja la lógica de la aplicación,
 * la navegación entre ventanas y el estado de los autómatas creados.
 */
public class Controller {

    // --- Referencias a las Vistas ---
    private final Principal viewMain;
    private final CrearBasico viewCrearBasico;
    private final CrearBasico2 viewCrearBasico2;
    private final Unir viewUnir;
    private final Concatenar viewConcatenar;
    private final CerrK viewCerrK;
    private final CerrO viewCerrO;
    private final CerrP viewCerrP;
    private final Views.AFD viewAFD; // Especificar paquete para la Vista AFD
    private final AnaLex viewAnaLex;
    private final ShowAFN viewShowAFN;

    // --- Estado de la Aplicación ---
    // Mapa para almacenar los AFNs creados por el usuario, usando un ID como clave
    private final Map<String, AFN> automatasCreados;

    /**
     * Constructor del Controlador.
     * Recibe todas las vistas, inicializa el estado y conecta los listeners.
     */
    public Controller(Principal viewMain, CrearBasico viewCrearBasico, CrearBasico2 viewCrearBasico2,
                      Unir viewUnir, Concatenar viewConcatenar, CerrK viewCerrK, CerrO viewCerrO,
                      CerrP viewCerrP, Views.AFD viewAFD, AnaLex viewAnaLex, ShowAFN viewShowAFN) {

        this.viewMain = viewMain;
        this.viewCrearBasico = viewCrearBasico;
        this.viewCrearBasico2 = viewCrearBasico2;
        this.viewUnir = viewUnir;
        this.viewConcatenar = viewConcatenar;
        this.viewCerrK = viewCerrK;
        this.viewCerrO = viewCerrO;
        this.viewCerrP = viewCerrP;
        this.viewAFD = viewAFD;
        this.viewAnaLex = viewAnaLex;
        this.viewShowAFN = viewShowAFN;

        this.automatasCreados = new HashMap<>();
        attachListeners();
    }

    /**
     * Adjunta todos los ActionListeners a los botones de las Vistas.
     */
    private void attachListeners() {
        // === Listeners Vista Principal ===
        viewMain.getBotonCrearBasico().addActionListener(e -> accionAbrirCrearBasico());
        viewMain.getBotonCrearBasico2().addActionListener(e -> accionAbrirCrearBasico2());
        viewMain.getBotonUnir().addActionListener(e -> accionAbrirUnir());
        viewMain.getBotonConcatenar().addActionListener(e -> accionAbrirConcatenar());
        viewMain.getBotonCerraduraPositiva().addActionListener(e -> accionAbrirCerrP());
        viewMain.getBotonCerraduraOpcional().addActionListener(e -> accionAbrirCerrO());
        viewMain.getBotonCerraduraKleene().addActionListener(e -> accionAbrirCerrK()); // Usa CK_btn
        viewMain.getBotonConvertirAFD().addActionListener(e -> accionAbrirConvertirAFD());
        viewMain.getBotonMostrarAFN().addActionListener(e -> accionAbrirMostrarAFN());
        viewMain.getBotonAnalizadorLexico().addActionListener(e -> accionAbrirAnalizadorLexico());
        viewMain.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { accionSalirAplicacion(); }
        });

        // === Listeners CrearBasico ===
        viewCrearBasico.getBotonOK().addActionListener(e -> accionEjecutarCrearBasico());
        viewCrearBasico.getBotonCancelar().addActionListener(e -> {
            viewCrearBasico.setCaracter("");
            navegar(viewCrearBasico, viewMain);
        });
        viewCrearBasico.getCampoDeTexto().addActionListener(e -> accionEjecutarCrearBasico()); // Enter en texto

        // === Listeners CrearBasico2 ===
        viewCrearBasico2.getBotonOK().addActionListener(e -> accionEjecutarCrearBasicoRango());
        viewCrearBasico2.getBotonCancelar().addActionListener(e -> {
            viewCrearBasico2.limpiarCampos();
            navegar(viewCrearBasico2, viewMain);
        });
        viewCrearBasico2.getCampoCaracterFinal().addActionListener(e -> accionEjecutarCrearBasicoRango()); // Enter en último texto

        // === Listeners Unir ===
        viewUnir.getBotonOK().addActionListener(e -> accionEjecutarUnir());
        viewUnir.getBotonCancelar().addActionListener(e -> navegar(viewUnir, viewMain));

        // === Listeners Concatenar ===
        viewConcatenar.getBotonOK().addActionListener(e -> accionEjecutarConcatenar());
        viewConcatenar.getBotonCancelar().addActionListener(e -> navegar(viewConcatenar, viewMain));

        // === Listeners CerrK ===
        viewCerrK.getBotonOK().addActionListener(e -> accionEjecutarCerraduraKleene());
        viewCerrK.getBotonCancelar().addActionListener(e -> navegar(viewCerrK, viewMain));

        // === Listeners CerrO ===
        viewCerrO.getBotonOK().addActionListener(e -> accionEjecutarCerraduraOpcional());
        viewCerrO.getBotonCancelar().addActionListener(e -> navegar(viewCerrO, viewMain));

        // === Listeners CerrP ===
        viewCerrP.getBotonOK().addActionListener(e -> accionEjecutarCerraduraPositiva());
        viewCerrP.getBotonCancelar().addActionListener(e -> navegar(viewCerrP, viewMain));

        // === Listeners AFD (Conversión) ===
        viewAFD.getBotonOK().addActionListener(e -> accionEjecutarConvertirAFD());
        viewAFD.getBotonCancelar().addActionListener(e -> navegar(viewAFD, viewMain));
        // (No hay botón seleccionar en la versión JTable)

        // === Listeners AnaLex ===
        viewAnaLex.getBotonOK().addActionListener(e -> accionEjecutarAnalisisLexico());
        viewAnaLex.getBotonCancelar().addActionListener(e -> navegar(viewAnaLex, viewMain));
        viewAnaLex.getBotonSeleccionar().addActionListener(e -> accionSeleccionarArchivo(viewAnaLex, viewAnaLex.getRutaAFD(), path -> viewAnaLex.setRutaAFD(path)));
        viewAnaLex.getCampoExpresion().addActionListener(e -> accionEjecutarAnalisisLexico()); // Enter en texto

        // === Listeners ShowAFN ===
        viewShowAFN.getBotonRegresar().addActionListener(e -> navegar(viewShowAFN, viewMain));
    }

    // --- Métodos de Navegación ---
    private void navegar(JFrame origen, JFrame destino) {
        origen.setVisible(false);
        destino.setVisible(true);
    }
    private void accionSalirAplicacion() {
        int choice = JOptionPane.showConfirmDialog(viewMain, "¿Salir de la aplicación?", "Confirmar Salida", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) { System.exit(0); }
    }

    // --- Acciones para ABRIR ventanas (preparando datos si es necesario) ---
    private void accionAbrirCrearBasico() { navegar(viewMain, viewCrearBasico); }
    private void accionAbrirCrearBasico2() { navegar(viewMain, viewCrearBasico2); }
    private void accionAbrirUnir() {
        List<String> ids = new ArrayList<>(automatasCreados.keySet());
        if (ids.size() < 2) { mostrarError(viewMain, "Necesita al menos 2 AFNs para unir."); return; }
        viewUnir.setListaAutomatas(ids);
        navegar(viewMain, viewUnir);
    }
    private void accionAbrirConcatenar() {
        List<String> ids = new ArrayList<>(automatasCreados.keySet());
        if (ids.size() < 2) { mostrarError(viewMain, "Necesita al menos 2 AFNs para concatenar."); return; }
        viewConcatenar.setListaAutomatas(ids);
        navegar(viewMain, viewConcatenar);
    }
    private void accionAbrirCerrP() {
        List<String> ids = new ArrayList<>(automatasCreados.keySet());
        if (ids.isEmpty()) { mostrarError(viewMain, "No hay AFNs creados."); return; }
        viewCerrP.setListaAutomatas(ids);
        navegar(viewMain, viewCerrP);
    }
    private void accionAbrirCerrO() {
        List<String> ids = new ArrayList<>(automatasCreados.keySet());
        if (ids.isEmpty()) { mostrarError(viewMain, "No hay AFNs creados."); return; }
        viewCerrO.setListaAutomatas(ids);
        navegar(viewMain, viewCerrO);
    }
     private void accionAbrirCerrK() {
        List<String> ids = new ArrayList<>(automatasCreados.keySet());
        if (ids.isEmpty()) { mostrarError(viewMain, "No hay AFNs creados."); return; }
        viewCerrK.setListaAutomatas(ids);
        navegar(viewMain, viewCerrK);
    }
    private void accionAbrirConvertirAFD() {
        List<Object[]> datosParaTabla = new ArrayList<>();
        List<String> sortedIds = automatasCreados.keySet().stream().sorted().collect(Collectors.toList());
        
        if (sortedIds.isEmpty()) {
            mostrarError(viewMain, "No hay AFNs creados para convertir."); return;
        }

        for (String id : sortedIds) {
            AFN afn = automatasCreados.get(id);
            if (afn == null) continue;

            // *** NUEVA LÓGICA PARA MOSTRAR EL TOKEN ***
            String tokensStr = "N/A"; // Valor por defecto
            if (afn.getEdosAcept() != null && !afn.getEdosAcept().isEmpty()) {
                tokensStr = afn.getEdosAcept().stream()
                                .map(Estado::getToken) // Asume Estado.getToken()
                                .filter(token -> token != -1)
                                .map(String::valueOf)
                                .distinct()
                                .collect(Collectors.joining(", "));
                if (tokensStr.isEmpty()) tokensStr = "N/A";
            }
            
            // Pasar {ID, Representacion, TokenString}
            datosParaTabla.add(new Object[]{ id, afn.toString(), tokensStr });
        }
        
        viewAFD.mostrarListaAFN(datosParaTabla);
        navegar(viewMain, viewAFD);
    }
    
    private void accionAbrirMostrarAFN() {
        List<Object[]> datosParaTabla = new ArrayList<>();
        
        if (automatasCreados.isEmpty()) {
            // Asegúrate de pasar 3 columnas
            datosParaTabla.add(new Object[]{"N/A", "No hay AFNs creados.", "N/A"});
        } else {
             List<String> sortedIds = automatasCreados.keySet().stream().sorted().collect(Collectors.toList());
             
             for (String id : sortedIds) {
                 AFN afn = automatasCreados.get(id);
                 if (afn == null) continue;

                 // *** LÓGICA AÑADIDA PARA OBTENER TOKENS ***
                 String tokensStr = "N/A"; // Valor por defecto
                 if (afn.getEdosAcept() != null && !afn.getEdosAcept().isEmpty()) {
                     // Recolecta todos los tokens únicos de los estados de aceptación
                     tokensStr = afn.getEdosAcept().stream()
                                    .map(Estado::getToken) // Asume Estado.getToken()
                                    .filter(token -> token != -1) // Filtra estados sin token
                                    .map(String::valueOf)
                                    .distinct() // Muestra solo tokens únicos
                                    .collect(Collectors.joining(", "));
                     
                     if (tokensStr.isEmpty()) { // Si todos eran -1
                         tokensStr = "N/A";
                     }
                 }
                 
                 // Añadir fila con 3 columnas
                 datosParaTabla.add(new Object[]{id, afn.toString(), tokensStr});
             }
        }
        viewShowAFN.setTablaAutomatas(datosParaTabla);
        navegar(viewMain, viewShowAFN);
    }
    
    private void accionAbrirAnalizadorLexico() { navegar(viewMain, viewAnaLex); }

    // --- Acciones para EJECUTAR operaciones ---
    
    private void accionEjecutarCrearBasico() {
        String texto = viewCrearBasico.getCaracter();
        if (!validarEntradaCharUnico(texto, viewCrearBasico)) return;
        char c = texto.charAt(0);
        
        // Pedir Token ID
        Integer tokenId = pedirIdToken(viewCrearBasico, "Token para '" + c + "'");
        if (tokenId == null) return; // Usuario canceló

        // Pedir AFN ID
        String idAutomata = pedirIdAutomata(viewCrearBasico, "AFN_" + c);
        if (idAutomata == null) return; // Usuario canceló

        try {
            // Llamar al Modelo con el token
            AFN nuevoAFN = new AFN().crearBasico(c, tokenId);
            // Guardar en el estado del Controlador
            automatasCreados.put(idAutomata, nuevoAFN);
            
            mostrarMensaje(viewCrearBasico, "Éxito: AFN '" + idAutomata + "' [Token:" + tokenId + "] creado.", "Éxito");
            viewCrearBasico.setCaracter("");
            navegar(viewCrearBasico, viewMain);
        } catch (Exception ex) { mostrarErrorModelo(viewCrearBasico, "Error Creación", ex); }
    }

    private void accionEjecutarCrearBasicoRango() {
        String sInicial = viewCrearBasico2.getCaracterInicial();
        String sFinal = viewCrearBasico2.getCaracterFinal();
        if (!validarEntradaCharUnico(sInicial, viewCrearBasico2) || !validarEntradaCharUnico(sFinal, viewCrearBasico2)) return;
        char c1 = sInicial.charAt(0); char c2 = sFinal.charAt(0);
        if (c1 > c2) { mostrarError(viewCrearBasico2, "Rango inválido (inicio > fin)."); return; }
        
        // Pedir Token ID
        Integer tokenId = pedirIdToken(viewCrearBasico2, "Token para [" + c1 + "-" + c2 + "]");
        if (tokenId == null) return;

        // Pedir AFN ID
        String idAutomata = pedirIdAutomata(viewCrearBasico2, "AFN_" + c1 + "-" + c2);
        if (idAutomata == null) return;

        try {
            // Llamar al Modelo con el token
            AFN nuevoAFN = new AFN().crearBasico(c1, c2, tokenId);
            automatasCreados.put(idAutomata, nuevoAFN);
            
            mostrarMensaje(viewCrearBasico2, "Éxito: AFN '" + idAutomata + "' [Token:" + tokenId + "] creado.", "Éxito");
            viewCrearBasico2.limpiarCampos();
            navegar(viewCrearBasico2, viewMain);
        } catch (Exception ex) { mostrarErrorModelo(viewCrearBasico2, "Error Creación Rango", ex); }
    }

    private void accionEjecutarUnir() {
        String id1 = viewUnir.getAutomata1Seleccionado();
        String id2 = viewUnir.getAutomata2Seleccionado();
        if (!validarSeleccionDoble(id1, id2, viewUnir)) return;
        AFN afn1Original = automatasCreados.get(id1);
        AFN afn2Original = automatasCreados.get(id2);
        if (!validarAutomatasExisten(afn1Original, id1, afn2Original, id2, viewUnir)) return;
        try {
            AFN afn1Clonado = afn1Original.clonar();
            AFN afn2Clonado = afn2Original.clonar();
            afn1Clonado.unir(afn2Clonado); // Modifica clon1, consume clon2
            automatasCreados.put(id1, afn1Clonado); // Reemplaza id1 con el resultado
            automatasCreados.remove(id2); // Elimina id2
            mostrarMensaje(viewUnir, String.format("Éxito: '%s' ahora es la unión. '%s' eliminado.", id1, id2), "Éxito");
            navegar(viewUnir, viewMain);
        } catch (Exception ex) { mostrarErrorModelo(viewUnir, "Error durante la unión", ex); }
    }

    private void accionEjecutarConcatenar() {
        String id1 = viewConcatenar.getAutomata1Seleccionado();
        String id2 = viewConcatenar.getAutomata2Seleccionado();
        if (!validarSeleccionDoble(id1, id2, viewConcatenar)) return;
        AFN afn1Original = automatasCreados.get(id1);
        AFN afn2Original = automatasCreados.get(id2);
        if (!validarAutomatasExisten(afn1Original, id1, afn2Original, id2, viewConcatenar)) return;
        try {
            AFN afn1Clonado = afn1Original.clonar();
            AFN afn2Clonado = afn2Original.clonar();
            afn1Clonado.concatenar(afn2Clonado); // Modifica clon1, consume clon2
            automatasCreados.put(id1, afn1Clonado); // Reemplaza id1
            automatasCreados.remove(id2); // Elimina id2
             mostrarMensaje(viewConcatenar, String.format("Éxito: '%s' ahora es la concatenación. '%s' eliminado.", id1, id2), "Éxito");
            navegar(viewConcatenar, viewMain);
        } catch (Exception ex) { mostrarErrorModelo(viewConcatenar, "Error durante la concatenación", ex); }
    }

     private void accionEjecutarCerraduraKleene() {
        String id = viewCerrK.getAutomataSeleccionado();
        if (!validarSeleccionSimple(id, viewCerrK)) return;
        AFN afn = automatasCreados.get(id);
        if (!validarAutomataExiste(afn, id, viewCerrK)) return;
        try {
            afn.cerraduraKleene(); // Modifica el AFN en el mapa
            mostrarMensaje(viewCerrK, "Éxito: Cerradura Kleene aplicada a '" + id + "'.", "Éxito");
            navegar(viewCerrK, viewMain);
        } catch (Exception ex) { mostrarErrorModelo(viewCerrK, "Error Cerradura *", ex); }
    }

    private void accionEjecutarCerraduraOpcional() {
        String id = viewCerrO.getAutomataSeleccionado();
        if (!validarSeleccionSimple(id, viewCerrO)) return;
        AFN afn = automatasCreados.get(id);
        if (!validarAutomataExiste(afn, id, viewCerrO)) return;
        try {
            afn.cerraduraOpcional(); // Modifica el AFN en el mapa
            mostrarMensaje(viewCerrO, "Éxito: Cerradura Opcional aplicada a '" + id + "'.", "Éxito");
            navegar(viewCerrO, viewMain);
        } catch (Exception ex) { mostrarErrorModelo(viewCerrO, "Error Cerradura ?", ex); }
    }

    private void accionEjecutarCerraduraPositiva() {
        String id = viewCerrP.getAutomataSeleccionado();
        if (!validarSeleccionSimple(id, viewCerrP)) return;
        AFN afn = automatasCreados.get(id);
        if (!validarAutomataExiste(afn, id, viewCerrP)) return;
        try {
            afn.cerraduraPositiva(); // Modifica el AFN en el mapa
            mostrarMensaje(viewCerrP, "Éxito: Cerradura Positiva aplicada a '" + id + "'.", "Éxito");
            navegar(viewCerrP, viewMain);
        } catch (Exception ex) { mostrarErrorModelo(viewCerrP, "Error Cerradura +", ex); }
    }

    private void accionEjecutarConvertirAFD() {
        // 1. Obtener la LISTA de IDs seleccionados
        List<String> idsSeleccionados = viewAFD.getSeleccionados(); // <-- USA EL NUEVO MÉTODO
        if (idsSeleccionados == null) return; // Error ya mostrado por la Vista

        AFN afnCombinado = null;
        boolean primerAFN = true;

        try {
            // 2. Unir (clonando) los AFNs
            for (String id : idsSeleccionados) {
                AFN afnOriginal = automatasCreados.get(id);
                if (!validarAutomataExiste(afnOriginal, id, viewAFD)) return;

                AFN copiaAFN = afnOriginal.clonar(); // ¡Clona el AFN con su token!

                // *** YA NO SE ASIGNA TOKEN AQUÍ ***
                // El token ya está DENTRO de la 'copiaAFN'.

                if (primerAFN) {
                    afnCombinado = copiaAFN;
                    primerAFN = false;
                } else {
                    afnCombinado.unir(copiaAFN);
                }
            }
            if (afnCombinado == null) { mostrarError(viewAFD, "No se pudo generar el AFN."); return; }

            // 3. Convertir
            Models.AFD afdConvertido = new Models.AFD().AFNtoAFD(afnCombinado);

            // 4. Guardar
            JFileChooser saveChooser = new JFileChooser();
            saveChooser.setDialogTitle("Guardar AFD combinado como...");
            String nombreSugerido = idsSeleccionados.stream().collect(Collectors.joining("_")) + ".afd";
            saveChooser.setSelectedFile(new File(nombreSugerido));

            if (saveChooser.showSaveDialog(viewAFD) == JFileChooser.APPROVE_OPTION) {
                String rutaGuardarAFD = saveChooser.getSelectedFile().getAbsolutePath();
                if (!rutaGuardarAFD.toLowerCase().endsWith(".afd")) rutaGuardarAFD += ".afd";
                try {
                    afdConvertido.guardarAFDenArchivo(rutaGuardarAFD);
                    mostrarMensaje(viewAFD, "Éxito. AFD guardado en:\n" + rutaGuardarAFD, "Conversión AFN->AFD");
                    navegar(viewAFD, viewMain);
                } catch (IOException ioEx) { mostrarErrorModelo(viewAFD, "Error al guardar AFD", ioEx); }
            } else { mostrarMensaje(viewAFD, "Guardado cancelado.", "Cancelado"); }

        } catch (RuntimeException cloneEx) {
             mostrarErrorModelo(viewAFD, "Error crítico al clonar AFN", cloneEx);
        } catch (Exception convEx) {
             mostrarErrorModelo(viewAFD, "Error durante la unión o conversión", convEx);
        }
    }

     private void accionEjecutarAnalisisLexico() {
        String rutaAFD = viewAnaLex.getRutaAFD();
        String expresion = viewAnaLex.getExpresion();
        if (rutaAFD.trim().isEmpty()) { mostrarError(viewAnaLex, "Seleccione archivo AFD."); return; }
        if (expresion.isEmpty()) { mostrarError(viewAnaLex, "Ingrese expresión."); return; }
        try {
            Models.AFD afdParaLexer = new Models.AFD();
            afdParaLexer.leerAFDdeArchivo(rutaAFD);
            AnalizLexico lexer = new AnalizLexico(expresion, afdParaLexer);
            List<Object[]> resultadosTabla = new ArrayList<>();
            int token;
            while ((token = lexer.yylex()) != SimbEsp.FIN) {
                String lexema = lexer.getLexema();
                if (token == SimbEsp.ERROR) { resultadosTabla.add(new Object[]{"ERROR", lexema}); }
                else if (token != SimbEsp.OMITIR) { resultadosTabla.add(new Object[]{lexema, token}); }
            }
            resultadosTabla.add(new Object[]{"<FIN>", SimbEsp.FIN});
            viewAnaLex.setTablaResultados(resultadosTabla);
            mostrarMensaje(viewAnaLex, "Análisis completado.", "Resultado");
        } catch (IOException | NumberFormatException | ArrayIndexOutOfBoundsException fileEx) {
             mostrarErrorModelo(viewAnaLex, "Error al leer o procesar archivo AFD", fileEx);
             viewAnaLex.setTablaResultados(new ArrayList<>());
        } catch (Exception lexEx) {
             mostrarErrorModelo(viewAnaLex, "Error durante el análisis léxico", lexEx);
             viewAnaLex.setTablaResultados(new ArrayList<>());
        }
    }

    // --- Acciones de Selección de Archivo (Java 8+) ---
    private void accionSeleccionarArchivo(JFrame vistaPadre, String rutaActual, Consumer<String> setterAccion) {
        JFileChooser fileChooser = new JFileChooser();
        File currentFile = new File(rutaActual);
        if (currentFile.exists() && !currentFile.isDirectory()) {
             fileChooser.setCurrentDirectory(currentFile.getParentFile());
             fileChooser.setSelectedFile(currentFile);
        } else if (currentFile.exists() && currentFile.isDirectory()) {
             fileChooser.setCurrentDirectory(currentFile);
        } else { fileChooser.setCurrentDirectory(new File(System.getProperty("user.home"))); }

        int resultado = fileChooser.showOpenDialog(vistaPadre);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();
            setterAccion.accept(archivoSeleccionado.getAbsolutePath());
        }
    }

    // --- Métodos Auxiliares ---
     private void mostrarError(JFrame parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
     private void mostrarErrorModelo(JFrame parent, String titulo, Exception ex) {
         String mensaje = "Ocurrió un error en la operación:\n" + ex.getMessage();
         ex.printStackTrace(); // Útil para debugging en consola
         JOptionPane.showMessageDialog(parent, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }
     private void mostrarMensaje(JFrame parent, String mensaje, String titulo) {
        JOptionPane.showMessageDialog(parent, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }
    private boolean validarEntradaCharUnico(String texto, JFrame parent) {
        if (texto.isEmpty()) { mostrarError(parent, "Ingrese carácter."); return false; }
        if (texto.length() > 1) { mostrarError(parent, "Solo un carácter."); return false; }
        return true;
    }
    private String pedirIdAutomata(JFrame parent, String sugerencia) {
        String idAutomata = null; boolean idValido = false;
        while (!idValido) {
            idAutomata = JOptionPane.showInputDialog(parent, "ID único para el nuevo AFN:", sugerencia);
            if (idAutomata == null) return null; // Canceló
            idAutomata = idAutomata.trim();
            if (idAutomata.isEmpty()) { mostrarError(parent, "El ID no puede estar vacío."); continue; }
            if (automatasCreados.containsKey(idAutomata)) { mostrarError(parent, "El ID '" + idAutomata + "' ya existe."); sugerencia = idAutomata; continue; }
            idValido = true;
        } return idAutomata;
    }
    private Integer pedirIdToken(JFrame parent, String descripcion) {
        Integer tokenId = null; boolean idValido = false;
        while (!idValido) {
            String input = JOptionPane.showInputDialog(parent, "ID numérico del Token para " + descripcion + ":\n(Ej: 100, 200, 2001=OMITIR)", "Asignar Token", JOptionPane.QUESTION_MESSAGE);
            if (input == null) return null; // Canceló
            try {
                tokenId = Integer.parseInt(input.trim());
                if (tokenId == SimbEsp.ERROR || tokenId == (int)SimbEsp.FIN) {
                     mostrarError(parent, "IDs " + SimbEsp.ERROR + "/" + (int)SimbEsp.FIN + " reservados."); continue;
                }
                idValido = true;
            } catch (NumberFormatException e) { mostrarError(parent, "Ingrese un número entero válido."); }
        } return tokenId;
    }
     private boolean validarSeleccionSimple(String id, JFrame parent) {
        if (id == null) { mostrarError(parent, "Seleccione un AFN."); return false; }
        return true;
    }
    private boolean validarSeleccionDoble(String id1, String id2, JFrame parent) {
        if (id1 == null || id2 == null) { mostrarError(parent, "Seleccione ambos AFNs."); return false; }
        if (id1.equals(id2)) { mostrarError(parent, "Seleccione AFNs diferentes."); return false; }
        return true;
    }
     private boolean validarAutomataExiste(AFN afn, String id, JFrame parent) {
        if (afn == null) { mostrarError(parent, "Error interno: AFN '" + id + "' no encontrado en memoria."); return false; }
        return true;
    }
    private boolean validarAutomatasExisten(AFN afn1, String id1, AFN afn2, String id2, JFrame parent) {
        return validarAutomataExiste(afn1, id1, parent) && validarAutomataExiste(afn2, id2, parent);
    }

} // Fin de la clase Controller