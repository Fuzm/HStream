package com.stream.enums;

import com.stream.util.StreamUtils;

/**
 * Created by Seven-one on 2017/9/26.
 */

public enum VideoFormat {

    m3u8(".m3u8"), mp4(".mp4");

    private String value;

    private VideoFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
