package com.acgapp.qr_proyect;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Adaptador_ver_productos extends RecyclerView.Adapter<Adaptador_ver_productos.ViewHolder>{
    private List<Producto> datos_produto;
    private Context context;
    public Adaptador_ver_productos(List<Producto> datos_produto,Context context){
//        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.datos_produto=datos_produto;
    }
    @NonNull
    @Override
    public Adaptador_ver_productos.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lista_productos,parent,false);
        return new Adaptador_ver_productos.ViewHolder(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull Adaptador_ver_productos.ViewHolder holder, int position) {
        holder.bindData(datos_produto.get(position));
    }

    @Override
    public int getItemCount() {return datos_produto.size();}
    public void setItems(List<Producto> datos_produto){this.datos_produto=datos_produto;}

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView nombre_pro, total_producto,precio;
        ImageView pro;
        //
        Producto producto;
        Adaptador_ver_productos adaptador;
        ViewHolder(View itemView) {
            super(itemView);
            nombre_pro=itemView.findViewById(R.id.nombre_producto);
            pro = itemView.findViewById(R.id.imagen_p);
            total_producto=itemView.findViewById(R.id.total_pro);
            precio=itemView.findViewById(R.id.total_dinero);

        }
        public Adaptador_ver_productos.ViewHolder linkAdapter(Adaptador_ver_productos adapter){
            this.adaptador=adapter;
            return this;
        }

        void bindData(Producto item){
//            id_pedido.setText(item.getId_pedido());
//            total_productos.setText(item.getTotal_produtos());
//            total_precio.setText("$ "+item.getTotal_precio());
//            if (item.status.equals("I")){
//                color.setBackgroundColor(itemView.getResources().getColor(R.color.pendiente) );
//            }else if (item.status.equals("C")){
//                color.setBackgroundColor(itemView.getResources().getColor(R.color.cancelado));
//            }else if (item.status.equals("F")){
//                color.setBackgroundColor(itemView.getResources().getColor(R.color.listo));
//            }
            nombre_pro.setText(item.getNombre());
            total_producto.setText(item.getCantidad());
            precio.setText("$ "+item.getPrecio());
            pro.setImageBitmap(BitmapFactory.decodeByteArray(item.getImagen(),0,item.getImagen().length));

            producto=item;
        }

    }
}
