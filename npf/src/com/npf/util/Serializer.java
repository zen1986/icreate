package com.npf.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.util.Log;

public class Serializer {
	private static byte buf[];
	private static final String fileName="mapnodes";
	
	private static void serializeObject(Object o) { 
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
	 
	    try { 
	      ObjectOutput out = new ObjectOutputStream(bos); 
	      out.writeObject(o); 
	      out.close(); 
	      buf = bos.toByteArray(); 
	    } catch(IOException ioe) { 
	      Log.e("NPFdebug", "serializeObject error", ioe); 
	    } 
	}
	
	private static Object deserializeObject() { 
	    try { 
	      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf)); 
	      Object object = in.readObject(); 
	      in.close(); 
	 
	      return object; 
	    } catch(ClassNotFoundException cnfe) { 
	      Log.e("NPFdebug", "deserializeObject class not found error", cnfe); 
	 
	      return null; 
	    } catch(IOException ioe) { 
	      Log.e("NPFdebug", "deserializeObject io error", ioe); 
	 
	      return null; 
	    } 
	} 
	
	public static void saveObject(Object o, Context ctx){
		serializeObject(o);
		try {	
			FileOutputStream file=ctx.openFileOutput(fileName, 0);
			file.write(buf);
			file.close();
			buf=null;
		} catch (FileNotFoundException e) {
			Log.e("NPFdebug", "saveObject cannot found", e); 
		} catch (IOException e) {
			Log.e("NPFdebug", "saveObject IOE", e); 
		}
	}
	
	public static Object loadObject(Context ctx) {
		try {
			FileInputStream file=ctx.openFileInput(fileName);
			file.read(buf);
			file.close();
			return deserializeObject();
		} catch (FileNotFoundException e) {
			Log.e("NPFdebug", "loadObject file not found", e); 
			return null;
		} catch (IOException e) {
			Log.e("NPFdebug", "loadObject IO exception", e); 
			return null;
		}
	}
	
}
