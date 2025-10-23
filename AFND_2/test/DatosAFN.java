package Controllers;

import Models.*;
//import java.awt.List;
import java.util.ArrayList;

public class DatosAFN {
    //Lista para almancenar los AFN creados 
    private java.util.List<AFN> listaAFN = new ArrayList<>();

    public void agregarAFN(AFN c) {
        listaAFN.add(c);
    }

    public java.util.List<AFN> getCaracteres() {
        return listaAFN;
    }
    
    public AFN CrearBasico(char c){
        AFN nuevo = new AFN();
        nuevo.crearBasico(c);
        listaAFN.add(nuevo);
        return nuevo;
    }
    
    public AFN CrearBasico2(char c1, char c2){
        AFN nuevo2 = new AFN();
        nuevo2.crearBasico(c1, c2);
        listaAFN.add(nuevo2);
        return nuevo2;
    }
    
    public AFN unir(int index1, int index2){
        if (index1 < 0 || index2 < 0 || index1 >= listaAFN.size() || index2 >= listaAFN.size()) {
            throw new IllegalArgumentException("Índices no para unión.");
        }

        AFN a1 = listaAFN.get(index1);
        AFN a2 = listaAFN.get(index2);

        AFN resultado = a1.unir(a2);
        listaAFN.add(resultado);
        
        if (index1 > index2) {
            getListaAFN().remove(index1);
            getListaAFN().remove(index2);
        } else {
            getListaAFN().remove(index2);
            getListaAFN().remove(index1);
        }
        //System.out.println("Intentando unir índices: " + index1 + " y " + index2);
        //System.out.println("Tamaño de lista: " + listaAFN.size());
        return resultado;
    }
    
    public AFN concatenar(int index1, int index2){
        if (index1 < 0 || index2 < 0 || index1 >= listaAFN.size() || index2 >= listaAFN.size()) {
            throw new IllegalArgumentException("Índices inválidos para concatenar.");
        }

        AFN a1 = listaAFN.get(index1);
        AFN a2 = listaAFN.get(index2);

        AFN resultado = a1.concatenar(a2);
        listaAFN.add(resultado);
        
        if (index1 > index2) {
            getListaAFN().remove(index1);
            getListaAFN().remove(index2);
        } else {
            getListaAFN().remove(index2);
            getListaAFN().remove(index1);
        }
        return resultado;
    }
    
    public AFN cerraduraKleene(int index) {
        if (index < 0 || index >= listaAFN.size()) {
            throw new IllegalArgumentException("Índice inválido para cerradura.");
        }

        AFN a = listaAFN.get(index);
        AFN resultado = a.cerraduraKleene();
        listaAFN.add(resultado);
        getListaAFN().remove(index);
        return resultado;
    }
    
    public AFN cerrPos(int index){
        if (index < 0 || index >= listaAFN.size()) {
            throw new IllegalArgumentException("Índice inválido para cerradura positiva.");
        }

        AFN a = listaAFN.get(index);
        AFN resultado = a.cerraduraPositiva();
        listaAFN.add(resultado);
        getListaAFN().remove(index);
        return resultado;
    }
    
    public AFN cerrOpc(int index){
        if (index < 0 || index >= listaAFN.size()) {
            throw new IllegalArgumentException("Índice inválido para cerradura opcional.");
        }

        AFN a = listaAFN.get(index);
        AFN resultado = a.cerraduraOpcional();
        listaAFN.add(resultado);
        getListaAFN().remove(index);
        return resultado;
    }
    
    //Métodos de apoyo
    // Devuelve todos los AFN almacenados
    public java.util.List<AFN> getListaAFN() {
        return listaAFN;
    }
    
    // Obtiene un AFN específico por índice
    public AFN getAFN(int index) {
        if (index < 0 || index >= listaAFN.size()) {
            throw new IllegalArgumentException("Índice inválido.");
        }
        return listaAFN.get(index);
    }
    
    public String mostrarAFNs() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < listaAFN.size(); i++) {
            sb.append(i).append(": ").append(listaAFN.get(i).toString()).append("\n");
        }
        return sb.toString();
    }
}
