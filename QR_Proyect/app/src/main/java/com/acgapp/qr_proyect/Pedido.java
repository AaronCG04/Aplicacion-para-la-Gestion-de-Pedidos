package com.acgapp.qr_proyect;

public class Pedido {
    public String id_pedido;
    public String total_produtos;
    public String total_precio;
    public String status;
    public String usuario;

    public Pedido(String id_pedido, String total_produtos, String total_precio, String status, String usuario) {
        this.id_pedido = id_pedido;
        this.total_produtos = total_produtos;
        this.total_precio = total_precio;
        this.status = status;
        this.usuario = usuario;
    }

    public String getId_pedido() {
        return id_pedido;
    }

    public void setId_pedido(String id_pedido) {
        this.id_pedido = id_pedido;
    }

    public String getTotal_produtos() {
        return total_produtos;
    }

    public void setTotal_produtos(String total_produtos) {
        this.total_produtos = total_produtos;
    }

    public String getTotal_precio() {
        return total_precio;
    }

    public void setTotal_precio(String total_precio) {
        this.total_precio = total_precio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
