package project.anik.wens.ani_blebot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;

    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    String receivedValue = "";

    TextView peripheralTextView;

    MenuItem menu_connect;
    MenuItem menu_disconnect;

    private BluetoothDevice selectedDevice;

    BluetoothGatt bluetoothGatt;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public Map<String, String> uuids = new HashMap<String, String>();


    // new place
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<BluetoothDevice>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        selectedDevice = getIntent().getExtras().getParcelable("wholedevice");

        peripheralTextView = (TextView) findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);

        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        menu_connect = menu.findItem(R.id.menu_connect);
        menu_disconnect = menu.findItem(R.id.menu_disconnect);
        if (mConnected) {
            menu_connect.setVisible(false);
            menu_disconnect.setVisible(true);
        } else {
            menu_connect.setVisible(true);
            menu_disconnect.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                //mBluetoothLeService.connect(mDeviceAddress);
                connectToDeviceSelected();
                return true;
            case R.id.menu_disconnect:
                bluetoothGatt.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void connectToDeviceSelected() {
        Toast.makeText(this, "Trying to connect to : " + selectedDevice.getName() + "\n", Toast.LENGTH_SHORT).show();
        //peripheralTextView.append("Trying to connect to device at index: " + deviceIndexInput.getText() + "\n");
        //int deviceSelected = Integer.parseInt(deviceIndexInput.getText().toString());
        bluetoothGatt = selectedDevice.connectGatt(this, false, btleGattCallback);
    }
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation

            byte[] messageBytes = characteristic.getValue();
            String messageString = "";
            try {
                messageString = new String(messageBytes, "UTF-8");
                if (messageString.length() > 0 && messageString != receivedValue) {

                    final String st = messageString;

                    DeviceControlActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            //Toast.makeText(DeviceControlActivity.this, "device read or wrote to", Toast.LENGTH_SHORT).show();
                            mDataField.setText(st);
                        }
                    });
                    receivedValue = messageString;
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unable to convert message bytes to string");
            }
        }
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    DeviceControlActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(DeviceControlActivity.this, "device disconnected", Toast.LENGTH_SHORT).show();
                            menu_connect.setVisible(true);
                            menu_disconnect.setVisible(false);
                        }
                    });
                    break;
                case 2:
                    DeviceControlActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(DeviceControlActivity.this, "device connected", Toast.LENGTH_SHORT).show();
                            menu_connect.setVisible(false);
                            menu_disconnect.setVisible(true);
                        }
                    });
                    bluetoothGatt.discoverServices();
                    break;
                default:
                    DeviceControlActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(DeviceControlActivity.this, "we encounterned an unknown state, uh oh", Toast.LENGTH_SHORT).show();

                        }
                    });
                    break;
            }
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a 			BluetoothGatt.discoverServices() call
            DeviceControlActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //Toast.makeText(DeviceControlActivity.this, "device services have been discovered", Toast.LENGTH_SHORT).show();
                    peripheralTextView.append("device services have been discovered\n");
                }
            });

            //BluetoothGattService gattService = bluetoothGatt.getServices().
            for (BluetoothGattService gattService : bluetoothGatt.getServices()) {

                final UUID uuid = gattService.getUuid();
                String[] uuids = uuid.toString().split("-");
                if (uuids[0].contains("000fff0")) { // make your changes here to adjust with your device's UUID
                    List<BluetoothGattCharacteristic> gattCharacteristics =
                            gattService.getCharacteristics();

                    // Loops through available Characteristics.
                    for (BluetoothGattCharacteristic gattCharacteristic :
                            gattCharacteristics) {

                        final UUID charUuid = gattCharacteristic.getUuid();
                        String[] charUuids = charUuid.toString().split("-");

                        if (charUuids[0].contains("000fff1")) { // make your changes here to adjust with your device's UUID

                            DeviceControlActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    //Toast.makeText(DeviceControlActivity.this, "device services have been discovered", Toast.LENGTH_SHORT).show();
                                    mConnectionState.setText("Connected");
                                    mDataField.setText("service: " + uuid + "\n characteristics: " + charUuid);
                                    peripheralTextView.append("service: " + uuid + "\n characteristics: " + charUuid);
                                }
                            });
                            BluetoothGattCharacteristic characteristic =
                                    gatt.getService(uuid)
                                            .getCharacteristic(charUuid);
                            gatt.setCharacteristicNotification(characteristic, true);

                            BluetoothGattDescriptor descriptor =
                                    characteristic.getDescriptor(convertFromInteger(0x2902)); // it's fixed for notify

                            descriptor.setValue(
                                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                            break;
                        }
                    }
                    break;
                }
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
    };

    public UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {

        mConnectionState.setText("Connected");
        mDataField.setText(characteristic.getUuid().toString());
        System.out.println(characteristic.getUuid());
    }

}