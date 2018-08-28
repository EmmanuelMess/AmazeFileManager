package com.amaze.filemanager.adapters.listitems;

import android.view.View;
import com.amaze.filemanager.adapters.data.LayoutElementParcelable;
import com.amaze.filemanager.adapters.holders.ItemViewHolder;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;

import java.util.List;

import static com.amaze.filemanager.adapters.RecyclerAdapter.TYPE_ITEM;

public class NormalItem extends AbstractFlexibleItem<ItemViewHolder> {
    public static final int CHECKED = 0, NOT_CHECKED = 1, UNCHECKABLE = 2;

    private LayoutElementParcelable elem;
    private String id;
    private int specialType;
    private boolean checked;
    private boolean animate;

    public NormalItem(String id, LayoutElementParcelable elem) {
        this.id = id;
        this.elem = elem;
        specialType = TYPE_ITEM;
    }

    public NormalItem(String id, int specialType) {
        this.id = id;
        this.specialType = specialType;
    }

    public void setChecked(boolean checked) {
        if(specialType == TYPE_ITEM) this.checked = checked;
    }

    public int getChecked() {
        if(checked) return CHECKED;
        else if(specialType == TYPE_ITEM) return NOT_CHECKED;
        else return UNCHECKABLE;
    }

    public void setAnimate(boolean animating) {
        if(specialType == -1) this.animate = animating;
    }

    public boolean getAnimating() {
        return animate;
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
        return 0;
    }

    @Override
    public ItemViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        return null;
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ItemViewHolder holder, int position, List<Object> payloads) {

    }
}