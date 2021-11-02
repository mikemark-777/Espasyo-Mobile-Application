package com.capstone.espasyo.landlord.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RoomRecyclerView extends RecyclerView {

    private List<View> mNonEmptyViews = Collections.emptyList();
    private List<View> mEmptyViews = Collections.emptyList();
    private List<View> mShowIfGreaterThanSevenViews = Collections.emptyList();

    private AdapterDataObserver mObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            toggleViews();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            toggleViews();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            toggleViews();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            toggleViews();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            toggleViews();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            toggleViews();
        }
    };

    private void toggleViews() {
        if(getAdapter() != null && !mEmptyViews.isEmpty()) {
            if(getAdapter().getItemCount() == 0) {

                // Show all empty views (this case the empty_property_property_recyclerView)
                for(View view : mEmptyViews) {
                    view.setVisibility(View.VISIBLE);
                }

                //hide the recyclerView since there is no data to be displayed
                setVisibility(View.GONE);

            } else {

                //Hide all empty views
                for(View view : mEmptyViews) {
                    view.setVisibility(View.GONE);
                }

                //show the recyclerView since there is data to be displayed
                setVisibility(View.VISIBLE);

                //will check if the rooms are greater than 6 and if so, it will show the SHOW ALL ROOMS Button
                if(getAdapter().getItemCount() > 6) {
                    for(View view : mShowIfGreaterThanSevenViews) {
                        view.setVisibility(View.VISIBLE);
                    }
                } else {
                    for(View view : mShowIfGreaterThanSevenViews) {
                        view.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    public RoomRecyclerView(@NonNull Context context) {
        super(context);
    }

    public RoomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RoomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);
        if(adapter != null) {
            adapter.registerAdapterDataObserver(mObserver);
        }
        mObserver.onChanged();
    }

    public void showIfEmpty(View ...view) {
        mEmptyViews = Arrays.asList(view);
    }

    public void showIfRoomsAreGreaterThanSeven(View ...view) {
        mShowIfGreaterThanSevenViews = Arrays.asList(view);
    }
}
