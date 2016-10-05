package appGame.Puzzle;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by yungyu.chen on 2016/9/19.
 */
public class MyViewPagerAdapter extends PagerAdapter {
    private List<View> mListViews;
    private Activity _activity;
    private final static String TAG = "MyViewPagerAdapter";
    public MyViewPagerAdapter(Activity activity, List<View> mListViews) {
        this.mListViews = mListViews;
        this._activity = activity;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mListViews.get(position % mListViews.size());
        view.setOnClickListener(new OnImageClickListener(position));
        try {
            container.addView(
                    view, 0);
        } catch (Exception e) {
        }

        return view;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    class OnImageClickListener implements View.OnClickListener {

        int _postion;

        // constructor
        public OnImageClickListener(int position) {
            this._postion = position;
        }

        @Override
        public void onClick(View v) {
            // on selecting grid view image
            // launch full screen activity
            Log.i(TAG, _postion+"");
            Intent i = new Intent(_activity, FullScreenViewActivity.class);
            i.putExtra("position", _postion);
            _activity.startActivity(i);
        }

    }
}
