package xyz.tsumugu2626.app.fami;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.List;

public class FSListAdapter extends ArrayAdapter<FSListItem> {

    /*
     * FamilyStructureActivityのListのAdapter
    */

    private int mResource;
    private List<FSListItem> mItems;
    private LayoutInflater mInflater;
    private Context c;

    public FSListAdapter(Context context, int resource, List<FSListItem> items) {
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
        FSListItem item = mItems.get(position);
        // メイン画像を設定
        ImageView main_image = view.findViewById(R.id.mainImageView);
        Glide.with(c).load(item.getMainImage()).into(main_image);
        // テキストを設定
        TextView name_text = view.findViewById(R.id.textView);
        name_text.setText(item.getNameText());
        TextView sub_text = view.findViewById(R.id.subTextView);
        sub_text.setText(item.getSubText());

        return view;
    }
}
