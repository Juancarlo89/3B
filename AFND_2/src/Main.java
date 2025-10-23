// Puedes colocar esta clase en tu paquete raíz (fuera de Models, Views, Controllers)
import Views.*; // Importa todas tus clases de Vista
import Controllers.Controller; // Importa tu Controlador

/**
 * Clase principal que inicia la aplicación del analizador de autómatas.
 * Crea e inicializa la arquitectura MVC (Modelo-Vista-Controlador).
 */
public class Main {

    /**
     * El punto de entrada principal de la aplicación.
     * @param args Argumentos de línea de comandos (no se usan).
     */
    public static void main(String[] args) {
        
        // Se asegura de que la creación de la GUI se ejecute en el hilo
        // de despacho de eventos de Swing (Event Dispatch Thread o EDT)
        // para evitar problemas de concurrencia.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // --- 1. Crear Instancias de TODAS las Vistas ---
                // (Se crean aquí, pero la mayoría permanecen ocultas)
                Principal viewMain = new Principal();
                CrearBasico viewCrearBasico = new CrearBasico();
                CrearBasico2 viewCrearBasico2 = new CrearBasico2();
                Unir viewUnir = new Unir();
                Concatenar viewConcatenar = new Concatenar();
                CerrK viewCerrK = new CerrK();
                CerrO viewCerrO = new CerrO();
                CerrP viewCerrP = new CerrP();
                // Usamos el nombre completo para la Vista AFD para evitar ambigüedad
                Views.AFD viewAFD = new Views.AFD(); 
                AnaLex viewAnaLex = new AnaLex();
                ShowAFN viewShowAFN = new ShowAFN();
                
                // (Los Modelos se crean dinámicamente dentro del Controlador o
                //  cuando son llamados, ej. new AFN(), new Models.AFD())

                // --- 2. Crear Instancia ÚNICA del Controlador ---
                // Le pasamos *todas* las vistas que necesita para trabajar.
                Controller controller = new Controller(
                    viewMain,
                    viewCrearBasico,
                    viewCrearBasico2,
                    viewUnir,
                    viewConcatenar,
                    viewCerrK,
                    viewCerrO,
                    viewCerrP,
                    viewAFD, // Pasa la instancia de Views.AFD
                    viewAnaLex,
                    viewShowAFN
                );
                
                // (En este punto, el constructor del 'controller' ya ha
                //  adjuntado todos los listeners a los botones de las vistas)

                // --- 3. Mostrar la Vista Principal ---
                // ¡La aplicación está lista para empezar!
                viewMain.setVisible(true);
            }
        });
    }
}