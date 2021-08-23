package com.xevius.tabletki;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class FTDI {

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

	public static final int readLength = 512;
    public int readcount = 0;
    public int iavailable = 0;
    byte[] readData;
    char[] readDataToText;

    boolean uart_configured = false;

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
		//bReadThreadGoing = false;
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
		}
		else 
		{			
			Toast.makeText(DeviceUARTContext, "open device port(" + tmpProtNumber + ") NG", Toast.LENGTH_LONG).show();
		}
	}

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

	public void SendMessage(int code) {

		if (ftDev.isOpen() == false) {
			Log.e("j2xx", "SendMessage: device not open");
			return;
		}

		ftDev.setLatencyTimer((byte) 16);

		int crc = 0;
		byte [] OutData = {(byte)0xCA,0x35,0x19,0x00,(byte)code,(byte) 0xF6,
				(byte) 0xCB,0x00,(byte) 0xD0,0x20,0x18,(byte)crc};
		int[] OutDataI = new int[OutData.length];
		for (int i=0;i<OutData.length; OutDataI[i] = OutData [i++]);

		crc = IntStream.of(OutDataI).sum();
		crc = crc & 0xff;
		crc = 256- crc;
		OutData[11] = (byte) crc;

		ftDev.write(OutData, OutData.length);
		//PAUSE
		try {
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

}

