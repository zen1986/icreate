package com.npf.data;

/*
 * mapnodes: id, name, longitude, latitude, category, description
 * rooms: id, name, buildingID, level
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MapDB {
	
	//constants to indicate category
	public static final int BUILDING = 1;
	public static final int ROAD = 2;
	public static final int BUSSTOP = 3;

	//field names for mapNode
	public static final String MAP_ID = "id";
	public static final String MAP_NAME = "name";
	public static final String MAP_LONG = "longitude";
	public static final String MAP_LATI = "latitude";
	public static final String MAP_CATEGORY = "category";
	public static final String MAP_DESC = "description";
	
	//field names for room
	public static final String ROOM_ID = "id";
	public static final String ROOM_NAME = "name";
	public static final String ROOM_BUILDING = "buildingID";
	public static final String ROOM_LEVEL = "level";

	//database and table name
    private static final String DATABASE_NAME = "mapDb";
    private static final String DATABASE_TABLE_MAPNODE = "mapnodes";
    private static final String DATABASE_TABLE_ROOM = "rooms";
    private static final int DATABASE_VERSION = 2;
    
    //tag
    private static final String TAG = "MapDb";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    //create db
    private static final String DATABASE_CREATE =
    	"CREATE TABLE mapnodes (id INTEGER PRIMARY KEY, " +
    	"name TEXT NOT NULL, " +
    	"longitude DOUBLE, " +
    	"latitude DOUBLE, " +
    	"category INTEGER, " +
    	"description TEXT); " +
    	"CREATE TABLE rooms (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
    	"name TEXT RPIMARY KEY, " +
    	"FOREIGN KEY(buildingID) REFERENCES mapnodes(id), " +
    	"level INTEGER); ";


    private final Context mCtx;

    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS rooms");
            db.execSQL("DROP TABLE IF EXISTS mapnodes");
            onCreate(db);
        }
    }

    public MapDB(Context ctx) {
        this.mCtx = ctx;
    }

    public MapDB open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createMapNode(int id, String name, double longitude, 
    		double latitude, int c, String desc) {
    	
        ContentValues initialValues = new ContentValues();
        initialValues.put(MAP_ID, id);
        initialValues.put(MAP_NAME, name);
        initialValues.put(MAP_LONG , longitude);
        initialValues.put(MAP_LATI, latitude);
        initialValues.put(MAP_CATEGORY, c);
        initialValues.put(MAP_DESC, desc);
        
        return mDb.insert(DATABASE_TABLE_MAPNODE, null, initialValues);

    }
    
    public long createRoom(String name, int l, int bID) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(ROOM_NAME, name);
        initialValues.put(ROOM_BUILDING , bID);
        initialValues.put(ROOM_LEVEL, l);

        return mDb.insert(DATABASE_TABLE_ROOM, null, initialValues);
    }

    public boolean deleteMapNode(int rowId) {

        return mDb.delete(DATABASE_TABLE_MAPNODE, MAP_ID + "=" + rowId, null) > 0;
    }
    
    public boolean deleteRoom(int rowId) {

        return mDb.delete(DATABASE_TABLE_ROOM, ROOM_ID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllMapNodes() {

        return mDb.query(DATABASE_TABLE_MAPNODE, new String[] {MAP_ID, MAP_NAME,
                MAP_LONG, MAP_LATI, MAP_CATEGORY, MAP_DESC}, null, null, null, null, null);
    }
    
    public Cursor fetchAllRooms() {

        return mDb.query(DATABASE_TABLE_ROOM, new String[] {ROOM_ID, ROOM_NAME,
        		 ROOM_BUILDING, ROOM_LEVEL}, null, null, null, null, null);
    }

    public Cursor fetchMapNode(int rowId) throws SQLException {

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE_MAPNODE, new String[] {MAP_ID, MAP_NAME,
                    MAP_LONG, MAP_LATI, MAP_CATEGORY, MAP_DESC} ,MAP_ID + "=" + rowId,
                    null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchRoom(int rowId) throws SQLException {

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE_ROOM, new String[] {ROOM_ID, ROOM_NAME,
           		 ROOM_BUILDING, ROOM_LEVEL} ,MAP_ID + "=" + rowId,
                    null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public boolean updateMapNode(int id, String name, double longitude, 
    		double latitude, int c, String desc) {
        ContentValues args = new ContentValues();
        args.put(MAP_ID, id);
        args.put(MAP_NAME, name);
        args.put(MAP_LONG , longitude);
        args.put(MAP_LATI, latitude);
        args.put(MAP_CATEGORY, c);
        args.put(MAP_DESC, desc);

        return mDb.update(DATABASE_TABLE_MAPNODE, args, MAP_ID + "=" + id, null) > 0;
    }
    
    public boolean updateRoom(long id, String name, int l, int bID) {
        ContentValues args = new ContentValues();
        args.put(ROOM_NAME, name);
        args.put(ROOM_BUILDING , bID);
        args.put(ROOM_LEVEL, l);

        return mDb.update(DATABASE_TABLE_ROOM, args, ROOM_ID + "=" + id, null) > 0;
    }
}