package com.xevius.tabletki;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

import com.ftdi.j2xx.D2xxManager;


public class MainActivity extends AppCompatActivity {

    Spinner startspiner,endspiner,typespiner;
    TextView starteds,endeds,compens;
    int startTEMP=0,endTEMP=0;
    double Comp85=0;
    SpinnerAdapter type_Adapter;
    Button btn;

    String xt;
    String type_Choice = "50М";

    double A= 4.28E-3; //100M
    double B = -6.2032E-7;
    double C = 8.5154E-10;

    double AA = 3.9690E-3; //100П
    double BB = -5.841E-7;
    double CC = -4.33E-12;
    int bflag=0;

    double[] K={
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

    public static D2xxManager ftD2xx = null;
    FTDI eni;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //IntentFilter filter = new IntentFilter();
        //filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        //filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        //filter.setPriority(500);
        //this.registerReceiver(mUsbReceiver,filter);

        starteds = findViewById(R.id.starteds);
        endeds = findViewById(R.id.endeds);
        compens = findViewById(R.id.compens);

        typespiner = Create_typespiner();
        startspiner = Create_startspiner(type_Choice);
        endspiner = Create_endspiner(type_Choice);

        //ImageView img = findViewById(R.id.image);
        btn = findViewById(R.id.btn);

        //tbtn.setOnCheckedChangeListener(this);

        try {
            ftD2xx = D2xxManager.getInstance(this);
        } catch (D2xxManager.D2xxException ex) {
            ex.printStackTrace();
        }
        eni = new FTDI(this , ftD2xx);

    }

    @Override
    public void onStart() {
        super.onStart();
        eni.createDeviceList();
    }

    @Override
    public void onResume() {
        super.onResume();
        eni.DevCount = 0;
        eni.createDeviceList();
        if(eni.DevCount > 0)
        {
            eni.connectFunction();
            eni.SetConfig();
        }
    }

    @Override
    public void onStop()
    {
        eni.disconnectFunction();
        super.onStop();
    }

    public void btnSetTempEni (View v)
    {
        if (eni.DevCount == -1) {return;}
        switch (bflag) {
            case 0: //First
                prepareTempEni();
            case 1: //tempeni_start
                setTempEni(startTEMP*10);
                eni.SendMessage(0x0D);//Save
                bflag = 2;
                break;
            case 2://tempeni_end
                setTempEni(endTEMP);
                eni.SendMessage(0x0D);//Save
                bflag = 1;
        }
    }

    public byte parseTemp(char c){
        if (c=='0') return 0x09;
        int n;
        n= Character.getNumericValue(c);
        n+=9;
        n%=10;
    return (byte)n;
    }

    public void prepareTempEni() {
        eni.SendMessage(0x0E);//TAB
        eni.SendMessage(0x0B);//Mode
        eni.SendMessage(0x0B);//Mode
        eni.SendMessage(0x0B);//Mode
        eni.SendMessage(0x0A);//Shift
        eni.SendMessage(0x0A);//Shift
        eni.SendMessage(0x0A);//Shift
        eni.SendMessage(0x09);//>t<
        eni.SendMessage(0x0E);//TAB
    }

    public void setTempEni(int temp){
        xt = String.valueOf(temp);
        char c;
        int i=0;
        byte code;
        do {
            c = xt.charAt(i);
            if (Character.isDigit(c)) {
                code = parseTemp(c);
                eni.SendMessage(code);
            } else {eni.SendMessage(0x0B);/*  -  */}
            i++;
        }
        while (i<xt.length());
    }

    public Spinner Create_typespiner() {
        typespiner = findViewById(R.id.type);
        String[] type_Array = getResources().getStringArray(
                R.array.PIType);
        type_Adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                type_Array);
        typespiner.setAdapter(type_Adapter);
        typespiner
                .setOnItemSelectedListener(new typespiner_Listener());
        return typespiner;
    }

    public Spinner Create_startspiner(String type_Choice) {
        String[] start_Array = null;
        startspiner = findViewById(R.id.start);
        switch (type_Choice){
            case "Pt100":
            case "100П":
                start_Array = getResources().getStringArray(
                        R.array.PT100Start);
                break;
            case "50М":
            case "100M":
                start_Array = getResources().getStringArray(
                        R.array.M100Start);
                break;
            case "ТХА-ПИ3":
            case "ТХА-ПИ4":
                start_Array = getResources().getStringArray(
                        R.array.thaStart);

        }

        SpinnerAdapter start_Adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                start_Array);
        startspiner.setAdapter(start_Adapter);
        startspiner
                .setOnItemSelectedListener(new spiner_Listener());
        return startspiner;
    }

    public Spinner Create_endspiner(String type_Choice) {
        String[] end_Array = null;
        endspiner = findViewById(R.id.end);
        switch (type_Choice){
            case "Pt100":
            case "100П":
                end_Array = getResources().getStringArray(
                        R.array.PT100End);
                break;
            case "50М":
            case "100M":
                end_Array = getResources().getStringArray(
                        R.array.M100MEnd);
                break;
            case "ТХА-ПИ3":
            case "ТХА-ПИ4":
                end_Array = getResources().getStringArray(
                        R.array.thaEnd);
        }

        SpinnerAdapter end_Adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                end_Array);
        endspiner.setAdapter(end_Adapter);
        endspiner
                .setOnItemSelectedListener(new spiner_Listener());
        return endspiner;
    }

    public class typespiner_Listener implements
            OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> type_Adapter_View,
                                   View v, int position, long row) {
            //type_Adapter.notifyDataSetChanged();
            type_Choice = type_Adapter_View
                    .getItemAtPosition(position).toString();
            if (type_Choice.equals("ТХА-ПИ4") || type_Choice.equals("ТХА-ПИ3")) {
                    btn.setVisibility(View.VISIBLE);}
            else    btn.setVisibility(View.INVISIBLE);

            Create_startspiner(type_Choice);
            Create_endspiner(type_Choice);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    }

    public class spiner_Listener implements
            OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> Category_Add_Adapter_View,
                                   View v, int position, long row) {
            set_T_or_R_Text();
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
                result += (K[i]*Math.pow(t,i));
            result+=0.1185976*Math.exp(-0.000118343*Math.pow(t-126.9686,2));
        }
        return result;
    }
    public double TempToR (double t, String type_Choice)
    {
        double Rt = 0;

        switch (type_Choice){
            case "50М":
            case "100M":
                if (t>=0) {Rt = 100*(1+A*t);}
                else Rt = 100*(1+A*t+B*t*(t+6.7)+C*t*t*t);
                if(type_Choice.equals("50М")){Rt/=2;}
                break;
            case "Pt100":
                if (t<0){Rt = 100 * (1 + 3.9083E-3 * t + -5.775E-07 * t *t + -4.183E-12 * (t - 100)* t * t * t);}
                else Rt = 100 * (1 + 3.9083E-3 * t + -5.775E-07 * t * t);
                break;
            case "100П":
                if(t>=0){Rt = 100*(1+AA*t+BB*t*t);}
                else Rt = 100*(1+AA*t+BB*t*t+CC*(t-100)*t*t*t);
        }
        return Rt;
    }

    public void set_T_or_R_Text(){
            xt = startspiner.getSelectedItem().toString();
            xt = xt.split("°C")[0];
            startTEMP = Integer.parseInt(xt);

            xt = endspiner.getSelectedItem().toString();
            xt = xt.split("°C")[0];
            endTEMP = Integer.parseInt(xt);

            switch (type_Choice){
                case ("ТХА-ПИ4"):
                    Comp85=((1.6*85)/(Math.abs(startTEMP)+endTEMP))+0.4;
                    break;
                case ("ТХА-ПИ3"):
                    Comp85 = 1.6*TempToEDS(85)/(Math.abs(TempToEDS(startTEMP))+TempToEDS(endTEMP))+0.4;
            }

            if (type_Choice.equals("ТХА-ПИ4") || type_Choice.equals("ТХА-ПИ3")) {
                compens.setText(String.format("%.4f",Comp85)+" (+85°C)");
                starteds.setText(String.format("%.3f",TempToEDS(startTEMP))+" mV");
                endeds.setText(String.format("%.3f",TempToEDS(endTEMP))+" mV"); }
            else {
                starteds.setText(String.format("%.2f",TempToR(startTEMP,type_Choice))+" Ω");
                endeds.setText(String.format("%.2f",TempToR(endTEMP,type_Choice))+" Ω");
                compens.setText(""); }
        }
    }

