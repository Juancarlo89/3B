package Models;

import java.io.*;
import java.util.*;

public class AFD extends AFN {

    private int numEdos;
    private int[][] tablaAFD;

    public AFD() {
        super();
        this.numEdos = 0;
        this.tablaAFD = null;
        this.alfabeto = new HashSet<>();
    }
    public AFD(int n) {
        this();
        this.numEdos = n;
        this.tablaAFD = new int[n][257];
        for (int[] fila : this.tablaAFD) Arrays.fill(fila, -1);
    }
    public AFD(int n, Set<Character> alf) {
        this(n);
        this.alfabeto = new HashSet<>(alf);
        this.alfabeto.remove(SimbEsp.EPSILON);
    }

    public void guardarAFDenArchivo(String nombreArchivo) throws IOException {
        if (tablaAFD == null || numEdos <= 0 || tablaAFD.length != numEdos) {
            throw new IOException("El AFD está vacío o inconsistente para guardar.");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo))) {
            writer.write(numEdos + "\n");
            for (int i = 0; i < numEdos; i++) {
                if (tablaAFD[i] == null || tablaAFD[i].length != 257) throw new IOException("Fila " + i + " inválida.");
                for (int j = 0; j < 257; j++) {
                    writer.write(tablaAFD[i][j] + (j != 256 ? ";" : ""));
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            System.err.println("Error al guardar el AFD: " + e.getMessage());
            throw e;
        }
    }
    public void leerAFDdeArchivo(String nombreArchivo) throws IOException, NumberFormatException, ArrayIndexOutOfBoundsException {
         try (BufferedReader reader = new BufferedReader(new FileReader(nombreArchivo))) {
            String renglon = reader.readLine();
            if (renglon == null) throw new IOException("Archivo vacío.");
            numEdos = Integer.parseInt(renglon.trim());
            if (numEdos < 0) throw new IOException("Número de estados inválido: " + numEdos);
            if (numEdos == 0) { tablaAFD = new int[0][257]; return; }
            tablaAFD = new int[numEdos][257];
            int idEdo = 0;
            while ((renglon = reader.readLine()) != null && idEdo < numEdos) {
                String[] valores = renglon.split(";");
                if (valores.length < 257) throw new IOException("Línea " + idEdo + " incompleta.");
                for (int k = 0; k < 257; k++) {
                    tablaAFD[idEdo][k] = Integer.parseInt(valores[k]);
                }
                idEdo++;
            }
            if (idEdo != numEdos) throw new IOException("Estados esperados " + numEdos + ", encontrados " + idEdo);
        } catch (IOException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Error al leer el AFD: " + e.getMessage());
            this.numEdos = 0; this.tablaAFD = null; this.alfabeto = new HashSet<>();
            throw e;
        }
    }

    /*
        AFD ConvertirAFN()
    {
        Conjunto <Sj> C;
    
        NumEstadosSj = 0;
        Sj.Conjunto = c.SimbEsp.EPSILON (this.EdoInicial);
        Sj.Id = NumS++;
        
        Q.add(Sj); //Q cola de Sj en donde vamos a tener el conteo de todas las partes en donde ocuparemos el analisis de nuestro alfabeto
        C.add(Sj); //Estos serian los estados que se van a guardar directamente en nuestro programa para tener el respaldo de los AFN
    
    while (Q! = NULL)
    {
    Saux = Q.Dequeue();
    ConjEdos = Saux.Edos;
        foreach (char c  in this.Alfabeto)
        {
            
            //Aqui es donde tenemos que implementar la parte de la identificacion de los EdosAcept
    
            Sjtemp.Edos = this.IrA(Conj.Edos.c);
            indice = Buscar (Sjtemp , c);
                if (indice == -1)
                {
                    Sjtemp.Id = NumSj++;
                    c.add(Sjtemp);
                    Q.add(Sjtemp);
                }
            //Aqui necesitamos realizar una tabla que nos permita trabajar con la tablaAFD, la cual sera de 257 y se iniciaran todos en -1
            //y tambien hemos de agregar que cuando se encuentre un EdoAcept, se guarde para tener de manera perfecta los tokens.
            //Por ejemplo, podriamos utilizar la cola de Q, que es una cola de Sj que guardara datos y la id (token).
        }
    }    
    
    
        int Buscar (Conjunto <edos> R, Conjunto <Sj> C)
    {
        foreach (Sj S in C)
            if (R.equal (S.Edos))
                return S.Id;
    
             return -1;
    }
    */
    
    public AFD AFNtoAFD(AFN a) {
        // Validación de que el AFN de entrada es válido
        if (a == null || a.getEdoInicial() == null || a.getEstados() == null || a.getEdosAcept() == null || a.getAlfabeto() == null) {
            System.err.println("Error: AFN inválido para conversión (componentes nulos).");
            return new AFD(); // Devuelve AFD vacío
        }

        // --- Inicialización ---
        Set<ElemSj> C = new HashSet<>();        // 1. Tu "Conjunto <Sj> C" (Estados AFD ya creados)
        Queue<ElemSj> Q = new LinkedList<>();   // 2. Tu cola "Q" (Estados AFD pendientes)
        int numNuevosEstados = 0;               // 3. Tu "NumEstadosSj"
        List<TransicionAFD> transicionesTemp = new ArrayList<>(); // Almacén temporal para transiciones
        Map<Integer, Integer> tokensAFD = new HashMap<>();       // Mapa para [ID Estado AFD] -> [ID Token]

        // --- Calcular S0 (Estado inicial AFD) ---
        // 4. Tu "Sj.Conjunto = c.SimbEsp.EPSILON (this.EdoInicial)"
        Set<Estado> S0_conjunto = cerraduraEpsilon(a.getEdoInicial()); // (usa método heredado de AFN)
        ElemSj Sj0 = new ElemSj();
        Sj0.setS(S0_conjunto);           // Asigna el conjunto de estados
        Sj0.setId(numNuevosEstados++);   // 5. Tu "Sj.Id = NumS++" (asigna ID 0)

        C.add(Sj0); // 6. Tu "C.add(Sj)"
        Q.add(Sj0); // 7. Tu "Q.add(Sj)"

        // 8. Identificar token para S0 (Petición de tu pseudocódigo)
        int tokenS0 = determinarTokenConPrecedencia(Sj0.getS(), a.getEdosAcept());
        tokensAFD.put(Sj0.getId(), tokenS0); // Guardar token

        // --- Algoritmo de Subconjuntos ---
        while (!Q.isEmpty()) { // 9. Tu "while (Q! = NULL)"
            ElemSj SjAct = Q.poll(); // 10. Tu "Saux = Q.Dequeue()"

            // 11. Tu "foreach (char c in this.Alfabeto)"
            for (char c : a.getAlfabeto()) {
                if (c == SimbEsp.EPSILON) continue; // Ignorar transiciones épsilon

                // 12. Tu "Sjtemp.Edos = this.IrA(Conj.Edos.c)"
                Set<Estado> U = ira(SjAct.getS(), c); // (usa método heredado de AFN)

                if (U.isEmpty()) {
                    transicionesTemp.add(new TransicionAFD(SjAct.getId(), c, -1)); // Transición a sumidero
                    continue;
                }

                // 13. Lógica "Buscar" (inline)
                int idDestino = -1;
                for (ElemSj sjExistente : C) {
                    if (sjExistente.getS().equals(U)) { // Tu "R.equal(S.Edos)"
                        idDestino = sjExistente.getId();   // Tu "return S.Id"
                        break;
                    }
                }

                // 14. Tu "if (indice == -1)"
                if (idDestino == -1) {
                    // Crear nuevo estado AFD (Sjtemp)
                    ElemSj nuevoSj = new ElemSj();
                    nuevoSj.setS(U);
                    nuevoSj.setId(numNuevosEstados++); // Tu "Sjtemp.Id = NumSj++"
                    idDestino = nuevoSj.getId();
                    
                    C.add(nuevoSj); // Tu "c.add(Sjtemp)"
                    Q.add(nuevoSj); // Tu "Q.add(Sjtemp)"

                    // 15. Identificar token para el NUEVO estado (Petición de tu pseudocódigo)
                    int tokenNuevo = determinarTokenConPrecedencia(nuevoSj.getS(), a.getEdosAcept());
                    tokensAFD.put(nuevoSj.getId(), tokenNuevo); // Guardar token
                }
                
                // Añadir la transición a la lista temporal
                transicionesTemp.add(new TransicionAFD(SjAct.getId(), c, idDestino));
            }
        }

        // --- Construcción final del objeto AFD ---
        // 16. Crear la tablaAFD (Petición de tu pseudocódigo)
        AFD afd_conv = new AFD();
        afd_conv.setNumEdos(numNuevosEstados);
        Set<Character> alfabetoFinal = new HashSet<>(a.getAlfabeto());
        alfabetoFinal.remove(SimbEsp.EPSILON);
        afd_conv.setAlfabeto(alfabetoFinal);

        int[][] tablaFinal = new int[numNuevosEstados][257];
        for (int[] fila : tablaFinal) Arrays.fill(fila, -1); // Iniciar en -1

        // Llenar tabla con transiciones
        for (TransicionAFD t : transicionesTemp) {
            if (t.origen>=0 && t.origen<numNuevosEstados && (int)t.simbolo>=0 && (int)t.simbolo<256) {
                tablaFinal[t.origen][(int) t.simbolo] = t.destino;
            }
        }
        // Llenar columna 256 con tokens
        for (int idEstado = 0; idEstado < numNuevosEstados; idEstado++) {
            tablaFinal[idEstado][256] = tokensAFD.getOrDefault(idEstado, -1);
        }
        afd_conv.setTablaAFD(tablaFinal); // Asignar la tabla construida

        // --- DEBUG (Opcional, recomendado) ---
        System.out.println("--- Tabla AFD Generada ---");
        System.out.print("Estado\t");
        for(char ch : alfabetoFinal) System.out.print(ch + "\t");
        System.out.println("...\tToken(256)");
        for(int i=0; i<afd_conv.getNumEdos(); i++) {
            System.out.print(i + "\t");
            for(char ch : alfabetoFinal) System.out.print(tablaFinal[i][(int)ch] + "\t");
            System.out.println("...\t" + tablaFinal[i][256]);
        }
        System.out.println("-------------------------");
        // --- FIN DEBUG ---

        return afd_conv;
    }

    private int determinarTokenConPrecedencia(Set<Estado> conjuntoEstados, Set<Estado> edosAceptAFN) {
        int tokenGanador = -1; // -1 = no aceptación
        if (conjuntoEstados == null || edosAceptAFN == null) return -1;
        
        for (Estado edoAFN : conjuntoEstados) {
            // Asumiendo que Estado tiene isAceptacion() y getToken()
            if (edoAFN != null && edoAFN.isAceptacion() && edosAceptAFN.contains(edoAFN)) {
                int tokenActual = edoAFN.getToken();
                if (tokenActual != -1) { // Si tiene un token válido
                    if (tokenGanador == -1 || tokenActual < tokenGanador) {
                        tokenGanador = tokenActual; // Encontramos uno mejor (ID más bajo)
                    }
                }
            }
        }
        return tokenGanador;
    }

    private static class TransicionAFD {
        int origen; char simbolo; int destino;
        TransicionAFD(int o, char s, int d) { origen = o; simbolo = s; destino = d; }
    }

    @Override
    public Set<Character> getAlfabeto() { return alfabeto; }
    public void setAlfabeto(Set<Character> alfabeto) {
         this.alfabeto = (alfabeto != null) ? new HashSet<>(alfabeto) : new HashSet<>();
         this.alfabeto.remove(SimbEsp.EPSILON);
    }
    public int getNumEdos() { return numEdos; }
    public void setNumEdos(int numEdos) { this.numEdos = numEdos; }
    public int[][] getTablaAFD() { return tablaAFD; }
    public void setTablaAFD(int[][] tablaAFD) { this.tablaAFD = tablaAFD; }
}