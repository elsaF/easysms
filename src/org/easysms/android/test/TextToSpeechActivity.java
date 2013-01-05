package org.easysms.android.test;

import java.util.Locale;

import org.easysms.android.R;
import org.easysms.android.R.id;
import org.easysms.android.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TextToSpeechActivity extends Activity implements
		TextToSpeech.OnInitListener {

	private static final String TAG = "TextToSpeechDemo";
	private Button mAgainButton;
	private TextToSpeech mTts;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.text_to_speech);

		// Initialize text-to-speech. This is an asynchronous operation.
		// The OnInitListener (second argument) is called after initialization
		// completes.
		mTts = new TextToSpeech(this, this);

		// The button is disabled in the layout.
		// It will be enabled upon initialization of the TTS engine.
		mAgainButton = (Button) findViewById(R.id.btnClick);

		mAgainButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				say("Bonjour!!!");
			}
		});
	}

	@Override
	public void onDestroy() {
		// do not forget to shutdown!
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}

		super.onDestroy();
	}

	// Implements TextToSpeech.OnInitListener.
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
		if (status == TextToSpeech.SUCCESS) {
			// Set preferred language to US English.
			// Note that a language may not be available, and the result will
			// indicate this.
			int result = mTts.setLanguage(Locale.US);
			// Try this someday for some interesting results.
			// int result mTts.setLanguage(Locale.FRANCE);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// language data is missing or the language is not supported.
				Log.e(TAG, "Language is not available.");
			} else {
				// Check the documentation for other possible result codes.
				// For example, the language may be available for the locale,
				// but not for the specified country and variant.

				// The TTS engine has been successfully initialized.
				// Allow the user to press the button for the app to speak
				// again.
				mAgainButton.setEnabled(true);
				// Greet the user.
				// sayHello();
			}
		} else {
			// Initialization failed.
			Log.e(TAG, "Could not initialize TextToSpeech.");
		}
	}

	private void say(String sentence) {
		mTts.setLanguage(Locale.FRENCH);
		// drop all pending entries in the playback queue.
		mTts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);
	}
}
