package com.npf.logic;

public class ComUtil {
	public static String decode(String s) {
		s=s.replace("&amp;", "&");
		s=s.replace("&apos;", "'");
		return s;
	}
	public static String encode(String s) {
		s=s.replace("&", "&amp;");
		s=s.replace("'", "&apos;");
		return s;
	}
	public static String[] decodeAll(String[] ss) {
		String [] ret = new String[ss.length];
		int i=0;
		for (String s:ss) {
			ret[i++]=decode(s);
		}
		return ret;
	}
}
