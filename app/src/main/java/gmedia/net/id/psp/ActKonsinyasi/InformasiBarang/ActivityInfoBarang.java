package gmedia.net.id.psp.ActKonsinyasi.InformasiBarang;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.maulana.custommodul.ApiVolley;
import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.CustomView.DialogBox;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.ActKonsinyasi.Adapter.ListInfoBarangAdapter;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ServerURL;

public class ActivityInfoBarang extends AppCompatActivity {

	private List<CustomItem> listReller = new ArrayList<>();
	private ListView lvBarang;
	private SessionManager session;
	private ListInfoBarangAdapter adapterReseller;
	private View footerList;
	private Context context;
	private DialogBox dialogBox;
	private ItemValidation iv = new ItemValidation();
	private String keyword = "";
	private int start = 0, count = 10;
	private EditText edtSearch;
	private boolean isLoading = false;
	private TextView tvTotal, tvJml;
	private String kdcus = "";
	private String name[] =
			{
					"haii",
					"hoyuy",
					"haii",
					"hoyuy",
					"haii",
					"hoyuy",
			};
	private String alamat[] =
			{
					"haii",
					"hoyuy",
					"haii",
					"hoyuy",
					"haii",
					"hoyuy",
			};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info_barang);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
		);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle("Informasi Barang");
		}

		context = this;
		session = new SessionManager(context);
		initUI();
		initEvent();
		initData();
	}

	private void initUI() {

		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footerList = li.inflate(R.layout.footer_list, null);
		dialogBox = new DialogBox(context);
		lvBarang = (ListView) findViewById(R.id.lv_barang);
		edtSearch = (EditText) findViewById(R.id.edt_search);
		tvTotal = (TextView) findViewById(R.id.tv_total);
		tvJml = (TextView) findViewById(R.id.tv_jml);

		start = 0;
		count = 10;
		keyword = "";
		isLoading = false;

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {

			kdcus = bundle.getString("kdcus", "");
			String nama = bundle.getString("nama", "");
			getSupportActionBar().setSubtitle(nama);
		}

		lvBarang.addFooterView(footerList);
		adapterReseller = new ListInfoBarangAdapter((Activity) context, listReller);
		lvBarang.removeFooterView(footerList);
		lvBarang.setAdapter(adapterReseller);

		/*lvBarang.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int i) {

				int threshold = 1;
				int total = lvBarang.getCount();

				if (i == SCROLL_STATE_IDLE) {
					if (lvBarang.getLastVisiblePosition() >= total - threshold && !isLoading) {

						isLoading = true;
						start += count;
						initData();
					}
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i1, int i2) {

			}
		});*/
	}

	private void initEvent() {
		edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

				if (i == EditorInfo.IME_ACTION_SEARCH) {

					keyword = edtSearch.getText().toString();
					start = 0;
					listReller.clear();
					initData();

					iv.hideSoftKey(context);
					return true;
				}

				return false;
			}
		});
	}

	private void initData() {
		/*listReller = prepareDataEKupon();
		adapterReseller = new ListInfoBarangAdapter((Activity) context, listReller);
		lvBarang.setAdapter(adapterReseller);*/
        /*listReller.add(new CustomItem(
                "1"
                ,"Tetew"
                ,"Jangli"
        ));*/

        isLoading = true;
        if(start == 0) dialogBox.showDialog(true);
        JSONObject jBody = new JSONObject();
        lvBarang.addFooterView(footerList);

        try {
            jBody.put("keyword", keyword);
            jBody.put("kdcus", kdcus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolley request = new ApiVolley(context, jBody, "POST", ServerURL.viewBarangRekonsinyasi,"","",0,session.getUsername(),session.getPassword(), new ApiVolley.VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                lvBarang.removeFooterView(footerList);
                if(start == 0) dialogBox.dismissDialog();
                String message = "";
                isLoading = false;

                try {

                    JSONObject response = new JSONObject(result);
                    String status = response.getJSONObject("metadata").getString("status");
                    message = response.getJSONObject("metadata").getString("message");

                    double total = 0, jml = 0;
                    if(iv.parseNullInteger(status) == 200){

                        JSONArray jsonArray = response.getJSONArray("response");
                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jo = jsonArray.getJSONObject(i);
                            listReller.add(new CustomItem(
                                    jo.getString("kodebrg")
                                    ,jo.getString("namabrg")
                                    ,jo.getString("ccid")
                            ));

                            total += iv.parseNullDouble(jo.getString("harga"));
                            jml += iv.parseNullDouble(jo.getString("jumlah"));
                        }

                    }else{

                        if(start == 0) DialogBox.showDialog(context, 3, message);
                    }

                    tvTotal.setText(iv.ChangeToRupiahFormat(total));
                    tvJml.setText(iv.ChangeToCurrencyFormat(iv.doubleToString(jml)));

                } catch (JSONException e) {

                    e.printStackTrace();
                    View.OnClickListener clickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            dialogBox.dismissDialog();
                            initData();
                        }
                    };

                    dialogBox.showDialog(clickListener, "Ulangi Proses", "Terjadi kesalahan, harap ulangi proses");
                }

                adapterReseller.notifyDataSetChanged();
            }

            @Override
            public void onError(String result) {

                lvBarang.removeFooterView(footerList);
                isLoading = false;
                if(start == 0) dialogBox.dismissDialog();
                View.OnClickListener clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        dialogBox.dismissDialog();
                        initData();
                    }
                };

                dialogBox.showDialog(clickListener, "Ulangi Proses", "Terjadi kesalahan, harap ulangi proses");
            }
        });
	}

	private ArrayList<CustomItem> prepareDataEKupon() {
		ArrayList<CustomItem> rvData = new ArrayList<>();
		for (int i = 0; i < name.length; i++) {
			CustomItem custom = new CustomItem(String.valueOf(listReller.size()), name[i], alamat[i]);
			rvData.add(custom);
		}
		return rvData;
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
		super.onBackPressed();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}
}
