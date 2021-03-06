package gmedia.net.id.psp.NavKunjungan.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.psp.R;


/**
 * Created by Shin on 1/8/2017.
 */

public class KunjunganOutletAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public KunjunganOutletAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.cv_list_kunjungan_outlet, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvItem1, tvItem2, tvItem3;
    }

    public void addMoreData(List<CustomItem> moreData){

        items.addAll(moreData);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.cv_list_kunjungan_outlet, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            holder.tvItem3 = (TextView) convertView.findViewById(R.id.tv_item3);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText(itemSelected.getItem2());
        holder.tvItem2.setText(itemSelected.getItem3());

        String keterangan = "";
        if(iv.parseNullDouble(itemSelected.getItem7()) <= 6371){
            if(iv.parseNullDouble(itemSelected.getItem7()) <= 1){
                keterangan = iv.doubleToString(iv.parseNullDouble(itemSelected.getItem7()) * 1000, "2") + " m";
            }else{
                keterangan = iv.doubleToString(iv.parseNullDouble(itemSelected.getItem7()), "2") + " km";
            }
        }else{
            keterangan = "Jarak outlet tidak diketahui";
        }

        holder.tvItem3.setText(keterangan);
        return convertView;

    }
}
