package com.npf.main;

import com.npf.data.DataCache;
import com.npf.data.MapNode;
import com.npf.util.GPSLocation;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

public class NPFStartActivity extends Activity {
    private AutoCompleteTextView auto_origin, auto_dest;
    private String locations[];
    private DataCache dbcache;
    private InputManager im;
    private final int DIALOG_GPS_SRC=0;
    private final int DIALOG_GPS_DST=1;
    
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.get_input);
	    
	    dbcache = DataCache.getInstance(getApplicationContext());
        im = InputManager.getInstance(this);
	    locations=dbcache.getLocationNames();
	    
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.locationlist_item, locations);
	    
	    auto_origin = (AutoCompleteTextView) findViewById(R.id.autocomplete_origin);
	    auto_origin.setAdapter(adapter);
	    
	    auto_dest = (AutoCompleteTextView) findViewById(R.id.autocomplete_destination);
	    auto_dest.setAdapter(adapter);
	    
	    final Button src_btn = (Button) findViewById(R.id.locate_src_button);
	    src_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showDialog(DIALOG_GPS_SRC);
            }
        });
        final Button dst_btn = (Button) findViewById(R.id.locate_dst_button);
        dst_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showDialog(DIALOG_GPS_DST);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK&& requestCode == 0) {
			String desiredOrigin = data.getStringExtra("desiredOrigin");
			auto_origin.setText(desiredOrigin);
		}
	}
	
	protected void onPrepareDialog (int id, Dialog dialog) {
		
	}
	
	protected Dialog onCreateDialog (int id, Bundle args) {
		GPSLocation gpsLoc= new GPSLocation(this);
		Dialog d;
		switch (id) {
		case DIALOG_GPS_SRC:
			d = gpsLoc.createDialog(auto_origin);
			break;
		case DIALOG_GPS_DST:
			d = gpsLoc.createDialog(auto_dest);
			break;
		default:
			d=null;
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