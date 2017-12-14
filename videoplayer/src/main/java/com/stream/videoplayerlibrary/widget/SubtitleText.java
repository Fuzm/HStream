package com.stream.videoplayerlibrary.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stream.videoplayerlibrary.R;
import com.stream.videoplayerlibrary.common.VideoUtils;
import com.stream.videoplayerlibrary.subtitle.Caption;
import com.stream.videoplayerlibrary.subtitle.FormatASS;
import com.stream.videoplayerlibrary.subtitle.FormatSCC;
import com.stream.videoplayerlibrary.subtitle.FormatSRT;
import com.stream.videoplayerlibrary.subtitle.FormatSTL;
import com.stream.videoplayerlibrary.subtitle.FormatTTML;
import com.stream.videoplayerlibrary.subtitle.TimedTextFileFormat;
import com.stream.videoplayerlibrary.subtitle.TimedTextObject;
import com.stream.videoplayerlibrary.tv.VideoPlayer;

import junit.framework.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.Collection;

/**
 * Created by Seven-one on 2017/10/7.
 */

public class SubtitleText extends StrokeTextView {

    private static final String TAG = SubtitleText.class.getSimpleName();

    private SubtitleProcessingTask mTask;
    private VideoPlayer mVideoPlayer;
    private TimedTextObject mSubtitle;

    private String mAssetsPath;
    private File mFile;
    private float mAjustTime = 0f;

    public SubtitleText(Context context) {
        super(context);
    }

    public SubtitleText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SubtitleText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * load subtitle from file
     * @param videoPlayer
     * @param file
     */
    public void load(VideoPlayer videoPlayer, File file) {
        Assert.assertNotNull("video player not null", videoPlayer);
        mFile = file;
        mVideoPlayer = videoPlayer;

        if(mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }

        mTask = new SubtitleProcessingTask(file);
        mTask.execute();
    }

    /**
     * load subtitle from assets dir
     * @param videoPlayer
     * @param assetsPath
     */
    public void load(VideoPlayer videoPlayer,String assetsPath) {
        Assert.assertNotNull("video player not null", videoPlayer);
        mAssetsPath = assetsPath;
        mVideoPlayer = videoPlayer;

        if(mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }

        mTask = new SubtitleProcessingTask(assetsPath);
        mTask.execute();
    }

    /**
     * get subtitle assets path
     * @return
     */
    public String getSubtitleAssetsPath() {
        return mAssetsPath;
    }

    /**
     * get subtitle file
     * @return
     */
    public File getSubtitleFile() {
        return mFile;
    }

    public void setAjustTime(float ajustTime) {
        mAjustTime = ajustTime;
    }

    public void cancel() {
        if (subtitleProcessesor != null) {
            removeCallbacks(subtitleProcessesor);
            if (mTask != null)
                mTask.cancel(true);
        }

        setVisibility(INVISIBLE);
        mSubtitle = null;
        mAjustTime = 0;
        setText("");
    }

    private void onTimedText(Caption text) {
        if (text == null) {
            setVisibility(View.INVISIBLE);
            return;
        }

//        if(text.style != null) {
//            Typeface typeface = Typeface.DEFAULT;
//            setTypeface(typeface);
//            setTextSize(Float.parseFloat(text.style.getFontSize()));
//            setTextColor(Integer.parseInt(text.style.getColor().substring(1),16));
//            setBackgroundColor(Integer.parseInt(text.style.getBackgroundColor().substring(1),16));
//        }

        setText(Html.fromHtml(text.content));
        setVisibility(View.VISIBLE);
    }

    private Runnable subtitleProcessesor = new Runnable() {

        @Override
        public void run() {
            if (mVideoPlayer != null && mVideoPlayer.isPlaying() && mSubtitle != null) {
                long currentPos = mVideoPlayer.getCurrentPosition();
                Log.d(TAG, "ajust time: " + mAjustTime);
                currentPos += (mAjustTime * 1000);

                Collection<Caption> subtitles = mSubtitle.captions.values();
                for (Caption caption : subtitles) {
                    if (currentPos >= caption.start.mseconds && currentPos <= caption.end.mseconds) {
                        onTimedText(caption);
                        break;
                    } else if (currentPos > caption.end.mseconds) {
                        onTimedText(null);
                    }
                }
            }
            postDelayed(this, 100);
        }
    };

    private class SubtitleProcessingTask extends AsyncTask<Void, Void, Void> {

        private String fileName;
        private InputStream inputStream;
        private String codeset;

        public SubtitleProcessingTask(File file) {
            try {
                fileName = file.getName();
                codeset = VideoUtils.getJavaEncode(file);
                inputStream = new FileInputStream(file);
                Log.d(TAG, "load subtitle file : " + file.getAbsolutePath() + " and codeset : " + codeset);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public SubtitleProcessingTask(String assetsPath) {
            try {
                fileName = assetsPath;
                codeset = getAssetsCodeSet(assetsPath);
                inputStream = getContext().getAssets().open(assetsPath);
                Log.d(TAG, "load subtitle file : " + assetsPath  + " and codeset : " + codeset);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String getAssetsCodeSet(String assetsPath) {
            byte[] rawtext;
            InputStream inputStream = null;
            try {
                inputStream = getContext().getAssets().open(assetsPath);
                rawtext = new byte[inputStream.available()];
                inputStream.read(rawtext);

                return VideoUtils.getJavaEncode(rawtext);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }
            }

            return null;
        }

        private TimedTextFileFormat createFormat(String subtitleName) {
            TimedTextFileFormat fileFormat = null;
            String lowerSubtitleName = subtitleName.toLowerCase();
            if(lowerSubtitleName.endsWith("ass") || lowerSubtitleName.endsWith("ssa")) {
                fileFormat = new FormatASS();
            } else if(lowerSubtitleName.endsWith("scc")) {
                fileFormat = new FormatSCC();
            } else if(lowerSubtitleName.endsWith("srt")) {
                fileFormat = new FormatSRT();
            } else if(lowerSubtitleName.endsWith("stl")) {
                fileFormat = new FormatSTL();
            } else if(lowerSubtitleName.endsWith("xml")) {
                fileFormat = new FormatTTML();
            }

            Log.d(TAG, "Creat format for " + subtitleName);

            return fileFormat;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //FormatASS formatAss = new FormatASS();
                TimedTextFileFormat fileFormat = createFormat(fileName);
                mSubtitle = fileFormat.parseFile(fileName, inputStream, codeset);
                Log.d(TAG, "format subtitle complete");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "error in downloadinf subs");
            } finally {
                if(inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    inputStream = null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (null != mSubtitle) {
                //subtitle.setText("");
                post(subtitleProcessesor);
            }
            super.onPostExecute(result);
        }
    }

    public static void main(String[] args) throws IOException {
        FileInputStream inputStream = new FileInputStream("E:/After....1.ssa");

        //byte[] testBytes = new byte[1024];
        //inputStream.read(testBytes);

        String codeset = VideoUtils.getJavaEncode("E:/After....1.ssa");
        System.out.println("code set: " + codeset);

        FormatASS formatASS = new FormatASS();
        TimedTextObject subtitle = formatASS.parseFile("sdfds", inputStream, codeset);

        System.out.print(subtitle.warnings);
    }
}
