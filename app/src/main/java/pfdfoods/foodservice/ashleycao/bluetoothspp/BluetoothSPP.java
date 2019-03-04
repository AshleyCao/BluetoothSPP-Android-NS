package pfdfoods.foodservice.ashleycao.bluetoothspp;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


public class BluetoothSPP {
    BluetoothAdapter mBluetoothAdapter;
    static BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Future future;
    CompletableFuture<String> cFuture;
    ExecutorService executor;
    byte[] buffer = new byte[64];
    //Standard SerialPortService ID
    final static UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String errorMessage = "";
    String bluetoothState;


    public BluetoothSPP(){

    }

    /**
     * Find target by device name
     * @param deviceName
     * @return
     */
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

    /**
     * check bt device is still under read data mode
     * @return
     */
    public boolean checkDeviceConnected(){
     return  executor.isShutdown();
    }

    /**
     * Get all connected device
     * @return
     */

    public Set<BluetoothDevice> getPairDevice(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        return pairedDevices;
    }


    /**
     * Get BT connection and read data stream status
     * @return
     */
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

    /**
     * Build connection and initial communication thread
     * @return
     */
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

    /**
     * Create InputStream
     */
    public void buildInputStream(){
        try{
           mmInputStream = mmSocket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate read in data into a string
     * @param buffer
     * @param size
     * @return
     */
    public String generateString(byte[] buffer, int size) {
        String scanResult = new String(buffer, 0, size);
        return scanResult;
    }

    /**
     * CompletableFuture needs API level above 24 (Android 7)
     * @param isAboveSeven
     */

    public String startReading(Boolean isAboveSeven){
        String result = "";
        if (isAboveSeven) {
            result = useCfuture();
        } else {
            result = loopThread();
        }

        return result;
    }

    /**
     * Async function to get read in string
     * For Android 5.1
     *
     * @return scanner readin data
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public String loopThread() {
        executor = Executors.newSingleThreadExecutor();
        String result = "";
        CallableResult nt = new CallableResult();
        future = executor.submit(nt);
        try {
            result = (String)future.get();
        } catch(InterruptedException | ExecutionException ie){
            ie.printStackTrace();
            result = "Can not scan this barcode";
            future.cancel(true);
        }
        return result;

    }

    /**
     * Async function to get read in string
     * For Android 7
     * @return
     */
    @TargetApi(Build.VERSION_CODES.N)
    public String useCfuture(){
        System.out.println("Use completable future");
        String result = "";
        executor = Executors.newSingleThreadExecutor();
        cFuture = CompletableFuture.supplyAsync(
                new Supplier<String>() {
                    @Override
                       public String get() {
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
                },


                executor);
        try {
            result = (String)cFuture.get();
        } catch(InterruptedException | ExecutionException ie ){
            ie.printStackTrace();
            result = "Can not scan this barcode";
            cFuture.completeExceptionally(new RuntimeException(result));
            cFuture.cancel(true);
        }

        System.out.println("Future is done " + cFuture.isDone());
        return result;
    }

    /**
     * Close async function
     * "000111" "is command" to complete
     * this will be no-blocking get
     * Android 7
     */
    @TargetApi(Build.VERSION_CODES.N)
    public void closeCFuture(boolean isLastStep) {
        if (isLastStep){
            cFuture.complete("000111");
            System.out.println("Future is done " + cFuture.isDone());
        }
        cFuture.cancel(true);
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
