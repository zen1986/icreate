package com.npf.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

public class GPSLocation {
	
	private Context ctx;
	
	public GPSLocation(final Context _ctx) {
		
		ctx = _ctx;
	}
	
	public Dialog createDialog(final EditText et) {
		final CharSequence[] items = {"Red", "Green", "Blue"};

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle("Pick nearby location");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	et.setText(items[item]);
		    }
		});
		return builder.create();
	}
}
