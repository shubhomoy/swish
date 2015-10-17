package com.bitslate.swish.SwishAdapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bitslate.swish.SwishFragments.IntroFragment_1;
import com.bitslate.swish.SwishFragments.IntroFragment_2;
import com.bitslate.swish.SwishFragments.SignupFragment;

/**
 * Created by shubhomoy on 16/10/15.
 */
public class IntroPageAdapter extends FragmentPagerAdapter {
    Context context;
    public IntroPageAdapter(Context c, FragmentManager fm) {
        super(fm);
        this.context = c;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                IntroFragment_1 fragment_1 = new IntroFragment_1();
                return fragment_1;
            case 1:
                IntroFragment_2 fragment_2 = new IntroFragment_2();
                return  fragment_2;
            case 2:
                SignupFragment signupFragment = new SignupFragment();
                return signupFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
