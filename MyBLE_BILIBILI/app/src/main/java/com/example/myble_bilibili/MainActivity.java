package com.example.myble_bilibili;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private MyBleBlueToothPresenter myBleBlueToothPresenter;
    private Toast toast;
    private int REQUEST_CODE_PERMISSION = 1;
    private ListView view;
    private myLvAdapter mLvAdapter;
    private List<BluetoothDevice> bluetoothDevices;
    private mScanCallBack myscanCallback;
    private final static String SERVICE_SEND = "0000fff2-0000-1000-8000-00805f9b34fb";
    private final static String SERVICE_READ = "0000fff1-0000-1000-8000-00805f9b34fb";
    private BluetoothGattCharacteristic test_characteristics;
    private final static String SERVICE_READ_1 = "00002902-0000-1000-8000-00805f9b34fb";
//    private final static String SERVICE_READ = "d973f2e1-b19e-11e2-9e96-0800200c9a66";
    private mBluetoothGattCallback mGattCallBack;
    private BluetoothGatt bluetoothGatt;
    private Handler mtimeHandler = new Handler();
    private BluetoothGattCharacteristic mNeedGattChar_send;
    private BluetoothGattCharacteristic test_NeedGattChar;
    private String message1, message2, message3, message4, message5, message6, message7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        mycheckPermission();
        myBleBlueToothPresenter = new MyBleBlueToothPresenter(this);
        mGattCallBack = new mBluetoothGattCallback();
    }

    //获取参数类型
    private static String getType(Object obj){
        return obj.getClass().toString();
    }

    //字节数组转16进制字符串
    public static String bytes2HexString(byte[] b) {
        String r = "";

        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            r += hex.toUpperCase();
        }

        return r;
    }

    public static int[] ByteToHex(byte[] bytes) {
        int[] hex = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            hex[i] = (bytes[i]&0XFF);
        }
        return hex;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == myBleBlueToothPresenter.REQUEST_CODE_BLUETOOTH) {
            if (requestCode == RESULT_OK) {
                showToast("蓝牙打开成功");
            } else {
                showToast("蓝牙打开失败");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++){
            System.out.println("onRequestPermissionsResult ->" + permissions[i] + "{{}}" + grantResults[i]);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_is_soupport_blue:
                boolean Flag_supportBlueTooth = myBleBlueToothPresenter.isSupportBlueTooth();
                if(Flag_supportBlueTooth){
                    showToast("该设备支持蓝牙");
                }
                else {
                    showToast("不支持");
                }
                break;

            case R.id.menu_is_turnon_blue:
                turnonBlueTooth();
                break;

            case R.id.menu_is_send_data:
                myBleBlueToothPresenter.sendData(bluetoothGatt, mNeedGattChar_send, "START");
                break;
            case R.id.menu_is_scan_blue:
                mainscanBlueTooth();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showToast(String s){
        if (toast == null){
            toast = Toast.makeText(this, null, Toast.LENGTH_LONG);
        }
        toast.setText(s);
        toast.show();
    }

    private void turnonBlueTooth(){
        myBleBlueToothPresenter.turnOnBlueTooth();
    }

    private void mycheckPermission(){
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
        }
    }

    private void initView(){

        view = findViewById(R.id.lv_my_Listview);
    }

    private void mainscanBlueTooth(){
        myBleBlueToothPresenter.scanBlueTooth(myscanCallback);
    }

    private void initData(){
        myscanCallback = new mScanCallBack();
        bluetoothDevices = new ArrayList<>();
        mLvAdapter = new myLvAdapter();
        view.setAdapter(mLvAdapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                myBleBlueToothPresenter.stopScanBlueTooth(myscanCallback);
                BluetoothDevice b = bluetoothDevices.get(i);
                bluetoothGatt = myBleBlueToothPresenter.connectBlueTooth(b, false, mGattCallBack, 2);
            }
        });
    }

    private class myLvAdapter extends BaseAdapter {

        @Override
        public int getCount() {

            return bluetoothDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return bluetoothDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint("MissingPermission")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView textView;
            if (view == null){
                textView = new TextView(MainActivity.this);
            }else {
                textView =(TextView) view;
            }
            textView.setText(bluetoothDevices.get(i).getName() + bluetoothDevices.get(i).getAddress());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            textView.setLayoutParams(params);
            textView.setHeight(200);
            return textView;
        }
    }

    private class mScanCallBack extends ScanCallback{

        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            System.out.println("蓝牙设备：" + result.getDevice().getName());

            BluetoothDevice m = result.getDevice();
            if(!bluetoothDevices.contains(m)){
                bluetoothDevices.add(m);
                mLvAdapter.notifyDataSetChanged();
            }

            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    }

    private class mBluetoothGattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            System.out.println("onConnectionStateChange");
            System.out.println("蓝牙连接了");
            super.onConnectionStateChange(gatt, status, newState);
            mtimeHandler.postDelayed(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    bluetoothGatt.discoverServices();
                }
            },1000);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            System.out.println("bluegett" + bluetoothGatt + "gatt" + gatt);
            List<BluetoothGattService> servers = bluetoothGatt.getServices();
            for (int i = 0; i < servers.size(); i++) {
                BluetoothGattService b  = servers.get(i);
                System.out.println( i +"服务是" + b);
                List<BluetoothGattCharacteristic> characteristics = b.getCharacteristics();
                for (int t = 0; t < characteristics.size(); t++) {
                    test_characteristics = characteristics.get(t);
                    if (test_characteristics.getUuid().toString().equals(SERVICE_READ)){
                        System.out.println("*******0000fff1-0000-1000-8000-00805f9b34fb存在*******");
                        break;
                    }
                }
                for (int j = 0; j < characteristics.size(); j++) {
                    BluetoothGattCharacteristic cur_characteristics = characteristics.get(j);
//                    System.out.println("UUID:" + cur_characteristics.getUuid().toString());
//                    System.out.println(i + "个服务" + j + "个" + cur_characteristics);

                    if (cur_characteristics.getUuid().toString().equals(SERVICE_SEND)) {
                        System.out.println("我找到了我所需要的特征了");
                        mNeedGattChar_send = cur_characteristics;
                        test_NeedGattChar = test_characteristics;
                        System.out.println("1");
                        bluetoothGatt.setCharacteristicNotification(mNeedGattChar_send,true);
                        System.out.println("2");
                        List<BluetoothGattDescriptor> descriptors = test_NeedGattChar.getDescriptors();
                        System.out.println( "长度是："+descriptors.size());
                        //showToast("长度是："+descriptors.size());
//                        if (cur_characteristics.getUuid().toString().equals(SERVICE_SEND_1)){
//                            System.out.println("我找到了我所需要的特征了************");
//                            test_NeedGattChar = cur_characteristics;
//                            bluetoothGatt.setCharacteristicNotification(test_NeedGattChar,true);
//                            List<BluetoothGattDescriptor> descriptors_test = test_NeedGattChar.getDescriptors();
//                            System.out.println( "长度是："+descriptors_test.size());
//                            BluetoothGattDescriptor clientConfig = test_NeedGattChar.getDescriptor(UUID.fromString(SERVICE_READ));
//                            if (clientConfig == null){
//                                System.out.println("**************clientconfig==null!!!!!*************");
//                            }
//                        }else {
//                            System.out.println("test失败");
//                        }

//                        for (BluetoothGattDescriptor bluetoothGattDescriptor: mNeedGattChar_send.getDescriptors()){
//                            System.out.println("开始了");
//                            System.out.println("bluetoothGattDescriptor"+ bluetoothGattDescriptor.getUuid().toString());
//                        }
                        mtimeHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                bluetoothGatt.setCharacteristicNotification(test_NeedGattChar,true);
                                BluetoothGattDescriptor clientConfig = test_NeedGattChar.getDescriptor(UUID.fromString(SERVICE_READ_1));
//                        if (clientConfig == null){
//                            clientConfig = new BluetoothGattDescriptor(
//                                    UUID.fromString(SERVICE_READ),
//                                    BluetoothGattDescriptor.PERMISSION_WRITE
//                            );
//                        }
                                //System.out.println("hahahahaaha"+clientConfig);
                                clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                bluetoothGatt.writeDescriptor(clientConfig);
                                //bluetoothGatt.setCharacteristicNotification(test_NeedGattChar,true);
                            }
                        },1000);
                        /**
                        BluetoothGattDescriptor clientConfig = test_NeedGattChar.getDescriptor(UUID.fromString(SERVICE_READ_1));
//                        if (clientConfig == null){
//                            clientConfig = new BluetoothGattDescriptor(
//                                    UUID.fromString(SERVICE_READ),
//                                    BluetoothGattDescriptor.PERMISSION_WRITE
//                            );
//                        }
                        //System.out.println("hahahahaaha"+clientConfig);
                        clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        bluetoothGatt.writeDescriptor(clientConfig);
//                        System.out.println("hahahahaaha"+clientConfig);
//                        clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                        bluetoothGatt.writeDescriptor(clientConfig);
//                        for (int k = 0; k < descriptors.size(); k++) {
//                            System.out.println("第" + i +"个服务"+ "第" + j + "个特征值" + k+ "个客户端配置"+ cur_characteristics.getUuid());
//                        }
                         */
                    }else {
                        System.out.println("没找到");
                    }

//                    if(cur_characteristics.getUuid().toString().equals(SERVICE_READ)){
//                        mNeedGattChar_read = cur_characteristics;
//                        bluetoothGatt.setCharacteristicNotification(mNeedGattChar_read,true);
//                    }else {
//                        System.out.println("没找到");
//                    }
//                    mtimeHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            BluetoothGattDescriptor clientConfig = mNeedGattChar_send.getDescriptor(UUID.fromString(SERVICE_READ));
//                            if (clientConfig != null){
//                                System.out.println("clientconfig 不为空！");
//                                clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                                bluetoothGatt.writeDescriptor(clientConfig);
//                            }else {
//                                System.out.println("clientconfig 为空！");
//                            }
//                        }
//                    }, 500);
                }
            }
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //showToast("1234");
            byte[] value = characteristic.getValue();
            int[] test_val = ByteToHex(value);
            if (test_val[0] == 0XFF ){
                String str1 = "心率-->" + test_val[1];
                message1 = str1;
                System.out.println(str1);
                String str2 = "血氧浓度->" + test_val[2];
                message2 = str2;
                System.out.println(str2);
                String str3 = "收缩压->" + test_val[3];
                message3 = str3;
                System.out.println(str3);
                String str4 = "舒张压->" + test_val[4];
                message4 = str4;
                System.out.println(str4);
                String str5 = "脉率->" + test_val[5];
                message5 = str5;
                System.out.println(str5);
                String str6 = "温度高位->" + test_val[6];
                message6 = str6;
                System.out.println(str6);
//                System.out.println("数据6的类型是->" + getType(str6));
                String str7 = "温度低位->" + test_val[7];
                message7 = str7;
                System.out.println(str7);
//                int wd_h = Integer.parseInt(str6);
//                int wd_l = Integer.parseInt(str7);
//                int wd = ((wd_h*256+wd_l)/256);
//               String wd_str = Integer.toString(wd);
//                System.out.println("真正的温度是->" + wd);

            }else {
                System.out.println("不是以FF开头");
                String str1 = "心率-->" + test_val[1];
                message1 = str1;
                System.out.println(str1);
                String str2 = "血氧浓度->" + test_val[2];
                message2 = str2;
                System.out.println(str2);
                String str3 = "收缩压->" + test_val[3];
                message3 = str3;
                System.out.println(str3);
                String str4 = "舒张压->" + test_val[4];
                message4 = str4;
                System.out.println(str4);
                String str5 = "脉率->" + test_val[5];
                message5 = str5;
                System.out.println(str5);
                String str6 = "温度高位->" + test_val[6];
                message6 = str6;
                System.out.println(str6);
//                System.out.println("数据6的类型是->" + getType(str6));
                String str7 = "温度低位->" + test_val[7];
                message7 = str7;
                System.out.println(str7);
            }
            String test_value = bytes2HexString(value);
            System.out.println("******-->" + test_value);
            System.out.println("数据1类型是：" + getType(value));
            String stext = new String(value);
            String stext_test = stext.substring(1);
//            String st = stext_test.replace("m", " ");
//            System.out.println("新的字符串->" + st);
//            String[] strings = st.split(" ");
//            for (int i = 0; i < strings.length; i++) {
//                System.out.println("第"+i+"个数据");
//                System.out.println(strings[i]);
//
//            }
            System.out.println("输出的数据是：" + stext);
            System.out.println("数据2类型是：" + getType(stext));
//            showToast(stext);
//            Toast.makeText(MainActivity.this, test_value,Toast.LENGTH_LONG).show();
           //System.out.println(characteristic.getValue()+"哈哈哈哈啊哈");
            Intent intent = new Intent(MainActivity.this, UIActivity.class);
            intent.putExtra("message1", message1);
            intent.putExtra("message2", message2);
            intent.putExtra("message3", message3);
            intent.putExtra("message4", message4);
            intent.putExtra("message5", message5);
            intent.putExtra("message6", message6);
            intent.putExtra("message7", message7);
            startActivity(intent);
            super.onCharacteristicChanged(gatt, characteristic);
        }
    }
}