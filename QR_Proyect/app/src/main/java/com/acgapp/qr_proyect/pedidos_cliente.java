package com.acgapp.qr_proyect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.emitter.Emitter;

public class pedidos_cliente extends AppCompatActivity {

    PedidosAdaptador adaptador;
    List<Pedido> pedidos;
    RecyclerView lista;
    String user="",datos;;
    ImageView atras;
    String CHANEL_ID="micanal",name="minombre";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos_cliente);
        lista = findViewById(R.id.lista_pedidos_cliente);
        atras = findViewById(R.id.imageButton);
        Bundle parametros = this.getIntent().getExtras();
        if (parametros!=null){
            user=parametros.getString("user");
            datos="{\"user\":\""+user+"\",\"room\":\"Cliente\"}";
        }
        Log.d("usuario",user);
        init();
        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    adaptador.socket.emit("salir",datos);
                    adaptador.socket.disconnect();
                }catch (Exception e){
                    Log.d("Error Socket","Error socket no inicado");
                }
                Intent in =new Intent(pedidos_cliente.this,MainActivity.class);
                in.putExtra("user",user);
                startActivity(in);
            }
        });
    }
    public void init(){
          pedidos = new ArrayList<>();
//        pedidos.add(new Pedido("0000000","10","5000.40","I","aaron_user"));
//        pedidos.add(new Pedido("0012300","6","31200.40","I","aaron_user"));
//        pedidos.add(new Pedido("0565600","30","51230.40","I","aaron_user"));
//        pedidos.add(new Pedido("5436500","2","2130.40","I","aaron_user"));
//        pedidos.add(new Pedido("6567560","1","5214.40","I","aaron_user"));
        consultar_pedidos(user);
    }
    @Override
    public void onBackPressed() {
        try {
            adaptador.socket.emit("salir",datos);
            adaptador.socket.disconnect();
        }catch (Exception e){
            Log.d("Error Socket","Error socket no inicado");
        }
        Intent in =new Intent(pedidos_cliente.this,MainActivity.class);
        in.putExtra("user",user);
        startActivity(in);
    }
    public void consultar_pedidos(String user){
        String url="http://192.168.43.126:3000/pedidos/optener_pedido",fecha;
        //pedidos = new ArrayList<>();
        //optener fecha
        SimpleDateFormat fomat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        fecha=fomat.format(date);
        //fecha="2022-06-20";
        Log.d("repuesta",fecha);
        //------------
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    if (!response.isEmpty()){
                        try {
                            Log.d("repuesta",response);
                            JSONObject json= new JSONObject(response);
                            JSONArray array= json.getJSONArray("datos");
                            JSONObject datos= array.getJSONObject(0);
                            if (datos.getString("id_pedido").equals("-1")){
                                Toast.makeText(pedidos_cliente.this, "No Cuenta Con pedidos", Toast.LENGTH_SHORT).show();
                            }else{
                                for (int j=0; j<array.length();j++){
                                    pedidos.add(new Pedido(array.getJSONObject(j).getString("id_pedido"),
                                            array.getJSONObject(j).getString("total_productos"),
                                            array.getJSONObject(j).getString("total"),
                                            array.getJSONObject(j).getString("estado"),
                                            user));

                                }
                                Log.d("sdf",pedidos.get(0).id_pedido);
                                adaptador=new PedidosAdaptador(pedidos,pedidos_cliente.this,user);
                                lista.setHasFixedSize(true);
                                lista.setLayoutManager(new LinearLayoutManager(pedidos_cliente.this));
                                lista.setAdapter(adaptador);
                                adaptador.socket.on("pedido_f",lanzar_noti_f);
                                adaptador.socket.on("pedido_c",lanzar_noti_c);
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(pedidos_cliente.this, "Sin Conexion\n con el Servidor", Toast.LENGTH_SHORT).show();
                }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<>();
                parametros.put("user",user);
                parametros.put("fecha",fecha);
                return parametros;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        //return pedidos;
    }
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