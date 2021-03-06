package gmedia.net.id.psp.OrderPerdana;

import android.content.Intent;
import android.content.res.TypedArray;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.OrderPerdana.Adapter.ListBarangAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class ListBarang extends AppCompatActivity {

    private ListView lvBarang;
    private List<CustomItem> masterList;
    private AutoCompleteTextView actvBarang;
    private ProgressBar pbProses;
    private ItemValidation iv = new ItemValidation();
    private boolean firstLoad = true;
    private String noCus = "", namaCus = "";
    private SessionManager session;
    private EditText edtPelanggan;
    private RadioGroup rgCrbayar;
    private RadioButton rbTunai, rbTempo;
    private EditText edtTempo;
    private boolean tunaiMode = true;
    private EditText edtNoBa;
    private String notelpCus = "";
    private View bottomView;
    private BottomSheetBehavior mBottomSheetBehavior2;
    private View llHeader;
    private ImageView ivExpand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_barang);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setTitle("Pilih Barang");

        initUI();
    }

    private void initUI() {

        edtPelanggan = (EditText) findViewById(R.id.edt_pelanggan);
        rgCrbayar = (RadioGroup) findViewById(R.id.rg_crbayar);
        rbTunai = (RadioButton) findViewById(R.id.rb_tunai);
        rbTempo = (RadioButton) findViewById(R.id.rb_tempo);
        edtTempo = (EditText) findViewById(R.id.edt_tempo);
        edtNoBa = (EditText) findViewById(R.id.edt_no_ba);
        lvBarang = (ListView) findViewById(R.id.lv_barang);
        actvBarang = (AutoCompleteTextView) findViewById(R.id.actv_barang);
        pbProses = (ProgressBar) findViewById(R.id.pb_proses);
        ivExpand = (ImageView) findViewById(R.id.iv_expand);

        //Bottom view
        llHeader= findViewById(R.id.ll_header);
        bottomView = findViewById(R.id.ns_barang);
        mBottomSheetBehavior2 = BottomSheetBehavior.from(bottomView);
        mBottomSheetBehavior2.setHideable(false);
        int[] display = iv.getScreenResolution(ListBarang.this);
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        mBottomSheetBehavior2.setPeekHeight(mActionBarSize + iv.dpToPx(ListBarang.this, 54));
        mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_COLLAPSED);
        //bottomView.setLayoutParams(new CoordinatorLayout.LayoutParams(display[0], display[1]- iv.dpToPx(ListBarang.this, 40)));

        ivExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    ivExpand.setImageDrawable(getResources().getDrawable(R.mipmap.ic_arrow_up));
                }
                else if(mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
                    ivExpand.setImageDrawable(getResources().getDrawable(R.mipmap.ic_arrow_down));
                }
                else if(mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
                    ivExpand.setImageDrawable(getResources().getDrawable(R.mipmap.ic_arrow_down));
                }
            }
        });

        mBottomSheetBehavior2.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if(mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    ivExpand.setImageDrawable(getResources().getDrawable(R.mipmap.ic_arrow_down));
                }
                else if(mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    ivExpand.setImageDrawable(getResources().getDrawable(R.mipmap.ic_arrow_up));
                }
                else if(mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    ivExpand.setImageDrawable(getResources().getDrawable(R.mipmap.ic_arrow_up));
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) lvBarang.getLayoutParams();
        params.height = display[1] - (3 * mActionBarSize);
        lvBarang.setLayoutParams(params);

        actvBarang.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        actvBarang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        session = new SessionManager(ListBarang.this);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            noCus = bundle.getString("nocus");
            namaCus = bundle.getString("namacus");
            notelpCus = bundle.getString("notelpcus");
            edtPelanggan.setText(namaCus);
            getDataReseller();
        }

        rgCrbayar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {

                if(i == R.id.rb_tunai){
                    edtTempo.setEnabled(false);
                    edtTempo.setBackground(getResources().getDrawable(R.drawable.bg_input_disable));
                    edtTempo.setText("1");
                    tunaiMode = true;
                }else{
                    edtTempo.setBackground(getResources().getDrawable(R.drawable.bg_input));
                    edtTempo.setEnabled(true);
                    edtTempo.setText("");
                    tunaiMode = false;
                }
            }
        });

        // Inisialisasi
        tunaiMode = true;
        rbTunai.setChecked(tunaiMode);
        /*edtTempo.setEnabled(false);
        edtTempo.setBackground(getResources().getDrawable(R.drawable.input_bg_disable));
        edtTempo.setText("1");*/
    }

    private void getDataReseller() {

        masterList = new ArrayList<>();
        pbProses.setVisibility(View.VISIBLE);
        String nik = session.getUserInfo(SessionManager.TAG_NIK);
        ApiVolley request = new ApiVolley(ListBarang.this, new JSONObject(), "GET", ServerURL.getBarangPerdana+nik, "", "", 0, session.getUserDetails().get(SessionManager.TAG_USERNAME), session.getUserDetails().get(SessionManager.TAG_PASSWORD), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");

                    if(iv.parseNullInteger(status) == 200){

                        JSONArray items = response.getJSONArray("response");
                        for(int i  = 0; i < items.length(); i++){

                            JSONObject jo = items.getJSONObject(i);
                            masterList.add(new CustomItem(jo.getString("kodebrg"), jo.getString("namabrg"), jo.getString("nobukti"), jo.getString("harga"), jo.getString("jml"), jo.getString("kodegudang")));
                        }
                    }

                    final List<CustomItem> tableList = new ArrayList<>(masterList);
                    getAutocompleteEvent(tableList);
                    getTableList(tableList);
                    pbProses.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    getAutocompleteEvent(null);
                    getTableList(null);
                    pbProses.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String result) {

                getAutocompleteEvent(null);
                getTableList(null);
                pbProses.setVisibility(View.GONE);
            }
        });
    }

    private void getAutocompleteEvent(final List<CustomItem> tableList) {

        if(firstLoad){
            firstLoad = false;

            actvBarang.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(actvBarang.getText().length() == 0) getTableList(masterList);
                }
            });
        }

        actvBarang.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_SEARCH){

                    List<CustomItem> items = new ArrayList<CustomItem>();
                    String keyword = actvBarang.getText().toString().trim().toUpperCase();

                    for (CustomItem item: tableList){

                        if(item.getItem2().toUpperCase().contains(keyword) || item.getItem3().toUpperCase().contains(keyword)) items.add(item);
                    }

                    getTableList(items);
                    iv.hideSoftKey(ListBarang.this);
                    return true;
                }

                return false;
            }
        });
    }

    private void getTableList(List<CustomItem> tableList) {

        lvBarang.setAdapter(null);

        if(tableList != null && tableList.size() > 0){

            ListBarangAdapter adapter = new ListBarangAdapter(ListBarang.this, tableList);
            lvBarang.setAdapter(adapter);

            lvBarang.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    CustomItem selectedItem = (CustomItem) adapterView.getItemAtPosition(i);
                    validateData(selectedItem);

                }
            });
        }
    }

    private void validateData(CustomItem selectedItem) {

        if(!tunaiMode && iv.parseNullDouble(edtTempo.getText().toString()) <= 0){

            Snackbar.make(findViewById(android.R.id.content),"Harap ini tempo, dan isi lebih dari 0", Snackbar.LENGTH_LONG).show();
            edtTempo.requestFocus();
            return;
        }

        if(iv.parseNullDouble(edtTempo.getText().toString()) > 7){

            Snackbar.make(findViewById(android.R.id.content),"Maksimal tempo 7 hari", Snackbar.LENGTH_LONG).show();
            edtTempo.requestFocus();
            return;
        }

        Intent intent = new Intent(ListBarang.this, DetailOrderPerdana.class);
        intent.putExtra("nocus", noCus);
        intent.putExtra("namacus", namaCus);
        intent.putExtra("notelpcus", notelpCus);
        intent.putExtra("kodebrg", selectedItem.getItem1());
        intent.putExtra("namabrg", selectedItem.getItem2());
        intent.putExtra("suratjalan", selectedItem.getItem3());
        intent.putExtra("crbayar", (tunaiMode)? "T":"F"); // T : tunai, F : tempo
        intent.putExtra("tempo", edtTempo.getText().toString());
        intent.putExtra("kdgudang", selectedItem.getItem6());
        intent.putExtra("harga", selectedItem.getItem4());
        intent.putExtra("noba", edtNoBa.getText().toString());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(mBottomSheetBehavior2 != null && mBottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_EXPANDED){
            mBottomSheetBehavior2.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }else{
            super.onBackPressed();
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }
    }
}
