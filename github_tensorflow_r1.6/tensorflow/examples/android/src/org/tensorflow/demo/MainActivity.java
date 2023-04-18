/*
 * MIT License
 *
 * Copyright (c) 2015 Douglas Nassif Roma Junior
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.tensorflow.demo;

import android.app.Service;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Binder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import org.tensorflow.demo.BluetoothDeviceDecorator;
import org.tensorflow.demo.BluetoothService;
import org.tensorflow.demo.BluetoothStatus;
import org.tensorflow.demo.BluetoothWriter;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Service implements BluetoothService.OnBluetoothScanCallback, BluetoothService.OnBluetoothEventCallback, DeviceItemAdapter.OnAdapterItemClickListener {

    /*
     * Change for the UUID that you want.
     */
    private static final UUID UUID_DEVICE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_SERVICE = UUID.fromString("e7810a71-73ae-499d-8c15-faa9aef0c3f2");
    private static final UUID UUID_CHARACTERISTIC = UUID.fromString("bef8d6c9-9c21-4c9e-b632-bd58c1009f9f");

    private final IBinder mBinder = new MyBinder();

    public static final String TAG = "BluetoothExample";

    private ProgressBar pgBar;
    private Menu mMenu;
    private RecyclerView mRecyclerView;
    private DeviceItemAdapter mAdapter;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothService mService;
    private boolean mScanning;


    private BluetoothWriter mWriter;

    public class MyBinder extends Binder {
        MainActivity getService() {
            // Return this instance of LocalService so clients can call public methods
            return MainActivity.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        BluetoothConfiguration config = new BluetoothConfiguration();

        config.bluetoothServiceClass = BluetoothClassicService.class; //  BluetoothClassicService.class or BluetoothLeService.class

        config.context = getApplicationContext();
        config.bufferSize = 1024;
        config.characterDelimiter = '\n';
        config.deviceName = "Bluetooth Sample";
        config.callListenersInMainThread = true;

        //config.uuid = null; // When using BluetoothLeService.class set null to show all devices on scan.
        config.uuid = UUID_DEVICE; // For Classic

        config.uuidService = UUID_SERVICE; // For BLE
        config.uuidCharacteristic = UUID_CHARACTERISTIC; // For BLE
        config.transport = BluetoothDevice.TRANSPORT_LE; // Only for dual-mode devices

        // For BLE
        config.connectionPriority = BluetoothGatt.CONNECTION_PRIORITY_HIGH; // Automatically request connection priority just after connection is through.
        //or request connection priority manually, mService.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);

        BluetoothService.init(config);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            /*Snackbar.make(view,"Bluetooth not available",Snackbar.)
                    .setAction("Action",null).show();*/
            // Device doesn't support Bluetooth
        }
        int REQUEST_ENABLE_BT = 1;
        //REQUEST_ENABLE_BT = 1;
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }

//        pgBar = (ProgressBar) findViewById(R.id.pg_bar);
//        pgBar.setVisibility(View.GONE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

//        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
//        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//        mRecyclerView.setLayoutManager(lm);

        /*mAdapter = new DeviceItemAdapter(this, mBluetoothAdapter.getBondedDevices());
        mAdapter.setOnAdapterItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);*/

        mService = BluetoothService.getDefaultInstance();
        mWriter = new BluetoothWriter(mService);

        mService.setOnScanCallback(this);
        mService.setOnEventCallback(this);

/*        BluetoothDeviceDecorator device = ";
        mService.connect(device.getDevice());*/

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (deviceName.equals("ArcBotics")){
                    mService.connect(device);
                }
            }
        }

        //mWriter.writeln(new String("f"));  //jerry: mWriter is null,

    }

/*    @Override
    protected void onResume() {
        super.onResume();
        mService.setOnEventCallback(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_scan) {
            startStopScan();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startStopScan() {
        if (!mScanning) {
            mService.startScan();
        } else {
            mService.stopScan();
        }
    }*/

    @Override
    public void onDeviceDiscovered(BluetoothDevice device, int rssi) {
        Log.d(TAG, "onDeviceDiscovered: " + device.getName() + " - " + device.getAddress() + " - " + Arrays.toString(device.getUuids()));
        BluetoothDeviceDecorator dv = new BluetoothDeviceDecorator(device, rssi);
        int index = mAdapter.getDevices().indexOf(dv);
        if (index < 0) {
            mAdapter.getDevices().add(dv);
            mAdapter.notifyItemInserted(mAdapter.getDevices().size() - 1);
        } else {
            mAdapter.getDevices().get(index).setDevice(device);
            mAdapter.getDevices().get(index).setRSSI(rssi);
            mAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void onStartScan() {
        Log.d(TAG, "onStartScan");
        mScanning = true;
        pgBar.setVisibility(View.VISIBLE);
        mMenu.findItem(R.id.action_scan).setTitle(R.string.action_stop);
    }

    @Override
    public void onStopScan() {
        Log.d(TAG, "onStopScan");
        mScanning = false;
        pgBar.setVisibility(View.GONE);
        mMenu.findItem(R.id.action_scan).setTitle(R.string.action_scan);
    }

    @Override
    public void onDataRead(byte[] buffer, int length) {
        Log.d(TAG, "onDataRead");
    }


    public void onStatusChange(BluetoothStatus status) {
        Log.d(TAG, "onStatusChange: " + status);
        Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show();

        if (status == BluetoothStatus.CONNECTED) {
            mWriter.writeln(new String("f"));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mWriter.writeln(new String("b"));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mWriter.writeln(new String("s"));
            int x1 = 1;
            /*CharSequence colors[] = new CharSequence[]{"Try text", "Try picture"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select");
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        startActivity(new Intent(MainActivity.this, DeviceActivity.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, BitmapActivity.class));
                    }
                }
            });
            builder.setCancelable(false);
            builder.show();*/
        }

    }

    @Override
    public void onDeviceName(String deviceName) {
        Log.d(TAG, "onDeviceName: " + deviceName);
    }

    @Override
    public void onToast(String message) {
        Log.d(TAG, "onToast");
    }

    @Override
    public void onDataWrite(byte[] buffer) {
        Log.d(TAG, "onDataWrite");
    }

    @Override
    public void onItemClick(BluetoothDeviceDecorator device, int position) {
        mService.connect(device.getDevice());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
