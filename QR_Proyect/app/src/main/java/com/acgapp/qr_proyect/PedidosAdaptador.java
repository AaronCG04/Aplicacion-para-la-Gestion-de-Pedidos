package com.acgapp.qr_proyect;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class PedidosAdaptador extends RecyclerView.Adapter<PedidosAdaptador.ViewHolder>{
    private List<Pedido> datos_pedido;
    private Context context;
    public Socket socket;
    private  String user;
    private String datos;
    public PedidosAdaptador(List<Pedido> datos_pedido,Context context,String user){
        conectar_socket();
        socket.connect();
        socket.on(Socket.EVENT_CONNECT,conectar);
//        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.datos_pedido=datos_pedido;
        this.user=user;
        this.datos="{\"user\":\""+this.user+"\",\"room\":\"Cliente\"}";
    }
    @NonNull
    @Override
    public PedidosAdaptador.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.elemento_lista_cliente,parent,false);
        return new ViewHolder(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidosAdaptador.ViewHolder holder, int position) {
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
                    String datos="{\"user\":\""+user+"\",\"room\":\"Cliente\"}";
                    socket.emit("ingresar_room",datos);
                }
            });
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView id_pedido,total_productos,total_precio,color;
        ImageView cancelar,terminar;
        Pedido pedido;
        PedidosAdaptador adaptador;
        ViewHolder(View itemView) {
            super(itemView);
            id_pedido = itemView.findViewById(R.id.card_pedido_l);
            total_productos = itemView.findViewById(R.id.card_pedido_total_pro);
            total_precio = itemView.findViewById(R.id.total_dinero);
            cancelar = itemView.findViewById(R.id.eliminar_p);
            terminar= itemView.findViewById(R.id.terminar_p);
            cancelar=itemView.findViewById(R.id.eliminar_p);
            color = itemView.findViewById(R.id.color);
            //terminar=itemView.findViewById(R.id.terminar_p);

            cancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(itemView.getContext(), "Eliminar "+pedido.getId_pedido(), Toast.LENGTH_SHORT).show();
                    eliminar_pedido(pedido);
                }
            });
        }
        public ViewHolder linkAdapter(PedidosAdaptador adapter){
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
}/* extends RecyclerView.Adapter<PedidosAdaptador.ViewHolder> {
    private List<Pedido> datos_pedido;
    private LayoutInflater mInflater;
    private Context context;

    public PedidosAdaptador(List<Pedido> datos_pedido,Context context){
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.datos_pedido=datos_pedido;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.elemento_lista,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListAdapter.ViewHolder holder, int position) {
        holder.bindData(datos_pedido.get(position));
    }

    @Override
    public int getItemCount() {return datos_pedido.size();}

    public void setItems(List<Pedido> datos_pedido){this.datos_pedido=datos_pedido;}

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView id_pedido,total_productos,total_precio;
        ImageView cancelar,terminar;
        ViewHolder(View itemView) {
            super(itemView);
            id_pedido = itemView.findViewById(R.id.card_pedido_l);
            total_productos = itemView.findViewById(R.id.card_pedido_total_pro);
            total_precio = itemView.findViewById(R.id.total_dinero);
            cancelar = itemView.findViewById(R.id.eliminar_p);
             terminar= itemView.findViewById(R.id.terminar_p);
        }
        void bindData(Pedido item){
            id_pedido.setText(item.getId_pedido());
            total_productos.setText(item.getTotal_produtos());
            total_precio.setText(item.getTotal_precio());
        }
    }
}
*/