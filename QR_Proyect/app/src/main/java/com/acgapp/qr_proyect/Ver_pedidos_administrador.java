package com.acgapp.qr_proyect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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

public class Ver_pedidos_administrador extends AppCompatActivity {
    List<Producto> lista_productos;
    RecyclerView recyclerView;
    ImageView imagen;
    String pedido="",user="";
    TextView titulo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_pedidos_administrador);
        Bundle parametros = this.getIntent().getExtras();
        if (parametros!=null){
            pedido=parametros.getString("id_pedido");
            user=parametros.getString("user");
        }

        imagen=findViewById(R.id.imageButton);
        recyclerView=findViewById(R.id.lista_productos_admin);
        titulo = findViewById(R.id.texto_pedido);
        titulo.setText("PRODUCTOS DE "+pedido);

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(Ver_pedidos_administrador.this,pedidos_admin.class);
                a.putExtra("user",user);
                startActivity(a);
            }
        });

        init();

    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(this,pedidos_admin.class);
        a.putExtra("user",user);
        startActivity(a);
    }

    private void init(){
        lista_productos = new ArrayList<>();
        consultar_productos(pedido);
    }

    private void consultar_productos(String pedido){
        String url="http://192.168.43.126:3000/pedidos/optener_productos",fecha;
        Log.d("repuesta",pedido);
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
                        if (datos.getString("id_producto").equals("-1")){
                            Toast.makeText(Ver_pedidos_administrador.this, "No Cuenta Con Productos", Toast.LENGTH_SHORT).show();
                        }else{
                            byte[] bytearreglo;

                            for (int j=0; j<array.length();j++){
                                bytearreglo= Base64.decode(array.getJSONObject(j).getString("imagen_producto"),Base64.DEFAULT);
                                lista_productos.add(new Producto(array.getJSONObject(j).getString("id_producto"),
                                        array.getJSONObject(j).getString("nombre"),
                                        array.getJSONObject(j).getString("precio"),
                                        array.getJSONObject(j).getString("cantidad"),
                                        bytearreglo));
                            }
                            Log.d("sdf",lista_productos.get(0).getId_producto());
                            Adaptador_ver_productos adaptador=new Adaptador_ver_productos(lista_productos,Ver_pedidos_administrador.this);
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setLayoutManager(new LinearLayoutManager(Ver_pedidos_administrador.this));
                            recyclerView.setAdapter(adaptador);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Ver_pedidos_administrador.this, "Sin Conexion\n con el Servidor", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<>();
                parametros.put("pedido",pedido);
                return parametros;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}