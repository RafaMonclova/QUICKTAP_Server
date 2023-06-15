/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

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
 * Clase para la creación de sesiones de clientes en memoria
 * @author rafam
 */
public class SesionCliente {

    private Pedido pedidoSesion; //Pedido de la sesión
    private Usuario clienteAnfitrion; //Cliente creador de la sesión
    private List<HiloCliente> invitados; //Colección de invitados de la sesión
    private List<LineaPedido> lineasDePedido; //Líneas de pedido en la sesión
    private int contadorId; //Contador con los id de cada linea (para inserción en la BDD)

    public SesionCliente(Usuario clienteAnfitrion, Pedido pedidoSesion) {
        this.clienteAnfitrion = clienteAnfitrion;
        this.pedidoSesion = pedidoSesion;
        this.lineasDePedido = new ArrayList<>();
        invitados = new ArrayList<>();
        contadorId = 1; //Empieza el contador en 1
        
        
    }

    /**
     * Genera un número aleatorio de 4 dígitos
     * @return Devuelve el número generado
     */
    public int generarNumeroAleatorio() {
        Random rand = new Random();
        int numero = rand.nextInt(9000) + 1000;
        return numero;
    }

    /**
     * Convierte la lista de líneas de pedido de la sesión a lista de Object
     * @return Devuelve la lista como lista de Object
     */
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
    
    //Getters y setters
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
    
    public List<HiloCliente> getInvitados() {
        return invitados;
    }

    public void setInvitados(List<HiloCliente> invitados) {
        this.invitados = invitados;
    }
    
    /**
     * Devuelve una lista de objetos Usuario de los invitados de la sesión
     * @return La lista de objetos Usuario
     */
    public ArrayList<Usuario> getUsuariosInvitados(){
        
        ArrayList<Usuario> usuarios = new ArrayList<>();
        
        for(HiloCliente hilo : invitados){
            usuarios.add(hilo.getUsuario());
        }
        
        return usuarios;
        
    }
    
    /**
     * Comprueba si el cliente ya pertenece a la sesión
     * @param cliente El cliente a comprobar
     * @return Devuelve un boolean indicando si pertenece a la sesión
     */
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

    /**
     * Añade un cliente a la sesión
     * @param invitado El hilo del cliente a añadir
     */
    public synchronized void añadirInvitado(HiloCliente invitado) {
        invitados.add(invitado);
    }
    
    /**
     * Elimina un cliente de la sesión
     * @param invitado El hilo del cliente a eliminar
     */
    public synchronized void eliminarInvitado(HiloCliente invitado) {
        invitados.remove(invitado);
    }

    /**
     * Añade nuevas líneas de pedido a la colección de la sesión, y notifica a los invitados
     * @param lineaDePedido La lista de líneas de pedido a añadir
     */
    public synchronized void agregarLineasPedido(ArrayList<LineaPedido> lineaDePedido) {

        for (LineaPedido lp : lineaDePedido) {
            lp.setId(new LineaPedidoId(contadorId,pedidoSesion.getId())); //Clave compuesta, con contador y el id del pedido
            lineasDePedido.add(lp);
            System.out.println(lp.getCliente().getNombre() + " - " + lp.getProducto().getNombre());
            contadorId++;
        }
        notificarPedidos();

    }
    
    /**
     * Elimina una línea de pedido de la sesión, y notifica a los invitados
     * @param lineaPedido La línea de pedido a eliminar
     */
    public synchronized void eliminarLineaPedido(LineaPedido lineaPedido){
        
        System.out.println(lineasDePedido.size());
        
        for(LineaPedido linea : lineasDePedido){
            
            if(linea.getId().getIdLineaPedido() == lineaPedido.getId().getIdLineaPedido()){
                
                lineasDePedido.remove(linea);
                break;
                
            }
            
        }
        
        notificarPedidos();
        
    }
    
    /**
     * Actualiza las cantidades de las líneas de pedido de la sesión, y notifica a los invitados
     * @param nuevasCantidades La lista con las nuevas cantidades
     */
    public synchronized void actualizarCantidades(ArrayList<Integer> nuevasCantidades){
        
        for (int i = 0; i < lineasDePedido.size(); i++) {
            
            lineasDePedido.get(i).setCantidad(nuevasCantidades.get(i));
            
        }
        
        notificarPedidos();
        
    }
    
    /**
     * Notifica a los hilos de cliente invitados con la lista de líneas de pedido actualizada
     */
    public synchronized void notificarPedidos() {
        for (HiloCliente observador : invitados) {
            observador.actualizarPedidos(convertirLista());
        }
    }
    
    /**
     * Notifica a los invitados del cierre de la sesión
     */
    public void notificarCierre(){
        
        for(HiloCliente cliente : invitados){
            
            cliente.salirSesion();
            
        }
        
    }
    
    /**
     * El cliente recibido abandona la sesión
     * @param cliente Hilo del cliente que abandona la sesión
     */
    public void notificarCierre(HiloCliente cliente){
        
        cliente.salirSesion();
        
    }
    
    

}
