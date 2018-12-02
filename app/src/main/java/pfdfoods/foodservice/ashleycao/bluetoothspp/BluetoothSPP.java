package pfdfoods.foodservice.ashleycao.bluetoothspp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class BluetoothSPP {
    BluetoothAdapter mBluetoothAdapter;
    static BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Future future;
    ExecutorService executor;
    byte[] buffer = new byte[64];
    final String targetName = "Rename";
    //Standard SerialPortService ID
    final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String errorMessage = "";
    String bluetoothState;


    public BluetoothSPP(){

    }

    public String findBT(String deviceName)
    {
        String deviceMessage = "find device";
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
           deviceMessage = "No bluetooth adapter available";
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(deviceName)) {
                    mmDevice = device;
                    break;
                }
            }

        } else {
            deviceMessage = "Cannot find any connected device";
        }
        return deviceMessage;
    }

    public boolean checkDeviceConnected(){
     return  executor.isShutdown();
    }

    public Set<BluetoothDevice> getPairDevice(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        return pairedDevices;
    }


    public String checkConnected(){
        String readyToreadin = "";
        try {
            if (mmSocket.isConnected()) {
                if (mmInputStream != null) {
                } else {
                    readyToreadin = "inputStream is empty";
                }
            } else {
                readyToreadin = "socket exists but not connected";
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return readyToreadin;
    }

    public boolean openBT()
    {
        boolean connectedResult = true;
        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
        } catch (IOException e){
            connectedResult = false;
            errorMessage = e.toString();
        }
        return connectedResult;
    }


    public void buildInputStream(){
        try{
           mmInputStream = mmSocket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String generateString(byte[] buffer, int size) {
        String scanResult = new String(buffer, 0, size);
        return scanResult;
    }


    public String loopThread() {
        executor = Executors.newSingleThreadExecutor();
        String result = "";
        CallableResult nt = new CallableResult();
        future = executor.submit(nt);
        try {
            result = (String)future.get();
        } catch (Exception var5) {
            var5.printStackTrace();
            result = "Can not scan this barcode";
            future.cancel(true);
        }
        return result;

    }


    public void closeReader() {
        future.cancel(true);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        System.out.println("Connection Closed");
    }

    public void closeConnection(){
        try {
            mmInputStream.close();
            mmSocket.close();
        } catch(Exception e ){

        }
    }

    public class CallableResult implements Callable<String> {

        public CallableResult() {

        }
        public String call() throws Exception {
            String rest = "000111";

            try {
                if(mmInputStream == null){
                    return null;
                }


                int size = mmInputStream.read(buffer);
                if (size > 0) {
                    rest = generateString(buffer, size);
                } else {

                }
            } catch (IOException var4) {
                var4.printStackTrace();
            }

            return rest;
        }
    }

}
