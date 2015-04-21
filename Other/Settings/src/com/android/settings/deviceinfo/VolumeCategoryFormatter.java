
package com.android.settings.deviceinfo;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

import java.util.ArrayList;
import java.util.Arrays;

public class VolumeCategoryFormatter {
    private StorageManager mStorageManager = null;
    private Context mContext = null;
    private Memory mPrefFreg = null;
    private ArrayList<StorageVolumePreferenceCategory> mCategories = null;
    private static final String TAG = "VolumeCategoryFormatter";
    private boolean mIsUsbConnected = false;
    private String mUsbFunctions = null;

    public VolumeCategoryFormatter(Context context, Memory freg,
            ArrayList<StorageVolumePreferenceCategory> categories) {
        mContext = context;
        mPrefFreg = freg;
        mCategories = categories;

        mStorageManager = StorageManager.from(context);
    }

    /**
     * Add StorageVolumeCategory to SettingsPreferenceFragment, order is align
     * with StorageManager.getVolumeList()
     *
     * @param volume the volume need to show
     * @return none
     */
    public StorageVolumePreferenceCategory addVolumeCategory(StorageVolume volume) {
        String volStat = mStorageManager.getVolumeState(volume.getPath());
        StorageVolume[] storageVolumes = mStorageManager.getVolumeList();

        // If volumes HW(SD Card or Pen Drive) plugged, show the
        // StorageVolumePreferenceCategory
        if (!Environment.MEDIA_BAD_REMOVAL.equals(volStat)
                && !Environment.MEDIA_REMOVED.equals(volStat)) {
            StorageVolumePreferenceCategory category = StorageVolumePreferenceCategory
                    .buildForPhysical(mContext, volume);
            category.setOrder(Arrays.asList(storageVolumes).indexOf(volume));
            mCategories.add(category);
            mPrefFreg.getPreferenceScreen().addPreference(category);
            category.init();
            return category;
        }
        return null;
    }

    /**
     * Add/remove and refresh the StorageVolumeCategory according to the new
     * state
     *
     * @param path the changed volume's mount path
     * @param newState which state the volume changed to
     */
    public void formatPreferenceFromState(String path, String newState) {
        if (!newState.equals(Environment.MEDIA_BAD_REMOVAL)
                && !newState.equals(Environment.MEDIA_REMOVED)) {
            StorageVolume eventVolume = pathToVolume(path);

            if (eventVolume == null)
                return;

            // New added volumes, add category
            if (!containCategory(eventVolume)) {
                StorageVolumePreferenceCategory category = addVolumeCategory(eventVolume);
                if (category == null)
                    return;
                category.onResume();
                if (mUsbFunctions != null)
                    category.onUsbStateChanged(mIsUsbConnected, mUsbFunctions);

                // Try to scroll to the new added item
                PreferenceScreen screen = mPrefFreg.getPreferenceScreen();
                int itemIndex = 0;
                for (int i = 0; i < screen.getPreferenceCount(); i++) {
                    itemIndex++;
                    CharSequence volDesc = screen.getPreference(i).getTitle();
                    if (volDesc != null
                            && volDesc.toString().equals(eventVolume.getDescription(mContext))) {
                        try {
                            final ListView list = mPrefFreg.getListView();
                            final int scrollTo = itemIndex;
                            list.post(new Runnable() {
                                @Override
                                public void run() {
                                    smoothScrollToPositionFromTopWithBugWorkAround(list,
                                            scrollTo - 1,
                                            0, 500);
                                }
                            });
                            break;
                        } catch (Exception e) {
                            Log.e(TAG, "Catch exception, not scroll to new item", e);
                        }
                    } else if (screen.getPreference(i) instanceof PreferenceGroup) {
                        itemIndex += ((PreferenceGroup) screen.getPreference(i))
                                .getPreferenceCount();
                    }
                }
            }
        }

        // If volumes removed, hide the corresponding preference
        if (newState.equals(Environment.MEDIA_REMOVED)
                || newState.equals(Environment.MEDIA_BAD_REMOVAL)) {
            removeVolumeCategory(path);
        }
    }

    public void onUsbStateChanged(boolean isUsbConnected, String functions) {
        mIsUsbConnected = isUsbConnected;
        mUsbFunctions = functions;
    }

    private void removeVolumeCategory(String path) {
        for (StorageVolumePreferenceCategory category : mCategories) {
            final StorageVolume volume = category.getStorageVolume();
            if (volume != null && path.equals(volume.getPath())) {
                category.onPause();
                category.removeAll();
                mCategories.remove(category);
                mPrefFreg.getPreferenceScreen().removePreference(category);
                break;
            }
        }
    }

    private StorageVolume pathToVolume(String path) {
        StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
        if (storageVolumes == null)
            return null;
        for (StorageVolume vol : storageVolumes) {
            if (vol.getPath().equals(path)) {
                return vol;
            }
        }
        return null;
    }

    private boolean containCategory(StorageVolume volume) {
        for (StorageVolumePreferenceCategory category : mCategories) {
            if (volume != null && volume.equals(category.getStorageVolume())) {
                return true;
            }
        }
        return false;
    }

    // Workaround for AOSP bug in method smoothScrollToPositionFromTop.
    // https://code.google.com/p/android/issues/detail?id=36062
    // the bug is the case that sometimes smooth Scroll To Position sort of
    // misses its intended position.
    // I got this solution from
    // http://stackoverflow.com/questions/14479078/smoothscrolltopositionfromtop-is-not-always-working-like-it-should
    void smoothScrollToPositionFromTopWithBugWorkAround(final AbsListView listView,
            final int position,
            final int offset,
            final int duration) {

        listView.smoothScrollToPositionFromTop(position, offset, duration);

        listView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    listView.setOnScrollListener(null);
                    listView.smoothScrollToPositionFromTop(position, offset, duration);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
            }
        });

    }
}
