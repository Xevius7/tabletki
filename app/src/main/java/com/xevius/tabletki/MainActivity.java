package com.xevius.tabletki;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
//import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity  {


    Spinner startspiner,endspiner,typespiner;
    TextView starteds,endeds,compens;
    double startTEMP=0,endTEMP=0,Comp85=0;
    int type;
    SpinnerAdapter type_Adapter;

    //String[] txt,comp;
    String xt;
    String type_Choice = "50М";
    Timer timer =new Timer();
    //ArrayAdapter<?> ad1,ad2,ad3;

    double[] A={
            -0.017600414,   //A0
            0.038921205,    //A1
            1.85588E-05,    //A2
            -9.94576E-08,   //A3
            3.18409E-10,    //A4
            -5.60728E-13,   //A5
            5.60751E-16,    //A6
            -3.20207E-19,   //A7
            9.71511E-23,    //A8
            -1.21047E-26,   //A9
            };




    @Override

    protected void onCreate(Bundle savedInstanceState) {
        //try {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        starteds = findViewById(R.id.starteds);
        endeds = findViewById(R.id.endeds);
        compens = findViewById(R.id.compens);



        typespiner = Create_typespiner();
        startspiner = Create_startspiner(type_Choice);
        endspiner = Create_endspiner(type_Choice);

        timer.schedule( new UpdateTimerTask(),0,1000);

            //startspiner = findViewById(R.id.start);
            //endspiner = findViewById(R.id.end);
            //typespiner = findViewById(R.id.type);




            // Настраиваем адаптер
            //ad1 = ArrayAdapter.createFromResource(this, R.array.thaStart, android.R.layout.simple_spinner_item);
            //ad1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //ad2 = ArrayAdapter.createFromResource(this, R.array.thaEnd, android.R.layout.simple_spinner_item);
            //ad2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //ad3 = ArrayAdapter.createFromResource(this, R.array.thaType, android.R.layout.simple_spinner_item);
            //ad3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            //startspiner.setAdapter(ad2);
            //endspiner.setAdapter(ad2);
            //typespiner.setAdapter(ad1);






            //startspiner.setOnItemSelectedListener(this);
            //endspiner.setOnItemSelectedListener(this);
            //typespiner.setOnItemSelectedListener(this);

        //} catch (Exception e) {
        //    Log.e("ERROR", e.toString());
        //    e.printStackTrace();
        //}




    }

    public Spinner Create_typespiner() {
        Spinner typespiner = (Spinner) findViewById(R.id.type);
        String[] type_Array = getResources().getStringArray(
                R.array.PIType);
        type_Adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item,
                type_Array);
        typespiner.setAdapter(type_Adapter);
        typespiner
                .setOnItemSelectedListener(new typespiner_Listener());
        return typespiner;
    }

    public Spinner Create_startspiner(String type_Choice) {
        String[] start_Array = null;
        Spinner startspiner = (Spinner) findViewById(R.id.start);
        if (type_Choice.equals("Pt100") || type_Choice.equals("100П") ) {
            start_Array = getResources().getStringArray(
                    R.array.PT100Start);
        } else if (type_Choice.equals("50М") || type_Choice.equals("100M")) {
            start_Array = getResources().getStringArray(
                    R.array.M100Start);
        } else if (type_Choice.equals("ТХА-ПИ4")) {
            start_Array = getResources().getStringArray(
                    R.array.thaStart);
        } else
            start_Array = getResources().getStringArray(
                    R.array.thaStart);
        SpinnerAdapter start_Adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item,
                start_Array);
        startspiner.setAdapter(start_Adapter);
        startspiner
                .setOnItemSelectedListener(new startspiner_Listener());
        return startspiner;
    }

    public Spinner Create_endspiner(String type_Choice) {
        String[] end_Array = null;
        Spinner endspiner = (Spinner) findViewById(R.id.end);
        if (type_Choice.equals("Pt100") || type_Choice.equals("100П")) {
            end_Array = getResources().getStringArray(
                    R.array.PT100End);
        } else if (type_Choice.equals("50М") || type_Choice.equals("100M")) {
            end_Array = getResources().getStringArray(
                    R.array.M100MEnd);
        } else if (type_Choice.equals("ТХА-ПИ4")) {
            end_Array = getResources().getStringArray(
                    R.array.thaEnd);
        } else
            end_Array = getResources().getStringArray(
                    R.array.thaEnd);
        SpinnerAdapter end_Adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item,
                end_Array);
        endspiner.setAdapter(end_Adapter);
        endspiner
                .setOnItemSelectedListener(new startspiner_Listener());
        return endspiner;
    }

    public class typespiner_Listener implements
            OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> type_Adapter_View,
                                   View v, int position, long row) {
            //type_Adapter.notifyDataSetChanged();
            type_Choice = type_Adapter_View
                    .getItemAtPosition(position).toString();
            Create_startspiner(type_Choice);
            Create_endspiner(type_Choice);
            //starteds.setText("asdd");
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    }

    public class startspiner_Listener implements
            OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> Category_Add_Adapter_View,
                                   View v, int position, long row) {
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    }


    public double TempToEDS (double t)
    {
        double result=0;

        /* -270 - 0 */
        if(t<0)
            result=(
                    (3.9450128025*1E-2)*t+
                            (2.3622373598*1E-5)*t*t+
                            (-3.2858906784*1E-7)*t*t*t+
                            (-4.9904828777*1E-9)*t*t*t*t+
                            (-6.7509059173*1E-11)*t*t*t*t*t+
                            (-5.7410327428*1E-13)*t*t*t*t*t*t+
                            (-3.1088872894*1E-15)*t*t*t*t*t*t*t+
                            (-1.0451609365*1E-17)*t*t*t*t*t*t*t*t+
                            (-1.9889266878*1E-20)*t*t*t*t*t*t*t*t*t+
                            (-1.6322697486*1E-23)*t*t*t*t*t*t*t*t*t*t
            );

        if(t>0)
        {
            for(int i=0; i<10; i++)
                result += (A[i]*Math.pow(t,i));
            result+=0.1185976*Math.exp(-0.000118343*Math.pow(t-126.9686,2));
        }
        return result;
    }
    public double TempToR (double temp, String type_Choice)
    {
        double result = 0;

        if (type_Choice.equals("Pt100") && temp>=0) {
            result = 100 * (1 + 3.9083E-3 * temp + -5.775E-07 * Math.pow(temp, 2));
        }
        if (type_Choice.equals("Pt100") && temp < 0){//Pt100-
            result = 100 * (1 + 3.9083E-3 * temp + -5.775E-07 * Math.pow(temp, 2) + -4.183E-12 * (temp - 100)*Math.pow(temp, 3));
        }
        if (type_Choice.equals("100M") && temp >= 0) {result= 100 * (1 + 4.28E-3 * temp );}
        if (type_Choice.equals("100M")  && temp < 0){
            result = 100 * (1 + 4.28E-3 * temp + -6.2032E-07 * temp*(temp+6.7) + 8.5154E-10 * Math.pow(temp,3));
        }

        return result;
    }

    class UpdateTimerTask extends TimerTask{
        public void run(){




            //type = typespiner.getSelectedItemPosition();

            xt = startspiner.getSelectedItem().toString();
            xt = xt.split("°C")[0];
            startTEMP = Integer.parseInt(xt);

            xt = endspiner.getSelectedItem().toString();
            xt = xt.split("°C")[0];
            endTEMP = Integer.parseInt(xt);

            if (type_Choice.equals("ТХА-ПИ4")) {
                Comp85=((1.6*85)/(Math.abs(startTEMP)+endTEMP))+0.4;
                compens.setText(String.format("%.4f",Comp85)+" (+85°C)");
                starteds.setText(String.format("%.3f",TempToEDS(startTEMP))+" mV");
                endeds.setText(String.format("%.3f",TempToEDS(endTEMP))+" mV");
            }

            if (type_Choice.equals("ТХА-ПИ3")){
                Comp85 = 1.6*TempToEDS(85);
                Comp85 = Comp85/(Math.abs(TempToEDS(startTEMP))+TempToEDS(endTEMP))+0.4;
                compens.setText(String.format("%.4f",Comp85)+" (+85°C)");
                starteds.setText(String.format("%.3f",TempToEDS(startTEMP))+" mV");
                endeds.setText(String.format("%.3f",TempToEDS(endTEMP))+" mV");
            }
            if (type_Choice.equals("50М")) {
                starteds.setText(String.format("%.3f",TempToR(startTEMP,"100M")/2)+" Ω");
                endeds.setText(String.format("%.3f",TempToR(endTEMP,"100M")/2)+" Ω");
                compens.setText("");
                //return;
            }

            if (type_Choice.equals("100M")) {
                starteds.setText(String.format("%.3f",TempToR(startTEMP,type_Choice))+" Ω");
                endeds.setText(String.format("%.3f",TempToR(endTEMP,type_Choice))+" Ω");
                compens.setText("");
            }

            if (type_Choice.equals("Pt100")){
                starteds.setText(String.format("%.2f",TempToR(startTEMP,type_Choice))+" Ω");
                endeds.setText(String.format("%.2f",TempToR(endTEMP,type_Choice))+" Ω");
                compens.setText("");
            }
        }
    }


    //public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Spinner startspiner = (Spinner)parent;
        //Spinner endspiner = (Spinner)parent;
        //Spinner typespiner = (Spinner)parent;

        //if (typespiner.getId() == R.id.type) {
        //    type = typespiner.getSelectedItemPosition();
        //    if (type == 4) {//pt100

                // Настраиваем адаптер
                //startspiner.setAdapter(null);
                //ad1 =
                //        ArrayAdapter.createFromResource(this, R.array.startPT100, android.R.layout.simple_spinner_item);

                //ad1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //startspiner.setAdapter(ad1);


                //ad1.
               // ad1.notifyDataSetChanged();
            //}

       // }




            //compens.setText(comp[nnn]);
        //}
        //if(endspiner.getId() == R.id.end) {





    }






