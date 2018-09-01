package moe.evelyn.albatross.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class Utils {
	public static String join(String[] a, String delimiter, Integer startIndex) {
		try {
			Collection<String> s = Arrays.asList(a);
			StringBuffer buffer = new StringBuffer();
			Iterator<String> iter = s.iterator();

			while (iter.hasNext()) {
				if (startIndex == 0) {
					buffer.append(iter.next());
					if (iter.hasNext()) {
						buffer.append(delimiter);
					}
				} else {
					startIndex--;
					iter.next();
				}
			}
			return buffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static String[] stripArray(String[] array){
		ArrayList<String> arguments=new ArrayList<String>();
		try{
			boolean b=false;
			for(String s:array){
				if(!b){b=true;continue;}
				arguments.add(s);
			}
		}catch(Exception e){}
		
		return arguments.toArray(new String[arguments.size()]);
	}
	
	public static String sit(String iStr, char delimiter, int part) {
		if (part == 0) {
			if (!iStr.contains(String.valueOf(delimiter)))
				return iStr;
		} else {
			if (!iStr.contains(String.valueOf(delimiter)))
				return "";
		}
		if (part == 0)
			return iStr.substring(0, (iStr.indexOf(delimiter, 0)));
		return iStr.substring(iStr.indexOf(delimiter, 0) + 1, iStr.length());
	}

}
