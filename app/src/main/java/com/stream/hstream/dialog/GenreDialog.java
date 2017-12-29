package com.stream.hstream.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.stream.dao.GenreInfo;
import com.stream.hstream.R;
import com.stream.util.GenreManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seven-one on 2017/12/26 0026.
 */

public class GenreDialog extends Dialog{

    private RecyclerView mCommonGenreView;
    private CommonGenreAdapter mCommonGenreAdapter;

    private RecyclerView mRecommendGenreView;
    private RecommendGenreAdapter mRecommendGenreAdapter;

    private List<GenreInfo> mCommonGenreList = new ArrayList<>();
    private List<GenreInfo> mRecommendGenreList = new ArrayList<>();

    private OnCloseListener mCloseListner;

    public GenreDialog(@NonNull Context context) {
        super(context);
        init(context);
    }

    public GenreDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected GenreDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_gener_list, null);
        setContentView(view);

        mCommonGenreView = (RecyclerView) view.findViewById(R.id.common_genre_view);
        mCommonGenreView.setLayoutManager(new GridLayoutManager(context, 3) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        mCommonGenreAdapter = new CommonGenreAdapter();
        mCommonGenreView.setAdapter(mCommonGenreAdapter);

        mRecommendGenreView = (RecyclerView) view.findViewById(R.id.recommend_genre_view);
        mRecommendGenreView.setLayoutManager(new GridLayoutManager(context, 3));
        mRecommendGenreAdapter = new RecommendGenreAdapter();
        mRecommendGenreView.setAdapter(mRecommendGenreAdapter);

        ImageView closeButton = (ImageView) view.findViewById(R.id.genre_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        initData();

        Window dialogWin = this.getWindow();
        dialogWin.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWin.getAttributes();
        lp.x = 0;
        lp.y = 0;
        lp.width = (int) context.getResources().getDisplayMetrics().widthPixels; // 宽度
        view.measure(0, 0);
        lp.height = context.getResources().getDisplayMetrics().heightPixels;
        lp.alpha = 9f; // 透明度
        dialogWin.setAttributes(lp);
    }

    private void initData() {
        mCommonGenreList.addAll(GenreManager.getUsedGenreInfo());
        mRecommendGenreList.addAll(GenreManager.getUnusedGenreInfo());
    }

    /**
     * set close listener
     * @param listener
     */
    public void setOnCloseListener(OnCloseListener listener) {
        mCloseListner = listener;
    }

    /**
     * close dialog
     */
    private void close() {
        this.dismiss();

        if(mCloseListner != null) {
            mCloseListner.onClose(this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(this.isShowing()) {
            close();
        }
    }

    private class CommonGenreAdapter extends RecyclerView.Adapter<CommonGenreHolder> {

        @Override
        public CommonGenreHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CommonGenreHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_dialog_genre_list, parent, false));
        }

        @Override
        public void onBindViewHolder(CommonGenreHolder holder, final int position) {
            final GenreInfo info = mCommonGenreList.get(position);
            holder.mItemView.setText(info.getGenre_name());
            holder.mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //set genre info to unused
                    GenreManager.unusedGenreInfo(info);

                    mCommonGenreList.remove(info);
                    mCommonGenreAdapter.notifyDataSetChanged();

                    mRecommendGenreList.add(0, info);
                    mRecommendGenreAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mCommonGenreList.size();
        }
    }

    private class RecommendGenreAdapter extends RecyclerView.Adapter<RecommendGenreHolder> {

        @Override
        public RecommendGenreHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecommendGenreHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_dialog_genre_list, parent, false));
        }

        @Override
        public int getItemCount() {
            return mRecommendGenreList.size();
        }

        @Override
        public void onBindViewHolder(RecommendGenreHolder holder, final int position) {
            final GenreInfo info = mRecommendGenreList.get(position);
            holder.mItemView.setText(info.getGenre_name());
            holder.mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //set genre info to used
                    GenreManager.useGenreInfo(info);

                    mRecommendGenreList.remove(info);
                    mRecommendGenreAdapter.notifyDataSetChanged();

                    mCommonGenreList.add(info);
                    mCommonGenreAdapter.notifyItemChanged(mCommonGenreList.size());
                }
            });
        }
    }

    private class CommonGenreHolder extends RecyclerView.ViewHolder {

        private TextView mItemView;

        public CommonGenreHolder(View itemView) {
            super(itemView);

            mItemView = (TextView) itemView;
        }
    }

    private class RecommendGenreHolder extends RecyclerView.ViewHolder {

        private TextView mItemView;

        public RecommendGenreHolder(View itemView) {
            super(itemView);

            mItemView = (TextView) itemView;
        }
    }

    /**
     * Close listener
     */
    public interface OnCloseListener {

        void onClose(GenreDialog dialog);
    }

}
