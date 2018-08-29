package com.amaze.filemanager.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.amaze.filemanager.R;
import com.amaze.filemanager.utils.Utils;
import com.amaze.filemanager.utils.provider.UtilitiesProvider;
import com.amaze.filemanager.utils.theme.AppTheme;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Check {@link com.amaze.filemanager.adapters.RecyclerAdapter}'s doc.
 *
 * @author Emmanuel
 *         on 29/5/2017, at 04:22.
 */

public class SpecialViewHolder extends FlexibleViewHolder {
    public static final int HEADER_FILES = 0, HEADER_FOLDERS = 1;
    // each data item is just a string in this case
    public final TextView txtTitle;
    public final int type;

    public SpecialViewHolder(View view, FlexibleAdapter flexibleAdapter, UtilitiesProvider utilsProvider,
                             int type) {
        super(view, flexibleAdapter);

        this.type = type;
        txtTitle = view.findViewById(R.id.text);

        switch (type) {
            case HEADER_FILES:
                txtTitle.setText(R.string.files);
                break;
            case HEADER_FOLDERS:
                txtTitle.setText(R.string.folders);
                break;
            default:
                throw new IllegalStateException(": " + type);
        }

        //if(utilsProvider.getAppTheme().equals(AppTheme.DARK))
        //    view.setBackgroundResource(R.color.holo_dark_background);

        if (utilsProvider.getAppTheme().equals(AppTheme.LIGHT)) {
            txtTitle.setTextColor(Utils.getColor(view.getContext(), R.color.text_light));
        } else {
            txtTitle.setTextColor(Utils.getColor(view.getContext(), R.color.text_dark));
        }
    }

}