package thermaltag.thermaltag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class ScanLogListviewAdapter extends BaseAdapter{

    private final Context context;
    private final ArrayList<OysterScan> values;
    LayoutInflater inflater;
    View rowView;


    public ScanLogListviewAdapter(Context context, ArrayList<OysterScan> values){
        super();
        this.context = context;
        this.values = values;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Object getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        rowView=convertView;
        if(convertView==null){
            rowView = inflater.inflate(R.layout.listview_cardview , parent, false);
        }
        TextView id=(TextView)rowView.findViewById(R.id.TVID);
        TextView oystertype=(TextView)rowView.findViewById(R.id.TVOT);
        TextView qty=(TextView)rowView.findViewById(R.id.TVQTY);
        TextView status=(TextView)rowView.findViewById(R.id.TVSTATUS);

        OysterScan bean=values.get(position);

        id.setText(bean.id);
        oystertype.setText(bean.oyster_type);
        qty.setText(bean.quantity);
        status.setText(bean.status);
         return rowView;
    }
}
