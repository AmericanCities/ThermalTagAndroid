package thermaltag.thermaltag;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

public class ScanLogActivity extends AppCompatActivity {

    Spinner spinner;
    ListView listView;
    EditText ETsearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        Bundle bundle = getIntent().getExtras();
        final String username = bundle.getString("username");
        //Toast.makeText(getApplicationContext(),username,Toast.LENGTH_LONG).show();
        listView=(ListView)findViewById(R.id.listView);
        spinner=(Spinner)findViewById(R.id.spinner);
        ETsearch=(EditText)findViewById(R.id.ETsearch);

        ETsearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                //Toast.makeText(getApplicationContext(),""+keyCode,Toast.LENGTH_LONG).show();
                if(keyCode==66){
                    String idd=ETsearch.getText().toString();
                    populateList(username,idd,"");
                }

                return false;
            }
        });

        final String[] test=new String[]{"Select All  ","Barcats","Rappahannock","cape May salt","Duxbury","Gallon Selects","Snow Hills","Moonstone","wellfleet Petite","Stony Brook"};
        ArrayAdapter<String> adapter= new ArrayAdapter<String>(ScanLogActivity.this,R.layout.spinner_card_view, test);
        spinner.setAdapter(adapter);

        populateList(username);



        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    populateList(username);

                    return;}
                String oystertype=test[position];
                populateList(username,oystertype);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public void populateList(String username){
        RequestQueue queue = Volley.newRequestQueue(ScanLogActivity.this);
        String url = "http://thermaltag.netau.net/androidwebservice/thirdPageData.php?username="+username+"";

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                ArrayList<OysterScan> list=new ArrayList<OysterScan>();
                OysterScan tagSearch=new OysterScan();
                tagSearch.id="ID";
                tagSearch.oyster_type="Oyster Type";
                tagSearch.quantity="Quantity";
                tagSearch.status="Status";
                list.add(tagSearch);

                if(response.equals("")){
                    ScanLogListviewAdapter adapter=new ScanLogListviewAdapter(getApplicationContext(),list);
                    listView.setAdapter(adapter);

                    return;
                }
                // Why did we name this beansarray?
                String beansarray[]=response.split(";");
                for(int i=0;i<beansarray.length;i++){
                    String bean[]=beansarray[i].split(",");
                    OysterScan oysterScan =new OysterScan();
                    oysterScan.id=bean[0];
                    oysterScan.oyster_type=bean[1];
                    oysterScan.quantity=bean[2];
                    oysterScan.status=bean[3];
                    list.add(oysterScan);
                }

                ScanLogListviewAdapter adapter=new ScanLogListviewAdapter(getApplicationContext(),list);
                listView.setAdapter(adapter);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Network "+error,Toast.LENGTH_LONG).show();

            }
        });
        queue.add(strReq);
   }



    public void populateList(String username,String ostertype){
        RequestQueue queue = Volley.newRequestQueue(ScanLogActivity.this);
        String url = "http://thermaltag.netau.net/androidwebservice/thirdPageDataFilterType.php?username="+username+"&oyster_type="+ostertype+"";

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                ArrayList<OysterScan> list=new ArrayList<OysterScan>();
                OysterScan tagSearch=new OysterScan();
                tagSearch.id="ID";
                tagSearch.oyster_type="Oyster Type";
                tagSearch.quantity="Quantity";
                tagSearch.status="Status";
                list.add(tagSearch);

                if(response.equals("")){
                    ScanLogListviewAdapter adapter=new ScanLogListviewAdapter(getApplicationContext(),list);
                    listView.setAdapter(adapter);

                    return;}
                String beansarray[]=response.split(";");
                for(int i=0;i<beansarray.length;i++){
                    String bean[]=beansarray[i].split(",");
                    OysterScan oysterScan =new OysterScan();
                    oysterScan.id=bean[0];
                    oysterScan.oyster_type=bean[1];
                    oysterScan.quantity=bean[2];
                    oysterScan.status=bean[3];
                    list.add(oysterScan);
                }

                ScanLogListviewAdapter adapter=new ScanLogListviewAdapter(getApplicationContext(),list);
                listView.setAdapter(adapter);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Network "+error,Toast.LENGTH_LONG).show();

            }
        });
        queue.add(strReq);
    }



    public void populateList(String username,String id,String temp){
        RequestQueue queue = Volley.newRequestQueue(ScanLogActivity.this);
        String url = "http://thermaltag.netau.net/androidwebservice/thirdPageDataFilterTypeId.php?username="+username+"&id="+id+"";
        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ArrayList<OysterScan> list=new ArrayList<OysterScan>();
                OysterScan tagSearch=new OysterScan();
                tagSearch.id="ID";
                tagSearch.oyster_type="Oyster Type";
                tagSearch.quantity="Quantity";
                tagSearch.status="Status";
                list.add(tagSearch);

                if(response.equals("")){
                    ScanLogListviewAdapter adapter=new ScanLogListviewAdapter(getApplicationContext(),list);
                    listView.setAdapter(adapter);

                    return;}
                String beansarray[]=response.split(";");
                for(int i=0;i<beansarray.length;i++){
                    String bean[]=beansarray[i].split(",");
                    OysterScan oysterScan =new OysterScan();
                    oysterScan.id=bean[0];
                    oysterScan.oyster_type=bean[1];
                    oysterScan.quantity=bean[2];
                    oysterScan.status=bean[3];
                    list.add(oysterScan);
                }

                ScanLogListviewAdapter adapter=new ScanLogListviewAdapter(getApplicationContext(),list);
                listView.setAdapter(adapter);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Network "+error,Toast.LENGTH_LONG).show();

            }
        });
        queue.add(strReq);
    }
}