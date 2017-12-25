package com.stream.enums;

import com.stream.hstream.Setting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seven-one on 2017/9/26.
 */

public enum GenreEnum {

    //gener-mucho
    Japanese(Setting.WEB_MUCHO, "Japanse", "japanese", GenreEnum.TYPE_GENER),
    English(Setting.WEB_MUCHO, "English", "english-subbed", GenreEnum.TYPE_GENER),
    PV(Setting.WEB_MUCHO, "PV", "preview", GenreEnum.TYPE_GENER),
    Anal(Setting.WEB_MUCHO, "AL", "anal", GenreEnum.TYPE_GENER),
    SchoolGirls(Setting.WEB_MUCHO, "SG", "school-girls", GenreEnum.TYPE_GENER),
    Rape(Setting.WEB_MUCHO, "RP", "rape", GenreEnum.TYPE_GENER),
//    Y2017(Setting.WEB_MUCHO, "2017", "2017", GenreEnum.TYPE_GENER),
//    Y2016(Setting.WEB_MUCHO, "2016", "2016", GenreEnum.TYPE_GENER),
//    Y2015(Setting.WEB_MUCHO, "2015", "2015", GenreEnum.TYPE_GENER),
//    Y2014(Setting.WEB_MUCHO, "2014", "2014", GenreEnum.TYPE_GENER),
//    Y2013(Setting.WEB_MUCHO, "2013", "2013", GenreEnum.TYPE_GENER),
//    Y2012(Setting.WEB_MUCHO, "2012", "2012", GenreEnum.TYPE_GENER),
//    Y2011(Setting.WEB_MUCHO, "2011", "2011", GenreEnum.TYPE_GENER),

    //gener-stream
    Home(Setting.WEB_STREAM, "Home", "home", GenreEnum.TYPE_GENER),

    //search
    StreamSearch("Stream", GenreEnum.TYPE_SEARCH),
    MochuSearch("Mucho", GenreEnum.TYPE_SEARCH);

    private static final String TYPE_GENER = "gener";
    private static final String TYPE_SEARCH = "search";

    private String value;
    private String title;
    private String web;
    private String type;

    private GenreEnum(String title, String type) {
        this.title = title;
        this.type = TYPE_SEARCH;
    }

    private GenreEnum(String web, String title, String value, String type) {
        this.value = value;
        this.title = title;
        this.web = web;
        this.type = type;
    }

    public String getValue() {
        return this.value;
    }

    public String getTitle() {
        return this.title;
    }

    public String getWeb() {
        return this.web;
    }

    private String getType() {
        return this.type;
    }

    public static List<GenreEnum> listForGener() {
        List<GenreEnum> list = new ArrayList<>();
        for(GenreEnum genreEnum: GenreEnum.values()) {
            if(Setting.getString(Setting.KEY_TYPE_WEB) == genreEnum.getWeb()) {
                list.add(genreEnum);
            }
        }

        return list;
    }

    public static List<GenreEnum> listForSearch() {
        List<GenreEnum> list = new ArrayList<>();
        for(GenreEnum genreEnum: GenreEnum.values()) {
            if(null != genreEnum.getType() && TYPE_SEARCH.equals(genreEnum.getType())) {
                list.add(genreEnum);
            }
        }

        return list;
    }

}
