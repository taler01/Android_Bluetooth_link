package com.example.myble_bilibili;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class MyBleBlueToothPresenter {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Activity activity;
    public static final int REQUEST_CODE_BLUETOOTH = 0;

    public MyBleBlueToothPresenter(Activity activity) {
//        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        获取蓝牙适配器(方法一)
       BluetoothManager BleBlueMg = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
       this.bluetoothAdapter = BleBlueMg.getAdapter();
       this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
       this.activity = activity;
    }

    public boolean isSupportBlueTooth(){
        if (bluetoothAdapter == null){
            return false;
        }
        return true;
    }

    public boolean isOpenBlueTooth(){
        return bluetoothAdapter.isEnabled();
    }

    public void turnOnBlueTooth(){
       Intent intent =  new Intent();
       intent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
       this.activity.startActivityForResult(intent, REQUEST_CODE_BLUETOOTH);
    }

    @SuppressLint("MissingPermission")
    public void turnoffBlueTooth(){
        bluetoothAdapter.disable();
    }

    @SuppressLint("MissingPermission")
    public void scanBlueTooth(ScanCallback scanCallBack){
        bluetoothLeScanner.startScan(scanCallBack);
    }

    @SuppressLint("MissingPermission")
    public void stopScanBlueTooth(ScanCallback scanCallback){
        bluetoothLeScanner.stopScan(scanCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    public BluetoothGatt connectBlueTooth(BluetoothDevice bluetoothDevice, boolean autoConnect,
                                 BluetoothGattCallback bluetoothGattCallback, int transport){
       return bluetoothDevice.connectGatt(this.activity, autoConnect, bluetoothGattCallback,
               transport, BluetoothDevice.PHY_LE_1M_MASK);
    }

    @SuppressLint("MissingPermission")
    public void sendData(BluetoothGatt bgatt, BluetoothGattCharacteristic charte, String text){
        charte.setValue(text);
        bgatt.writeCharacteristic(charte);
    }

    public void receiveData(){

    }

}
