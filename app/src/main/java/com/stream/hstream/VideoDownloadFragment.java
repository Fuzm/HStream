package com.stream.hstream;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hippo.yorozuya.FileUtils;
import com.stream.dao.DownloadInfo;
import com.stream.download.DownloadManager;
import com.stream.download.DownloadService;
import com.stream.hstream.adapter.SimpleAdapter;
import com.stream.hstream.adapter.SimpleHolder;
import com.stream.scene.SceneFragment;
import com.stream.scene.ToolBarFragment;
import com.stream.widget.LoadImageView;

import java.util.List;

/**
 * Created by Fuzm on 2017/5/7 0007.
 */

public class VideoDownloadFragment extends ToolBarFragment implements DownloadManager.DownloadInfoListener{

    private RecyclerView mRecyclerView;
    private DownloadListAdapter mAdapter;
    private List<DownloadInfo> mData;
    private DownloadManager mDownloadManager;

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
        return view;
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

    private class DownloadHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View mItemView;
        public LoadImageView mDownloadThumb;
        public TextView mDownloadTitle;
        public ProgressBar mProgressBar;
        public TextView mSpeed;
        public TextView mTotalText;
        public ImageView mStart;
        public ImageView mStop;

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

            mStart.setOnClickListener(this);
            mStop.setOnClickListener(this);
            mProgressBar.setMax(100);
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
            }
        }
    }
}
