package com.example.nearbyreceive;




import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.SimpleArrayMap;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.BitmapKt;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CpuUsageInfo;
import android.os.HardwarePropertiesManager;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.security.identity.ResultData;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionOptions;
import com.google.android.gms.nearby.connection.ConnectionType;
import com.google.android.gms.nearby.connection.BandwidthInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;


import android.os.SystemClock;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private static final String[] REQUIRED_PERMISSIONS;
    public String DeviceEndPointID;
    public String transfer = "File Received!!";
    DeviceAdapter adapter;
    int insertIndex = 1;
    int taskDevice = 0;
    int second = 0;
    private int currentDelegate = 1;
    public long totalTime;
    public Boolean mainBusy = false;
    //private Map<String,String> DeviceMap = new HashMap<>();
    List<NDevice> devices = new ArrayList<>();

    Context context;


    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            REQUIRED_PERMISSIONS =
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE

                    };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            REQUIRED_PERMISSIONS =
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    };
        } else {
            REQUIRED_PERMISSIONS =
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    };
        }
    }

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    private static final String TAG = "RockPaperScissors";
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    TextView textView;

    private static final int READ_REQUEST_CODE = 42;
    private static final String ENDPOINT_ID_EXTRA = "com.foo.myapp.EndpointId";

    public static final String SERVICE_ID = "120001";
    public long time1 = 0;
    public long time2 = 0;

    ImageView imageView;


    private SignRecognition signRecognition;
    private ConnectionsClient connectionsClient;
    private ConnectionType connectionType;
    private BandwidthInfo bandwidthInfo;
    private ConnectionOptions connectionOptions;
    Queue<Long> queue = new PriorityQueue<Long>();
    private List<String> listOfResults = new ArrayList<>();
    private List<Integer> displayedSignClass = new ArrayList<>();


    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                private final SimpleArrayMap<Long, Payload> incomingFilePayloads = new SimpleArrayMap<>();
                private final SimpleArrayMap<Long, Payload> completedFilePayloads = new SimpleArrayMap<>();
                private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();


                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    if (payload.getType() == Payload.Type.BYTES) {
                        String payloadFilenameMessage = new String(payload.asBytes(), StandardCharsets.UTF_8);
                        Log.i(TAG, "Bytes received are:" + payloadFilenameMessage);
                        if (payloadFilenameMessage.substring(0, 6).equals("Result")) {
                            for (NDevice d : devices) {
                                if (d.endPointId.equals(endpointId)) {
                                    d.sentFrameTime = System.currentTimeMillis() - d.sentFrameTime;
                                    d.frameProcessingPower = Math.round(1000 / d.sentFrameTime);
                                    d.isBusy = false;
                                    Log.i(TAG, "Processing power for" + d.name + " " + d.endPointId + "is" + d.frameProcessingPower);
                                    Log.i(TAG, "Sent frame time is:" + d.sentFrameTime);
                                }
                            }
                            schedule();
                        } else if (payloadFilenameMessage.substring(0, 7).equals("Battery")) {
                            String[] splitArr = payloadFilenameMessage.split(":");
                            for (NDevice d : devices) {
                                if (d.endPointId.equals(endpointId)) {
                                    d.battery = Integer.parseInt(splitArr[1]);

                                }
                            }
                        } else if (payloadFilenameMessage.substring(0, 9).equals("PhoneData")) {
                            String[] splitArr = payloadFilenameMessage.split(":");
                            for (NDevice d : devices) {
                                if (d.endPointId.equals(endpointId)) {
                                    d.cores = Integer.parseInt(splitArr[1]);
                                    d.cpu = Integer.parseInt(splitArr[2]);
                                }
                            }
                        } else {
                            long payloadId = addPayloadFilename(payloadFilenameMessage);
                            checkMainDevice();
                            //processFilePayload(payloadId);

                        }

                    } else if (payload.getType() == Payload.Type.FILE) {
                        // Add this to our tracking map, so that we can retrieve the payload later.
                        incomingFilePayloads.put(payload.getId(), payload);
                    }
                }


                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                        long payloadId = update.getPayloadId();
                        Payload payload = incomingFilePayloads.remove(payloadId);
                        completedFilePayloads.put(payloadId, payload);
                        if (payload != null && payload.getType() == Payload.Type.FILE) {
                            Log.i(TAG, "payload transfer complete with id:" + filePayloadFilenames.get(payloadId));
                            queue.add(payloadId);
                            schedule();
                            //processFilePayload(payloadId);
                            totalTime = SystemClock.uptimeMillis();
                            checkMainDevice();
                        }
                    }
                }


                private NDevice scheduleFirstDevice() {

                    NDevice tempDevice = new NDevice("", "", true, 0, 0, false, 0, 0, 0);
                    for (NDevice nDevice : devices) {
                        // find a device that is available
                        if (nDevice.isWorker && !nDevice.isBusy ) {
                            if (tempDevice.endPointId.isEmpty()) {
                                tempDevice = nDevice;
                            } else {
                                // change device based on core count
                                if (nDevice.cores > tempDevice.cores) {
                                    //battery check
                                    if (nDevice.battery >= 20) {
                                        tempDevice = nDevice;
                                    }
                                }
                                //if same cores, than we check based on maxCpuFrequency
                                if (nDevice.cores == tempDevice.cores) {
                                    if (nDevice.cpu > tempDevice.cpu) {
                                        if (nDevice.battery >= 20) {
                                            tempDevice = nDevice;
                                        }
                                    }
                                    //if same frequency, than check battery
                                    if (nDevice.cpu == tempDevice.cpu) {
                                        if (nDevice.battery > tempDevice.battery) {
                                            tempDevice = nDevice;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return tempDevice;

                }

                public void schedule() {
                        NDevice bestDeviceChoice = scheduleFirstDevice();

                        if(!bestDeviceChoice.name.equals("MainDevice") && !bestDeviceChoice.endPointId.isEmpty()){
                            bestDeviceChoice.sentFrameTime = System.currentTimeMillis();
                            Log.i(TAG, "Time sent is :" + bestDeviceChoice.sentFrameTime);
                            if (queue.size() > 0) {
                                Payload payload = completedFilePayloads.get(queue.remove());
                                connectionsClient.sendPayload(bestDeviceChoice.endPointId, payload);
                                bestDeviceChoice.isBusy = true;
                                Log.i(TAG, "Payload sent " + filePayloadFilenames.get(payload.getId()) + "sent to device" + bestDeviceChoice.name + "With battery" + bestDeviceChoice.battery);
                            }
                        } else {
                            bestDeviceChoice.isBusy = true;
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    if(queue.size()>1){
                                        processFilePayload(queue.remove());
                                    }
                                }
                            });
                    }

                }


                private long addPayloadFilename(String payloadFilenameMessage) {
                    String[] parts = payloadFilenameMessage.split(":");
                    long payloadId = Long.parseLong(parts[0]);
                    String filename = parts[1];
                    filePayloadFilenames.put(payloadId, filename);
                    return payloadId;
                }

                private String processFilePayload(long payloadId) {
                    // BYTES and FILE could be received in any order, so we call when either the BYTES or the FILE
                    // payload is completely received. The file payload is considered complete only when both have
                    // been received.
                    mainBusy = true;
                    Payload filePayload = completedFilePayloads.get(payloadId);
                    String filename = filePayloadFilenames.get(payloadId);
                    if (filePayload != null) {
                        ParcelFileDescriptor img = filePayload.asFile().asParcelFileDescriptor();
                        Log.i(TAG, "file payload is not null");
                        FileDescriptor fd = img.getFileDescriptor();
                        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd);

                        Mat image = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4); // rgb
                        Utils.bitmapToMat(bitmap, image);
                        listOfResults.clear();
                        image = signRecognition.detectionImage(image, listOfResults, displayedSignClass);
                        Log.i(TAG,listOfResults.get(0));

                        Log.i(TAG, "Main device called Process payload file checker method for " + payloadId + "," + filename);
                        mainBusy = false;

                    } else {
                        Log.i(TAG, "Null file payload" + payloadId + "," + filename);
                    }
                    return "";

                }
            };

                private final EndpointDiscoveryCallback endpointDiscoveryCallback =
                        new EndpointDiscoveryCallback() {
                            @Override
                            public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                                Log.i(TAG, "onEndpointFound: endpoint found, connecting to ");
                                textView.setText("Send Connection request");
                                DeviceEndPointID = endpointId;
                                connectionsClient.requestConnection("1", endpointId, connectionLifecycleCallback, connectionOptions);
                            }

                            @Override
                            public void onEndpointLost(String endpointId) {
                                Log.i(TAG, "Connection lost with endpoint id" + endpointId);
                            }
                        };

                private final ConnectionLifecycleCallback connectionLifecycleCallback =
                        new ConnectionLifecycleCallback() {
                            @Override
                            public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                                Log.i(TAG, "onConnectionInitiated: accepting connection" + connectionInfo.getEndpointName());
                                connectionsClient.acceptConnection(endpointId, payloadCallback);
                                String deviceName = connectionInfo.getEndpointName();
                                if (deviceName.equals("Dashcam")) {
                                    devices.add(new NDevice(connectionInfo.getEndpointName(), endpointId, false, 0, 0, false, 0, 0, 0));
                                } else {
                                    devices.add(new NDevice(connectionInfo.getEndpointName(), endpointId, true, 0, 2, false, 0, 0, 0));
                                }
                                insertIndex++;
                                adapter.notifyItemInserted(insertIndex);


                            }

                            @Override
                            public void onConnectionResult(String endpointId, ConnectionResolution result) {
                                if (result.getStatus().isSuccess()) {
                                    Log.i(TAG, "onConnectionResult: connection successful");

                                    textView.setText("Connection Successful with " + endpointId);
                                    getAllDeviceBattery();
                                    //showImageChooser(endpointId);
                                    // connectionsClient.sendPayload(
                                    //         endpointId, Payload.fromBytes(transfer.getBytes(UTF_8)));

                                } else {
                                    Log.i(TAG, "onConnectionResult: connection failed");
                                }
                            }

                            @Override
                            public void onDisconnected(@NonNull String endPointId) {
                                Log.i(TAG, "onDisconnected: disconnected from the opponent" + endPointId);
                                for (int i = 0; i < devices.size(); i++) {
                                    if (devices.get(i).endPointId.equals(endPointId)) {
                                        Log.i(TAG, "found the disconnected" + i);
                                        devices.remove(i);
                                        adapter.notifyItemRemoved(i);
                                    }
                                }
                            }
                        };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectionsClient = Nearby.getConnectionsClient(this);
        Button findDevice = (Button) findViewById(R.id.button);
        Button stopAdvertising = (Button) findViewById(R.id.button2);
        Button start = (Button)findViewById(R.id.startProcessing);
        textView = findViewById(R.id.textView);
        //NDevice testDevice = new NDevice("test","Successful",true);

       // HardwarePropertiesManager hardwarePropertiesManager = getApplicationContext().getSystemService(HardwarePropertiesManager.class);


        // load model
        try {
            signRecognition = new SignRecognition(getAssets());
            Log.d(TAG, "Model is successfully loaded");
        } catch (IOException e) {
            Log.d(TAG, "Getting some error: " + e.getMessage());
        }

        long mf = getCpuFreq(8);
        int b = getBatteryPercentage();
        NDevice mainDevice = new NDevice("MainDevice","",true,0,4,false,b,mf,8);
        devices.add(mainDevice);
        RecyclerView deviceView = (RecyclerView) findViewById(R.id.device_recycler_view);
        adapter = new DeviceAdapter(devices, (v, position) -> {
            NDevice workerDevice = devices.get(position);
            workerDevice.setWorker();
            devices.add(position, workerDevice);
            Log.d(TAG, devices.toString());
        });
        deviceView.setAdapter(adapter);
        deviceView.setLayoutManager(new LinearLayoutManager(this));
        //imageView = (ImageView) findViewById(R.id.displayImage);
        //imageView.setImageResource(com.google.android.gms.base.R.drawable.common_google_signin_btn_text_light_focused);
        findDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startAdvertising();
            }
        });

        stopAdvertising.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectionsClient.stopAdvertising();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timer T = new Timer();
                TimerTask DynamicFrameRate = new TimerTask(){
                    @Override
                    public void run(){
                        calculateDynamicFrameRate();
                    }
                };
                T.schedule(DynamicFrameRate,0,1000);
            }
        });

    }

    static{
        if(OpenCVLoader.initDebug()){

            Log.d("check","OpenCv configured successfully");

        } else{

            Log.d("check","OpenCv doesn’t configured successfully");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }


    }

    @Override
    protected void onStop() {
        connectionsClient.stopAllEndpoints();

        super.onStop();
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        int i = 0;
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "Failed to request the permission " + permissions[i]);
                Toast.makeText(this, "Missing Permission", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            i++;
        }
        recreate();
    }


    private void startAdvertising() {
        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startAdvertising(
                "Nearby Main", SERVICE_ID, connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).setConnectionType(ConnectionType.DISRUPTIVE).build());
        Log.i(TAG, "advertising started");
    }

    public  int getBatteryPercentage(){
        if (Build.VERSION.SDK_INT >= 21) {

            BatteryManager bm = (BatteryManager) this.getSystemService(BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {

            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = this.registerReceiver(null, iFilter);

            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

            double batteryPct = level / (double) scale;

            return (int) (batteryPct * 100);
        }
    }

    public void calculateDynamicFrameRate() {
        String endpointid = "";
        int frameRate = 4;
        if (devices.size() >= 1) {
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).isWorker == true) {
                    frameRate += devices.get(i).frameProcessingPower;
                } else {
                    endpointid = devices.get(i).endPointId;
                }
            }

            String response = "FrameRate"+ ":" + frameRate;
            Payload framePayload = Payload.fromBytes(response.getBytes(StandardCharsets.UTF_8));
            connectionsClient.sendPayload(endpointid, framePayload);
        }
    }

    public void getAllDeviceBattery(){
        for (NDevice d:devices){
            String battery = "Battery";
            Payload batteryInfo = Payload.fromBytes(battery.getBytes(StandardCharsets.UTF_8));
            connectionsClient.sendPayload(d.endPointId,batteryInfo);
        }
    }

    public void checkMainDevice(){
        if(!mainBusy){
            devices.get(0).isBusy = false;
        }
    }

    private long getCpuFreq(int cpuCores) {
        long maxFreq = -1;

        for (int i = 0; i < cpuCores; i++) {
            try {
                String filepath = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq";
                RandomAccessFile raf = new RandomAccessFile(filepath, "r");
                String line = raf.readLine();

                if (line != null) {
                    long freq = Long.parseLong(line);
                    if (freq > maxFreq) {
                        maxFreq = freq;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, String.format("Could not retrieve CPU frequency: \n%s", e.getMessage()));
            }
        }

        return maxFreq;
    }

    public int getCores(){
        int numCores = (int) Os.sysconf(OsConstants._SC_NPROCESSORS_CONF);
        return numCores;
    }

    public int getCpuClockSpeed(){
        int clockSpeedHz = (int) Os.sysconf(OsConstants._SC_CLK_TCK);
        return clockSpeedHz;
    }
}



