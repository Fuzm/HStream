package com.stream.client.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Fuzm on 2017/3/25 0025.
 */

public class VideoDetailInfo {

    public String name;
    public String url;

    public Map parseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("url", url);
        return map;
    }
}
