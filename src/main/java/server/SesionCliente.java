/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import utilidades.ObservadorSesion;
import entidades.LineaPedido;
import entidades.Pedido;
import entidades.Usuario;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rafam
 */
public class SesionCliente {
    
    private Pedido pedidoSesion;
    private Usuario clienteAnfitrion;
    private List<HiloCliente> invitados;
    private List<LineaPedido> lineasDePedido;
    private List<ObservadorSesion> observadores;
    
    public SesionCliente(Usuario clienteAnfitrion, Pedido pedidoSesion){
        this.clienteAnfitrion = clienteAnfitrion;
        this.pedidoSesion = pedidoSesion;
        invitados = new ArrayList<>();
        observadores = new ArrayList<>();
    }

    public void suscribir(ObservadorSesion observador) {
        observadores.add(observador); // Se añade el observador a la lista de observadores
    }

    public void notificarActualizacion() {
        // Se llama al método "actualizar" de todos los observadores
        observadores.forEach(ObservadorSesion::actualizar);
    }

    public void agregarLineaDePedido(LineaPedido lineaDePedido) {
        lineasDePedido.add(lineaDePedido);
        notificarActualizacion(); // Se notifica a los observadores que ha habido una actualización en la sesión
    }

    public Pedido getPedidoSesion() {
        return pedidoSesion;
    }

    public void setPedidoSesion(Pedido pedidoSesion) {
        this.pedidoSesion = pedidoSesion;
    }

    public Usuario getClienteAnfitrion() {
        return clienteAnfitrion;
    }

    public void setClienteAnfitrion(Usuario clienteAnfitrion) {
        this.clienteAnfitrion = clienteAnfitrion;
    }

    public List<LineaPedido> getLineasDePedido() {
        return lineasDePedido;
    }

    public void setLineasDePedido(List<LineaPedido> lineasDePedido) {
        this.lineasDePedido = lineasDePedido;
    }
    
    
    
}
