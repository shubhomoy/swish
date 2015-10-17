package com.bitslate.swish.SwishAdapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bitslate.swish.SwishFragments.ChatFragment;
import com.bitslate.swish.SwishFragments.ImageFragment;
import com.bitslate.swish.SwishFragments.PreviewFragment;

/**
 * Created by shubhomoy on 17/10/15.
 */
public class PreviewPageAdapter extends FragmentPagerAdapter {
    Context context;
    public PreviewPageAdapter(Context c, FragmentManager fm) {
        super(fm);
        this.context = c;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                PreviewFragment previewFragment = new PreviewFragment();
                return previewFragment;
            case 1:
                ImageFragment imageFragment = new ImageFragment();
                return imageFragment;
            case 2:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
