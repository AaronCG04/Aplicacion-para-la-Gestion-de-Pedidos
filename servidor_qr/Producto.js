class Producto {
  constructor(id = null,nombre=null,precio=null,stock=null,imagen=null) {
    this.id = id;
    this.nombre = nombre
    this.precio = precio;
    this.stock = stock;
    this.imagen = imagen;
  }

  static consultar_producto(id){
    console.log("dfsdfafds");
  }
}
module.exports = Producto;
