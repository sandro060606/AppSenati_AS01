package com.example.fmi.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.fmi.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class RecepcionFragment extends Fragment {

    Button btnBuscarHerramienta, btnActualizar;
    RequestQueue requestQueue; //Cola de solicitudes
    String condicionActual = "", tipoActual = ""; //RadioButton
    EditText edtIdHerramientaB, edtNombreB, edtMarcaB, edtDescripcionB;
    RadioButton rbtBuenoB, rbtRegularB, rbtMaloB;  //Condicion
    RadioButton rbtManualB, rbtElectricaB;        //Tipo
    RadioGroup rgCondicionB, rgTipoB;             //RadioGroup

    private final String URL = "http://192.168.101.41:3000/api/herramientas/";

    //Constructor
    public RecepcionFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Asociar el fragment con el XML
        return inflater.inflate(R.layout.fragment_recepcion, container, false);
    }

    private void BuscarHerramienta() {
        String id = edtIdHerramientaB.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(getContext(), "Ingresar ID de la Herramienta", Toast.LENGTH_LONG).show();
            return;
        }

        String URLBusqueda = URL + id;

        requestQueue = Volley.newRequestQueue(requireContext().getApplicationContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URLBusqueda,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                JSONObject registro = jsonObject.getJSONObject("data");

                                // Llenar los campos
                                edtNombreB.setText(registro.getString("nombre"));
                                edtMarcaB.setText(registro.getString("marca"));
                                edtDescripcionB.setText(registro.getString("descripcion"));

                                seleccionarCondicion(registro.getString("condicion"));
                                seleccionarTipo(registro.getString("tipo"));
                            }
                        } catch (Exception e) {
                            Log.e("ErrorJSON", e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Cuando no realiza la operacion...
                        NetworkResponse response = volleyError.networkResponse;

                        //Si existe un error
                        if (response != null && response.data != null) {
                            //Codigo (INT)
                            int statusCode = response.statusCode;

                            //Contenido (STRING > JSON)
                            String messageJSON = new String(response.data);
                            try {
                                JSONObject jsonWS = new JSONObject(messageJSON);
                                String message = jsonWS.getString("message");
                                if (statusCode == 404) {
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e("ErrorJSON", e.toString());
                            }
                        }
                    }
                }

        );
        requestQueue.add(jsonObjectRequest);
    }

    private void seleccionarTipo(String tipo) {
        rgTipoB.clearCheck();

        rbtManualB.setChecked(false);
        rbtElectricaB.setChecked(false);

        if (tipo.equals("Manual")) { rbtManualB.setChecked(true); }
        if (tipo.equals("Electrica")) { rbtElectricaB.setChecked(true); }
    }

    private void seleccionarCondicion(String condicion) {
        rgCondicionB.clearCheck();

        rbtBuenoB.setChecked(false);
        rbtRegularB.setChecked(false);
        rbtMaloB.setChecked(false);

        if (condicion.equals("Bueno")) { rbtBuenoB.setChecked(true); }
        if (condicion.equals("Regular")) { rbtRegularB.setChecked(true); }
        if (condicion.equals("Malo")) { rbtMaloB.setChecked(true); }
    }

    private void ActualizarDatos() {
        String id = edtIdHerramientaB.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(getContext(), "Primero busque una herramienta", Toast.LENGTH_SHORT).show();
            return;
        }

        condicionActual = "";
        if (rbtBuenoB.isChecked()) { condicionActual = "Bueno"; }
        if (rbtRegularB.isChecked()) { condicionActual = "Regular"; }
        if (rbtMaloB.isChecked()) { condicionActual = "Malo"; }

        tipoActual = "";
        if (rbtManualB.isChecked()) { tipoActual = "Manual"; }
        if (rbtElectricaB.isChecked()) { tipoActual = "Electrica"; }

        JSONObject datosEnviar = new JSONObject();
        try {
            datosEnviar.put("nombre", edtNombreB.getText().toString());
            datosEnviar.put("marca", edtMarcaB.getText().toString());
            datosEnviar.put("descripcion", edtDescripcionB.getText().toString());
            datosEnviar.put("condicion", condicionActual);
            datosEnviar.put("tipo", tipoActual);
        } catch (Exception e) {
            Log.e("ErrorJSON", e.toString());
        }

        String URLUpdate = URL + id;

        requestQueue = Volley.newRequestQueue(requireContext().getApplicationContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                URLUpdate,
                datosEnviar,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            String message = response.getString("message");
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

                            if(success){
                                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                                resetUI();
                            }
                        } catch (Exception e) {
                            Log.e("ErrorJSON", e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Cuando no realiza la operacion...
                        NetworkResponse response = error.networkResponse;

                        //Si existe un error
                        if (response != null && response.data != null) {
                            //STATUS CODE
                            int statusCode = response.statusCode;
                            //MESSAGE DETAIL
                            String errorJSON = new String(response.data);

                            Log.d("ErrorStatusCode", String.valueOf(statusCode));
                            Log.d("ErrorDetallado", errorJSON);
                        }
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void resetUI() {
        edtNombreB.getText().clear();
        edtMarcaB.getText().clear();
        edtDescripcionB.getText().clear();
        rgCondicionB.clearCheck();
        rgTipoB.clearCheck();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBuscarHerramienta = view.findViewById(R.id.btnBuscarHerramienta);
        btnActualizar = view.findViewById(R.id.btnActualizar);

        edtIdHerramientaB = view.findViewById(R.id.edtIdHerramientaB);
        edtNombreB = view.findViewById(R.id.edtNombreB);
        edtMarcaB = view.findViewById(R.id.edtMarcaB);
        edtDescripcionB = view.findViewById(R.id.edtDescripcionB);
        rbtBuenoB = view.findViewById(R.id.rbtBuenoB);
        rbtRegularB = view.findViewById(R.id.rbtRegularB);
        rbtMaloB = view.findViewById(R.id.rbtMaloB);
        rbtManualB = view.findViewById(R.id.rbtManualB);
        rbtElectricaB = view.findViewById(R.id.rbtElectricaB);

        rgCondicionB = view.findViewById(R.id.rgCondicionB);
        rgTipoB = view.findViewById(R.id.rgTipoB);

        btnBuscarHerramienta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BuscarHerramienta();
            }
        });

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActualizarDatos();
            }
        });
    }
}
