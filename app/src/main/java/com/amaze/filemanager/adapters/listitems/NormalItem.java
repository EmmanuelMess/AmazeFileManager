package com.amaze.filemanager.adapters.listitems;

import com.amaze.filemanager.adapters.data.LayoutElementParcelable;
import com.amaze.filemanager.adapters.holders.ItemViewHolder;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public class NormalItem extends AbstractFlexibleItem<ItemViewHolder> {
    public static final int CHECKED = 0, NOT_CHECKED = 1, UNCHECKABLE = 2;

    private LayoutElementParcelable elem;
    private int specialType;
    private boolean checked;
    private boolean animate;

    public NormalItem(LayoutElementParcelable elem) {
        this.elem = elem;
        specialType = TYPE_ITEM;
    }

    public NormalItem(int specialType) {
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
}