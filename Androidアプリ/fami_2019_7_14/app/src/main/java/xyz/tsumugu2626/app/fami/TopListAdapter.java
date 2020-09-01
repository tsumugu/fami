package xyz.tsumugu2626.app.fami;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.core.content.res.ResourcesCompat;
import com.bumptech.glide.Glide;
import java.util.List;

public class TopListAdapter extends ArrayAdapter<TopListItem> {

    /*
     * TopActivityの顔写真とかを表示するListのAdapter
    */

    private int mResource;
    private List<TopListItem> mItems;
    private LayoutInflater mInflater;
    private Context c;

    public TopListAdapter(Context context, int resource, List<TopListItem> items) {
        super(context, resource, items);

        mResource = resource;
        mItems = items;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        c = context;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(mResource, null);
        }
        TopListItem item = mItems.get(position);
        RelativeLayout cRL = view.findViewById(R.id.cellRelativeLayout);
        if (item.getIsINVISIBLE()) {
            cRL.setVisibility(View.INVISIBLE);
        } else {
            // サムネイル画像を設定
            ImageView thumbnail = view.findViewById(R.id.cIV);
            Glide.with(c).load(item.getThumbnail()).into(thumbnail);
            // 外出中のグレー
            FrameLayout ll = cRL.findViewById(R.id.out_cover);
            if (item.getIsGettingOut()) {
                ll.setVisibility(View.VISIBLE);
            } else {
                ll.setVisibility(View.GONE);
            }
            ImageView border_iv = cRL.findViewById(R.id.border);
            border_iv.setImageDrawable( ResourcesCompat.getDrawable(c.getResources(), R.drawable.image_border, null));
            ImageView gray_iv = ll.findViewById(R.id.gray);
            gray_iv.setImageDrawable( ResourcesCompat.getDrawable(c.getResources(), R.drawable.bg_tp, null));
        }
        return view;
    }
}
