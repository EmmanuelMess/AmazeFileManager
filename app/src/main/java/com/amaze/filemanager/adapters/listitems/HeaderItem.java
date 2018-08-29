package com.amaze.filemanager.adapters.listitems;

import android.view.View;
import com.amaze.filemanager.R;
import com.amaze.filemanager.adapters.holders.SpecialViewHolder;
import com.amaze.filemanager.utils.provider.UtilitiesProvider;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.IFlexible;

import java.util.List;

public class HeaderItem extends AbstractHeaderItem<SpecialViewHolder> {
    public static final int TYPE_HEADER_FOLDERS = 1, TYPE_HEADER_FILES = 2;

    private final UtilitiesProvider utilsProvider;
    private final boolean isList;
    private final int headerType;

    public HeaderItem(UtilitiesProvider utilsProvider, boolean isList, int headerType) {
        this.utilsProvider = utilsProvider;
        this.isList = isList;
        this.headerType = headerType;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof HeaderItem) {
            HeaderItem inItem = (HeaderItem) other;
            return this.headerType == inItem.headerType;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return headerType*37 + 37;
    }

    @Override
    public int getLayoutRes() {
        if(isList) {
            return R.layout.list_header;
        } else {
            return R.layout.grid_header;
        }
    }

    @Override
    public SpecialViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        int type = headerType == TYPE_HEADER_FOLDERS ? SpecialViewHolder.HEADER_FOLDERS : SpecialViewHolder.HEADER_FILES;

        return new SpecialViewHolder(view, adapter, utilsProvider, type);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, SpecialViewHolder holder, int position, List<Object> payloads) {

    }
}
