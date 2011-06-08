package com.npf.main;

import com.npf.map.MapManager;
import com.npf.util.GPSLocation;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

public class NPFStartActivity extends Activity {
    private AutoCompleteTextView auto_origin, auto_dest;
    private String locations[];
    private MapManager mm;
    private int size;    
    private Bitmap map;
    private Bitmap marker;
    private final int DIALOG_GPS_SRC=0;
    private final int DIALOG_GPS_DST=1;
    
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.get_input);
	    
	    mm=MapManager.getInstance();
	    mm.loadData(this);
        if (map==null) map = BitmapFactory.decodeResource(getResources(), R.drawable.nus);
        if (marker==null) marker = BitmapFactory.decodeResource(getResources(), R.drawable.pin);

        mm.setBitmaps(map, marker);
        Cursor c=mm.getNodes();
	    
        size=c.getCount();
        
	    locations=new String[size];
	    
	    Log.i("NPFdebug", "nodes size "+c.getColumnCount()+" "+c.getCount());
	    int i=0;
	    while (c.moveToNext()) {
			locations[i]=c.getString(c.getColumnIndexOrThrow("name"));
			i++;
	    }; 
	    
	    c.close();
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
				String origin, dest;
				Cursor o_node, d_node;
				int p1u,p1v,p2u,p2v;
				origin = auto_origin.getText().toString();
				dest = auto_dest.getText().toString();
				o_node=mm.getNode(origin);
				d_node=mm.getNode(dest);
		        
				p1u=(int) (o_node.getDouble(o_node.getColumnIndexOrThrow("texu")) * map.getWidth());
				p1v=(int)  (o_node.getDouble(o_node.getColumnIndexOrThrow("texv")) * map.getHeight());
				p2u=(int) (d_node.getDouble(o_node.getColumnIndexOrThrow("texu")) * map.getWidth());
				p2v=(int)  (d_node.getDouble(o_node.getColumnIndexOrThrow("texv")) * map.getHeight());
				
				mm.resetMarkers();
				mm.locatePoint(p1u,p1v);
				mm.locatePoint(p2u,p2v);
				o_node.close();
				d_node.close();
				Intent intent = new Intent(NPFStartActivity.this,NPFMapActivity.class);
				startActivity(intent);
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

		mm.closeDb();
	}
}