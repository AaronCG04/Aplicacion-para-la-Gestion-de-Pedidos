package com.acgapp.qr_proyect;

import androidx.annotation.Nullable;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    CardView realizar,ver;
    ImageView atras;
    //String user="aaronuser";
    String user="hola123";
    String datos="{\"user\":\""+user+"\",\"room\":\"Cliente\"}";
    String CHANEL_ID="micanal",name="minombre";
    private Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        conectar_socket();
        socket.connect();
        socket.on(Socket.EVENT_CONNECT,conectar);
        socket.on("pedido_f",lanzar_noti_f);
        socket.on("pedido_c",lanzar_noti_c);

        atras = findViewById(R.id.atras_boton);
        realizar=findViewById(R.id.r_pedido);
        ver=findViewById(R.id.v_pedido);
        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socket.emit("salir",datos);
                socket.disconnect();
                Intent ir_login=new Intent(MainActivity.this,Inicio.class);
                startActivity(ir_login);
            }
        });
        realizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new IntentIntegrator(MainActivity.this).initiateScan();
            }
        });
        ver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socket.emit("salir",datos);
                socket.disconnect();
                Intent ir_login=new Intent(MainActivity.this,pedidos_cliente.class);
                ir_login.putExtra("user",user);
                startActivity(ir_login);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult resultado = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (resultado!=null){
            if(resultado.getContents()!=null){
                socket.emit("salir",datos);
                socket.disconnect();
                String datos = resultado.getContents();
                Intent ver_pro =new Intent(this,QREscaner.class);
                ver_pro.putExtra("user",user);
                ver_pro.putExtra("id_producto",datos);
                startActivity(ver_pro);
            }else{
                Toast.makeText(this, "No Hay Datos", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Operacion Cancelada", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        socket.emit("salir",datos);
        socket.disconnect();
        Intent ir_login=new Intent(MainActivity.this,Inicio.class);
        startActivity(ir_login);
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
                    socket.emit("ingresar_room",datos);
                }
            });
        }
    };
    private Emitter.Listener lanzar_noti_f=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String d = args[0].toString();
                    notificar(d,"F");
                }
            });
        }
    };
    private Emitter.Listener lanzar_noti_c=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String d = args[0].toString();
                    notificar(d,"C");
                }
            });
        }
    };
    private void notificar(String d,String t){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String description="Este es el canal de notificacines";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANEL_ID,name,importance);
            channel.setDescription(description);
            NotificationManager notificationManager =getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            if (t.equals("F")) {
                notificacion(d);
            }else if (t.equals("C")){
                notificacion_c(d);
            }
        }
    }
    private void notificacion(String d){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANEL_ID)
                .setSmallIcon(R.drawable.delete)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.mipmap.icono))
                .setContentTitle("Pedido Listo")
                .setContentText("Su pedido "+d+" esta listo")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Su pedido "+d+" esta listo"))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(0,builder.build());
    }

    private void notificacion_c(String d){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANEL_ID)
                .setSmallIcon(R.drawable.aceptar)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.mipmap.icono))
                .setContentTitle("Pedido Cancelado")
                .setContentText("Su pedido "+d+" se ha sido cancelado")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Su pedido "+d+" se ha sido cancelado"))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(0,builder.build());
    }
}