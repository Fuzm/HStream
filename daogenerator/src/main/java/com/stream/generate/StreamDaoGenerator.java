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
        Utilities.deleteContents(new File(DELETE_DIR));
        File outDir = new File(OUT_DIR);
        outDir.delete();
        outDir.mkdirs();

        Schema schema = new Schema(VERSION, PACKAGE);
        addDownloads(schema);
        addFavorites(schema);
        addSuggestions(schema);

        new DaoGenerator( ).generateAll(schema, OUT_DIR);
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

        entity.addIntProperty("id").primaryKey().notNull();;
        entity.addStringProperty("query");
        entity.addLongProperty("date");
    }

    public static void main(String[] args) throws Exception {
        StreamDaoGenerator.generate ();
    }
}
