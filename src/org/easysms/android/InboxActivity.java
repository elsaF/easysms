package org.easysms.android;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.easysms.android.data.Conversation;
import org.easysms.android.data.Sms;
import org.easysms.android.ui.FlowLayout;
import org.easysms.android.util.ApplicationTracker;
import org.easysms.android.util.ApplicationTracker.EventType;
import org.easysms.android.util.TextToSpeechManager;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

@TargetApi(8)
public class InboxActivity extends SherlockListActivity {

	/** Flag used to determine if the help message has already being shown. */
	public static boolean HELP_MESSAGE = false;

	// list of hash map for the message threads
	private static final ArrayList<HashMap<String, Object>> mMessageList = new ArrayList<HashMap<String, Object>>();
	protected Boolean mIsComplete = false;
	/** Timer used to refresh the Message list. */
	protected Handler mTaskHandler = new Handler();
	/** LinearLayout used to show the help message. */
	private LinearLayout mThreadPage;

	public void displayListSMS() {

		smsAllRetrieve();
		final ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(InboxActivity.this,
						MessagingActivity.class);

				Object selectedFromList = (lv.getItemAtPosition(position));
				HashMap<String, Object> o = (HashMap<String, Object>) mMessageList
						.get(position);
				String telnum = "unknown";
				String name = "unknown";
				Object telnumobj = o.get("telnumber");
				if (telnumobj != null)
					telnum = telnumobj.toString();
				Object nameobj = o.get("name");
				if (nameobj != null) {
					name = nameobj.toString();
				}

				// Next create the bundle and initialize it
				Bundle bundle = new Bundle();
				// Add the parameters to bundle
				bundle.putString("Name", name);
				bundle.putString("Tel", telnum);
				bundle.putBoolean("NewMsg", false);
				// Add this bundle to the intent
				i.putExtras(bundle);
				startActivity(i);

			}
		});
		final SimpleAdapter adapter = new SimpleAdapter(this, mMessageList,
				R.layout.custom_row_view, new String[] { "avatar", "telnumber",
						"date", "name", "message", "sent" }, new int[] {
						R.id.contact_image, R.id.text1, R.id.text2, R.id.text3,
						R.id.text4, R.id.isent });

		setListAdapter(adapter);
	}

	private String getContactNameFromNumber(String number) {
		/*
		 * We have a phone number and we want to grab the name of the contact
		 * with that number, if such a contact exists
		 */
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		/* phoneNumber here being a variable with the phone number stored. */
		Cursor c = getBaseContext().getContentResolver().query(lookupUri,
				new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		/*
		 * If we want to get something other than the displayed name for the
		 * contact, then just use something else instead of DISPLAY_NAME
		 */
		String name = "Contact inconnu";
		while (c.moveToNext()) {
			// if we find a match we put it in a String.
			name = c.getString(c
					.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
		}
		return name;
	}

	private String getContactPhotoFromNumber(String number) {
		/*
		 * We have a phone number and we want to grab the name of the contact
		 * with that number, if such a contact exists
		 */
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		/* phoneNumber here being a variable with the phone number stored. */
		Cursor c = getBaseContext().getContentResolver().query(lookupUri,
				new String[] { PhoneLookup.PHOTO_ID }, null, null, null);
		// long photo =
		// c.getLong(c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_ID));
		/*
		 * If we want to get something other than the displayed name for the
		 * contact, then just use something else instead of DISPLAY_NAME
		 */
		String photoId = null;
		while (c.moveToNext()) {
			// if we find a match we put it in a String.
			photoId = c
					.getString(c.getColumnIndexOrThrow(PhoneLookup.PHOTO_ID));
		}
		return photoId;
	}

	/**
	 * Gets the tag used to log the actions performed by the user. The tag is
	 * obtained from the name of the class.
	 * 
	 * @return the tag that represents the class.
	 */
	public String getLogTag() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);

		// sets the theme used throughout the application.
		setTheme(EasySmsApp.THEME);

		// sets the desired layout for the activity.
		setContentView(R.layout.act_main);

		// initializes the TextToSpeech
		TextToSpeechManager.init(getApplicationContext());

		// sets the device id which will be used to track the activity of all
		// phones.
		ApplicationTracker.getInstance().setDeviceId(
				((EasySmsApp) getApplication()).getDeviceId());

		mThreadPage = (LinearLayout) findViewById(R.id.threadpage);

		displayListSMS();

		// le timer fait ramer toute l'application!!! trouver un autre moyen
		// ==> retrieve a signal when a new msg is received.
		// -------------------timer------------------------
		final long elapse = 10000;
		Runnable t = new Runnable() {
			public void run() {
				// Toast.makeText(getApplicationContext(), "dans timer",
				// Toast.LENGTH_SHORT).show();
				mMessageList.clear();
				displayListSMS();
				if (!mIsComplete) {
					mTaskHandler.postDelayed(this, elapse);
				}
			}
		};
		mTaskHandler.postDelayed(t, elapse);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_activity, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_new_message:

			// creates an intent for the next screen.
			Intent i = new Intent(InboxActivity.this, MessagingActivity.class);
			// creates and initializes a new bundle.
			Bundle bundle = new Bundle();
			// indicates that a new message is being created.
			bundle.putBoolean(MessagingActivity.NEW_MESSAGE_EXTRA, true);
			// adds the bundle to the intent.
			i.putExtras(bundle);
			startActivity(i);

			// tracks the user activity.
			ApplicationTracker.getInstance().logEvent(EventType.CLICK,
					getLogTag(), R.id.menu_new_message);

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void playKaraoke(final FlowLayout fl) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				for (int i = 1; i < fl.getChildCount(); ++i) {
					final Button btn = (Button) fl.getChildAt(i);
					btn.setFocusableInTouchMode(true);
					try {
						Thread.sleep(800);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// mHandler.post(new Runnable() {
					// @Override
					// public void run() {
					// // progress.setProgress(value);
					// btn.requestFocus();
					// TextToSpeechManager.getInstance().say(
					// (String) btn.getText());
					// }
					// });
				}
			}
		};
		new Thread(runnable).start();

	}

	private List<Conversation> populateList(List<Sms> allSMS) {

		// list with all the conversations
		List<Conversation> allconversations = new ArrayList<Conversation>();
		for (Sms smsnew : allSMS) {
			boolean add = false;
			// case where the SMS is a draft, otherwise the pp bugs.
			if (smsnew.contact != null) {
				for (Conversation conv : allconversations) {
					if (conv.threadid.equals(smsnew.threadid)) {
						conv.listsms.add(smsnew);
						add = true;
					}
				}
				if (add == false) { // we create a new conversation
					Conversation newconv = new Conversation();
					List<Sms> newlist = new ArrayList<Sms>();
					newlist.add(smsnew);
					newconv.listsms = newlist;
					newconv.threadid = smsnew.threadid;
					allconversations.add(newconv);
				}

			}
		}

		for (Conversation conv : allconversations) {
			HashMap<String, Object> temp2 = new HashMap<String, Object>();
			// on regarde le 1er sms de chaque liste

			Sms firstsms = conv.listsms.get(0);
			// get name associated to phone number
			String name = getContactNameFromNumber(firstsms.contact);
			String photoid = getContactPhotoFromNumber(firstsms.contact);
			if (photoid == null) {
				temp2.put("avatar", R.drawable.nophotostored);

			} else {

				Cursor photo2 = getContentResolver().query(Data.CONTENT_URI,
						new String[] { Photo.PHOTO }, // column for the blob
						Data._ID + "=?", // select row by id
						new String[] { photoid }, // filter by photoId
						null);
				Bitmap photoBitmap = null;
				if (photo2.moveToFirst()) {
					byte[] photoBlob = photo2.getBlob(photo2
							.getColumnIndex(Photo.PHOTO));
					photoBitmap = BitmapFactory.decodeByteArray(photoBlob, 0,
							photoBlob.length);

					if (photoBitmap != null) {
						temp2.put("avatar", R.drawable.nophotostored);
					} else {
						temp2.put("avatar", R.drawable.nophotostored);
					}

				}
				photo2.close();

			}
			temp2.put("telnumber", firstsms.contact);

			final Calendar c = Calendar.getInstance();
			// int date = Calendar.DATE;
			int mYear = c.get(Calendar.YEAR);
			int mMonth = c.get(Calendar.MONTH) + 1;
			int mDay = c.get(Calendar.DAY_OF_MONTH);
			// String dateToday = mYear + "-" + mMonth + "-" + mDay;
			String dateToday = mDay + "-" + mMonth + "-" + mYear;
			String datesms = firstsms.datesms;
			if (dateToday.equals(datesms)) {
				temp2.put("date", firstsms.timesms);
			} else {
				temp2.put("date", firstsms.datesms);
			}
			temp2.put("name", name);
			temp2.put("message", firstsms.body);
			if (firstsms.sent == "yes") {
				temp2.put("sent", R.drawable.ic_action_send);
			} else if (firstsms.sent == "no") {
				temp2.put("sent", R.drawable.received);
			}

			mMessageList.add(temp2);
		}

		return allconversations;
	}

	// return a list with all the SMS and for each sms a status sent: yes or no
	public void smsAllRetrieve() {
		// we put all the SMS sent and received in a list
		List<Sms> allSMSlocal = new ArrayList<Sms>();
		Uri uriSMSURIinbox = Uri.parse("content://sms/");
		Cursor curinbox = getContentResolver().query(uriSMSURIinbox, null,
				null, null, null);
		if (curinbox.moveToFirst()) {
			long datesms = 0;
			String phoneNumber = null;
			String body = null;
			String threadid;
			String datestring = "Date inconnue";
			String timestring = "Heure inconnue";
			Date dateFromSms = null;
			int type = -1;
			int read = -1;
			String dateTimeString = "erreur date";
			// return -1 if the column does not exist.
			int dateColumn = curinbox.getColumnIndex("date");
			int numberColumn = curinbox.getColumnIndex("address");
			int bodyColumn = curinbox.getColumnIndex("body");
			int threadColumn = curinbox.getColumnIndex("thread_id");
			int typeColumn = curinbox.getColumnIndex("type");
			int typeRead = curinbox.getColumnIndex("read");
			do {
				if (dateColumn != -1) {
					datesms = curinbox.getLong(dateColumn);
					dateTimeString = (String) android.text.format.DateFormat
							.format("yyyy-MM-dd'T'kk:mm:ss'Z'", datesms);
					// datestring = (String)
					// android.text.format.DateFormat.format("yyyy-MM-dd",datesms);
					datestring = (String) android.text.format.DateFormat
							.format("dd-MM-yyyy", datesms);
					timestring = (String) android.text.format.DateFormat
							.format("kk:mm", datesms);
					// Toast.makeText(getApplicationContext(), datestring,
					// Toast.LENGTH_SHORT).show();
					dateFromSms = new Date(datesms);

				}
				if (typeRead != -1)
					read = curinbox.getInt(typeRead);
				if (typeColumn != -1)
					type = curinbox.getInt(typeColumn);
				if (bodyColumn != -1)
					body = curinbox.getString(bodyColumn);
				if (numberColumn != -1)
					phoneNumber = curinbox.getString(numberColumn);
				if (threadColumn != -1)
					threadid = curinbox.getString(threadColumn);
				else
					threadid = "nada";
				Sms smsnew;

				if (phoneNumber != null) {
					smsnew = new Sms("unknown", threadid, datestring,
							timestring, phoneNumber, body, read);

					// to know if it is a message sent or received
					if (type == 2) { // SENT
						smsnew.sent = "yes";
					} else if (type == 1) { // INBOX
						smsnew.sent = "no";
					}
					if (read == 0) { // message is not read
						smsnew.read = 0;
					} else if (read == 1) { // message is read
						smsnew.read = 1;
					}
					// we add this SMS to the list of all the SMS
					allSMSlocal.add(smsnew);
				}

			} while (curinbox.moveToNext());
		}

		List<Conversation> listconv = populateList(allSMSlocal);
		if (listconv.isEmpty() && !HELP_MESSAGE) {

			// sets the flag.
			HELP_MESSAGE = true;

			LinearLayout wholeLayout = new LinearLayout(this);
			wholeLayout.setLayoutParams(new LayoutParams((int) TypedValue
					.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 260,
							getResources().getDisplayMetrics()),
					LayoutParams.WRAP_CONTENT));
			// for the speech to text
			// bubble conversation.
			final FlowLayout flowlayoutspeechrecog1 = new FlowLayout(this);
			flowlayoutspeechrecog1.setLayoutParams(new LayoutParams(
					(int) TypedValue.applyDimension(
							TypedValue.COMPLEX_UNIT_DIP, 310, getResources()
									.getDisplayMetrics()),
					LayoutParams.WRAP_CONTENT));
			flowlayoutspeechrecog1.setBackgroundResource(R.drawable.bubblelast);
			// microphone button
			ImageView speakButton = new ImageView(this);
			speakButton.setBackgroundResource(R.drawable.emptyinbox);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins((int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_SP, 2, getResources()
							.getDisplayMetrics()), 0, 0, 0);
			flowlayoutspeechrecog1.addView(speakButton, layoutParams);
			// play button
			ImageView helpplay = new ImageView(this);
			helpplay.setBackgroundResource(R.drawable.playsms);
			helpplay.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					playKaraoke(flowlayoutspeechrecog1);
				}

			});

			String[] messageHelp = new String[] { "Bo�te", "de", "r�ception",
					"vide.", "Pour", "�crire", "un", "nouveau", "message",
					"cliquez", "sur", "le", "bouton", "en", "bas", "de",
					"l'�cran." };
			for (int j = 0; j < messageHelp.length; ++j) {
				final Button but = new Button(this);
				but.setText(messageHelp[j]);
				but.setBackgroundResource(R.drawable.button);
				but.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						TextToSpeechManager.getInstance().say(
								but.getText().toString());
					}
				});
				flowlayoutspeechrecog1.addView(but);
			}

			LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams2.setMargins((int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_SP, 10, getResources()
							.getDisplayMetrics()), (int) TypedValue
					.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2,
							getResources().getDisplayMetrics()), 0,
					(int) TypedValue.applyDimension(
							TypedValue.COMPLEX_UNIT_DIP, 2, getResources()
									.getDisplayMetrics()));

			wholeLayout.addView(flowlayoutspeechrecog1);
			wholeLayout.addView(helpplay);
			mThreadPage.addView(wholeLayout, layoutParams2);
		}
	}
}