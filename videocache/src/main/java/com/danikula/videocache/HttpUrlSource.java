package com.danikula.videocache;

import android.text.TextUtils;
import android.util.Log;

import com.danikula.videocache.headers.EmptyHeadersInjector;
import com.danikula.videocache.headers.HeaderInjector;
import com.danikula.videocache.sourcestorage.SourceInfoStorage;
import com.danikula.videocache.sourcestorage.SourceInfoStorageFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.danikula.videocache.Preconditions.checkNotNull;
import static com.danikula.videocache.ProxyCacheUtils.DEFAULT_BUFFER_SIZE;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

/**
 * Created by Seven-one on 2017/10/17.
 */

public class HttpUrlSource implements Source {

    private static final Logger LOG = LoggerFactory.getLogger("HttpUrlSource");

    private static final int MAX_REDIRECTS = 5;
    private final SourceInfoStorage sourceInfoStorage;
    private final HeaderInjector headerInjector;
    private SourceInfo sourceInfo;
    private OkHttpConnection connection;
    private InputStream inputStream;

    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(20,TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();

    public HttpUrlSource(String url) {
        this(url, SourceInfoStorageFactory.newEmptySourceInfoStorage());
    }

    public HttpUrlSource(String url, SourceInfoStorage sourceInfoStorage) {
        this(url, sourceInfoStorage, new EmptyHeadersInjector());
    }

    public HttpUrlSource(String url, SourceInfoStorage sourceInfoStorage, HeaderInjector headerInjector) {
        this.sourceInfoStorage = checkNotNull(sourceInfoStorage);
        this.headerInjector = checkNotNull(headerInjector);
        SourceInfo sourceInfo = sourceInfoStorage.get(url);
        this.sourceInfo = sourceInfo != null ? sourceInfo :
                new SourceInfo(url, Integer.MIN_VALUE, ProxyCacheUtils.getSupposablyMime(url));
    }

    public HttpUrlSource(HttpUrlSource source) {
        this.sourceInfo = source.sourceInfo;
        this.sourceInfoStorage = source.sourceInfoStorage;
        this.headerInjector = source.headerInjector;
    }

    @Override
    public synchronized long length() throws ProxyCacheException {
        if (sourceInfo.length == Integer.MIN_VALUE) {
            fetchContentInfo();
        }
        return sourceInfo.length;
    }

    @Override
    public void open(long offset) throws ProxyCacheException {
        try {
            Log.d("HttUrlSource", "Open okhttp connection for " + sourceInfo.url);
            connection = openConnection(offset);
            String mime = connection.getContentType();
            inputStream = new BufferedInputStream(connection.getInputStream(), DEFAULT_BUFFER_SIZE);
            long length = readSourceAvailableBytes(connection, offset, connection.getResponseCode());
            this.sourceInfo = new SourceInfo(sourceInfo.url, length, mime);
            this.sourceInfoStorage.put(sourceInfo.url, sourceInfo);
        } catch (IOException e) {
            throw new ProxyCacheException("Error opening connection for " + sourceInfo.url + " with offset " + offset, e);
        }
    }

    private long readSourceAvailableBytes(OkHttpConnection connection, long offset, int responseCode) throws IOException {
        long contentLength = getContentLength(connection);
        return responseCode == HTTP_OK ? contentLength
                : responseCode == HTTP_PARTIAL ? contentLength + offset : sourceInfo.length;
    }

    private long getContentLength(OkHttpConnection connection) {
        String contentLengthValue = connection.getHeaderField("Content-Length");
        return contentLengthValue == null ? -1 : Long.parseLong(contentLengthValue);
    }

    @Override
    public void close() throws ProxyCacheException {
        if (connection != null) {
            try {
                Log.d("HttUrlSource", "Close okhttp connection for " + sourceInfo.url);
                connection.disconnect();
            } catch (NullPointerException | IllegalArgumentException e) {
                String message = "Wait... but why? WTF!? " +
                        "Really shouldn't happen any more after fixing https://github.com/danikula/AndroidVideoCache/issues/43. " +
                        "If you read it on your device log, please, notify me danikula@gmail.com or create issue here " +
                        "https://github.com/danikula/AndroidVideoCache/issues.";
                throw new RuntimeException(message, e);
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.error("Error closing connection correctly. Should happen only on Android L. " +
                        "If anybody know how to fix it, please visit https://github.com/danikula/AndroidVideoCache/issues/88. " +
                        "Until good solution is not know, just ignore this issue :(", e);
            }
        }
    }

    @Override
    public int read(byte[] buffer) throws ProxyCacheException {
        if (inputStream == null) {
            throw new ProxyCacheException("Error reading data from " + sourceInfo.url + ": okHttpClient is absent!");
        }
        try {
            //Log.d("HttpUrlSource", "Read data :" + buffer.length);
            return inputStream.read(buffer, 0, buffer.length);
        } catch (InterruptedIOException e) {
            throw new InterruptedProxyCacheException("Reading source " + sourceInfo.url + " is interrupted", e);
        } catch (IOException e) {
            throw new ProxyCacheException("Error reading data from " + sourceInfo.url, e);
        }
    }

    private void fetchContentInfo() throws ProxyCacheException {
        LOG.debug("Read content info from " + sourceInfo.url);
        OkHttpConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = openConnection(0);
            long length = getContentLength(urlConnection);
            String mime = urlConnection.getContentType();
            inputStream = urlConnection.getInputStream();
            this.sourceInfo = new SourceInfo(sourceInfo.url, length, mime);
            this.sourceInfoStorage.put(sourceInfo.url, sourceInfo);
            LOG.debug("Source info fetched: " + sourceInfo);
            Log.d("HttUrlSource", "Source info fetched: " + sourceInfo);
        } catch (IOException e) {
            LOG.error("Error fetching info from " + sourceInfo.url, e);
        } finally {
            ProxyCacheUtils.close(inputStream);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private OkHttpConnection openConnectionForHeader() throws IOException, ProxyCacheException {
        boolean redirected;
        int redirectCount = 0;
        String url = this.sourceInfo.url;

        Call requestCall = null;
        Response response = null;
        do {
            Request request = new Request.Builder()
                    .head()
                    .url(url)
                    .build();

            requestCall = okHttpClient.newCall(request);
            response = requestCall.execute();

            int code = response.code();
            redirected = code == HTTP_MOVED_PERM || code == HTTP_MOVED_TEMP || code == HTTP_SEE_OTHER;
            if (redirected) {
                url = response.header("Location");
                redirectCount++;
                requestCall.cancel();
            }

            if (redirectCount > MAX_REDIRECTS) {
                throw new ProxyCacheException("Too many redirects: " + redirectCount);
            }

        } while (redirected);

        return new OkHttpConnection(requestCall, response);
    }

    private OkHttpConnection openConnection(long offset) throws IOException, ProxyCacheException {
        boolean redirected;
        int redirectCount = 0;
        String url = this.sourceInfo.url;

        Call requestCall = null;
        Response response = null;
        do {
            LOG.debug("Open okHttpClient " + (offset > 0 ? " with offset " + offset : "") + " to " + url);
            Log.d("HttUrlSource", "Open okHttpClient " + (offset > 0 ? " with offset " + offset : "") + " to " + url);
            requestCall = okHttpClient.newCall(buildRequest(offset, url));
            response = requestCall.execute();

            int code = response.code();
            redirected = code == HTTP_MOVED_PERM || code == HTTP_MOVED_TEMP || code == HTTP_SEE_OTHER;
            if (redirected) {
                url = response.header("Location");
                redirectCount++;
                requestCall.cancel();
            }

            if (redirectCount > MAX_REDIRECTS) {
                throw new ProxyCacheException("Too many redirects: " + redirectCount);
            }

        } while (redirected);

        return new OkHttpConnection(requestCall, response);
    }

    private Request buildRequest(long offset, String url) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        injectCustomHeaders(builder, url);

        if (offset > 0) {
            builder.addHeader("Range", "bytes=" + offset + "-");
        }

        builder.addHeader("Connection", "close");
        return builder.build();
    }

    private void injectCustomHeaders(Request.Builder builder, String url) {
        Map<String, String> extraHeaders = headerInjector.addHeaders(url);
        for (Map.Entry<String, String> header : extraHeaders.entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }
    }

    public synchronized String getMime() throws ProxyCacheException {
        if (TextUtils.isEmpty(sourceInfo.mime)) {
            fetchContentInfo();
        }
        return sourceInfo.mime;
    }

    public String getUrl() {
        return sourceInfo.url;
    }

    @Override
    public String toString() {
        return "HttpUrlSource{url='" + sourceInfo.url + "}";
    }

    private class OkHttpConnection {

        private Call call;
        private Response response;

        public OkHttpConnection(Call call, Response response) {
            this.call = call;
            this.response = response;
        }

        public String getContentType() {
            return response.header("Content-Type");
        }

        public InputStream getInputStream() {
            return response.body().byteStream();
        }

        public int getResponseCode() {
            return response.code();
        }

        public String getHeaderField(String key) {
            return response.header(key);
        }

        public void disconnect() {
            call.cancel();
        }
    }
}
