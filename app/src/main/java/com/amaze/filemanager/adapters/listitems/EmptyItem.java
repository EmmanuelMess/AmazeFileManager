package com.amaze.filemanager.adapters.listitems;

import android.content.Context;
import android.view.View;
import com.amaze.filemanager.R;
import com.amaze.filemanager.adapters.holders.EmptyViewHolder;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;

import java.util.List;

public class EmptyItem extends AbstractFlexibleItem<EmptyViewHolder> {
    @Override
    public boolean equals(Object other) {
        return other instanceof EmptyItem;
    }

    @Override
    public int hashCode() {
        return 37;
    }

    @Override
    public int getLayoutRes() {
        return 0;// TODO: 28/08/18 wtf
    }

    @Override
    public EmptyViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        Context context = view.getContext();

        int totalFabHeight = (int) context.getResources().getDimension(R.dimen.fab_height),
                marginFab = (int) context.getResources().getDimension(R.dimen.fab_margin);
        view = new View(context);
        view.setMinimumHeight(totalFabHeight + marginFab);
        return new EmptyViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, EmptyViewHolder holder, int position, List<Object> payloads) {

    }
}
