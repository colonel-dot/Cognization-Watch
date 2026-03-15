package util;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemSpacingDecoration extends RecyclerView.ItemDecoration {

    private final int margin;
    private final int space;
    private final int extraEnd;
    private final boolean isHorizontal;

    public ItemSpacingDecoration(Context context, int margin, int space, int extraEnd, boolean isHorizontal) { // px
        float density = context.getResources().getDisplayMetrics().density;
        this.margin = (int)(margin * density + 0.5f);
        this.space = (int)(space * density + 0.5f);
        this.extraEnd = (int)(extraEnd * density + 0.5f);
        this.isHorizontal = isHorizontal;
    }

    public ItemSpacingDecoration(Context context, int space, boolean isHorizontal) { // px
        this(context, 0, space, 0, isHorizontal);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (isHorizontal) {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.left = margin;
            } else if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                outRect.left = space;
                outRect.right = margin + extraEnd;
            } else {
                outRect.left = space;
            }
        } else {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = margin;
            } else if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                outRect.top = space;
                outRect.bottom = margin + extraEnd;
            } else {
                outRect.top = space;
            }
        }
    }
}
