package com.acgapp.qr_proyect;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AgregarProducto extends AppCompatActivity {
    ImageView imagen_cargar,qr_cargar_im;
    EditText id_pro,nombre,precio,stock;
    Button agregar, salir;
    Bitmap bitmap_qr=null,bitmap_img=null;
    String user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_producto);

        user = getIntent().getStringExtra("user");

        imagen_cargar=findViewById(R.id.cargar_imagen);
        qr_cargar_im=findViewById(R.id.ver_qr);
        id_pro=findViewById(R.id.opt_id);
        nombre=findViewById(R.id.opt_nombre);
        precio=findViewById(R.id.opt_precio);
        stock=findViewById(R.id.opt_stock);
        //---
        agregar=findViewById(R.id.agregrar_boton);
        salir=findViewById(R.id.salir_boton);
        imagen_cargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cargar_imagen_f();
            }
        });
        //*************
        qr_cargar_im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cargar_qr_en_im();
            }
        });
        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(AgregarProducto.this, "Agregar", Toast.LENGTH_SHORT).show();
                agregar_producto(bitmap_img,bitmap_qr);
            }
        });
        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent salir_menu=new Intent(AgregarProducto.this,Admin.class);
                salir_menu.putExtra("user",user);
                startActivity(salir_menu);
            }
        });
    }
    ////-------------------------------
    private void cargar_imagen_f(){
        Intent cargar = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        cargar.setType("image/");
        startActivityForResult(Intent.createChooser(cargar,"Selecione la Aplicacion"),10);
    }

    private void cargar_qr_en_im(){
        //falta validar
        String id_prod=id_pro.getText().toString().trim();
        if (id_prod==""){
            Toast.makeText(this,"CAMPOS VACIOS", Toast.LENGTH_LONG).show();
        }else{
            MultiFormatWriter crear= new MultiFormatWriter();
            try {
                BitMatrix matriz= crear.encode(id_prod, BarcodeFormat.QR_CODE,800,800);
                BarcodeEncoder encoder = new BarcodeEncoder();
                bitmap_qr = encoder.createBitmap(matriz);
                qr_cargar_im.setImageBitmap(bitmap_qr);

            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
            Uri path=data.getData();
            imagen_cargar.setImageURI(path);
            //comvertir uri a bitmap
            try {
                bitmap_img=MediaStore.Images.Media.getBitmap(this.getContentResolver(),path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void agregar_producto(Bitmap img,Bitmap qr){
        String url,img_string,qr_string;
        //url="https://modcertificadote.000webhostapp.com/insertar_datos.php";
        //url="http://172.16.90.161:3000/producto/subir_producto";
        url="http://192.168.43.126:3000/producto/subir_producto";
        ByteArrayOutputStream array_img=new ByteArrayOutputStream();
        ByteArrayOutputStream array_qr=new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG,100,array_img);
        qr.compress(Bitmap.CompressFormat.PNG,100,array_qr);
        byte[] img_byte=array_img.toByteArray();
        img_string= Base64.encodeToString(img_byte,Base64.DEFAULT);
        byte[] qr_byte=array_qr.toByteArray();
        qr_string= Base64.encodeToString(qr_byte,Base64.DEFAULT);
        System.out.println(img_string);
        System.out.println(qr_string);
        //---------------------------
        StringRequest stringRequest=new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()){
                    Log.d("respuesta",response);
                    Toast.makeText(AgregarProducto.this,response, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AgregarProducto.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros=new HashMap<String,String>();
                parametros.put("id_producto",id_pro.getText().toString());
                parametros.put("nombre",nombre.getText().toString());
                parametros.put("precio",precio.getText().toString());
                parametros.put("stock",stock.getText().toString());
                parametros.put("img",img_string);
                parametros.put("qr",qr_string);
                return parametros;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}