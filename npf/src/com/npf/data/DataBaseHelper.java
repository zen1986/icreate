package com.npf.data;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper{
	 
    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.npf.main/databases/";
 
    private static String DB_NAME = "npfdb";
 
    private SQLiteDatabase myDataBase; 
 
    private final Context myContext;
    
    public DataBaseHelper(Context context) {
 
    	super(context, DB_NAME, null, 1);
        this.myContext = context;
     
        try {
        	createDataBase();
        	Log.i("NPFdebug", "Done setup DataBase");
        } catch (IOException ioe) {
 
        	throw new Error("Unable to create database");
        }
        try {
        	openDataBase();
        }catch(SQLException sqle){
 
        	throw sqle;
        }
    }
 

    public void createDataBase() throws IOException{
 
    	boolean dbExist = checkDataBase();
 
    	if(dbExist){
    	}else{
    		this.getReadableDatabase();
        	try {
    			copyDataBase();
    		} catch (IOException e) {
        		throw new Error("Error copying database");
        	}
    	}
    }
 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    		Log.i("NPFdebug", "Database exist!");
 
    	}catch(SQLiteException e){
    		//database does't exist yet.
    	}
    	if(checkDB != null){
    		checkDB.close();
    	}
    	return checkDB != null ? true : false;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{
 
    	Log.i("NPFdebug", "copy database");
    	InputStream myInput = myContext.getAssets().open(DB_NAME);
 
    	String outFileName = DB_PATH + DB_NAME;
 
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
 
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
 
    }
    
    public void openDataBase() throws SQLException{
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }
    
    @Override
	public synchronized void close() {
 
    	if(myDataBase != null)
    		myDataBase.close();
    	super.close();
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
 
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
	}
	
	//////////////////////query related functions//////////////////////////////////////
	
    public Cursor fetchBuildingNode(String nm) throws SQLException {
    	String sql="select * from building where name=?";
        Cursor mCursor = myDataBase.rawQuery(sql,new String[] {nm} );
        if (mCursor!=null) mCursor.moveToFirst();
        return mCursor;
    }
    
    public Cursor fetchAllBuildingNodes() {
        return myDataBase.rawQuery("select * from building", null);
    }
    
    public Cursor fetchNodeNeighbor(int id) {
    	return myDataBase.rawQuery("select node2 from building_neighbour where node1=?", new String[] {Integer.toString(id)});
    }
    public Cursor fetchLocationByBuildingName(String name) {
    	return myDataBase.rawQuery("select * from location where building=?", new String[] {name});
    }
    public Cursor fetchLocationByName(String name) {
    	return myDataBase.rawQuery("select * from location where name=?", new String[] {name});
    }
    public Cursor fetchAllBusstopNodes() {
    	return myDataBase.rawQuery("select * from busstop", null);
    }
    public Cursor fetchBusesForBusstop(int id) {
    	return myDataBase.rawQuery("select * from bus_busstop where busstop_id = ?", new String[] {Integer.toString(id)});
    }
    public Cursor fetchRouteSeq(int bus_id, int buss_id) {
    	return myDataBase.rawQuery("select * from busroute where bus_id=? and busstop_id=?", new String[] {Integer.toString(bus_id), Integer.toString(buss_id)});
    }
    public Cursor fetchBusstopBySeqAndBus(int seq, int bus_id) {
    	return myDataBase.rawQuery("select * from busroute where seq=? and bus_id = ?", new String[] {Integer.toString(seq), Integer.toString(bus_id)});
    }
    public boolean isBusLoop(int id) {
    	Cursor c = myDataBase.rawQuery("select * from bus where _id = ?", new String[]{Integer.toString(id)});
    	if (!c.moveToFirst()) return false;
    	int loop = c.getInt(c.getColumnIndexOrThrow("loop"));
    	return loop==1;
    }
}