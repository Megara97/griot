package com.example.griot.ui.main;


import static com.github.mikephil.charting.components.Legend.LegendForm.LINE;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.android.volley.toolbox.Volley;
import com.example.griot.R;
import com.example.griot.databinding.FragmentMain2Binding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlaceholderFragment2 extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private PageViewModel pageViewModel;
    private FragmentMain2Binding binding;
    private View root;
    private TextView Fecha1, Fecha2;
    private String Sfecha1, Sfecha2, Efecha1, Efecha2;
    private Button btnVis;
    private Spinner spvsel;
    private LineChart grafica;
    private LineData data;
    private RequestQueue requestQueuep;
    private String newf, newfecha;
    private float newhs,newha,newt,newl,newv,newp,newra;
    private int newr;
    private List<Entry> entradashs, entradasha, entradast, entradasl, entradasv, entradasp, entradasra, entradasr;
    private List<String> listfechas,listfechasriego;
    private LineDataSet datos;
    private List<ILineDataSet> dataSets;
    private String[] listaElementos;



    public static PlaceholderFragment2 newInstance(int index) {
        PlaceholderFragment2 fragment = new PlaceholderFragment2();
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

        binding = FragmentMain2Binding.inflate(inflater, container, false);
        root = binding.getRoot();

        btnVis = root.findViewById(R.id.visualizar);
        Fecha1 = root.findViewById(R.id.Date1);
        Fecha2 = root.findViewById(R.id.Date2);
        grafica = root.findViewById(R.id.grafica1);
        spvsel = root.findViewById(R.id.variablespiner);

        listaElementos = getResources().getStringArray(R.array.variables_array);
        grafica.setNoDataText("No hay datos disponibles");

        final TextView textView = binding.sectionLabel2;
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        spvsel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Se ejecuta cuando el usuario selecciona un elemento del Spinner
                //String elementoSeleccionado = listaElementos[position];
                //Toast.makeText(requireContext(), "Elemento seleccionado: " + elementoSeleccionado, Toast.LENGTH_SHORT).show();
                Graficar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Se ejecuta cuando no hay elementos seleccionados (rara vez se usa)
            }
        });

        Fecha1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener la fecha actual para mostrarla como fecha predeterminada en el calendario
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                // Crear un diálogo de selección de fecha utilizando DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        requireContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                if (!isDateBlocked(year, month, dayOfMonth, 1)) {
                                    Sfecha1 = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                                    Efecha1 = String.format(Locale.getDefault(), "%04d%02d%02d000000", year, month + 1, dayOfMonth);
                                    //Log.i("Magali", Efecha1);
                                    // Actualizar el TextView para mostrar la fecha seleccionada
                                    Fecha1.setText(Sfecha1);
                                }
                            }
                        },
                        year,
                        month,
                        dayOfMonth
                );
                // Mostrar el diálogo
                datePickerDialog.show();
            }
        });

        Fecha2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                // Crear un diálogo de selección de fecha utilizando DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        requireContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                if (!isDateBlocked(year, month, dayOfMonth, 2)) {
                                    Sfecha2 = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                                    Efecha2 = String.format(Locale.getDefault(), "%04d%02d%02d235959", year, month + 1, dayOfMonth);
                                    //Log.i("Magali", Efecha2);
                                    // Actualizar el TextView para mostrar la fecha seleccionada
                                    Fecha2.setText(Sfecha2);
                                }
                            }
                        },
                        year,
                        month,
                        dayOfMonth
                );
                // Mostrar el diálogo
                datePickerDialog.show();
            }
        });

        btnVis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    if (Efecha1 != null && !Efecha1.isEmpty() && Efecha2 != null && !Efecha2.isEmpty()) {
                        JSONObject SendAWSini = new JSONObject();
                        try {
                            SendAWSini.put("Consulta", 2);
                            SendAWSini.put("InPer", Efecha1);
                            SendAWSini.put("FiPer", Efecha2);
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
                                //Log.i("Respuesta", response.toString());
                                try {
                                    Gdatos(response);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //Log.i("Magali", "Auxilio: " + error.toString());
                            }
                        });
                        requestQueuep.add(requestc);
                    } else{
                        Toast.makeText(requireContext(), "Seleccione una periodo de visualización", Toast.LENGTH_SHORT).show();
                    }
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

    public void Graficar() {
        grafica.clear();
        grafica.notifyDataSetChanged();
        if (entradashs != null) {
            grafica = root.findViewById(R.id.grafica1);
            grafica.setNoDataText("No hay datos disponibles");
            int Variable = spvsel.getSelectedItemPosition();
            //Log.i("Confirmación", String.valueOf(Variable));
            switch (Variable) {
                case 0:
                    datos = new LineDataSet(entradashs, "Humedad del suelo (%)"); // add entries to dataset
                    datos.setColor(Color.parseColor("#16A085"));
                    datos.setCircleColor(Color.parseColor("#0E6655"));
                    datos.setDrawValues(true);
                    break;
                case 1:
                    datos = new LineDataSet(entradasha, "Humedad ambiental (%)"); // add entries to dataset
                    datos.setColor(Color.parseColor("#16A085"));
                    datos.setCircleColor(Color.parseColor("#0E6655"));
                    datos.setDrawValues(true);
                    break;
                case 2:
                    datos = new LineDataSet(entradast, "Temperatura ambiental (°C)"); // add entries to dataset
                    datos.setColor(Color.parseColor("#F39C12"));
                    datos.setCircleColor(Color.parseColor("#D35400"));
                    datos.setDrawValues(true);
                    break;
                case 3:
                    datos = new LineDataSet(entradasl, "Luminosidad (lx)"); // add entries to dataset
                    datos.setColor(Color.parseColor("#F39C12"));
                    datos.setCircleColor(Color.parseColor("#D35400"));
                    datos.setDrawValues(true);
                    break;
                case 4:
                    datos = new LineDataSet(entradasv, "Velocidad del viento (m/s)"); // add entries to dataset
                    datos.setColor(Color.parseColor("#FFEB3B"));
                    datos.setCircleColor(Color.parseColor("#FFC107"));
                    datos.setDrawValues(true);
                    break;
                case 5:
                    datos = new LineDataSet(entradasp, "Presión atmosférica (hPa)"); // add entries to dataset
                    datos.setColor(Color.parseColor("#FFEB3B"));
                    datos.setCircleColor(Color.parseColor("#FFC107"));
                    datos.setDrawValues(false);
                    break;
                case 6:
                    datos = new LineDataSet(entradasra, "Lluvia (%)"); // add entries to dataset
                    datos.setColor(Color.parseColor("#00BCD4"));
                    datos.setCircleColor(Color.parseColor("#0F7BD1"));
                    datos.setDrawValues(true);
                    break;
                case 7:
                    datos = new LineDataSet(entradasr, "Riego"); // add entries to dataset
                    datos.setColor(Color.parseColor("#00BCD4"));
                    datos.setCircleColor(Color.parseColor("#0F7BD1"));
                    datos.setDrawValues(false);
                    break;
                default:
                    break;
            }

            datos.setDrawCircleHole(false);
            //datos.setDrawValues(true);
            datos.setValueTextColor(Color.parseColor("#888888"));
            dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(datos);
            data = (LineData) new LineData(dataSets);
            grafica.setData(data);
            grafica.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            grafica.getXAxis().setTextColor(Color.parseColor("#888888"));
            grafica.getAxisLeft().setTextColor(Color.parseColor("#888888"));
            //grafica.getAxisLeft().setAxisMinimum(0);
            //grafica.getAxisLeft().setAxisMaximum(100);
            grafica.getAxisLeft().resetAxisMinimum();
            grafica.getAxisLeft().resetAxisMaximum();
            grafica.setAutoScaleMinMaxEnabled(true);
            grafica.getAxisRight().setEnabled(false);
            //grafica.setDrawBorders(true);
            //grafica.setBorderColor(Color.parseColor("#888888"));
            grafica.getLegend().setTextColor(Color.parseColor("#888888"));
            grafica.getLegend().setForm(LINE);
            grafica.getDescription().setEnabled(false);

            if (Variable < 7) {
                grafica.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(listfechas));
            } else if (Variable == 7) {
                grafica.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(listfechasriego));
                //grafica.setAutoScaleMinMaxEnabled(false);
                //grafica.getAxisLeft().setAxisMinimum(0);
                //grafica.getAxisLeft().setAxisMaximum(1);
            }

            //grafica.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(listfechas));
            //grafica.getXAxis().setValueFormatter(null);
            grafica.getXAxis().setLabelRotationAngle(-90);
            grafica.setVisibleXRangeMaximum(20f);
            grafica.setScaleXEnabled(true);
            grafica.setVisibleXRangeMinimum(20f);
            grafica.getXAxis().setGranularity(5f);
            //grafica.setMaxVisibleValueCount(10);
            grafica.invalidate();
            grafica.moveViewToX(data.getEntryCount()); // Mover vista a la última entrada
        //} else {
            //Toast.makeText(requireContext(), "Da click en Visualizar", Toast.LENGTH_SHORT).show();
        }
    }

    public void Gdatos(JSONArray datos) throws JSONException {
        newf = "";
        newhs = 0;
        newha = 0;
        newt = 0;
        newl = 0;
        newv = 0;
        newp = 0;
        newra = 0;
        newr = 0;
        listfechas = new ArrayList<String>();
        listfechasriego = new ArrayList<String>();
        entradashs = new ArrayList<Entry>();
        entradasha = new ArrayList<Entry>();
        entradast = new ArrayList<Entry>();
        entradasl = new ArrayList<Entry>();
        entradasv = new ArrayList<Entry>();
        entradasp = new ArrayList<Entry>();
        entradasra = new ArrayList<Entry>();
        entradasr = new ArrayList<Entry>();

        if (datos != null && datos.length()>0) {
            //Log.i("Numero", String.valueOf(datos.length()));
            JSONArray datos1 = datos.getJSONArray(0);
            JSONArray datos2 = datos.getJSONArray(1);
            //Log.i("Numero 1°", String.valueOf(datos1.length()));
            //Log.i("Numero 2°", String.valueOf(datos2.length()));
            for (int i = 0; i < datos1.length(); i++) {
                JSONObject actual;
                try {
                    actual = datos1.getJSONObject(i);
                    //Log.i("Valor", String.valueOf(actual));
                    try {
                        newf = actual.getString("Fecha");
                        newfecha = newf.substring(6, 8)+"/"+newf.substring(4, 6)+"/"+newf.substring(0, 4)+" "+newf.substring(8,10)+":"+newf.substring(10,12)+":"+newf.substring(12,14);
                        //Log.i("fecha", String.valueOf(newfecha));
                        listfechas.add(String.valueOf(newfecha));
                        newhs = (float) actual.getDouble("Hs");
                        //entradashs.add(new Entry(newf, newhs));
                        entradashs.add(new Entry(i, newhs));
                        newha = (float) actual.getDouble("Ha");
                        entradasha.add(new Entry(i, newha));
                        newt = (float) actual.getDouble("Ta");
                        entradast.add(new Entry(i, newt));
                        newl = (float) actual.getDouble("La");
                        entradasl.add(new Entry(i, newl));
                        newv = (float) actual.getDouble("Vv");
                        entradasv.add(new Entry(i, newv));
                        newp = (float) actual.getDouble("Pa");
                        entradasp.add(new Entry(i, newp));
                        newra = (float) actual.getDouble("Ll");
                        entradasra.add(new Entry(i, newra));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            for (int i = 0; i < datos2.length(); i++) {
                JSONObject actual;
                try {
                    actual = datos2.getJSONObject(i);
                    //Log.i("Valor", String.valueOf(actual));
                    try {
                        newf = actual.getString("Fecha");
                        newfecha = newf.substring(6, 8)+"/"+newf.substring(4, 6)+"/"+newf.substring(0, 4)+" "+newf.substring(8,10)+":"+newf.substring(10,12)+":"+newf.substring(12,14);
                        //Log.i("fecha", String.valueOf(newfecha));
                        listfechasriego.add(String.valueOf(newfecha));
                        newr = actual.getInt("Er");
                        entradasr.add(new Entry(i, newr));/*  */
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            Toast.makeText(requireContext(), "No hay datos en el periodo seleccionado", Toast.LENGTH_SHORT).show();
        }
        Graficar();
    }

    private boolean isDateBlocked(int year, int month, int dayOfMonth, int tipo) {
        // Si la variable selectedDate tiene una fecha seleccionada, usarla como fecha predeterminada en el segundo calendario
        Calendar calendar = Calendar.getInstance();
        int ano = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);
        if (year > ano || (year == ano && month > mes) || (year == ano && month == mes && dayOfMonth > dia)) {
            Toast.makeText(requireContext(), "No seleccione una fecha del futuro", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (tipo == 2) {
            if (Sfecha1 != null && !Sfecha1.isEmpty()) {
                String[] parts = Sfecha1.split("/");
                if (parts.length == 3) {
                    dia = Integer.parseInt(parts[0]);
                    mes = Integer.parseInt(parts[1]) - 1;
                    ano = Integer.parseInt(parts[2]);
                    if (year < ano || (year == ano && month < mes) || (year == ano && month == mes && dayOfMonth < dia)) {
                        Toast.makeText(requireContext(), "Seleccione una fecha posterior a la 1° fecha", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                }
            }
            return false;
        }
        if (tipo == 1) {
            if (Sfecha2 != null && !Sfecha2.isEmpty()) {
                String[] parts = Sfecha2.split("/");
                if (parts.length == 3) {
                    dia = Integer.parseInt(parts[0]);
                    mes = Integer.parseInt(parts[1]) - 1;
                    ano = Integer.parseInt(parts[2]);
                    if (year > ano || (year == ano && month > mes) || (year == ano && month == mes && dayOfMonth > dia)) {
                        Toast.makeText(requireContext(), "Seleccione una fecha anterior a la 2° fecha", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    // Si la fecha seleccionada no está bloqueada, devolver false
                    return false;
                }
            }
            return false;
        }
        return false;
    }
}

