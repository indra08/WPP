package gmedia.net.id.psp.NavKunjungan;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.ImageUtils;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.PermissionUtils;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gmedia.net.id.psp.CustomView.CustomMapView;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.TambahCustomer.Adapter.PhotosAdapter;
import gmedia.net.id.psp.TambahCustomer.Model.AreaModel;
import gmedia.net.id.psp.Utils.MockLocChecker;
import gmedia.net.id.psp.Utils.ServerURL;

public class DetailCheckin extends AppCompatActivity implements LocationListener {

    private Toolbar toolbar;
    private TextView tvToolbarTitle;
    private AppBarLayout appBarLayout;
    private CustomMapView mvMap;
    private GoogleMap googleMap;
    private String title;
    private CollapsingToolbarLayout collapsingToolbar;
    private ItemValidation iv = new ItemValidation();

    // Location
    private double latitude, longitude, latitudeOutlet, longitudeOutlet;
    private LocationManager locationManager;
    private Criteria criteria;
    private String provider;
    private Location location, locationOutlet;
    private final int REQUEST_PERMISSION_COARSE_LOCATION=2;
    private final int REQUEST_PERMISSION_FINE_LOCATION=3;
    public boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute
    private TextView tvTitle;
    private String TAG = "DetailCheckin";
    private String address0 = "";
    private boolean refreshMode = false;
    private EditText edtNama, edtAlamat, edtKota, edtTelepon, edtNoHP, edtBank, edtRekening, edtCP, edtLatitude, edtLongitude, edtState;
    private Spinner spArea;
    private Button btnSimpan;
    private List<AreaModel> listArea;
    private SessionManager session;
    private RecyclerView rvPhoto, rvPhotoUpload;
    private ImageButton ibAddPhoto;

    //Upload Handler
    private static int RESULT_OK = -1;
    private static int PICK_IMAGE_REQUEST = 1212;
    private ImageUtils iu = new ImageUtils();
    private Bitmap bitmap;
    private List<Bitmap> photoList, photoUploadList;
    private PhotosAdapter adapter, adapterUpload;
    private ProgressDialog progressDialog;
    private EditText edtEmail;
    private String kdcus = "";
    private boolean editMode = false;
    private String kdArea = "";
    private String statusAktif = "";
    private EditText edtKeterangan;

    public DetailCheckin() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_checkin);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        initUI();
    }

    private void initUI() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle = (TextView) findViewById(R.id.tv_title);

        edtNama = (EditText) findViewById(R.id.edt_nama);
        edtAlamat = (EditText) findViewById(R.id.edt_alamat);
        edtKota = (EditText) findViewById(R.id.edt_kota);
        edtTelepon = (EditText) findViewById(R.id.edt_telepon);
        edtNoHP = (EditText) findViewById(R.id.edt_nohp);
        edtEmail = (EditText) findViewById(R.id.edt_email);
        edtBank = (EditText) findViewById(R.id.edt_bank);
        edtRekening = (EditText) findViewById(R.id.edt_rekening);
        edtCP = (EditText) findViewById(R.id.edt_contant_person);
        spArea = (Spinner) findViewById(R.id.sp_area);
        edtLatitude = (EditText) findViewById(R.id.edt_latitude);
        edtLongitude = (EditText) findViewById(R.id.edt_longitude);
        edtState = (EditText) findViewById(R.id.edt_state);
        rvPhoto = (RecyclerView) findViewById(R.id.rv_photo);
        rvPhotoUpload = (RecyclerView) findViewById(R.id.rv_photo_upload);
        ibAddPhoto = (ImageButton) findViewById(R.id.ib_add_photo);
        edtKeterangan = (EditText) findViewById(R.id.edt_keterangan);
        btnSimpan = (Button) findViewById(R.id.btn_simpan);

        session = new SessionManager(DetailCheckin.this);
        mvMap = (CustomMapView) findViewById(R.id.mv_map);
        mvMap.onCreate(null);
        mvMap.onResume();
        try {
            MapsInitializer.initialize(DetailCheckin.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        photoList = new ArrayList<>();
        photoUploadList = new ArrayList<>();
        LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManager2= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);

        adapter = new PhotosAdapter(DetailCheckin.this, photoList);
        adapterUpload = new PhotosAdapter(DetailCheckin.this, photoUploadList);

        rvPhoto.setLayoutManager(layoutManager);
        rvPhoto.setAdapter(adapter);

        rvPhotoUpload.setLayoutManager(layoutManager2);
        rvPhotoUpload.setAdapter(adapterUpload);

        tvToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        title = "Detail Kunjungan";
        //tvTitle.setText(title);
        initCollapsingToolbar();

        initLocation();

        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MockLocChecker checker = new MockLocChecker(DetailCheckin.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }

    private void initCollapsingToolbar() {
        collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.ctl_main);
        collapsingToolbar.setTitle(" ");
        tvToolbarTitle.setText(" ");
        appBarLayout = (AppBarLayout) findViewById(R.id.abl_main);
        appBarLayout.setExpanded(true);

        // hiding & showing the tvTitle when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (appBarLayout.getTotalScrollRange() + verticalOffset <= 30) {
                    tvToolbarTitle.setText(title);
                    isShow = true;
                } else if (isShow) {

                    tvToolbarTitle.setText(" ");
                    isShow = false;
                }
            }
        });
    }

    private void initLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setCriteria();
        latitude = 0;
        longitude = 0;
        latitudeOutlet = 0;
        longitudeOutlet = 0;
        location = new Location("set");
        locationOutlet = new Location("set");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        locationOutlet.setLatitude(latitudeOutlet);
        locationOutlet.setLongitude(longitudeOutlet);

        refreshMode = true;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            kdcus = bundle.getString("kdcus");
            if(kdcus != null && kdcus.length() > 0){

                editMode = true;
                getDataCustomer();
                getPhotos();
            }
        }

        location = getLocation();
        getDataArea();
    }

    public Location getLocation() {
        try {

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            Log.v("isGPSEnabled", "=" + isGPSEnabled);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

            if (isGPSEnabled == false && isNetworkEnabled == false) {
                // no network provider is enabled
                Toast.makeText(DetailCheckin.this, "Cannot identify the location.\nPlease turn on GPS or turn on your data.",
                        Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    location = null;

                    // Granted the permission first
                    if (ActivityCompat.checkSelfPermission(DetailCheckin.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailCheckin.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailCheckin.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_COARSE_LOCATION);
                        }

                        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailCheckin.this,
                                Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
                        } else {
                            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
                        }
                        return null;
                    }

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            //onLocationChanged(location);
                        }
                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    location=null;
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                //onLocationChanged(location);
                            }
                        }
                    }
                }else{
                    //Toast.makeText(context, "Turn on your GPS for better accuracy", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(location != null){
            onLocationChanged(location);
        }
        return location;
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailCheckin.this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(DetailCheckin.this,
                new String[]{permissionName}, permissionRequestCode);
    }

    private void initEvent() {

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateBeforeSave();
            }
        });

        ibAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadChooserDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            /*File file = new File(String.valueOf(filePath));
            long length = file.length();
            length = length/1024; //in KB*/

            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(
                        filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Bitmap bmp = BitmapFactory.decodeStream(imageStream);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 70, stream);
            byte[] byteArray = stream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            bitmap = scaleDown(bitmap, 380, true);

            try {
                stream.close();
                stream = null;
            } catch (IOException e) {

                e.printStackTrace();
            }

            if(bitmap != null){

                photoUploadList.add(bitmap);
                adapterUpload.notifyDataSetChanged();
            }

        }else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            /*try {

                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(photoFromCameraURI));
                bitmap = scaleDown(bitmap, 380, true);

                if(bitmap != null){

                    photoUploadList.add(bitmap);
                    adapterUpload.notifyDataSetChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            if(data != null){
                bitmap = (Bitmap) data.getExtras().get("data");
                bitmap = scaleDown(bitmap, 360, true);
                if(bitmap != null){

                    photoList.add(bitmap);
                    adapter.notifyDataSetChanged();
                }
            }else{
                Toast.makeText(DetailCheckin.this, "Gambar tidak termuat, harap ulangi kembali", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadChooserDialog(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(DetailCheckin.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_chooser, null);
        builder.setView(view);

        final LinearLayout llBrowse= (LinearLayout) view.findViewById(R.id.ll_browse);
        final LinearLayout llCamera = (LinearLayout) view.findViewById(R.id.ll_camera);

        final AlertDialog alert = builder.create();

        llBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showFileChooser();
                alert.dismiss();
            }
        });

        llCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openCamera();
                alert.dismiss();
            }
        });

        alert.show();
    }

    //region File Chooser

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    private final int REQUEST_IMAGE_CAPTURE = 1;
    private String photoFromCameraURI;

    private void openCamera(){

        if(PermissionUtils.hasPermissions(DetailCheckin.this,Manifest.permission.CAMERA)){

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            /*if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                Uri photoURL = null;
                try {
                    photoURL = FileProvider.getUriForFile(context,
                            BuildConfig.APPLICATION_ID + ".provider",
                            createImageFile());
                    photoFromCameraURI = photoURL.toString();
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.i(TAG, "IOException");
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURL);
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                }
            }*/
        }else{

            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(DetailCheckin.this)
                    .setTitle("Ijin dibutuhkan")
                    .setMessage("Ijin dibutuhkan untuk mengakses kamera, harap ubah ijin kamera ke \"diperbolehkan\"")
                    .setPositiveButton("Buka Ijin", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        photoFromCameraURI = "file:" + image.getAbsolutePath();
        return image;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        try {

            float ratio = Math.min(
                    (float) maxImageSize / realImage.getWidth(),
                    (float) maxImageSize / realImage.getHeight());
            int width = Math.round((float) ratio * realImage.getWidth());
            int height = Math.round((float) ratio * realImage.getHeight());

            realImage = Bitmap.createScaledBitmap(realImage, width,
                    height, filter);
        }catch (Exception e){
            e.printStackTrace();
        }

        return realImage;
    }

    private void validateBeforeSave() {

        if( edtKeterangan.getText().length() == 0){

            edtKeterangan.setError("Keterangan Kunjungan harap diisi");
            edtKeterangan.requestFocus();
            return;
        }else{
            edtKeterangan.setError(null);
        }

        saveData();
    }

    private void saveData() {

        progressDialog = new ProgressDialog(DetailCheckin.this, R.style.AppTheme_Login_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        btnSimpan.setEnabled(false);

        String nik = session.getUserInfo(SessionManager.TAG_UID);
        JSONObject jDataLocation = new JSONObject();

        try {
            jDataLocation.put("kdcus", kdcus);
            jDataLocation.put("latitude", iv.doubleToStringFull(latitude));
            jDataLocation.put("longitude", iv.doubleToStringFull(longitude));
            jDataLocation.put("keterangan", edtKeterangan.getText().toString());
            jDataLocation.put("nik", nik);
            jDataLocation.put("state", edtState.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray jDataImages = new JSONArray();

        for (Bitmap item : photoUploadList){

            JSONObject jo = new JSONObject();
            try {
                jo.put("image", ImageUtils.convert(item));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jDataImages.put(jo);
        }

        String method = "POST";
        JSONObject jBody = new JSONObject();

        try {
            jBody.put("data", jDataLocation);
            jBody.put("data_images", jDataImages);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailCheckin.this, jBody, method, ServerURL.saveCheckin, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                progressDialog.dismiss();
                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        String message = response.getJSONObject("response").getString("message");
                        Toast.makeText(DetailCheckin.this, message, Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }else{
                        String message = response.getJSONObject("metadata").getString("message");
                        Toast.makeText(DetailCheckin.this, message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(DetailCheckin.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
                }

                btnSimpan.setEnabled(true);
            }

            @Override
            public void onError(String result) {

                progressDialog.dismiss();
                Toast.makeText(DetailCheckin.this, "Terjadi kesalahan, mohon ulangi kembali", Toast.LENGTH_LONG).show();
                btnSimpan.setEnabled(true);
            }
        });
    }

    private void getDataCustomer() {

        String nik = session.getUserDetails().get(SessionManager.TAG_UID);
        JSONObject jBody = new JSONObject();
        try {
            jBody.put("nik", nik);
            jBody.put("kdcus", kdcus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(DetailCheckin.this, jBody, "POST", ServerURL.getCustomer, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            edtNama.setText(jo.getString("nama"));
                            edtAlamat.setText(jo.getString("alamat"));
                            edtKota.setText(jo.getString("kota"));
                            edtTelepon.setText(jo.getString("notelp"));
                            edtNoHP.setText(jo.getString("nohp"));
                            edtEmail.setText(jo.getString("email"));
                            edtBank.setText(jo.getString("bank"));
                            edtRekening.setText(jo.getString("norekening"));
                            edtCP.setText(jo.getString("contact_person"));
                            kdArea = jo.getString("kodearea");
                            /*edtLatitude.setText(jo.getString("latitude"));
                            edtLongitude.setText(jo.getString("longitude"));
                            edtState.setText(jo.getString("state"));*/

                            if(jo.getString("latitude").length() > 0){
                                latitudeOutlet = iv.parseNullDouble(jo.getString("latitude"));
                                longitudeOutlet = iv.parseNullDouble(jo.getString("longitude"));
                                locationOutlet.setLatitude(latitudeOutlet);
                                locationOutlet.setLongitude(longitudeOutlet);
                                refreshMode = true;
                                onLocationChanged(location);
                            }

                            statusAktif = jo.getString("status");
                            /*if(iv.parseNullInteger(statusAktif) != 2){
                                btnSimpan.setEnabled(false);
                            }*/
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getDataArea();
            }

            @Override
            public void onError(String result) {

                getDataArea();
            }
        });
    }

    private void getDataArea() {

        ApiVolley request = new ApiVolley(DetailCheckin.this, new JSONObject(), "GET", ServerURL.getArea, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    listArea = new ArrayList<>();

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            listArea.add(new AreaModel(jo.getString("kode_kota"), jo.getString("omo")));
                        }

                        setSPAreaEntry();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String result) {

            }
        });
    }

    private void setSPAreaEntry() {

        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, listArea);
        spArea.setAdapter(adapter);

        if(listArea.size() > 0 && editMode && kdArea.length() > 0){

            int x = 0;
            for(AreaModel areaItem : listArea){

                if(kdArea.equals(areaItem.getValue())){
                    spArea.setSelection(x);
                    break;
                }
                x++;
            }
        }
    }

    public void setCriteria() {
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        provider = locationManager.getBestProvider(criteria, true);
    }

    private void getPhotos() {

        ApiVolley request = new ApiVolley(DetailCheckin.this, new JSONObject(), "GET", ServerURL.getImages+kdcus, "", "", 0, session.getUsername(), session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");

                        for (int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            String image = jo.getString("image");
                            new AsyncGettingBitmapFromUrl().execute(image);
                        }
                    }

                } catch (JSONException e) {
                }
            }

            @Override
            public void onError(String result) {

            }
        });
    }

    private class AsyncGettingBitmapFromUrl extends AsyncTask<String, Void, Bitmap> {


        @Override
        protected Bitmap doInBackground(String... params) {

            System.out.println("doInBackground");

            Bitmap bitmap1 = null;

            bitmap1 = downloadImage(params[0]);
            photoList.add(bitmap1);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
            return bitmap1;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            System.out.println("bitmap" + bitmap);

        }
    }

    public static  Bitmap downloadImage(String url) {
        Bitmap bitmap = null;
        InputStream stream = null;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 1;

        try {
            stream = getHttpConnection(url);
            bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
            if(stream != null){
                stream.close();
            }
        }
        catch (IOException e1) {
            e1.printStackTrace();
            System.out.println("downloadImage"+ e1.toString());
        }
        return bitmap;
    }

    public static  InputStream getHttpConnection(String urlString)  throws IOException {

        InputStream stream = null;
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        try {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("downloadImage" + ex.toString());
        }
        return stream;
    }

    private void setPointMap(){

        mvMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

                googleMap = mMap;
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions()
                        .anchor(0.0f, 1.0f)
                        .position(new LatLng(latitude, longitude)));

                googleMap.addMarker(new MarkerOptions()
                        .anchor(0.0f, 1.0f)
                        .title("Outlet")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        .position(new LatLng(latitudeOutlet, longitudeOutlet)));

                if (ActivityCompat.checkSelfPermission(DetailCheckin.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DetailCheckin.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(DetailCheckin.this, "Please allow location access from your app permission", Toast.LENGTH_SHORT).show();
                    return;
                }

                //googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                MapsInitializer.initialize(DetailCheckin.this);
                LatLng position = new LatLng(latitude, longitude);
                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(15).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                updateKeterangan(position);
            }
        });
    }

    private void updateKeterangan(LatLng position){

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        latitude = position.latitude;
        longitude = position.longitude;

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(position.latitude, position.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(addresses != null && addresses.size() > 0){
            address0 = addresses.get(0).getAddressLine(0);
        }

        edtLatitude.setText(iv.doubleToStringFull(latitude));
        edtLongitude.setText(iv.doubleToStringFull(longitude));
        edtState.setText(address0);
    }

    @Override
    public void onLocationChanged(Location location) {
        refreshMode = false;
        this.location = location;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        setPointMap();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
