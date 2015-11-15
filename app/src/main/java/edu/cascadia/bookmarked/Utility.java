package edu.cascadia.bookmarked;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.preference.PreferenceManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
}
