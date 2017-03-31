package com.stream.okhttp;

import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Request;

/**
 * Created by Fuzm on 2017/3/27 0027.
 */

public class MobileRequestBuilder extends Request.Builder {

//    private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.3.7; en-us; Nexus One Build/FRF91) " +
//            "AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
    private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 5.0.1; zh-CN; M351 Build/LRX22C) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/40.0.2214.89 UCBrowser/11.4.5.937 Mobile Safari/537.36";

    public MobileRequestBuilder(String url) throws MalformedURLException {
        url(new URL(url));
        addHeader("User-Agent", MOBILE_USER_AGENT);

    }
}
