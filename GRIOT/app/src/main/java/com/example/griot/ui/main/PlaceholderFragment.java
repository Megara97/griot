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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
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
import com.example.griot.databinding.FragmentMainBinding;
import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private PageViewModel pageViewModel;
    private FragmentMainBinding binding;
    private ImageButton btnEditar, btnGuardarEdit;
    private EditText tmuestra;
    private ProgressBar PBHumS,PBHumA,PBTem,PBLum,PBVie,PBPre,PBLlu;
    private TextView ValHS,ValHA,ValT,ValL,ValV,ValP,ValLluvia,ValRiego,FMostrada;
    private ImageView RiegoON,RiegoOFF;
    private Button btnAct;
    private RequestQueue requestQueue;
    private RequestQueue requestQueuep;
    private float newhs,newha,newt,newl,newv,newp,newra,newtm;
    private int newr;
    private String newf, newfecha;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentMainBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnAct=root.findViewById(R.id.actualizarm);
        btnEditar=root.findViewById(R.id.TiempoMuestreo);
        btnGuardarEdit=root.findViewById(R.id.GuardarTiempo);
        tmuestra=root.findViewById(R.id.editTextNumber);
        FMostrada=root.findViewById(R.id.textfecha);
        RiegoON=root.findViewById(R.id.gota1);
        RiegoOFF=root.findViewById(R.id.gota0);
        ValHS = root.findViewById(R.id.VHS);
        ValHA = root.findViewById(R.id.VHA);
        ValT = root.findViewById(R.id.VT);
        ValL = root.findViewById(R.id.VL);
        ValV = root.findViewById(R.id.VV);
        ValP = root.findViewById(R.id.VP);
        ValLluvia = root.findViewById(R.id.VR);
        ValRiego = root.findViewById(R.id.VRiego);
        PBHumS = root.findViewById(R.id.PBHS);
        PBHumA = root.findViewById(R.id.PBHA);
        PBTem = root.findViewById(R.id.PBT);
        PBLum = root.findViewById(R.id.PBL);
        PBVie = root.findViewById(R.id.PBV);
        PBPre = root.findViewById(R.id.PBP);
        PBLlu= root.findViewById(R.id.PBR);
        PBHumS.setProgress(0);
        PBHumA.setProgress(0);
        PBTem.setProgress(0);
        PBLum.setProgress(0);
        PBVie.setProgress(0);
        PBPre.setProgress(0);
        PBLlu.setProgress(0);

        btnGuardarEdit.setVisibility(View.INVISIBLE);
        RiegoON.setVisibility(View.INVISIBLE);
        tmuestra.setEnabled(false);

        final TextView textView = binding.sectionLabel;
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnEditar.setVisibility(View.INVISIBLE);
                btnGuardarEdit.setVisibility(View.VISIBLE);
                tmuestra.setEnabled(true);
            }
        });

        btnGuardarEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    String tmue = tmuestra.getText().toString();
                    if (!tmue.isEmpty()){
                        int muestra = Integer.parseInt(tmue);
                        tmuestra.setText(tmue);
                        JSONObject SendAWStm = new JSONObject();
                        try {
                            SendAWStm.put("Muestreo", muestra * 60000);
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
                    tmuestra.setText("");
                    Toast.makeText(requireContext(), "No se pueden realizar cambios sin conexión a Internet", Toast.LENGTH_SHORT).show();
                }
                btnEditar.setVisibility(View.VISIBLE);
                btnGuardarEdit.setVisibility(View.INVISIBLE);
                tmuestra.setEnabled(false);
            }
        });

        btnAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    JSONObject SendAWSini = new JSONObject();
                    try {
                        SendAWSini.put("Consulta", 1);
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
                                Mdatos(response);
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

    public void Mdatos(JSONArray datos) throws JSONException {
        newf = "";
        newhs = 0;
        newha = 0;
        newt = 0;
        newl = 0;
        newv = 0;
        newp = 0;
        newra = 0;
        //newr = 0;

        if (datos != null && datos.length()>0) {
            JSONObject actual;
            try {
                actual = datos.getJSONObject(0);
                try {
                    newf = actual.getString("Fecha");
                    newfecha = newf.substring(6, 8) + "/" + newf.substring(4, 6) + "/" + newf.substring(0, 4) + " " + newf.substring(8, 10) + ":" + newf.substring(10, 12) + ":" + newf.substring(12, 14);
                    Log.i("fecha", String.valueOf(newfecha));
                    newhs = (float) actual.getDouble("Hs");
                    newha = (float) actual.getDouble("Ha");
                    newt = (float) actual.getDouble("Ta");
                    newl = (float) actual.getDouble("La");
                    newv = (float) actual.getDouble("Vv");
                    newp = (float) actual.getDouble("Pa");
                    newra = (float) actual.getDouble("Ll");

                    FMostrada.setText(newfecha);
                    PBHumS.setProgress((int)newhs);
                    PBHumA.setProgress((int) newha);
                    PBTem.setProgress((int)newt);
                    PBLum.setProgress((int)newl);
                    PBVie.setProgress((int)newv);
                    PBPre.setProgress((int)newp);
                    PBLlu.setProgress((int)newra);
                    ValHS.setText(String.format("%.2f", newhs) + " %");
                    ValHA.setText(String.format("%.2f", newha) + " %");
                    ValT.setText(String.format("%.2f", newt) + " °C");
                    ValL.setText(String.format("%.2f", newl) + " lx");
                    ValV.setText(String.format("%.2f", newv) + " m/s");
                    ValP.setText(String.format("%.2f", newp) + " hPa");
                    ValLluvia.setText(String.format("%.2f", newra) + " %");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            try {
                actual = datos.getJSONObject(1);
                try {
                    //newf = actual.getString("Fecha");
                    //newfecha = newf.substring(6, 8) + "/" + newf.substring(4, 6) + "/" + newf.substring(0, 4) + " " + newf.substring(8, 10) + ":" + newf.substring(10, 12) + ":" + newf.substring(12, 14);
                    //Log.i("fecha", String.valueOf(newfecha));
                    //newr = actual.getInt("Er");
                    newtm = (float) actual.getDouble("Tm");

                    //FMostrada.setText(newfecha);
                    /*if (newr==0){
                        RiegoON.setVisibility(View.INVISIBLE);
                        RiegoOFF.setVisibility(View.VISIBLE);
                        ValRiego.setText("Apagado");
                    } else{
                        RiegoON.setVisibility(View.VISIBLE);
                        RiegoOFF.setVisibility(View.INVISIBLE);
                        ValRiego.setText("Encendido");
                    }*/
                    tmuestra.setText(String.format("%.2f", newtm));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}