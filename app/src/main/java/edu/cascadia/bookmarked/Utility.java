package edu.cascadia.bookmarked;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.preference.PreferenceManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.util.Base64;

/**
 * Class which has Utility methods
 * 
 */
public class Utility {
	private final static String defServer = "mytomcatapp-bookmarked.rhcloud.com";
	private static Pattern pattern;
	private static Matcher matcher;
	//Email Pattern
	private static final String EMAIL_PATTERN = 
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	/**
	 * Validate Email with regular expression
	 * 
	 * @param email
	 * @return true for Valid Email and false for Invalid Email
	 */
	public static boolean validateEmail(String email) {
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(email);
		return matcher.matches();
 
	}

	// user can enter a 5-digit zipcode
	// just do simple validation for now
	public static boolean validateZipcode(String zipcode) {
		return (zipcode.length() == 5);
	}
	/**
	 * Checks for Null String object
	 * 
	 * @param txt
	 * @return true for not null and false for null String object
	 */
	public static boolean isNotNull(String txt){
		return txt!=null && txt.trim().length()>0 ? true: false;
	}


	public static String getServerAddress(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("prefs_server", defServer);
	}

	public static void beep() {
		final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
		tg.startTone(ToneGenerator.TONE_PROP_BEEP);
	}

	/**
	 * Encodes the byte array into base64 string
	 * @param imageByteArray - byte array
	 * @return String a {@link java.lang.String}
	 */
	public static String encodeImage(byte[] imageByteArray) {
		return Base64.encodeToString(imageByteArray, Base64.DEFAULT);
	}

	/**
	 * Decodes the base64 string into byte array
	 * @param imageDataString - a {@link java.lang.String}
	 * @return byte array
	 */
	public static byte[] decodeImage(String imageDataString) {
		return Base64.decode(imageDataString, Base64.DEFAULT);
	}

}
