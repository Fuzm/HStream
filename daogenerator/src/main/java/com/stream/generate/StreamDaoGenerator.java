package com.stream.generate;

import java.io.File;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class StreamDaoGenerator {

    private static final int VERSION = 1;

    private static final String PACKAGE = "com.stream.dao";
    private static final String OUT_DIR = "app/src/main/java-gen";
    private static final String DELETE_DIR = "app/src/main/java-gen/com/stream/dao";

    public static void generate() throws Exception {
        //Utilities.deleteContents(new File(DELETE_DIR));
        //File outDir = new File(OUT_DIR);
        //outDir.delete();
        //outDir.mkdirs();

        Schema schema = new Schema(VERSION, PACKAGE);
        addDetailInfo(schema);
        addSourceInfo(schema);
        addDownloads(schema);
        addFavorites(schema);
        addSuggestions(schema);
        addGenerInfo(schema);

        new DaoGenerator( ).generateAll(schema, OUT_DIR);
    }

    private static void addDetailInfo(Schema schema) {
        Entity entity = schema.addEntity("DetailInfo");
        entity.setTableName("hs_detail_info");
        entity.setClassNameDao("DetailDao");

        entity.addStringProperty("token").primaryKey();
        entity.addStringProperty("video_title").notNull();
        entity.addStringProperty("alternative_name");
        entity.addStringProperty("offering_date");
        entity.addStringProperty("subtitle_path");
        entity.addStringProperty("detail_url");
    }

    private static void addSourceInfo(Schema schema) {
        Entity entity = schema.addEntity("SourceInfo");
        entity.setTableName("hs_source_info");
        entity.setClassNameDao("SourceDao");

        entity.addIdProperty().autoincrement();
        entity.addStringProperty("token").notNull();
        entity.addStringProperty("video_title");
        entity.addStringProperty("source_url").notNull();
        entity.addStringProperty("source_name").notNull();
        entity.addStringProperty("video_url").notNull();
        entity.addLongProperty("upd_time").notNull();
    }

    private static void addDownloads(Schema schema) {
        Entity entity = schema.addEntity("DownloadInfo");
        entity.setTableName("hs_dowanload_info");
        entity.setClassNameDao("DownloadDao");

        entity.addStringProperty("token").primaryKey().notNull();;
        entity.addStringProperty("title");
        entity.addStringProperty("thumb");
        entity.addStringProperty("source_url");
        entity.addStringProperty("url");
        entity.addStringProperty("alternative_name");
        //entity.addDoubleProperty("content_length");
        //entity.addFloatProperty("rating");
        entity.addIntProperty("state").notNull();
        entity.addLongProperty("time").notNull();
    }

    private static void addFavorites(Schema schema) {
        Entity entity = schema.addEntity("Favorite");
        entity.setTableName("hs_favorites");
        entity.setClassNameDao("FavoriteDao");

        entity.addStringProperty("token").primaryKey().notNull();;
        entity.addStringProperty("title");
        entity.addStringProperty("thumb");
        entity.addStringProperty("source_url");
        entity.addStringProperty("video_url");
        entity.addLongProperty("time").notNull();
    }

    private static void addSuggestions(Schema schema) {
        Entity entity = schema.addEntity("Suggestion");
        entity.setTableName("hs_suggestions");
        entity.setClassNameDao("SuggestionDao");

        entity.addIdProperty().autoincrement();
        entity.addStringProperty("query");
        entity.addLongProperty("date");
    }

    private static void addGenerInfo(Schema schema) {
        Entity entity = schema.addEntity("GenreInfo");
        entity.setTableName("hs_genre_info");
        entity.setClassNameDao("GenreDao");

        entity.addStringProperty("genre_id").primaryKey().notNull();
        entity.addStringProperty("genre_name").notNull();
        entity.addIntProperty("status");
    }

    public static void main(String[] args) throws Exception {
        StreamDaoGenerator.generate ();
    }
}
