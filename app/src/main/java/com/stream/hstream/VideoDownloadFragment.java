package com.stream.hstream;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.hippo.yorozuya.FileUtils;
import com.stream.dao.DownloadInfo;
import com.stream.download.DownloadManager;
import com.stream.download.DownloadService;
import com.stream.download.DownloadUtil;
import com.stream.hstream.adapter.SimpleAdapter;
import com.stream.hstream.adapter.SimpleHolder;
import com.stream.scene.SceneFragment;
import com.stream.scene.ToolBarFragment;
import com.stream.util.DrawableManager;
import com.stream.widget.LoadImageView;
import com.stream.widget.view.ViewTransition;

import java.util.List;

/**
 * Created by Fuzm on 2017/5/7 0007.
 */

public class VideoDownloadFragment extends ToolBarFragment implements DownloadManager.DownloadInfoListener{

    private RecyclerView mRecyclerView;
    private DownloadListAdapter mAdapter;
    private List<DownloadInfo> mData;
    private DownloadManager mDownloadManager;
    private ViewTransition mViewTransition;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDownloadManager = HStreamApplication.getDownloadManager(getContext());
        mDownloadManager.setDownloadInfoListener(this);
        updateData();
    }

    @Nullable
    @Override
    public View onCreateView2(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download_main, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.download_list);
        mAdapter = new DownloadListAdapter(inflater, getContext());

        TextView tip = (TextView) view.findViewById(R.id.tip);
        Drawable drawable = DrawableManager.getDrawable(getContext(), R.drawable.ic_big_download);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        tip.setCompoundDrawables(null, drawable, null, null);

        mViewTransition = new ViewTransition(mRecyclerView, tip);
        updateView();
        return view;
    }

    private void updateView() {
        if(mData != null && mData.size() > 0) {
            mViewTransition.showView(0, true);
        } else {
            mViewTransition.showView(1, true);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(getResources().getString(R.string.menu_download));
        setNavigationIcon(R.drawable.v_arrow_left_dark_x24);
    }

    @Override
    public void onNavigationClick() {
        onBackPressed();
    }

    @Override
    public int getNavCheckedItem() {
        return R.id.nav_download;
    }

    @Override
    public int getMenuResId() {
        return R.menu.fragment_download;
    }

    private void updateData() {
        mData = mDownloadManager.getDownloadInfoList();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_start_all:
                Intent intent = new Intent(getContext(), DownloadService.class);
                intent.setAction(DownloadService.ACTION_START_ALL);
                getActivity().startService(intent);
                return true;
            case R.id.action_stop_all:
                if(mDownloadManager != null) {
                    mDownloadManager.stopAllDownload();
                }
                return true;
        }
        return false;
    }

    @Override
    public void onAdd(DownloadInfo info) {
        int index = mData.indexOf(info);
        if(index != -1) {
            mAdapter.notifyItemChanged(index);
        }

        updateView();
    }

    @Override
    public void onUpdate(DownloadInfo info) {
        int index = mData.indexOf(info);
        if(index != -1) {
            mAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void onUpdateAll() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCancel(DownloadInfo info) {
        int index = mData.indexOf(info);
        if(index != -1) {
            mAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void onRemove(DownloadInfo info, int position) {
        if(position != -1) {
            mAdapter.notifyItemRemoved(position);
        }

        updateView();
    }

    private void bindForState(DownloadHolder holder, DownloadInfo info) {
        Resources resources = getResources();
        if (null == resources) {
            return;
        }

        int state = info.getState();
        switch (state) {
            case DownloadManager.STATE_NONE:
                bindState(holder, info, resources.getString(R.string.download_state_none));
                break;
            case DownloadManager.STATE_DOWNLOAD:
                bindProgress(holder, info);
                break;
            case DownloadManager.STATE_FAILED:
                bindState(holder, info, resources.getString(R.string.download_state_failed));
                break;
            case DownloadManager.STATE_FINISH:
                bindState(holder, info, resources.getString(R.string.download_state_finish));
                break;
            case DownloadManager.STATE_WAIT:
                bindState(holder, info, resources.getString(R.string.download_state_wait));
                break;
        }
    }

    private void bindState(DownloadHolder holder, DownloadInfo info, String state) {
        holder.mProgressBar.setVisibility(View.GONE);
        //holder.mSpeed.setVisibility(View.GONE);
        //holder.mTotalText.setVisibility(View.VISIBLE);

        if (info.getState() == DownloadManager.STATE_WAIT || info.getState() == DownloadManager.STATE_DOWNLOAD) {
            holder.mStart.setVisibility(View.GONE);
            holder.mStop.setVisibility(View.VISIBLE);
        } else {
            holder.mStart.setVisibility(View.VISIBLE);
            holder.mStop.setVisibility(View.GONE);
        }

        holder.mSpeed.setText(state);
        //holder.mTotalText.setText(FileUtils.humanReadableByteCount(info.getTotal(), true));
    }

    private void bindProgress(DownloadHolder holder, DownloadInfo info) {
        holder.mProgressBar.setVisibility(View.VISIBLE);
        //holder.mSpeed.setVisibility(View.VISIBLE);
        //holder.mTotalText.setVisibility(View.GONE);

        if (info.getState() == DownloadManager.STATE_WAIT || info.getState() == DownloadManager.STATE_DOWNLOAD) {
            holder.mStart.setVisibility(View.GONE);
            holder.mStop.setVisibility(View.VISIBLE);
        } else {
            holder.mStart.setVisibility(View.VISIBLE);
            holder.mStop.setVisibility(View.GONE);
        }

        int progress = 0 ;
        if(info.getTotal() != 0) {
            progress =(int)(info.getFinished()/(double)info.getTotal()*100);
        }
        holder.mProgressBar.setProgress(progress);
        holder.mSpeed.setText(FileUtils.humanReadableByteCount(info.getSpeed(), true) + "/S  " +
                FileUtils.humanReadableByteCount(info.getTotal(), true));
    }

    private class DownloadListAdapter extends RecyclerView.Adapter<DownloadHolder> {

        private final LayoutInflater mInflater;
        private final Context mContext;

        public DownloadListAdapter(@NonNull LayoutInflater inflater, Context context) {
            mContext = context;
            mInflater = inflater;
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);

            mRecyclerView.setAdapter(this);
            mRecyclerView.setLayoutManager(layoutManager);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        @Override
        public DownloadHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VideoDownloadFragment.DownloadHolder(mInflater.inflate(R.layout.item_download_list, parent, false));
        }

        @Override
        public void onBindViewHolder(VideoDownloadFragment.DownloadHolder holder, int position) {
            DownloadInfo info = mData.get(position);
            holder.mDownloadThumb.load(info.getToken(), info.getThumb());
            holder.mDownloadTitle.setText(info.getTitle());
            bindForState(holder, info);
        }
    }

    private class DownloadHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private View mItemView;
        public LoadImageView mDownloadThumb;
        public TextView mDownloadTitle;
        public ProgressBar mProgressBar;
        public TextView mSpeed;
        public TextView mTotalText;
        public ImageView mStart;
        public ImageView mStop;
        public Button mPlay;
        public Button mDelete;
        public SwipeLayout mSwipeLayout;

        public DownloadHolder(View itemView) {
            super(itemView);
            mItemView = itemView;

            mDownloadThumb = (LoadImageView) itemView.findViewById(R.id.download_thumb);
            mDownloadTitle = (TextView) itemView.findViewById(R.id.download_title);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.download_progress);
            mSpeed = (TextView) itemView.findViewById(R.id.speed_text);
            mTotalText = (TextView) itemView.findViewById(R.id.total_text);
            mStart = (ImageView) itemView.findViewById(R.id.start);
            mStop = (ImageView) itemView.findViewById(R.id.stop);
            mPlay = (Button) itemView.findViewById(R.id.play);
            mDelete = (Button) itemView.findViewById(R.id.delete);
            mSwipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe_layout);

            mStart.setOnClickListener(this);
            mStop.setOnClickListener(this);
            mPlay.setOnClickListener(this);
            mDelete.setOnClickListener(this);
            mProgressBar.setMax(100);

            //itemView.setOnTouchListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mData == null) {
                return;
            }

            int index = mRecyclerView.getChildAdapterPosition(mItemView);
            if(index < 0 && index >= mData.size()) {
                return;
            }

            if(v.getId() == R.id.start) {
                Intent intent = new Intent(getActivity(), DownloadService.class);
                intent.setAction(DownloadService.ACTION_START_RANGE);
                intent.putExtra(DownloadService.KEY_TOKEN_LIST, new String[]{mData.get(index).getToken()});
                getActivity().startService(intent);
            } else if(v.getId() == R.id.stop) {
                if(mDownloadManager != null) {
                    mDownloadManager.stopDownload(mData.get(index));
                }
            } else if(v.getId() == R.id.play) {
                DownloadInfo info = mData.get(index);
                if(mDownloadManager != null) {
                    String filePath = DownloadUtil.getFilePath(info.getTitle());
                    if(filePath != null && filePath.length() > 0) {
                        Intent intent = new Intent(getContext(), VideoPalyActivity.class);
                        intent.putExtra(VideoPalyActivity.KEY_TITLE, info.getTitle());
                        intent.putExtra(VideoPalyActivity.KEY_URL, filePath);
                        getActivity().startActivity(intent);
                    } else {
                        showTip("无法获取路径", Toast.LENGTH_SHORT);
                    }
                } else {
                    showTip("无法获取下载管理器", Toast.LENGTH_SHORT);
                }
            } else if(v.getId() == R.id.delete) {
                //mSwipeLayout.close();
                DownloadInfo info = mData.get(index);
                if(mDownloadManager != null) {
                    mDownloadManager.deleteDownload(info);
                }
            }
        }
    }
}
