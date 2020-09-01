package xyz.tsumugu2626.app.fami;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ImagePickerListAdapter extends ArrayAdapter<ImagePickerListItem> {

    /*
     * CameraActivityの顔写真を選択するListのAdapter
    */

    private int mResource;
    private List<ImagePickerListItem> mItems;
    private LayoutInflater mInflater;
    private Context c;

    public ImagePickerListAdapter(Context context, int resource, List<ImagePickerListItem> items) {
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
        ImagePickerListItem item = mItems.get(position);
        // メイン画像を設定
        ImageView main_image = view.findViewById(R.id.mainImageView);
        Glide.with(c).load(item.getMainImage()).into(main_image);
        // グレー透明度を設定
        ImageView gray_image = view.findViewById(R.id.grayImageView);
        gray_image.setAlpha(item.getGrayImageAlpha());

        return view;
    }
}
