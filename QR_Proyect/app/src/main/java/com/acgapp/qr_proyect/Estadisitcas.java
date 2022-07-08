package com.acgapp.qr_proyect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Estadisitcas extends AppCompatActivity {
    String user;
    TextView todos_productos,todos_cantidad,p_f,p_p,p_c,total_v;
    @Override
    public void onBackPressed() {
        Intent admin=new Intent(this,Admin.class);
        admin.putExtra("user",user);
        startActivity(admin);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisitcas);
        Bundle parametros = this.getIntent().getExtras();
        if (parametros!=null){
            user=parametros.getString("user");
        }
        todos_productos = findViewById(R.id.productos);
        todos_cantidad = findViewById(R.id.total_vendidos);
        p_f =findViewById(R.id.p_f);
        p_p=findViewById(R.id.p_p);
        p_c=findViewById(R.id.p_c);
        total_v=findViewById(R.id.total_hoy);
        consultar_estadisticas(user);
    }

    public void consultar_estadisticas(String user){
        String url="http://192.168.43.126:3000/estadisticas",fecha;
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
                        JSONArray pro_vendidos= json.getJSONArray("pro_vendidos");
                        JSONArray estados_pedidos;
                        String total,pro_v="",can_v="";
                        JSONObject datos= pro_vendidos.getJSONObject(0);
                        if (datos.getString("nombre").equals("-1")){
                            Toast.makeText(Estadisitcas.this, "No Cuenta Con Estadistica", Toast.LENGTH_SHORT).show();
                        }else{
                            for (int i=0;i<pro_vendidos.length();i++){
                                pro_v += pro_vendidos.getJSONObject(i).getString("nombre")+"\n";
                                can_v+=pro_vendidos.getJSONObject(i).getString("c")+"\n";
                            }
                            todos_productos.setText(pro_v);
                            todos_cantidad.setText(can_v);
                            estados_pedidos= json.getJSONArray("estados_pedidos");
                            p_f.setText(estados_pedidos.getJSONObject(0).getString("f"));
                            p_c.setText(estados_pedidos.getJSONObject(0).getString("c"));
                            p_p.setText(estados_pedidos.getJSONObject(0).getString("p"));
                            total =json.getString("total_venta");
                            total_v.setText(total);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Estadisitcas.this, "Sin Conexion\n con el Servidor", Toast.LENGTH_SHORT).show();
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
}