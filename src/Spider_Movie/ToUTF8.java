package Spider_Movie;

public class ToUTF8
{
	public static String toUtf8String(String s) {  
	    if (s == null || s.equals("")) {  
	        return null;  
	    }  
	    StringBuffer sb = new StringBuffer();  
	    try {  
	        char c;  
	        for (int i = 0; i < s.length(); i++) {  
	            c = s.charAt(i);  
	            if (c >= 0 && c <= 255) {  
	                sb.append(c);  
	            } else {  
	                byte[] b;  
	                b = Character.toString(c).getBytes("utf-8");  
	                for (int j = 0; j < b.length; j++) {  
	                    int k = b[j];  
	                    if (k < 0)  
	                        k += 256;  
	                    sb.append("%" + Integer.toHexString(k).toUpperCase());  
	                }  
	            }  
	        }  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	    return sb.toString();  
	}  
}
