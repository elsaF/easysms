package org.easysms.android.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import android.os.Environment;
import android.util.Log;

/**
 * 
 * @author Oscar Bola�os <@oscarbolanos>
 * 
 */
public class ApplicationTracker {

	/** Defines the event types that are trackable through out the application. */
	public enum EventType {
		ACTIVITY_VIEW, CLICK, ERROR, LONG_CLICK, SYNC
	}

	/**
	 * Format used to stored the data in the log. It corresponds to
	 * <code>[date] userId EventType activityName label </code>
	 * */
	private static final String DATA_ENTRY_FORMAT = "[%s] %d - %s - %s - %s";
	/** Shorter format used to store data in the log. */
	private static final String DATA_ENTRY_FORMAT_SMALL = "[%s] %d - %s - %s";
	/** Format used to store the date information. */
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	/** Default value for the maximum size of the log that will force a flush. */
	public static final int DEFAULT_MAX_LOG_SIZE = 5;
	/** Name of the file where the log is stored. */
	public static final String LOG_FILENAME = "UIlog.txt";
	/** Name of the folder where the log is located. */
	public static final String LOG_FOLDER = "/.csn_app_logs";
	/** Identifier of the class used for logging. */
	private static final String LOG_TAG = "ApplicationTracker";
	/** Singleton instance of the ActionLogger. */
	private static ApplicationTracker sInstance = null;
	/**
	 * Format used to log the Sync events. It is formatted as
	 * <code>[date] EventType label data</code>
	 */
	private static final String SYNC_DATA_ENTRY_FORMAT = "[%s] %s - %s - %s";
	/** Name of the file where the sync log is stored. */
	public static final String SYNC_LOG_FILENAME = "Synclog.txt";

	/**
	 * Gets the singleton instance of the ActionTracker.
	 * 
	 * @return an ActionTracker instance.
	 */
	public static ApplicationTracker getInstance() {
		if (sInstance == null) {
			Log.i(LOG_TAG, "Initializing Application Tracker");
			sInstance = new ApplicationTracker();
		}
		return sInstance;
	}

	/** Data structure used to store locally the log. */
	private LinkedList<String> mActivityLog;
	/** Date format used in each log. */
	private SimpleDateFormat mDateFormat;
	/** DeviceId used to name the log file. */
	private String mDeviceId;
	/** Path where the log is stored in the SD card. */
	private String mExternalDirectoryLog;
	/**
	 * Size of the log that will force the ApplicationTracker to flush its data.
	 * Set this value to -1 to disable this feature.
	 */
	public int mMaxLogSize;
	/** Data structure used to store the sync events. */
	private LinkedList<String> mSyncLog;

	private ApplicationTracker() {
		// creates the date formatter.
		mDateFormat = new SimpleDateFormat(DATE_FORMAT);
		// initializes the list where the data will be stored.
		mActivityLog = new LinkedList<String>();
		mSyncLog = new LinkedList<String>();
		// initializes the value with the default
		mMaxLogSize = DEFAULT_MAX_LOG_SIZE;
	}

	/**
	 * Forces the class to write the activity log
	 */
	public void flush() {

		// cancels the flush operation if there is nothing to flush
		synchronized (mActivityLog) {
			if (mActivityLog.size() == 0) {
				return;
			}
		}

		File mFile;
		FileWriter mFileWriter;
		File folder = new File(Environment.getExternalStorageDirectory()
				+ LOG_FOLDER);

		// if the folder does not exist it is created.
		if (!folder.exists()) {
			folder.mkdir();
		}

		// gets the path of the folder where the data will be stored.
		mExternalDirectoryLog = folder.getAbsolutePath();

		// creates the log file.
		mFile = new File(mExternalDirectoryLog,
				(mDeviceId != null ? (mDeviceId + "-") : "") + LOG_FILENAME);

		try {
			// initializes the file writer
			mFileWriter = new FileWriter(mFile, true);
			// forces every line to be a log entry.
			PrintWriter pw = new PrintWriter(mFileWriter);

			// writes the stored files into the log file.
			synchronized (mActivityLog) {
				for (int x = 0; x < mActivityLog.size(); x++) {
					pw.println(mActivityLog.get(x));
				}

				// removes all current elements of the list.
				mActivityLog.clear();
			}

			// closes the file writer.
			mFileWriter.close();
		} catch (Exception e) {
			Log.e("WRITE TO SD", e.getMessage());
		}
	}

	public void flushAll() {
		// flushes the UI usage data.
		flush();
		// flushes the sync related data.
		flushSync();
	}

	/**
	 * Writes the Sync related data into a file in the SD folder.
	 */
	public void flushSync() {

		// cancels the flush operation if there is nothing to flush
		synchronized (mSyncLog) {
			if (mSyncLog.size() == 0) {
				return;
			}
		}

		File mFile;
		FileWriter mFileWriter;
		File folder = new File(Environment.getExternalStorageDirectory()
				+ LOG_FOLDER);

		// if the folder does not exist it is created.
		if (!folder.exists()) {
			folder.mkdir();
		}

		// gets the path of the folder where the data will be stored.
		mExternalDirectoryLog = folder.getAbsolutePath();

		// creates the log file.
		mFile = new File(mExternalDirectoryLog,
				(mDeviceId != null ? (mDeviceId + "-") : "")
						+ SYNC_LOG_FILENAME);

		try {
			// initializes the file writer
			mFileWriter = new FileWriter(mFile, true);
			// forces every line to be a log entry.
			PrintWriter pw = new PrintWriter(mFileWriter);

			// writes the stored files into the log file.
			synchronized (mSyncLog) {
				for (int x = 0; x < mSyncLog.size(); x++) {
					pw.println(mSyncLog.get(x));
				}

				// removes all current elements of the list.
				mSyncLog.clear();
			}

			// closes the file writer.
			mFileWriter.close();
		} catch (Exception e) {
			Log.e("WRITE TO SD", e.getMessage());
		}
	}

	public String getDeviceId() {
		return mDeviceId;
	}

	public int getMaxLogSize() {
		return mMaxLogSize;
	}

	/**
	 * Logs an event that occurred in the application. Each event has a type,
	 * the name of the source that generated the event and then optionally any
	 * other parameter. This can be the name of the button, or any important
	 * value to record.
	 * 
	 * 
	 * @param eventType
	 *            type of the event.
	 * @param userId
	 *            id of the user that performed the activity
	 * @param activityName
	 *            name of the activity that generated the object.
	 * @param args
	 *            additional values to log.
	 */
	public void logEvent(EventType eventType, long userId, String activityName,
			Object... args) {

		if (args.length == 0) {

			switch (eventType) {
			case SYNC:
				synchronized (mSyncLog) {
					mActivityLog.add(String.format(DATA_ENTRY_FORMAT_SMALL,
							mDateFormat.format(new Date()), userId, eventType,
							activityName));
				}
				break;
			default:
				// synchronized access to the log since concurrent access could
				// be enabled.
				synchronized (mActivityLog) {
					mActivityLog.add(String.format(DATA_ENTRY_FORMAT_SMALL,
							mDateFormat.format(new Date()), userId, eventType,
							activityName));
				}
			}

		} else {

			// creates a string with all the available objects.
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < args.length; i++) {
				// appends the next object.
				sb.append(args[i].toString());
				// appends a comma and a space.
				if (i + 1 < args.length) {
					sb.append(", ");
				}
			}
			// synchronized access to the log since concurrent access could be
			// enabled.
			synchronized (mActivityLog) {
				mActivityLog.add(String.format(DATA_ENTRY_FORMAT,
						mDateFormat.format(new Date()), userId, eventType,
						activityName, sb.toString()));
			}
		}

		// if the maximum size has been exceeded, the data is flushed
		// automatically.
		synchronized (mActivityLog) {
			if (mActivityLog.size() > mMaxLogSize && mMaxLogSize != -1) {
				flush();
			}
		}
	}

	public void logSyncEvent(EventType eventType, String label, String data) {

		synchronized (mSyncLog) {
			// creates the entry.
			String entry = String.format(SYNC_DATA_ENTRY_FORMAT,
					mDateFormat.format(new Date()), eventType, label, data);
			// adds the entry to the list.
			mSyncLog.add(entry);
			// logs the message into log cat.
			Log.d(LOG_TAG, entry);
		}

		// forces the sync data to be written.
		flushSync();

		// // if the maximum size has been exceeded, the data is flushed
		// // automatically.
		// synchronized (mSyncLog) {
		// if (mSyncLog.size() > mMaxLogSize && mMaxLogSize != -1) {
		// flushSync();
		// }
		// }
	}

	public void setDeviceId(String deviceId) {
		mDeviceId = deviceId;
	}

	public void setMaxLogSize(int value) {
		mMaxLogSize = value;
	}
}
