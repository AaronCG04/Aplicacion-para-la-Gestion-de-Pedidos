const mysql = require('mysql');
let conexion= mysql.createPool({
  host : 'localhost',
  user : 'root',
  password : 'A@roncruz123',
  database : 'gestion_de_pedidos',
  connectionLimit: 10,
  insecureAuth : true
});


module.exports = {
  conexion: conexion
}
