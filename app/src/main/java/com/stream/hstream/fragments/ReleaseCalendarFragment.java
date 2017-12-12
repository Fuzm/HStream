package com.stream.hstream.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarView;
import com.stream.client.HsClient;
import com.stream.client.HsRequest;
import com.stream.client.HsUrl;
import com.stream.client.data.ReleaseInfo;
import com.stream.client.parser.ReleaseInfoParser;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.R;
import com.stream.scene.SceneFragment;
import com.stream.util.DateUtils;
import com.stream.widget.LoadImageView;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Seven-one on 2017/11/7 0007.
 */

public class ReleaseCalendarFragment extends SceneFragment {

    private TextView mTextMonthDay;
    private TextView mTextYear;
    private TextView mTextLunar;
    private TextView mTextCurrentDay;

    //private ProgressView mProgressView;
    private CalendarView mCalendarView;
    private RecyclerView mRecycleView;
    private ReleaseInfoAdatper mAdapter;

    private Map<Calendar, List<ReleaseInfo>> mDataMap;
    private List<ReleaseInfo> mData;
    private HsClient mClient;

    private int mYear;
    private int mMonth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_release_main, container, false);

        mTextMonthDay = (TextView) view.findViewById(R.id.text_month_day);
        mTextYear = (TextView) view.findViewById(R.id.text_year);
        mTextLunar = (TextView) view.findViewById(R.id.text_lunar);
        mCalendarView = (CalendarView) view.findViewById(R.id.calendarView);
        mTextCurrentDay = (TextView) view.findViewById(R.id.text_current_day);

        mTextYear.setText(String.valueOf(mCalendarView.getCurYear()));
        mTextMonthDay.setText(mCalendarView.getCurMonth() + "月" + mCalendarView.getCurDay() + "日");
        mTextLunar.setText("今日");
        mTextCurrentDay.setText(String.valueOf(mCalendarView.getCurDay()));

        mRecycleView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ReleaseInfoAdatper(getContext());
        mRecycleView.setAdapter(mAdapter);

        mData = new ArrayList<>();
        mDataMap = new HashMap<>();
        mClient = HStreamApplication.getHsClient(getContext());

        requireReleaseInfo(mCalendarView.getCurYear(), mCalendarView.getCurMonth());
        registeListener();
        return view;
    }

    /**
     * registe listener
     */
    private void registeListener() {

        mTextMonthDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendarView.showSelectLayout(mCalendarView.getCurYear());
                mTextLunar.setVisibility(View.GONE);
                mTextYear.setVisibility(View.GONE);
                mTextMonthDay.setText(String.valueOf(mCalendarView.getCurYear()));
            }
        });

        mTextCurrentDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendarView.scrollToCurrent();
            }
        });

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onDateChange(Calendar calendar) {
                mTextLunar.setVisibility(View.VISIBLE);
                mTextYear.setVisibility(View.VISIBLE);
                mTextMonthDay.setText(calendar.getMonth() + "月" + calendar.getDay() + "日");
                mTextYear.setText(String.valueOf(calendar.getYear()));
                mTextLunar.setText(calendar.getLunar());

                //if the year or month be changed, it need require new information
                if(!(mYear == calendar.getYear() && mMonth == calendar.getMonth())) {
                    requireReleaseInfo(calendar.getYear(), calendar.getMonth());
                }

                notifyDataChange();
            }

            @Override
            public void onYearChange(int year) {
                mTextMonthDay.setText(String.valueOf(year));
            }
        });
    }

    /**
     * notify data change
     */
    private void notifyDataChange() {
        Calendar calendar = mCalendarView.getSelectedCalendar();
        if(calendar != null && mDataMap.containsKey(calendar)) {
            mData.clear();
            mData.addAll(mDataMap.get(calendar));
            mAdapter.notifyDataSetChanged();
        } else {
            mData.clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * success require
     * @param result
     */
    private void onRequireReleaseInfoSuccess(ReleaseInfoParser.Result result) {
        if(result != null && result.releaseInfoList != null) {
             //mData.addAll(result.releaseInfoList);
             //mAdapter.notifyDataSetChanged();
             List<Calendar> calendarList = new ArrayList<>();
             for(ReleaseInfo info: result.releaseInfoList) {
                 String releaseDate = info.getReleaseDate();

                 Calendar calendar = createCalendarByStr(releaseDate);
                 if(calendar != null) {
                     if(!mDataMap.containsKey(calendar)) {
                         mDataMap.put(calendar, new ArrayList<ReleaseInfo>());
                         calendarList.add(calendar);
                     }
                     mDataMap.get(calendar).add(info);
                 }
             }

             if(calendarList.size() > 0) {
                 mCalendarView.setSchemeDate(calendarList);
             }

             notifyDataChange();
        }
    }

    /**
     * fail require
     */
    private void onRequireReleaseInfoFail() {
        //mData.clear();
    }

    /**
     * create calendar by dateStr
     * @param dateStr  formate: yyyymmdd
     */
    private Calendar createCalendarByStr(String dateStr) {
        Date date = DateUtils.fromDateStr(dateStr);

        if(date != null) {
            Calendar calendar = new Calendar();
            calendar.setTime(date);
            calendar.setScheme("release");
            calendar.setSchemeColor(0xFF13acf0);

            return calendar;
        } else {
            return null;
        }
    }

    /**
     * require release info
     */
    private void requireReleaseInfo(int year, int month) {
        mData.clear();
        mDataMap.clear();
        mAdapter.notifyDataSetChanged();

        mYear = year;
        mMonth = month;

        final HsRequest request = new HsRequest();
        request.setMethod(HsClient.METHOD_GET_RELEASE_INFO);
        request.setArgs(HsUrl.getReleaseInfoUrl(year + "-" + (month < 10 ? "0" : "") + month));
        request.setCallback(new HsClient.Callback<ReleaseInfoParser.Result>() {

            @Override
            public void onSuccess(ReleaseInfoParser.Result result) {
                ReleaseCalendarFragment.this.onRequireReleaseInfoSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                ReleaseCalendarFragment.this.onRequireReleaseInfoFail();
            }

            @Override
            public void onCancel() {

            }
        });

        mClient.execute(request);
    }

    private class ReleaseInfoAdatper extends RecyclerView.Adapter<ReleaseInfoHolder> {

        private final LayoutInflater mInflater;

        public ReleaseInfoAdatper(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public ReleaseInfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ReleaseInfoHolder(mInflater.inflate(R.layout.item_release_list, parent, false));
        }

        @Override
        public void onBindViewHolder(ReleaseInfoHolder holder, int position) {
            ReleaseInfo info = mData.get(position);

            if(info != null) {
                holder.mAttachImage.load(info.getSummary(), info.getAttach());
                holder.mTextSummary.setText(info.getSummary());
                holder.mTextDescription.setText(info.getDescription().replace("\\n", "\n"));
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    private class ReleaseInfoHolder extends RecyclerView.ViewHolder {

        public LoadImageView mAttachImage;
        public TextView mTextSummary;
        public TextView mTextDescription;

        public ReleaseInfoHolder(View itemView) {
            super(itemView);

            mAttachImage = (LoadImageView) itemView.findViewById(R.id.attach_image);
            mTextSummary = (TextView) itemView.findViewById(R.id.summary);
            mTextDescription = (TextView) itemView.findViewById(R.id.description);
        }
    }

}
