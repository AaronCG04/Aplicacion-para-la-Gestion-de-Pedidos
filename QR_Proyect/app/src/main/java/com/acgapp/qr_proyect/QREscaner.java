package com.acgapp.qr_proyect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QREscaner extends AppCompatActivity {
    TextView id_producto_text,nombre,precio;
    Button agregar,finalizar,cancelar;
    ImageView producto;
    String dato,dato_id_pro,json_enviar="{\"dato\":[",username;
    List<String> lista_productos = new ArrayList<String>();
    List<String> cantidad_pro = new ArrayList<String>();
    EditText optener_cantidad_pro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrescaner);

        dato=getIntent().getStringExtra("id_producto");
        username = getIntent().getStringExtra("user");

        id_producto_text=findViewById(R.id.num_producto);
        //id_producto_text.setText(dato);
        nombre=findViewById(R.id.nombre_con);
        precio=findViewById(R.id.precio_con);
        optener_cantidad_pro = findViewById(R.id.optener_cantidad);
        ///gregar esa parte de la cantidad
        agregar=findViewById(R.id.agregar_producto);
        finalizar=findViewById(R.id.fin);
        cancelar=findViewById(R.id.cancelar_pedido);
        producto=findViewById(R.id.ver_producto);
        //consultar_db("https://modcertificadote.000webhostapp.com/optener_datos.php");
        consultar_db("http://192.168.43.126:3000/producto/optener");
        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dato_id_pro!= null){
                    Log.d("validar_cantidad",optener_cantidad_pro.getText().toString()+"--");
                    System.out.println(optener_cantidad_pro.getText().toString().equals(""));
                    if(!nombre.getText().toString().equals("Nombre") && !nombre.getText().toString().equals("NOMBRE") && !(optener_cantidad_pro.getText().toString()).equals("")) {
                    
                        lista_productos.add(dato_id_pro);
                        cantidad_pro.add(optener_cantidad_pro.getText().toString());
                    }
                }
                System.out.println(lista_productos);
                id_producto_text.setText("ID producto");
                nombre.setText("Nombre");
                precio.setText("Precio");
                optener_cantidad_pro.setText("");
                producto.setImageResource(R.mipmap.ver_pedidos);
                new IntentIntegrator(QREscaner.this).initiateScan();
            }
        });
        finalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dato_id_pro!= null){
                    Log.d("validar_cantidad",optener_cantidad_pro.getText().toString());
                    if(!nombre.getText().toString().equals("Nombre") && !nombre.getText().toString().equals("NOMBRE") && !(optener_cantidad_pro.getText().toString()).equals("")) {
                        lista_productos.add(dato_id_pro);
                        cantidad_pro.add(optener_cantidad_pro.getText().toString());
                    }
                }
                int tam=lista_productos.size();
                if (tam!=0){
                    for (int i=0;i<tam;i++){
                        if (i==tam-1){
                            //verificar si funciona
                            json_enviar+="{\"id_producto\":\""+lista_productos.get(i)+"\",\"cantidad\":\""+cantidad_pro.get(i)+"\",\"precio\":\""+precio.getText().toString()+"\"}]}";
                        }else{
                            json_enviar+="{\"id_producto\":\""+lista_productos.get(i)+"\",\"cantidad\":\""+cantidad_pro.get(i)+"\",\"precio\":\""+precio.getText().toString()+"\"},";
                        }
                    }
                    subir_pedidos(tam);
                    //ir_home_cliente();
                }
                ir_home_cliente();
            }
        });
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tam=lista_productos.size();
                for(int i=0;i<tam ;i++){
                    lista_productos.remove(i);
                    cantidad_pro.remove(i);
                }
                ir_home_cliente();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult resultado = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (resultado!=null){
            if(resultado.getContents()!=null){
                dato = resultado.getContents();
                //id_producto_text.setText(datos);
                //consultar_db("https://modcertificadote.000webhostapp.com/optener_datos.php");
                consultar_db("http://192.168.43.126:3000/producto/optener");
            }else{
                Toast.makeText(this, "Operacion Cancelada", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Operacion Cancelada", Toast.LENGTH_SHORT).show();
        }
    }
    private void ir_home_cliente(){
        Intent ir_home_cliente=new Intent(QREscaner.this,MainActivity.class);
        ir_home_cliente.putExtra("user",username);
        startActivity(ir_home_cliente);
    }
    private void subir_pedidos(int tam){
        //String URL = "https://modcertificadote.000webhostapp.com/optener_datos.php";
        String URL = "http://192.168.43.126:3000/pedidos/subir_pedido";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.d("pedidos",s);
                Toast.makeText(QREscaner.this, s, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(QREscaner.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros=new HashMap<String,String>();
                parametros.put("datos",json_enviar);
                parametros.put("tam",""+tam);
                parametros.put("user",username);
                return parametros;
            }
        };
        RequestQueue requestQueue=Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void consultar_db(String URL){
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()){
                    //Toast.makeText(QREscaner.this, "RESPUESTA OK", Toast.LENGTH_SHORT).show();
                    Log.d("json",response);
                    try {
                        JSONObject json= new JSONObject(response);
                        JSONArray array= json.getJSONArray("datos");
                        JSONObject datos= array.getJSONObject(0);
                        Log.d("id_producto",datos.getString("id_producto"));
                        //Log.d("precio",datos.getString("precio"));
                        if(datos.getString("id_producto").equals("-1")){
                            dato_id_pro=null;
                            Log.d("verificar 1","entre primer if 111111");
                            Toast.makeText(QREscaner.this, "Producto No diponible", Toast.LENGTH_SHORT).show();
                        }else{
                            Log.d("verificar 2","entre primer if 22222");
                            id_producto_text.setText(dato_id_pro = datos.getString("id_producto"));
                            nombre.setText(datos.getString("nombre"));
                            precio.setText(datos.getString("precio"));

                            byte[] bytearreglo= Base64.decode(datos.optString("imagen_producto"),Base64.DEFAULT);
                            producto.setImageBitmap(BitmapFactory.decodeByteArray(bytearreglo,0,bytearreglo.length));

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(QREscaner.this, "No Respuesta", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(QREscaner.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros=new HashMap<String,String>();
                parametros.put("id",dato);
                return parametros;
            }
        };
        RequestQueue requestQueue=Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}