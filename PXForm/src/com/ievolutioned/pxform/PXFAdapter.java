package com.ievolutioned.pxform;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class PXFAdapter extends BaseAdapter {
    private List<PXWidget> lWidgets = new ArrayList<PXWidget>();
    private JsonElement jseObject;
    private Activity aActivity;

    public PXFAdapter(Activity activity, List<PXWidget> widgets){
        lWidgets = widgets;
        aActivity = activity;
    }

    @Override
    public int getCount() {
        return lWidgets.size();
    }

    @Override
    public int getItemViewType(int position) {
        return lWidgets.get(position).getAdapterItemType();
    }

    @Override
    public int getViewTypeCount() {
        return PXWidget.getAdapterItemTypeCount();
    }

    @Override
    public PXWidget getItem(int position) {
        return lWidgets.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        final PXWidget w = getItem(pos);

        if(view == null){
            view = w.createControl(aActivity);
        }

        w.setWidgetData(view);

        return view;
    }

    private View getWidgetFromType(PXWidget widget){
        return widget.createControl(aActivity);
    }
}
