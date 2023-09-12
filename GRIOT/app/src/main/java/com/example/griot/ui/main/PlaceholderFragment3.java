package com.example.griot.ui.main;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.griot.R;
import com.example.griot.databinding.FragmentMain3Binding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class PlaceholderFragment3 extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private PageViewModel pageViewModel;
    private FragmentMain3Binding binding;
    private ImageButton btnEditar, btnGuardarEdit;
    private EditText tminima;
    private TextView taut1,taut2;
    private Button btnAct;
    private TextView ValRiego,FMostrada;
    private ImageView RiegoON,RiegoOFF;
    private RequestQueue requestQueue;
    private RequestQueue requestQueuep;
    private Switch ModoRiego;
    private ToggleButton Manual;
    private String Modo;
    private int Riego;
    private int newr;
    private float newtm;
    private String newf,newfecha,newm;

    public static PlaceholderFragment3 newInstance(int index) {
        PlaceholderFragment3 fragment = new PlaceholderFragment3();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 2;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentMain3Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnEditar=root.findViewById(R.id.Humedadminima);
        btnGuardarEdit=root.findViewById(R.id.GuardarHumedad);
        tminima=root.findViewById(R.id.editTextNumber2);
        taut1=root.findViewById(R.id.texttm);
        taut2=root.findViewById(R.id.ToleranciaHumedad);
        ModoRiego=root.findViewById(R.id.Modo);
        Manual=root.findViewById(R.id.RiegoManual);
        btnAct=root.findViewById(R.id.verriego);
        RiegoON=root.findViewById(R.id.gota1R);
        RiegoOFF=root.findViewById(R.id.gota0R);
        ValRiego = root.findViewById(R.id.VRiegoR);
        FMostrada=root.findViewById(R.id.textfechaR);

        btnEditar.setVisibility(View.INVISIBLE);
        btnGuardarEdit.setVisibility(View.INVISIBLE);
        taut1.setVisibility(View.INVISIBLE);
        taut2.setVisibility(View.INVISIBLE);
        tminima.setVisibility(View.INVISIBLE);
        tminima.setEnabled(false);
        Manual.setVisibility(View.VISIBLE);
        RiegoON.setVisibility(View.INVISIBLE);

        final TextView textView = binding.sectionLabel3;
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        ModoRiego.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isNetworkAvailable()) {
                    if (isChecked) {
                        // El Switch está activado (ON)
                        btnEditar.setVisibility(View.VISIBLE);
                        btnGuardarEdit.setVisibility(View.INVISIBLE);
                        taut1.setVisibility(View.VISIBLE);
                        taut2.setVisibility(View.VISIBLE);
                        tminima.setVisibility(View.VISIBLE);
                        tminima.setEnabled(false);
                        Manual.setVisibility(View.INVISIBLE);
                        Modo="Automatico";
                    } else {
                        // El Switch está desactivado (OFF)
                        btnEditar.setVisibility(View.INVISIBLE);
                        btnGuardarEdit.setVisibility(View.INVISIBLE);
                        taut1.setVisibility(View.INVISIBLE);
                        taut2.setVisibility(View.INVISIBLE);
                        tminima.setVisibility(View.INVISIBLE);
                        tminima.setEnabled(false);
                        Manual.setVisibility(View.VISIBLE);
                        Modo="Manual";
                    }
                    JSONObject SendAWStm = new JSONObject();
                    try {
                        SendAWStm.put("Modo", Modo);
                        Log.i("Magali", String.valueOf(SendAWStm));

                        String url = "https://yotcqhygk0.execute-api.us-east-2.amazonaws.com/test/muestreo";
                        requestQueue = Volley.newRequestQueue(getActivity());
                        //requestQueue = Volley.newRequestQueue(this);

                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, SendAWStm, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.i("Magali", response.toString());
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("Magali", "Auxilio " + error.toString());
                            }
                        });
                        requestQueue.add(request);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (isChecked) {
                        // El ToggleButton está activado (ON)
                        ModoRiego.setChecked(false);
                    } else {
                        // El ToggleButton está desactivado (OFF)
                        ModoRiego.setChecked(true);
                    }
                    Toast.makeText(requireContext(), "No se pueden realizar cambios sin conexión a Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Manual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isNetworkAvailable()) {
                    if (isChecked) {
                        // El ToggleButton está activado (ON)
                        Riego = 1;
                    } else {
                        // El ToggleButton está desactivado (OFF)
                        Riego = 0;
                    }
                    JSONObject SendAWStm = new JSONObject();
                    try {
                        SendAWStm.put("Riego", Riego);
                        Log.i("Magali", String.valueOf(SendAWStm));

                        String url = "https://yotcqhygk0.execute-api.us-east-2.amazonaws.com/test/muestreo";
                        requestQueue = Volley.newRequestQueue(getActivity());
                        //requestQueue = Volley.newRequestQueue(this);

                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, SendAWStm, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.i("Magali", response.toString());
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("Magali", "Auxilio " + error.toString());
                            }
                        });
                        requestQueue.add(request);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (isChecked) {
                        // El ToggleButton está activado (ON)
                        Manual.setChecked(false);
                    } else {
                        // El ToggleButton está desactivado (OFF)
                        Manual.setChecked(true);
                    }
                    Toast.makeText(requireContext(), "No se pueden realizar cambios sin conexión a Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnEditar.setVisibility(View.INVISIBLE);
                btnGuardarEdit.setVisibility(View.VISIBLE);
                tminima.setEnabled(true);
            }
        });

        btnGuardarEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    String tmin = tminima.getText().toString();
                    if (!tmin.isEmpty()) {
                        int tolerancia = Integer.parseInt(tmin);
                        tminima.setText(tmin);
                        JSONObject SendAWStm = new JSONObject();
                        try {
                            SendAWStm.put("Tolerancia", tolerancia);
                            Log.i("Magali", String.valueOf(SendAWStm));

                            String url = "https://yotcqhygk0.execute-api.us-east-2.amazonaws.com/test/muestreo";
                            requestQueue = Volley.newRequestQueue(getActivity());
                            //requestQueue = Volley.newRequestQueue(this);

                            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, SendAWStm, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i("Magali", response.toString());
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.i("Magali", "Auxilio " + error.toString());
                                }
                            });
                            requestQueue.add(request);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    tminima.setText("");
                    Toast.makeText(requireContext(), "No se pueden realizar cambios sin conexión a Internet", Toast.LENGTH_SHORT).show();
                }
                btnEditar.setVisibility(View.VISIBLE);
                btnGuardarEdit.setVisibility(View.INVISIBLE);
                tminima.setEnabled(false);
            }
        });

        btnAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    JSONObject SendAWSini = new JSONObject();
                    try {
                        SendAWSini.put("Consulta", 3);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(SendAWSini);
                    //Log.i("Magali", String.valueOf(jsonArray));

                    String url = "https://yotcqhygk0.execute-api.us-east-2.amazonaws.com/test/consulta";
                    requestQueuep = Volley.newRequestQueue(getActivity());

                    JsonArrayRequest requestc = new JsonArrayRequest(Request.Method.POST, url, jsonArray, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.i("Respuesta", response.toString());
                            try {
                                MdatosR(response);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("Magali", "Auxilio: " + error.toString());
                        }
                    });
                    requestQueuep.add(requestc);
                } else {
                    Toast.makeText(requireContext(), "No se pueden realizar consultas sin conexión a Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return root;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    public void MdatosR(JSONArray datos) throws JSONException {
        newf = "";
        newm = "";
        newr = 0;
        newtm = 0;

        if (datos != null && datos.length()>0) {
            JSONObject actual;
            try {
                actual = datos.getJSONObject(0);
                try {
                    newf = actual.getString("Fecha");
                    newfecha = newf.substring(6, 8) + "/" + newf.substring(4, 6) + "/" + newf.substring(0, 4) + " " + newf.substring(8, 10) + ":" + newf.substring(10, 12) + ":" + newf.substring(12, 14);
                    Log.i("fecha", String.valueOf(newfecha));
                    newr = actual.getInt("Er");
                    newtm = (float) actual.getDouble("Th");
                    newm = actual.getString("Mr");

                    FMostrada.setText(newfecha);
                    if (newr==0){
                        RiegoON.setVisibility(View.INVISIBLE);
                        RiegoOFF.setVisibility(View.VISIBLE);
                        ValRiego.setText("Apagado");
                        Manual.setChecked(false);
                    } else{
                        RiegoON.setVisibility(View.VISIBLE);
                        RiegoOFF.setVisibility(View.INVISIBLE);
                        ValRiego.setText("Encendido");
                        Manual.setChecked(true);
                    }
                    tminima.setText(String.format("%.2f", newtm));
                    if (Objects.equals(newm, "Automatico")){
                        ModoRiego.setChecked(true);
                    } else if (Objects.equals(newm, "Manual")){
                        ModoRiego.setChecked(false);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


