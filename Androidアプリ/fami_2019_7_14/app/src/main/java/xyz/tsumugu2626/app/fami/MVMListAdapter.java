package xyz.tsumugu2626.app.fami;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;

public class MVMListAdapter extends ArrayAdapter<MVMListItem> {

    /*
     * ManageVoiceMessageのListのAdapter
    */

    private int mResource;
    private List<MVMListItem> mItems;
    private LayoutInflater mInflater;
    private Context c;

    public MVMListAdapter(Context context, int resource, List<MVMListItem> items) {
        super(context, resource, items);

        mResource = resource;
        mItems = items;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        c = context;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(mResource, null);
        }
        final MVMListItem item = mItems.get(position);
        TextView from_tv = view.findViewById(R.id.mbmic_text1);
        from_tv.setText(item.getFromText());
        TextView to_tv = view.findViewById(R.id.mbmic_text2);
        to_tv.setText(item.getToText());
        TextView patams_tv = view.findViewById(R.id.paramTextView);
        patams_tv.setText(item.getParams());
        view.findViewById(R.id.playButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView) parent).performItemClick(view, position, R.id.playButton);
            }
        });
        view.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView) parent).performItemClick(view, position, R.id.deleteButton);
            }
        });
        return view;
    }
}
