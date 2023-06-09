/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import utilidades.ObservadorSesion;
import entidades.LineaPedido;
import entidades.LineaPedidoId;
import entidades.Pedido;
import entidades.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.Message;

/**
 *
 * @author rafam
 */
public class SesionCliente {

    private Pedido pedidoSesion;
    private Usuario clienteAnfitrion;
    private List<HiloCliente> invitados;
    private List<LineaPedido> lineasDePedido;
    private int codigoSesion;
    private int contadorId; //Contador con los id de cada linea (para inserción en la BDD)

    public SesionCliente(Usuario clienteAnfitrion, Pedido pedidoSesion) {
        this.clienteAnfitrion = clienteAnfitrion;
        this.pedidoSesion = pedidoSesion;
        this.lineasDePedido = new ArrayList<>();
        invitados = new ArrayList<>();
        contadorId = 1;
        
        int aleatorio = generarNumeroAleatorio();
        
        while(existeNumSesion(aleatorio)){
            aleatorio = generarNumeroAleatorio();
        }
        
        codigoSesion = aleatorio;
        System.out.println(codigoSesion);
        
    }

    public int generarNumeroAleatorio() {
        Random rand = new Random();
        int numero = rand.nextInt(9000) + 1000;
        return numero;
    }
    
    public boolean existeNumSesion(int aleatorio){
        
        boolean aparece = false;
        
        for(SesionCliente sesion : Server.getSesiones()){
            
            if(sesion.getCodigoSesion() == aleatorio){
                aparece = true;
                break;
            }
            
        }
        
        return aparece;
        
    }

    public ArrayList<Object> convertirLista() {
        ArrayList<Object> listaNueva = new ArrayList<>();

        for (LineaPedido lp : lineasDePedido) {
            ArrayList<Object> lineaPedidoEnviar = new ArrayList<>();
            int idLinea = lp.getId().getIdLineaPedido();
            String cliente = lp.getCliente().getNombre();
            String producto = lp.getProducto().getNombre();
            String estado = lp.getEstado();
            int cantidad = lp.getCantidad();

            lineaPedidoEnviar.add(idLinea);
            lineaPedidoEnviar.add(cliente);
            lineaPedidoEnviar.add(producto);
            lineaPedidoEnviar.add(estado);
            lineaPedidoEnviar.add(cantidad);
            lineaPedidoEnviar.add(lp.getProducto().getPrecio());
            lineaPedidoEnviar.add(lp.getProducto().getImagen());
            
            if(lp.getCodigoRecogida() != 0){
                lineaPedidoEnviar.add(lp.getCodigoRecogida());
            }            


            listaNueva.add(lineaPedidoEnviar);

        }

        return listaNueva;

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
        notificarPedidos();
    }
    
    public int getCodigoSesion(){
        return codigoSesion;
    }

    public List<HiloCliente> getInvitados() {
        return invitados;
    }

    public void setInvitados(List<HiloCliente> invitados) {
        this.invitados = invitados;
    }
    
    public ArrayList<Usuario> getUsuariosInvitados(){
        
        ArrayList<Usuario> usuarios = new ArrayList<>();
        
        for(HiloCliente hilo : invitados){
            usuarios.add(hilo.getUsuario());
        }
        
        return usuarios;
        
    }
    
    public boolean apareceCliente(String cliente){
        
        boolean aparece = false;
        
        for(HiloCliente invitado : invitados){
            
            if(invitado.getUsuario().getNombre().equals(cliente)){
                aparece = true;
                break;
            }
            
        }
        
        return aparece;
        
    }

    public synchronized void añadirInvitado(HiloCliente invitado) {
        invitados.add(invitado);
    }
    
    public synchronized void eliminarInvitado(HiloCliente invitado) {
        invitados.remove(invitado);
    }

    public synchronized void agregarLineasPedido(ArrayList<LineaPedido> lineaDePedido) {

        for (LineaPedido lp : lineaDePedido) {
            lp.setId(new LineaPedidoId(contadorId,pedidoSesion.getId())); //Clave compuesta, con contador y el id del pedido
            lineasDePedido.add(lp);
            System.out.println(lp.getCliente().getNombre() + " - " + lp.getProducto().getNombre());
            contadorId++;
        }
        notificarPedidos();

    }
    
    public synchronized void eliminarLineaPedido(LineaPedido lineaPedido){
        
        System.out.println(lineasDePedido.size());
        
        for(LineaPedido linea : lineasDePedido){
            
            if(linea.getId().getIdLineaPedido() == lineaPedido.getId().getIdLineaPedido()){
                
                lineasDePedido.remove(linea);
                break;
                
            }
            
        }
        
        System.out.println(lineasDePedido.size());
        notificarPedidos();
        
    }
    
    /*
    public synchronized void setLineasPedido(ArrayList<LineaPedido> lineaDePedido){
        
        lineasDePedido = lineaDePedido;
        notificarPedidos();
        
        
    }
*/
    
    
    public synchronized void actualizarCantidades(ArrayList<Integer> nuevasCantidades){
        
        for (int i = 0; i < lineasDePedido.size(); i++) {
            
            lineasDePedido.get(i).setCantidad(nuevasCantidades.get(i));
            
        }
        
        notificarPedidos();
        
    }
    
    
    public synchronized void notificarPedidos() {
        for (HiloCliente observador : invitados) {
            System.out.println("Observador: " + observador.getUsuario().getNombre());
            observador.actualizarPedidos(convertirLista());
        }
    }
    
    public void notificarCierre(){
        
        for(HiloCliente cliente : invitados){
            
            cliente.salirSesion();
            
        }
        
    }
    
    public void notificarCierre(HiloCliente cliente){
        
        
            
        cliente.salirSesion();
            
        
        
    }
    
    

}
