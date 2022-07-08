package com.acgapp.qr_proyect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Admin extends AppCompatActivity {
    CardView add,verpedidos,estadisticas;
    ImageView atras;
    private Socket socket;
    String CHANEL_ID="micanal",name="minombre",user="admin";
    String datos="{\"user\":\""+user+"\",\"room\":\"Admin\"}";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        //inicando conexion con socket
        conectar_socket();
        socket.connect();
        socket.on(Socket.EVENT_CONNECT,conectar);
        socket.on("prueba_c",prueba_c);
        //--------------------------
        add=findViewById(R.id.a_pro);
        verpedidos = findViewById(R.id.v_pedido);
        atras = findViewById(R.id.atras_boton);
        estadisticas = findViewById(R.id.estadisitcas_card);

        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String datos="{\"user\":\""+user+"\",\"room\":\"Admin\"}";
                socket.emit("salir",datos);
                socket.disconnect();
                Intent ir_login=new Intent(Admin.this,Inicio.class);
                startActivity(ir_login);
            }
        });
        verpedidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socket.emit("salir",datos);
                socket.disconnect();
                Intent ir_pedidos = new Intent(Admin.this,pedidos_admin.class);
                ir_pedidos.putExtra("user",user);
                startActivity(ir_pedidos);

            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pasar_agregar_producto();
            }
        });
        estadisticas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                estadisticas_ver();
            }
        });
    }
    @Override
    public void onBackPressed() {

        socket.emit("salir",datos);
        socket.disconnect();
        Intent ir_login=new Intent(Admin.this,Inicio.class);
        startActivity(ir_login);
    }
    private void pasar_agregar_producto(){
        String datos="{\"user\":\""+user+"\",\"room\":\"Admin\"}";
        socket.emit("salir",datos);
        socket.disconnect();
        Intent add_p=new Intent(this,AgregarProducto.class);
        add_p.putExtra("user",user);
        startActivity(add_p);
    }

    private void estadisticas_ver(){
        String datos="{\"user\":\""+user+"\",\"room\":\"Admin\"}";
        socket.emit("salir",datos);
        socket.disconnect();
        Intent est=new Intent(this,Estadisitcas.class);
        est.putExtra("user",user);
        startActivity(est);
    }
    private void conectar_socket(){
        try {
            //This address is the way you can connect to localhost with AVD(Android Virtual Device)
            socket = IO.socket("http://192.168.43.126:3000");

        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.d("fail", "Failed to connect");
        }
        Log.d("success", socket.id()+"99");
    }
    private Emitter.Listener conectar = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String datos="{\"user\":\""+user+"\",\"room\":\"Admin\"}";
                    socket.emit("ingresar_room",datos);
                }
            });
        }
    };

    private Emitter.Listener prueba_c = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String d = args[0].toString();
                    notificar(d);
                    Toast.makeText(Admin.this, d, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private void notificar(String d){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String description="Este es el canal de notificacines";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANEL_ID,name,importance);
            channel.setDescription(description);
            NotificationManager notificationManager =getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificacion(d);
        }
    }
    private void notificacion(String d){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANEL_ID)
                .setSmallIcon(R.drawable.aceptar)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.mipmap.icono))
                .setContentTitle("Nuevo Pedido")
                .setContentText("Tiene un nuevo Pedido "+d)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Tiene un nuevo Pedido "+d))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(0,builder.build());
    }
}