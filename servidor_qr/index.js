const express = require('express');
const bodyParser = require('body-parser');
const mysql = require('mysql');

const fs = require('fs');
const Producto = require('./Producto');
const conectar_db = require('./conectar_db');
const app = express();
const port = 3000;

var server = app.listen(port, ()=>{
  console.log('Example app listening at http://localhost:'+port);
});
var io = require('socket.io')(server);


Producto.consultar_producto();
//--------
app.use(bodyParser.urlencoded({limit:'200mb',extented: true}));

//seccion de api--------------------
app.get('/',function(req,res){
    res.sendFile(__dirname+'/index.html');
})

app.post('/user/add',(req,res)=>{
  //res.send('Agregar usuario '+req.body.name);

});

app.post('/user/remove',(req,res)=>{
  res.send('Remover usuario');
});

app.post('/producto/optener',(req,res)=>{
    // es para get
   //res.send('Hola sfdfs sdque hace'+req.query.name);
   //console.log('Hola sfdfs sdque hace '+res);
   /*conectar_db.conexion.connect(function(err) {
     if (err) console.log(err)//throw err;*/
     //Select all customers and return the result object:
     req.connection.setTimeout(1000*60*10);
     conectar_db.conexion.query("select id_producto,nombre,precio,stock,imagen_producto from productos where id_producto=?",[req.body.id], function (err, result, fields) {
        if (err){
          
          throw err;
        }else{
          //console.log(result);
          if (result.length>0) {
            result[0].imagen_producto=Buffer.from(result[0].imagen_producto).toString('base64');
            var resultado= {datos:result};
            //console.log(resultado);

            res.json(resultado);
            console.log("Enviado");
          }else{
            res.json({datos:[{id_producto:"-1"}]});  
          }
          
        }
     //});
   });
});

app.post('/producto/subir_producto',(req,res)=>{
  req.connection.setTimeout(1000*60*10);
  if (req.body.id_producto) {
    var datos = {
      id_producto: req.body.id_producto,
      nombre: req.body.nombre,
      precio: parseFloat(req.body.precio),
      stock: parseInt(req.body.stock)
      //imagen: fs.readFileSync(req.body.id_producto+"_img.png"),
      //qr: fs.readFileSync(req.body.id_producto+"_qr.png")
    };
    //-- crear imagen
    fs.writeFile(req.body.id_producto+"_img.png",Buffer.from(req.body.img,'base64'), (err) => {
      if (err)throw err;
      console.log("Img generado\n");
      datos.imagen_producto=fs.readFileSync(req.body.id_producto+"_img.png");
      ///---
      fs.writeFile(req.body.id_producto+"_qr.png",Buffer.from(req.body.qr,'base64'), (err) => {
        if (err) throw err;
        console.log(req.body.id_producto+"QR generado\n");
        datos.qr_producto=fs.readFileSync(req.body.id_producto+"_qr.png");
        console.log(datos); 
        conectar_db.conexion.query("INSERT INTO productos SET ?",datos, function (err, result, fields) {
          if (err){ 
            res.send("-1");
            //throw err;
          }else{
            res.send("Registro");
          }
        });
        
      });
    });
    //-- crear qr


    //------------------------

       
       
  }else{
    res.send("-1");
  }
  //res.send(`Subir producto ${req.body.name}`);
});

app.post('/pedidos/subir_pedido',(req,res)=>{
  req.connection.setTimeout(1000*60*10);
  var va=req.body.datos;
  const datos=JSON.parse(va);
  var hoy= new Date();
  var fecha_p = hoy.getFullYear();
  if (( hoy.getMonth() + 1 )<10) {
    fecha_p += "-0"+( hoy.getMonth() + 1 )+"-"+ hoy.getDate();
  }else{
    fecha_p += "-"+( hoy.getMonth() + 1 )+"-"+ hoy.getDate();
  }
  var insert_pedido={
    id_pedido: ""+hoy.getFullYear()+( hoy.getMonth() + 1 )+ hoy.getDate()+hoy.getHours() + hoy.getMinutes() + hoy.getSeconds()+Math.floor(Math.random() * (100 - 0) + 0),
    fecha: fecha_p,
    estado:'I'
  }
  var insert_ped_user=[];
  var total_p=0,total_pro=0;
  for (var i = 0; i < parseInt(req.body.tam); i++) {
    insert_ped_user.push([datos.dato[i].id_producto,insert_pedido.id_pedido,req.body.user,parseInt(datos.dato[i].cantidad)]);
    total_p+=(parseFloat(datos.dato[i].precio)*parseInt(datos.dato[i].cantidad));
    //falta agregar esto a la base de datos
    total_pro+=parseInt(datos.dato[i].cantidad);
  }
  insert_pedido.total=total_p;
  insert_pedido.total_productos=total_pro;
  conectar_db.conexion.query("INSERT INTO pedidos SET ?",insert_pedido, function (err, result, fields) {
    if (err){
      throw err;
    }else{
      conectar_db.conexion.query("INSERT INTO pro_pedidos(id_producto,id_pedido,id_usuario,cantidad)VALUES ?",[insert_ped_user], function (err, result, fields) {
        if (err){
          throw err;
        }else{
          res.send("insercion corecta");
          //io.in("Admin").emit("prueba_c",JSON.stringify({id_pedido:insert_pedido.id_pedido,user:req.body.user});
          io.in("Admin").emit("prueba_c",insert_pedido.id_pedido);
        }
      });    
    }
  });
  console.log(insert_ped_user);
  //res.send("insercion corecta");
  //parseInt(req.body.tam);
  //datos.dato.forEach(el => console.log(el));
  
  //res.send(`Subir pedido ${req.body.name}`);
});
app.post('/pedidos/optener_pedido',(req,res)=>{
   req.connection.setTimeout(1000*60*10);
   conectar_db.conexion.query("select p.id_pedido,p.total_productos,p.total,p.estado from pedidos p join pro_pedidos pp on pp.id_pedido = p.id_pedido where pp.id_usuario = ? and p.fecha=? group by(p.id_pedido) order by (p.estado) DESC;",[req.body.user,req.body.fecha], function (err, result, fields) {
    if (err){   
      throw err;
    }else{
      //console.log(result);
      if (result.length>0) {
        //result[0].imagen_producto=Buffer.from(result[0].imagen_producto).toString('base64');
        var resultado= {datos:result};
        console.log(resultado);

        res.json(resultado);
        //console.log("Enviado");
      }else{
        res.json({datos:[{id_pedido:"-1"}]});  
      }
          
    }
   });
});
app.post('/pedidos/optener_pedido/admin',(req,res)=>{
   req.connection.setTimeout(1000*60*10);
   conectar_db.conexion.query("select p.id_pedido,p.total_productos,p.total,p.estado,id_usuario from pedidos p join pro_pedidos pp on p.id_pedido=pp.id_pedido where p.fecha=? group by(id_pedido) order by (p.estado) DESC;", [req.body.fecha],function (err, result, fields) {
    if (err){   
      throw err;
    }else{
      if (result.length>0) {
        var resultado= {datos:result};
        console.log(resultado);

        res.json(resultado);
        //console.log("Enviado");
      }else{
        res.json({datos:[{id_pedido:"-1"}]});  
      }
          
    }
   });
});


app.post('/pedidos/borrar_pedido',(req,res)=>{
  req.connection.setTimeout(1000*60*10);
  conectar_db.conexion.query("UPDATE pedidos SET estado=? WHERE id_pedido=?",[req.body.estado,req.body.id_pedido], function (err, result, fields) {
        if (err){
          throw err;
        }else{
          console.log("Se actulizo el pedido "+req.body.id_pedido+" a el esado "+req.body.estado);
          res.send("Actualizacion correcta");
          
        }
  });
  /*conectar_db.conexion.query("delete from pro_pedidos where id_pedido=?",[req.body.id_pedido], function (err, result, fields) {
    if (err){
      throw err;
    }else{
      conectar_db.conexion.query("delete from pedidos where id_pedido=?",[req.body.id_pedido], function (err, result, fields) {
        if (err){
          throw err;
        }else{
          console.log("Se borro"+req.body.id_pedido);
          res.send("Delete corecta");
          
        }
      });    
    }
  });*/
  
});

app.post('/pedidos/actualizar_status',(req,res)=>{
  req.connection.setTimeout(1000*60*10);
  conectar_db.conexion.query("UPDATE pedidos SET estado=? WHERE id_pedido=?",[req.body.estado,req.body.id_pedido], function (err, result, fields) {
        if (err){
          throw err;
        }else{
          console.log("Se actulizo el pedido "+req.body.id_pedido+" a el esado "+req.body.estado);
          res.send("Actualizacion correcta");
          
        }
    });  
});

app.post('/pedidos/optener_productos',(req,res)=>{
  req.connection.setTimeout(1000*60*10);
  conectar_db.conexion.query("select p.id_producto,p.nombre,p.precio,pp.cantidad,p.imagen_producto from pro_pedidos pp join productos p on pp.id_producto=p.id_producto where pp.id_pedido=?",[req.body.pedido], function (err, result, fields) {
        if (err){
          throw err;
        }else{
          if (result.length>0) {
            for (var i = 0; i < result.length; i++) {
              result[i].imagen_producto=Buffer.from(result[i].imagen_producto).toString('base64');
            }
            var resultado= {datos:result};
            res.json(resultado);
            console.log("Enviando productos del pedido "+req.body.pedido);
          }else{
            res.json({datos:[{id_producto:"-1"}]});  
          }
          
        }
    });  
});

app.post('/estadisticas',(req,res)=>{
  req.connection.setTimeout(1000*60*10);
  let estadisticas={},total=0;
  conectar_db.conexion.query(" select (select sum(pp.cantidad) from pro_pedidos pp join productos p on pp.id_producto=p.id_producto join pedidos pd on pp.id_pedido=pd.id_pedido where pd.fecha=? and pr.id_producto=p.id_producto and pd.estado=\"F\") as c,pr.nombre,pr.precio from productos pr having c >0;",[req.body.fecha], function (err, result, fields) {
    if (err){
      throw err;
    }else{
      if (result.length>0) {
        estadisticas.pro_vendidos=result;
        for (var i = 0; i < result.length; i++) {
          total+=(result[i].c*result[i].precio);
        }
        
        estadisticas.total_venta=total;
        conectar_db.conexion.query("select (select count(*) from pedidos where estado=\"F\" and fecha=?)as f,  (select count(*) from pedidos where estado=\"C\" and fecha=?) as c, (select count(*) from pedidos where estado=\"I\" and fecha=?) as p;",[req.body.fecha,req.body.fecha,req.body.fecha], function (err, result, fields) {
          if (err){
            throw err;
          }else{
            if (result.length>0) {
              estadisticas.estados_pedidos=result;
              res.json(estadisticas);
            }else{
              res.json({pro_vendidos:[{nombre:"-1"}]});
            }
          }
        });
      }else{
        res.json({pro_vendidos:[{nombre:"-1"}]});
      }
    }
  }); 
});

//--sockets
let usuarios ={datos_s:[]};
io.on('connection',function(socket) {
    console.log(`Connection : SocketId = ${socket.id}`);
    socket.on('ingresar_room',(datos)=>{
      var datosv = JSON.parse(datos);
      socket.join(datosv.room);
      usuarios.datos_s.push({id_socket: socket.id,user:datosv.user});
      console.log(usuarios);
      //console.log(usuarios.datos_s[0].id_socket);
      console.log(`Username : ${datosv.user} joined Room Name : ${datosv.room}`);
    });

    socket.on("prueba",(datos)=>{
      console.log("Entro a la prueba");
      socket.broadcast.to("Admin").emit("prueba_c",datos);
      //io.in("Admin").emit("prueba_c",datos);
    }); 

    socket.on("notificar_cliente_p_f",(datos)=>{
      var datosv = JSON.parse(datos);
      console.log(datosv);
      for (var i = 0; i < usuarios.datos_s.length; i++) {
        if(usuarios.datos_s[i].user==datosv.user) {
          socket.broadcast.to(usuarios.datos_s[i].id_socket).emit('pedido_f', datosv.pedido);
        }
      }
      console.log("notificar cliente"+ datosv.user);
      
    });

    socket.on("notificar_cliente_p_c",(datos)=>{
      var datosv = JSON.parse(datos);
      console.log(datosv);
      for (var i = 0; i < usuarios.datos_s.length; i++) {
        if(usuarios.datos_s[i].user==datosv.user) {
          socket.broadcast.to(usuarios.datos_s[i].id_socket).emit('pedido_c', datosv.pedido);
        }
      }
      console.log("notificar cliente"+ datosv.user);
      
    });

    socket.on("salir",(datos)=>{
      var datosv = JSON.parse(datos);
      let i;
      for (i = 0; i < usuarios.datos_s.length; i++) {
        if (usuarios.datos_s[i].id_socket==socket.id) break;
      }
      usuarios.datos_s.splice(i,1);
      console.log(usuarios);
      socket.leave(datosv.room);
      console.log(`Username : ${datosv.user} leave Room Name : ${datosv.room}`);
    });
    socket.on('disconnect', function () {
        console.log("One of sockets disconnected from our server."+ socket.id)
    });
});