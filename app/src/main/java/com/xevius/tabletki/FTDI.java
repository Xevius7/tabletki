package com.xevius.tabletki;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class FTDI {
	
	
	/*final Handler handler =  new Handler()
    {
    	@Override
    	public void handleMessage(Message msg)
    	{
    		if(iavailable > 0)
    		{
    			//readText.append(String.copyValueOf(readDataToText, 0, iavailable));

    		}
    	}
    };*/

    
    static Context DeviceUARTContext;
	D2xxManager ftdid2xx;
	FT_Device ftDev = null;
	int DevCount = -1;
    int currentIndex = -1;
    int openIndex = 0;
	

    /*local variables*/
    int baudRate = 19200;
    byte stopBit = D2xxManager.FT_STOP_BITS_2;
    byte dataBit = D2xxManager.FT_DATA_BITS_8;
    byte parity = D2xxManager.FT_PARITY_NONE;
    byte flowControl = D2xxManager.FT_FLOW_NONE;
    int portNumber = 1;
    //ArrayList<CharSequence> portNumberList;
	
	public static final int readLength = 512;
    public int readcount = 0;
    public int iavailable = 0;
    byte[] readData;
    char[] readDataToText;
    public boolean bReadThreadGoing = false;
    public readThread read_thread;

    boolean uart_configured = false;
	static int iEnableReadFlag = 1;
	
	public FTDI (){}
	
	public FTDI (Context parentContext, D2xxManager ftdid2xxContext)
		{
			DeviceUARTContext = parentContext;
			ftdid2xx = ftdid2xxContext;
		}

	public void createDeviceList()
	{
		int tempDevCount = ftdid2xx.createDeviceInfoList(DeviceUARTContext);

		if (tempDevCount > 0)
		{
			if( DevCount != tempDevCount )
			{
				DevCount = tempDevCount;
				//updatePortNumberSelector();
			}
		}
		else
		{
			DevCount = -1;
			currentIndex = -1;
		}
	}
	
	public void disconnectFunction()
	{
		DevCount = -1;
		currentIndex = -1;
		bReadThreadGoing = false;
		try {
			Thread.sleep(50);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		if(ftDev != null)
		{
			synchronized(ftDev)
			{
				if( true == ftDev.isOpen())
				{
					ftDev.close();
				}
			}
		}
	}

	public void connectFunction()
	{
		int tmpProtNumber = openIndex + 1;

		if( currentIndex != openIndex )
		{
			if(null == ftDev)
			{
				ftDev = ftdid2xx.openByIndex(DeviceUARTContext, openIndex);
			}
			else
			{
				synchronized(ftDev)
				{
					ftDev = ftdid2xx.openByIndex(DeviceUARTContext, openIndex);
				}
			}
			uart_configured = false;
		}
		else
		{
			Toast.makeText(DeviceUARTContext,"Device port " + tmpProtNumber + " is already opened",Toast.LENGTH_LONG).show();
			return;
		}

		if(ftDev == null)
		{
			Toast.makeText(DeviceUARTContext,"open device port("+tmpProtNumber+") NG, ftDev == null", Toast.LENGTH_LONG).show();
			return;
		}

		if (true == ftDev.isOpen())
		{
			currentIndex = openIndex;
			Toast.makeText(DeviceUARTContext, "open device port(" + tmpProtNumber + ") OK", Toast.LENGTH_SHORT).show();

			if(false == bReadThreadGoing)
			{
				//read_thread = new readThread(handler);
				//read_thread.start();
				//bReadThreadGoing = true;
			}
		}
		else 
		{			
			Toast.makeText(DeviceUARTContext, "open device port(" + tmpProtNumber + ") NG", Toast.LENGTH_LONG).show();
			//Toast.makeText(DeviceUARTContext, "Need to get permission!", Toast.LENGTH_SHORT).show();			
		}
	}
	
	/*public void updatePortNumberSelector()
	{
		//Toast.makeText(DeviceUARTContext, "updatePortNumberSelector:" + DevCount, Toast.LENGTH_SHORT).show();

		if(DevCount == 2)
		{
			portAdapter = ArrayAdapter.createFromResource(DeviceUARTContext, R.array.port_list_2,
														  R.layout.my_spinner_textview);
			portAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
			portSpinner.setAdapter(portAdapter);
			portAdapter.notifyDataSetChanged();
			Toast.makeText(DeviceUARTContext, "2 port device attached", Toast.LENGTH_SHORT).show();
			//portSpinner.setOnItemSelectedListener(new MyOnPortSelectedListener());
		}
		else if(DevCount == 4)
		{
			portAdapter = ArrayAdapter.createFromResource(DeviceUARTContext, R.array.port_list_4,
														  R.layout.my_spinner_textview);
			portAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
			portSpinner.setAdapter(portAdapter);
			portAdapter.notifyDataSetChanged();
			Toast.makeText(DeviceUARTContext, "4 port device attached", Toast.LENGTH_SHORT).show();
			//portSpinner.setOnItemSelectedListener(new MyOnPortSelectedListener());
		}
		else
		{
			portAdapter = ArrayAdapter.createFromResource(DeviceUARTContext, R.array.port_list_1,
														  R.layout.my_spinner_textview);
			portAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
			portSpinner.setAdapter(portAdapter);
			portAdapter.notifyDataSetChanged();	
			Toast.makeText(DeviceUARTContext, "1 port device attached", Toast.LENGTH_SHORT).show();
			//portSpinner.setOnItemSelectedListener(new MyOnPortSelectedListener());
		}

	}*/
	
	public void SetConfig()
	{
		if (ftDev.isOpen() == false) {
			Log.e("j2xx", "SetConfig: device not open");
			return;
		}
		ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
		ftDev.setBaudRate(baudRate);
		ftDev.setDataCharacteristics(dataBit, stopBit, parity);
		ftDev.setFlowControl(flowControl, (byte) 0x0b, (byte) 0x0d);
		uart_configured = true;
		Toast.makeText(DeviceUARTContext, "Config done", Toast.LENGTH_SHORT).show();
	}

	public void EnableRead (){
    	iEnableReadFlag = (iEnableReadFlag + 1)%2;

		if(iEnableReadFlag == 1) {
			ftDev.purge((byte) (D2xxManager.FT_PURGE_TX));
			ftDev.restartInTask();
			//readEnButton.setText("Read Enabled");
		}
		else{
			ftDev.stopInTask();
			//readEnButton.setText("Read Disabled");
		}
    }

	public void SendMessage(int code) {

		if (ftDev.isOpen() == false) {
			Log.e("j2xx", "SendMessage: device not open");
			return;
		}

		ftDev.setLatencyTimer((byte) 16);
//		ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX))*/

		int crc = 0;
		byte [] OutData = {(byte)0xCA,0x35,0x19,0x00,(byte)code,(byte) 0xF6,
				(byte) 0xCB,0x00,(byte) 0xD0,0x20,0x18,(byte)crc};
		int[] OutDataI = new int[OutData.length];
		for (int i=0;i<OutData.length; OutDataI[i] = OutData [i++]);

		crc = IntStream.of(OutDataI).sum();
		crc = crc & 0xff;
		crc = 256- crc;
		OutData[11] = (byte) crc;

		//ftDev.write(OutData, OutData.length);
		//PAUSE
		try {
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

	private class readThread  extends Thread
	{
		Handler mHandler;

		readThread(Handler h){
			mHandler = h;
			this.setPriority(Thread.MIN_PRIORITY);
		}

		@Override
		public void run()
		{
			int i;
			while(true == bReadThreadGoing)
			{
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}

				synchronized(ftDev)
				{
					iavailable = ftDev.getQueueStatus();				
					if (iavailable > 0) {
						if(iavailable > readLength){
							iavailable = readLength;
						}
						ftDev.read(readData, iavailable);

						/*for (i = 0; i < iavailable; i++) {
							readDataToText[i] = (char) readData[i];*/
						//}
						//Message msg = mHandler.obtainMessage();
						//mHandler.sendMessage(msg);
					}
				}
			}
		}

		
	}
}

