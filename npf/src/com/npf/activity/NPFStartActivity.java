package com.npf.activity;

import com.npf.data.DataCache;
import com.npf.data.MapNode;
import com.npf.gps.GPSLocator;
import com.npf.logic.InputManager;
import com.npf.main.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

public class NPFStartActivity extends Activity {
    private AutoCompleteTextView auto_origin, auto_dest;
    private String locations[];
    private DataCache dbcache;
    private InputManager im;
    private GPSLocator gps;
    private final int WAITING = 0;
    private final int SHOWING = 1;
	private Location gpsLocation;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    
	    dbcache = DataCache.getInstance(getApplicationContext());
        im = InputManager.getInstance(this);
	    locations=dbcache.getLocationNames();
	    
	    gps = new GPSLocator(this);
		
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.locationlist_item, locations);
	    
	    auto_origin = (AutoCompleteTextView) findViewById(R.id.autocomplete_origin);
	    auto_origin.setAdapter(adapter);
	    
	    auto_dest = (AutoCompleteTextView) findViewById(R.id.autocomplete_destination);
	    auto_dest.setAdapter(adapter);
	    

	    
	    
	    final Button src_btn = (Button) findViewById(R.id.locate_src_button);
	    src_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		gps.startUpdate();
        		
        		progressDialog = new ProgressDialog(NPFStartActivity.this);
        		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        		progressDialog.setMessage("Loading...");
        		progressDialog.setCancelable(false);
        		progressDialog.show();
        		
                progressDialog.show();
                
        		new Thread() {
        			public void run() {
        				while (true) {
        					gpsLocation = gps.getBestKnowLocation();
        					if (gpsLocation !=null) {
        						handler.obtainMessage(0).sendToTarget();
        						break;
        					}
        				}
        			}
        			
        		}.start();
        		
            }
        });
        
        
        final Button submit = (Button) findViewById(R.id.get_location);
        submit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MapNode s_node, d_node;
				
				s_node=dbcache.getNodeByName(auto_origin.getText().toString());
				d_node=dbcache.getNodeByName(auto_dest.getText().toString());
		        
				if (s_node !=null && d_node !=null) {
					im.setDestinationLocation(d_node.name);
					im.setSourceLocation(s_node.name);
					Intent intent = new Intent(NPFStartActivity.this,NPFMapActivity.class);
					startActivity(intent);
				}
			}
		});
        
	}
	
    Handler handler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case 0:
                	progressDialog.dismiss();
                	gps.stopUpdate();

                	showDialog(SHOWING);
                	break;
            }
        }
    };
    
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK&& requestCode == 0) {
			String desiredOrigin = data.getStringExtra("desiredOrigin");
			auto_origin.setText(desiredOrigin);
		}
	}
	
	protected void onPrepareDialog (int id, Dialog dialog) {
		
	}
	
	protected Dialog onCreateDialog (int id) {
		Dialog d;
		switch (id) {
		case WAITING:
			ProgressDialog dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
			d = dialog;
			break;
		case SHOWING:
			final CharSequence[] items = {"Red", "Green", "Blue"};
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Pick nearby location");
			builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	auto_origin.setText(items[item]);
			    }
			});
			AlertDialog n =  builder.create();
			d = n;
			break;
		default:
			d = null;
		}
		return d;
	}
	
	@Override 
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}