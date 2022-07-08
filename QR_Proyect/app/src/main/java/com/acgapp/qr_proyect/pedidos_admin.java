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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.emitter.Emitter;

public class pedidos_admin extends AppCompatActivity {
    List<Pedido> pedidos;
    RecyclerView lista;
    ImageView atras;
    String user;
    String datos;
    String CHANEL_ID="micanal",name="minombre";
    PedidosAdaptadorAdmin adaptador;
    @Override
    public void onBackPressed() {
        try {
            adaptador.socket.emit("salir",datos);
            adaptador.socket.disconnect();
        }catch (Exception e){
            Log.d("Error Socket","Error socket no inicado");
        }
        Intent in =new Intent(pedidos_admin.this,Admin.class);
        in.putExtra("user",user);
        startActivity(in);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos_admin);
        lista = findViewById(R.id.lista_pedidos_cliente);
        atras =findViewById(R.id.imageButton);

        Bundle parametros = this.getIntent().getExtras();
        if (parametros!=null){
            user=parametros.getString("user");
            datos="{\"user\":\""+user+"\",\"room\":\"Admin\"}";
        }
        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    adaptador.socket.emit("salir",datos);
                    adaptador.socket.disconnect();
                }catch (Exception e){
                    Log.d("Error Socket","Error socket no inicado");
                }
                Intent in =new Intent(pedidos_admin.this,Admin.class);
                in.putExtra("user",user);
                startActivity(in);
            }
        });
        init();
    }
    public void init(){
        pedidos = new ArrayList<>();
        consultar_pedidos(user);
    }

    public void consultar_pedidos(String user){
        String url="http://192.168.43.126:3000/pedidos/optener_pedido/admin",fecha;
        //pedidos = new ArrayList<>();
        //optener fecha
        SimpleDateFormat fomat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        fecha=fomat.format(date);
        //fecha="2022-06-08";
        Log.d("repuesta","entrea el consultar");
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
                            Toast.makeText(pedidos_admin.this, "No Cuenta Con pedidos", Toast.LENGTH_SHORT).show();
                        }else{
                            for (int j=0; j<array.length();j++){
                                pedidos.add(new Pedido(array.getJSONObject(j).getString("id_pedido"),
                                        array.getJSONObject(j).getString("total_productos"),
                                        array.getJSONObject(j).getString("total"),
                                        array.getJSONObject(j).getString("estado"),
                                        array.getJSONObject(j).getString("id_usuario")));
                            }
                            Log.d("sdf",pedidos.get(0).id_pedido);
                            adaptador=new PedidosAdaptadorAdmin(pedidos,pedidos_admin.this,user);
                            lista.setHasFixedSize(true);
                            lista.setLayoutManager(new LinearLayoutManager(pedidos_admin.this));
                            lista.setAdapter(adaptador);
                            adaptador.socket.on("prueba_c",prueba_c);

                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(pedidos_admin.this, "Sin Conexion\n con el Servidor", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<>();
                parametros.put("fecha",fecha);
                return parametros;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        //return pedidos;
    }

    private Emitter.Listener prueba_c = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String d = args[0].toString();
                    notificar(d);
                    Toast.makeText(pedidos_admin.this, d, Toast.LENGTH_SHORT).show();
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