package com.example.namrome.demomap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.example.namrome.MyLocation.MyLocation;
import com.example.namrome.MyLocation.Node;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "MainActivity";
    private static final String data_filename = "data.txt";

    //    ArrayList<String> arrayList = new ArrayList<>();
    ArrayList<Node> nodes = new ArrayList<>();
    ArrayList<String> nodeNames = new ArrayList<>();

    EditText editText;
    String file_name = "hello_file";
    //String arr[] = {"All", "Node 1", "Node 2", "Node 3"};
    private Timer timer;
    private TimerTask timerTask;
    private int count = 2;
    private int t = 1;
    private int time = 5;
    private int timeSelected = 5;
    private GoogleMap mMap;
    Spinner spinnerType, spinnerNode;
    ArrayList<String> dsType, dsNode;
    ArrayAdapter<String> adapterType, adapterNode;
    ProgressDialog progressDialog;
    private ArrayList<LatLng> latLngs = new ArrayList<>();
    private MarkerOptions options = new MarkerOptions();


    List<MyLocation> myLocations = new ArrayList<MyLocation>();
    ArrayAdapter<MyLocation> arrayAdapter = null;

    private long mTimeStamp = 0;
    private int mHour = 0;
    private int mMinute = 0;
    private int mDate = 0;

    private double kinhdo = 0;
    private double vido = 0;

    private int currentNodeId = 0;
    //private int a =0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Check if user triggered a refresh:
            case R.id.action_addnode:

                final EditText editText = new EditText(this);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);   // Number method
                new AlertDialog.Builder(this)
                        .setTitle("Enter Node Number: ")
                        //.setMessage("The message")
                        .setIcon(R.drawable.devicea)
                        .setView(editText)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String inputFromUser = editText.getText().toString();
                                int id;
                                try {
                                    id = Integer.parseInt(inputFromUser);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    return;
                                }
                                // Create new node
                                for (Node node : nodes) {
                                    if (id == node.getId()) {
                                        Toast.makeText(getApplicationContext(), "Id already exists", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                }
                                Node newNode = new Node(id, "Node " + id);

                                // Add old nodes (not include nodes from 1-3)


                                // Add new node
                                nodes.add(newNode);
                                updateNodeNames();

                                saveData();


                            }
                        }).show();
                break;

            case R.id.action_remove:
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
                builderSingle.setTitle("Remove node: ")
                        .setIcon(R.drawable.unnamed);


                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
                for (int i = 1; i < nodeNames.size(); i++) {
                    String nodeName = nodeNames.get(i);
                    arrayAdapter.add(nodeName);
                }

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        if (which <= 0) { // Node 1 (index 0)
                            Toast.makeText(getApplicationContext(), "Node 1 is fixed! You can't remove it", Toast.LENGTH_LONG).show();
                            Log.i(TAG, "Which <= 0");
                            return;
                        }
                        String strName = arrayAdapter.getItem(which);
                        AlertDialog.Builder builderInner = new AlertDialog.Builder(MainActivity.this);
                        builderInner.setMessage("Remove " + strName);
                        builderInner.setTitle("Are you sure?");
                        builderInner.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int second_which) {
//                                dialog.dismiss();

                                nodes.remove(which);
                                updateNodeNames();
                                adapterNode.notifyDataSetChanged();
                                saveData();

                                Log.i(TAG, "which: " + which);
                            }
                        });
                        builderInner.show();
                    }
                });
                builderSingle.show();
                break;
            case R.id.action_settime:

                OpenDialog();
            case R.id.action_clearmap:
                mMap.clear();

        }
        return true;
        //default: return super.onOptionsItemSelected(item);
    }

    private void updateNodeNames() {
        while (nodeNames.size() != 0) {
            nodeNames.remove(nodeNames.size() - 1);
        }
        nodeNames.add("All");
        for (Node node : nodes) {
            nodeNames.add(node.getName());
        }
    }

    private void saveData() {
        ArrayList<Node> subNodes = new ArrayList<>();
        for (int i = 1; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            subNodes.add(node);
        }
        FileOutputStream fos = null;
        try {
            fos = getApplicationContext().openFileOutput(data_filename, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(subNodes);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void OpenDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Select Time To Update");

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.view, null);
        dialog.setView(v);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //textView = (TextView)findViewById(R.id.textView2);
                timeSelected = time;
                //textView.setText(times);
                timer.cancel();
                timer = new Timer();
                timerTask = new TimerTask() {
                    synchronized public void run() {

                        // here your todo;
                        //Log.i(MYTAG, "Repeated");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                t = 0;

                                if (currentNodeId == 0) {
                                    new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes");
                                } else {
                                    new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/" + currentNodeId);
                                }
                            }
                        });
                    }

                };
                // Log.i(TAG, "timeSelected:" + timeSelected);
                timer.scheduleAtFixedRate(timerTask, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(timeSelected));
                //timer.scheduleAtFixedRate(timerTask, 0, timeSelected);
            }
        });

        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();

        Spinner spinner = (Spinner) v.findViewById(R.id.spinner3);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.option, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //((TextView) v).setGravity(Gravit.CENTER);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {

                checkTime(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void checkTime(int i) {
        Log.i(TAG, "checkTime: begin");
        if (timer == null) {
            Log.i(TAG, "checkTime: timer is null");
        }
        switch (i) {
            case 0:
                time = 5;
                break;
            case 1:
                time = 10;
                break;
            case 2:
                time = 15;
                break;
            case 3:
                time = 20;
                break;
            case 4:
                time = 30;
                break;
            case 5:
                time = 60;
                break;
            case 6:
                time = 300;
                break;

            default: // do nothing

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        arrayList.add("All");
//        arrayList.add("Node 1");
//        arrayList.add("Node 2");


        nodes.add(new Node(1, "Node 1"));
//        nodes.add(new Node(2, "Node 2"));
//        nodes.add(new Node(3, "Node 3"));

        if (editText == null) Log.i(TAG, "onCreate: editText null");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.aaa);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        ArrayList<Node> subNodes = null;
        FileInputStream fis = null;
        try {
            fis = getApplicationContext().openFileInput(data_filename);

            ObjectInputStream is = new ObjectInputStream(fis);
            subNodes = (ArrayList<Node>) is.readObject();
            is.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (subNodes != null) {
            for (Node node : subNodes) {
                nodes.add(node);
            }
        }

        //onBackPressed();
        ConTroller();
        Events();
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Log.i(TAG, "Repeated");
//                new ReadJson().execute("https://loratracking.mybluemix.net/api/v1/nodes");
//            }
//        });


        // Set up timer to refresh
        timer = new Timer();
        Log.i(TAG, "Da khoi tao!");
        timerTask = new TimerTask() {

            synchronized public void run() {

                // here your todo;
                //Log.i(MYTAG, "Repeated");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        t = 0;
                        //Log.i(MYTAG, "Repeated");
                        switch (currentNodeId) {
                            case 0: // All node
                                new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes");
                                break;
                            case 1: // Node 1
                                new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/1");
                                break;
                            case 2: // Node 2
                                new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/2");
                                break;
                            case 3: // Node 3
                                new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/3");
                                break;
                            case 4: // Node 4
                                new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/4");
                                break;
                            case 5: // Node 5
                                new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/5");
                                break;
                            case 6: // Node 6
                                new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/6");
                                break;
                            case 7: // Node 7
                                new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/7");
                                break;
                            case 8: // Node 8
                                new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/8");
                                break;
                            case 9: // Node 9
                                new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/9");
                                break;

                            default:
                                // do not thing
                        }

                    }
                });
            }

        };
        timer.scheduleAtFixedRate(timerTask, TimeUnit.SECONDS.toMillis(timeSelected), TimeUnit.SECONDS.toMillis(timeSelected));
//        timer.purge();
        //Log.i(MYTAG, "After Repeated");
        if (timer == null) {
            Log.i(TAG, "onCreate: Timer bi null");
        }
    }


    // Event when click button back to exit
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Do you want to exit?");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void ConTroller() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        spinnerType = (Spinner) findViewById(R.id.spinner);
        spinnerNode = (Spinner) findViewById(R.id.spinner2);

        dsType = new ArrayList<>();
        dsType.addAll(Arrays.asList(getResources().getStringArray(R.array.arrType)));
        adapterType = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, dsType);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapterType);

        dsNode = new ArrayList<>();
        dsNode.addAll(Arrays.asList(getResources().getStringArray(R.array.node)));

        nodeNames.add("All");
        for (Node x : nodes) {
            nodeNames.add(x.getName());
        }
        adapterNode = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, nodeNames);
        adapterNode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNode.setAdapter(adapterNode);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Notification");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

    }

    private void Events() {
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                checkMapType(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerNode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int j, long id) {

                checkNode(j);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void checkNode(int j) {

        if (j == 0) { // All nodes
            currentNodeId = 0;
            mMap.clear();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/1");
                    new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/2");
                    new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/3");
                    new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/4");
                    new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/5");
                    new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/6");
                    ;
                }
            });
        } else {
            j = j - 1;
            Node node = nodes.get(j);
            final int id = node.getId();
            currentNodeId = id;
            mMap.clear();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new ReadJson().execute("https://motortracking.mybluemix.net/api/v1/nodes/" + id);
                }
            });
        }

    }

    private void checkMapType(int i) {
        switch (i) {
            case 0:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case 1:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case 2:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case 3:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case 4:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //progressDialog.dismiss();
        mMap = googleMap;
        updateMarker(mMap);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                progressDialog.dismiss();
            }
        });
    }

    private void updateMarker(GoogleMap googleMap) {
        mMap = googleMap;

    }

    // DOC GHI CHUOI JSON
    class ReadJson extends AsyncTask<String, Integer, String> {


        @Override
        protected String doInBackground(String... params) {
            String kq = docNoiDung_Tu_URL(params[0]);
            return kq;
        }

        @Override
        protected void onPostExecute(String s) {
            updateData(s);
            //Toast.makeText(MainActivity.this, "Dữ liệu đã cập nhật", Toast.LENGTH_LONG).show();
        }
    }

    private void updateData(String s) {
        //Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();

        try {
            JSONObject root = new JSONObject(s);
            JSONArray mang = root.getJSONArray("data");

            String kqa = "";
            //List<Double> locate = Arrays.asList(null);

            LatLng polyline;
            ArrayList<LatLng> polylines = new ArrayList<LatLng>();

            String result = "";
            ArrayList<String> mangThoiGian = new ArrayList<String>();
            String dateString = "";

            // add doi tuong Location
            MyLocation r = new MyLocation();


            for (int i = 0; i < mang.length(); i++) {
                JSONObject son = mang.getJSONObject(i);
                JSONArray loca = son.getJSONArray("location");

                //JSONObject chuan = mang.getJSONObject(0);
                //JSONArray locachuan = chuan.getJSONArray("location");
                for (int j = 0; j < loca.length(); j++) {
                    kinhdo = loca.getDouble(0);
                    vido = loca.getDouble(1);

                }
                mangThoiGian.add(son.getString("timestamp"));
                //locate.add(son.getDouble("location"));

                kqa = son.getString("timestamp");
                long time = Long.parseLong(son.getString("timestamp"));
                //long timechuan = Long.parseLong(chuan.getString("timestamp"));

                mTimeStamp = time;
                ///////////////////////////////////////////////////0123456789abcdef
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy ");
                dateString = formatter.format(new Date(time));
//                mHour = Integer.valueOf(dateString.substring(0x0b,0x0d));
//                mMinute = Integer.valueOf(dateString.substring(0x0e,0x0e + 2));
//                mDate = Integer.valueOf(dateString.substring(0x08,0x0a));
                r.setTimes(dateString);
                r.setLat(kinhdo);
                r.setLog(vido);


                polyline = new LatLng(kinhdo, vido);
                if (kinhdo != 1000.000000 | vido != 1000.000000) {
                    polylines.add(polyline);
                }

//                Polyline polylinea = mMap.addPolyline(new PolylineOptions()
//                        .addAll(polylines)
//                        .width(11)
//                        .visible(true)
//                        .color(Color.GRAY)
//
//                );
                Log.i(TAG, "Node nay la node: " + currentNodeId);
                switch (currentNodeId) {
                    case 0:
                        Polyline polylinea = mMap.addPolyline(new PolylineOptions()
                                .addAll(polylines)
                                .width(11)
                                .visible(true)
                                .color(Color.GRAY)

                        );
                        break;
                    case 1:
                        Polyline polylinea1 = mMap.addPolyline(new PolylineOptions()
                                .addAll(polylines)
                                .width(11)
                                .visible(true)
                                .color(Color.GREEN)

                        );
                        break;
                    case 2:
                        Polyline polylinea2 = mMap.addPolyline(new PolylineOptions()
                                .addAll(polylines)
                                .width(11)
                                .visible(true)
                                .color(Color.MAGENTA)
                        );
                        //polylinea.setColor(R.color.color1);
                        break;
                    case 3:
                        Polyline polylinea3 = mMap.addPolyline(new PolylineOptions()
                                .addAll(polylines)
                                .width(11)
                                .visible(true)
                                .color(Color.YELLOW)

                        );
                        break;
                    case 4:
                        Polyline polylinea4 = mMap.addPolyline(new PolylineOptions()
                                .addAll(polylines)
                                .width(11)
                                .visible(true)
                                .color(Color.RED)

                        );
                        break;
                    default: Polyline polylineax = mMap.addPolyline(new PolylineOptions()
                            .addAll(polylines)
                            .width(11)
                            .visible(true)
                            .color(Color.RED));
                }

                //int color  = R.color.colorPrimary;
                //polylinea.setColor(color);
                for (LatLng point : polylines) {
                    options.position(point);
                    options.title(r.getTimes());
                    //options.snippet("Check Internet Connection");
                    //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.btn_map_pointer));
                    mMap.addMarker(options);
                    if (t == 1) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 17));
                    }
                }
                if (polylines == null) {
                    Toast.makeText(MainActivity.this, "No data to show", Toast.LENGTH_LONG).show();
                }
                //add cac thuoc tinh vao doi tuong location
                myLocations.add(r);
            }
            //Toast.makeText(MainActivity.this, "" + kinhdo + " " + vido, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String docNoiDung_Tu_URL(String theUrl) {
        StringBuilder content = new StringBuilder();

        try {
            // create a url object
            URL url = new URL(theUrl);

            // create a urlconnection object
            URLConnection urlConnection = url.openConnection();

            // wrap the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;

            // read from the urlconnection via the bufferedreader
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
