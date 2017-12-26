package com.stream.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.stream.hstream.R;
import com.stream.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seven-one on 2017/12/25 0025.
 */

public class BottomDialog extends Dialog {

    private RecyclerView mRecyclerView;
    private MenuAdapter mAdapter;

    private List<MenuItem> mData = new ArrayList<>();

    public BottomDialog(@NonNull Context context) {
        super(context);
        init(context);
    }

    public BottomDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected BottomDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_list_menu, null);
        setContentView(view);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.menu_recycler_view);
        mAdapter = new MenuAdapter(context);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        Button closeButton = (Button) view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomDialog.this.dismiss();
            }
        });

        Window dialogWin = this.getWindow();
        dialogWin.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWin.getAttributes();
        lp.x = 0;
        lp.y = 0;
        lp.width = (int) context.getResources().getDisplayMetrics().widthPixels; // 宽度
        view.measure(0, 0);
        lp.height = view.getMeasuredHeight();
        lp.alpha = 9f; // 透明度
        dialogWin.setAttributes(lp);
    }

    public void setMenuItems(List<MenuItem> items) {
        if(mData.size() > 0) {
            mData.clear();
        }

        mData.addAll(items);
        mAdapter.notifyDataSetChanged();
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuHolder> {

        private LayoutInflater mInflater;

        private MenuAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public MenuHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MenuHolder(mInflater.inflate(R.layout.item_dialog_menu_list, parent, false));
        }

        @Override
        public void onBindViewHolder(MenuHolder holder, int position) {
            MenuItem menuItem = mData.get(position);
            //holder.mButton.setText(menuItem.mText);
            //holder.mButton.setOnClickListener(menuItem.mClickListener);

            holder.mMenuText.setText(menuItem.mText);
            if(menuItem.mDrawable != null) {
                holder.mImgeView.setImageDrawable(menuItem.mDrawable);
                holder.mImgeView.setOnClickListener(menuItem.mClickListener);

                holder.mImgeView.setVisibility(View.VISIBLE);
                holder.mMenuDisplayText.setVisibility(View.GONE);
            } else {
                holder.mMenuDisplayText.setText(StringUtils.getFirstStr(menuItem.mText));
                holder.mMenuDisplayText.setOnClickListener(menuItem.mClickListener);

                holder.mMenuDisplayText.setVisibility(View.VISIBLE);
                holder.mImgeView.setVisibility(View.GONE);
            }

            if(menuItem.mBackground != null) {
                holder.setBackground(menuItem.mBackground);
            }

            if(menuItem.mDisplayColor != 0) {
                holder.setDisplayColor(menuItem.mDisplayColor);
            }

            if(menuItem.mTextColor != 0) {
                holder.setTextColor(menuItem.mTextColor);
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    private class MenuHolder extends RecyclerView.ViewHolder {

        public TextView mMenuText;
        public SquareTextView mMenuDisplayText;
        public SquareImageView mImgeView;
        public FrameLayout mDisplayContainer;

        public MenuHolder(View itemView) {
            super(itemView);

            mMenuText = (TextView) itemView.findViewById(R.id.menu_text);
            mMenuDisplayText = (SquareTextView) itemView.findViewById(R.id.menu_display_text);
            mImgeView = (SquareImageView) itemView.findViewById(R.id.menu_img);
            mDisplayContainer = (FrameLayout) itemView.findViewById(R.id.display_container);
        }

        public void setBackground(Drawable drawable) {
            mDisplayContainer.setBackground(drawable);
        }

        public void setDisplayColor(@ColorInt int displayColor) {
            mMenuDisplayText.setTextColor(displayColor);
        }

        public void setTextColor(@ColorInt int textColor) {
            mMenuDisplayText.setTextColor(textColor);
        }
    }

    public static class MenuItem {

        private String mText;
        private Drawable mDrawable;
        private View.OnClickListener mClickListener;
        private Drawable mBackground;

        private int mTextColor;
        private int mDisplayColor;

        public MenuItem(String text, View.OnClickListener listener) {
            this(text, null, listener);
        }

        public MenuItem(String text, Drawable drawable, View.OnClickListener listener) {
            mText = text;
            mDrawable = drawable;
            mClickListener = listener;
        }

        public void setBackground(Drawable drawable) {
            mBackground = drawable;
        }

        public void setDisplayColor(@ColorInt int color) {
            mDisplayColor = color;
        }

        public void setTextColor(@ColorInt int color) {
            mTextColor = color;
        }
    }

}
