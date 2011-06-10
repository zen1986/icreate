package com.npf.activity;

import java.util.ArrayList;

import com.npf.data.DataCache;
import com.npf.data.MapNode;
import com.npf.logic.ComUtil;
import com.npf.logic.GPSLocator;
import com.npf.logic.InputManager;
import com.npf.logic.Pathfinder;
import com.npf.main.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

public class NPFStartActivity extends Activity {
    private AutoCompleteTextView auto_origin, auto_dest;
    private String locations[];
    private DataCache dbcache;
    private InputManager im;
    private GPSLocator gps;
    //private final int WAITING = 0;
    private final int SHOWING = 1;
	private Location gpsLocation;
	private ProgressDialog progressDialog;
	private ListView routeList;
    private Pathfinder pf;
    private Button btnToMap;
    
    private Handler handler = new Handler()
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
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    
	    dbcache = DataCache.getInstance(getApplicationContext());
        im = InputManager.getInstance(this);
	    locations=dbcache.getLocationNames();
	    locations=ComUtil.decodeAll(locations);
	    
	    gps = new GPSLocator(this);
		
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.locationlist_item, locations);
	    
	    auto_origin = (AutoCompleteTextView) findViewById(R.id.autocomplete_origin);
	    auto_origin.setAdapter(adapter);
	    
	    auto_dest = (AutoCompleteTextView) findViewById(R.id.autocomplete_destination);
	    auto_dest.setAdapter(adapter);
	    
	    final Button src_btn = (Button) findViewById(R.id.locate_src_button);
	    src_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		
        		progressDialog = new ProgressDialog(NPFStartActivity.this);
        		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        		progressDialog.setMessage("Locating Current Position...");
        		progressDialog.setCancelable(false);
        		progressDialog.show();
        		
        		gps.startUpdate();
                progressDialog.show();
                
                //thread loading the 1st GPS location
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
        
    	routeList = (ListView) findViewById(R.id.route_list);
    	
    	btnToMap = new Button(NPFStartActivity.this);
    	btnToMap.setText("Go to map");
		routeList.addFooterView(btnToMap);
        
        final Button submit = (Button) findViewById(R.id.get_location);
        submit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MapNode s_node, d_node;
				
				s_node=dbcache.getNodeByLocationName(ComUtil.encode(auto_origin.getText().toString()));  //encode to compare values from database
				d_node=dbcache.getNodeByLocationName(ComUtil.encode(auto_dest.getText().toString()));
		        
				if (s_node !=null && d_node !=null) {
					im.setDestinationLocation(d_node.name);
					im.setSourceLocation(s_node.name);
					
			    	pf = new Pathfinder(s_node,d_node);
			    	ArrayList<MapNode> pathNodes =pf.getPath();
			    	ArrayList<String> nodes = new ArrayList<String>();
			    	String[] str_nodes = new String[pathNodes.size()+1];
			    	nodes.add("Follow the path below:");
			    	
			    	//need to add path node from behind to front
			    	for (int i=pathNodes.size()-1; i>=0;i--) {
			    		MapNode n = pathNodes.get(i);
			    		nodes.add(n.name);
			    	}
			    	
			    	nodes.toArray(str_nodes);

					routeList.setAdapter(new ArrayAdapter<String>(NPFStartActivity.this, R.layout.routelist_item, str_nodes));
					
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					
					btnToMap.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(NPFStartActivity.this,NPFMapActivity.class);
							startActivity(intent);
						}
					});
				}
			}
		});
	}
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*if (resultCode == RESULT_OK&& requestCode == 0) {
			String desiredOrigin = data.getStringExtra("desiredOrigin");
			auto_origin.setText(desiredOrigin);
		}*/
	}
	
	protected void onPrepareDialog (int id, Dialog dialog) {
		
	}
	
	protected Dialog onCreateDialog (int id) {
		return gps.getDialog(auto_origin);
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