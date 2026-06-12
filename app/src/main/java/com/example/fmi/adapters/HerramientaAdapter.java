package com.example.fmi.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.fmi.R;
import com.example.fmi.entity.Herramienta;

import org.json.JSONObject;

import java.util.List;

public class HerramientaAdapter extends RecyclerView.Adapter<HerramientaAdapter.ViewHolder> {

    //PASO 01
    //Contenedor de herremienta (objeto derivado de una clase)
    private List<Herramienta> listaHerramientas;    //Atributo de clase
    private Context context;
    RequestQueue requestQueue;
    private final String URL = "http://192.168.101.41:3000/api/herramientas/"; //EndPoint

    //PASO 02
    //Constructor para la clase principal "HerramientaAdapter"
    public HerramientaAdapter(List<Herramienta> listaHerramientas, Context context) {
        this.listaHerramientas = listaHerramientas;
        this.context = context;
    }

    //PASO 03
    //Acceso a los Widget del XML (plantilla)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        //Referencias
        TextView edtNombreRV, edtDescripcionRV;
        Button btnEliminarRV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            edtNombreRV = itemView.findViewById(R.id.edtNombreRV);
            edtDescripcionRV = itemView.findViewById(R.id.edtDescripcionRV);
            btnEliminarRV = itemView.findViewById(R.id.btnEliminarRV);
        }
    }

    //PASO 05
    //Backend cúal es la plantilla
    @NonNull
    @Override
    public HerramientaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_herramientas, parent, false);
        return new HerramientaAdapter.ViewHolder(view);
    }

    //PASO 04
    //Bin (unir, vincular, asociar), View (Vista - XML), Holder (Soporte)
    @Override
    public void onBindViewHolder(@NonNull HerramientaAdapter.ViewHolder holder, int position) {
        //Todos los elementos que mostrara el Recyvler View estan almacenados en memoria (listaHerramientas)
        Herramienta herramienta = listaHerramientas.get(position);
        holder.edtNombreRV.setText(herramienta.getNombre());    // getNombre (entity)
        holder.edtDescripcionRV.setText(herramienta.getDescripcion());  //getDescripcion(entity)

        //Botones no dan datos, activa um evento | Lambda java
        holder.btnEliminarRV.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Herramientas");
            builder.setMessage("¿Desea eliminar la herramienta:" + herramienta.getNombre() + "?");
            builder.setPositiveButton("Si", (a, b) -> {
                eliminarHerramienta(herramienta.getIdherramienta(), position);
            });
            builder.setNegativeButton("No", null);
            builder.show();
        });
    }

    //Eliminacion por WS
    private void eliminarHerramienta(int idherramienta, int position) {
        requestQueue = Volley.newRequestQueue(context);
        String endPoint = URL + String.valueOf(idherramienta);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.DELETE,
                endPoint,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //El resultado es un JSON entonces tenemos que trabajar en un TryCatch
                        try{
                            if (response.getBoolean("success")) {
                                //Debemos quitar el elemento de la vista y del recycler
                                listaHerramientas.remove(position); //Se va de la lista
                                notifyItemRemoved(position);        //Se va del Recycler
                                notifyItemRangeChanged(position, listaHerramientas.size());
                                //Notificacion
                                Toast.makeText(context, response.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e("ErrorJSON", e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ErrorEliminando", error.toString());
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    //Paso 06
    //El adaptador debe conocer el total de elemetos
    @Override
    public int getItemCount() {
        return listaHerramientas.size();
    }
}
