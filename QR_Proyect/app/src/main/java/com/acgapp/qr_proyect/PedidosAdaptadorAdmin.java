package com.acgapp.qr_proyect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PedidosAdaptadorAdmin extends RecyclerView.Adapter<PedidosAdaptadorAdmin.ViewHolder> {
    private List<Pedido> datos_pedido;
    private Context context;
    public Socket socket;
    private View.OnClickListener listener;
    private  String user;
    private String datos;
    public PedidosAdaptadorAdmin(List<Pedido> datos_pedido,Context context,String user){
        conectar_socket();
        socket.connect();
        socket.on(Socket.EVENT_CONNECT,conectar);

//        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.datos_pedido=datos_pedido;
        this.user=user;
        this.datos="{\"user\":\""+this.user+"\",\"room\":\"Admin\"}";
    }
    @NonNull
    @Override
    public PedidosAdaptadorAdmin.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.elemento_lista,parent,false);
        return new PedidosAdaptadorAdmin.ViewHolder(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidosAdaptadorAdmin.ViewHolder holder, int position) {
        holder.bindData(datos_pedido.get(position));
    }

    @Override
    public int getItemCount() {return datos_pedido.size();}
    public void setItems(List<Pedido> datos_pedido){this.datos_pedido=datos_pedido;}
    private void conectar_socket() {
        try {
            //This address is the way you can connect to localhost with AVD(Android Virtual Device)
            socket = IO.socket("http://192.168.43.126:3000");

        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.d("fail", "Failed to connect");
        }
        Log.d("success", socket.id() + "99");
    }
    private Emitter.Listener conectar = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String datos="{\"user\":\""+user+"\",\"room\":\"Admin\"}";
                    socket.emit("ingresar_room",datos);
                }
            });
        }
    };


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView id_pedido,total_productos,total_precio,color;
        public ImageView cancelar,terminar;
        LinearLayout contenido_click;
        Pedido pedido;
        PedidosAdaptadorAdmin adaptador;


        ViewHolder(View itemView) {
            super(itemView);
            id_pedido = itemView.findViewById(R.id.card_pedido_l);
            total_productos = itemView.findViewById(R.id.card_pedido_total_pro);
            total_precio = itemView.findViewById(R.id.total_dinero);
            cancelar = itemView.findViewById(R.id.eliminar_p);
            terminar= itemView.findViewById(R.id.terminar_p);
            cancelar=itemView.findViewById(R.id.eliminar_p);
            terminar=itemView.findViewById(R.id.terminar_p);
            color=itemView.findViewById((R.id.color));
            contenido_click=itemView.findViewById(R.id.contenedor);

            cancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(itemView.getContext(), "Eliminar "+pedido.getId_pedido(), Toast.LENGTH_SHORT).show();
                    eliminar_pedido(pedido);
                }
            });
            terminar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //finalizar
                    finalizar_pedido(pedido);
                }
            });
            contenido_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ///mostrar todos los productos de el id
                    socket.emit("salir",datos);
                    socket.disconnect();
                    Intent a = new Intent(itemView.getContext(),Ver_pedidos_administrador.class);
                    a.putExtra("id_pedido",pedido.id_pedido);
                    a.putExtra("user",user);
                    itemView.getContext().startActivity(a);

                    //ver_pedidos(pedido);
                }
            });
        }
        public PedidosAdaptadorAdmin.ViewHolder linkAdapter(PedidosAdaptadorAdmin adapter){
            this.adaptador=adapter;
            return this;
        }
        public void eliminar_pedido(Pedido pedido){
            String url="http://192.168.43.126:3000/pedidos/borrar_pedido";
            //------------
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (!response.isEmpty()){
                        socket.emit("notificar_cliente_p_c","{\"pedido\":\""+pedido.getId_pedido()+"\",\"user\":\""+pedido.getUsuario()+"\"}");
                        adaptador.datos_pedido.remove(getAdapterPosition());
                        adaptador.notifyItemRemoved(getAdapterPosition());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(itemView.getContext(), "Sin Conexion\n con el Servidor", Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> parametros = new HashMap<>();
                    parametros.put("id_pedido",pedido.getId_pedido());
                    parametros.put("estado","C");
                    return parametros;
                }
            };
            RequestQueue requestQueue= Volley.newRequestQueue(itemView.getContext());
            requestQueue.add(stringRequest);
        }
        
        private void finalizar_pedido(Pedido pedido){
            String url="http://192.168.43.126:3000/pedidos/actualizar_status";
            //------------
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (!response.isEmpty()){
                        color.setBackgroundColor(itemView.getResources().getColor(R.color.listo));
                        socket.emit("notificar_cliente_p_f","{\"pedido\":\""+pedido.getId_pedido()+"\",\"user\":\""+pedido.getUsuario()+"\"}");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(itemView.getContext(), "Sin Conexion\n con el Servidor", Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> parametros = new HashMap<>();
                    parametros.put("id_pedido",pedido.getId_pedido());
                    parametros.put("estado","F");
                    return parametros;
                }
            };
            RequestQueue requestQueue= Volley.newRequestQueue(itemView.getContext());
            requestQueue.add(stringRequest);
        }



        void bindData(Pedido item){
            id_pedido.setText(item.getId_pedido());
            total_productos.setText(item.getTotal_produtos());
            total_precio.setText("$ "+item.getTotal_precio());
            if (item.status.equals("I")){
                color.setBackgroundColor(itemView.getResources().getColor(R.color.pendiente) );
            }else if (item.status.equals("C")){
                color.setBackgroundColor(itemView.getResources().getColor(R.color.cancelado));
            }else if (item.status.equals("F")){
                color.setBackgroundColor(itemView.getResources().getColor(R.color.listo));
            }
            pedido=item;
        }

    }
}
