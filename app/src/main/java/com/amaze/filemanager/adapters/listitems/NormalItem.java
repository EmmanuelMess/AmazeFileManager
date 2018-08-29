package com.amaze.filemanager.adapters.listitems;

import android.view.View;
import com.amaze.filemanager.R;
import com.amaze.filemanager.adapters.data.LayoutElementParcelable;
import com.amaze.filemanager.adapters.glide.RecyclerPreloadSizeProvider;
import com.amaze.filemanager.adapters.holders.ItemViewHolder;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.ISectionable;

import java.util.List;

import static com.amaze.filemanager.adapters.RecyclerAdapter.TYPE_ITEM;
import static com.amaze.filemanager.adapters.RecyclerAdapter.VIEW_GENERIC;

public class NormalItem extends AbstractFlexibleItem<ItemViewHolder> implements ISectionable {
    public static final int CHECKED = 0, NOT_CHECKED = 1, UNCHECKABLE = 2;

    private final boolean isList;
    private final RecyclerPreloadSizeProvider sizeProvider;

    private LayoutElementParcelable elem;
    private String id;

    public NormalItem(boolean isList, RecyclerPreloadSizeProvider sizeProvider, String id, LayoutElementParcelable elem) {
        this.isList = isList;
        this.sizeProvider = sizeProvider;
        this.id = id;
        this.elem = elem;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof NormalItem) {
            NormalItem inItem = (NormalItem) other;
            return this.id.equals(inItem.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int getLayoutRes() {
        if(isList) {
            return R.layout.rowlayout;
        } else {
            return R.layout.griditem;
        }
    }

    @Override
    public ItemViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        if (isList) {
            sizeProvider.addView(VIEW_GENERIC, view.findViewById(R.id.generic_icon));
            sizeProvider.addView(VIEW_PICTURE, view.findViewById(R.id.picture_icon));
            sizeProvider.addView(VIEW_APK, view.findViewById(R.id.apk_icon));
        } else {
            sizeProvider.addView(VIEW_GENERIC, view.findViewById(R.id.generic_icon));
            sizeProvider.addView(VIEW_THUMB, view.findViewById(R.id.icon_thumb));
        }
        sizeProvider.closeOffAddition();

        return new ItemViewHolder(view);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ItemViewHolder holder, int position, List<Object> payloads) {

    }

}