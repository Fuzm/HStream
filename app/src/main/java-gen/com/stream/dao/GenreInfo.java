package com.stream.dao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "hs_genre_info".
 */
public class GenreInfo {

    /** Not-null value. */
    private String genre_id;
    /** Not-null value. */
    private String genre_name;
    private Integer status;

    public GenreInfo() {
    }

    public GenreInfo(String genre_id) {
        this.genre_id = genre_id;
    }

    public GenreInfo(String genre_id, String genre_name, Integer status) {
        this.genre_id = genre_id;
        this.genre_name = genre_name;
        this.status = status;
    }

    /** Not-null value. */
    public String getGenre_id() {
        return genre_id;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setGenre_id(String genre_id) {
        this.genre_id = genre_id;
    }

    /** Not-null value. */
    public String getGenre_name() {
        return genre_name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setGenre_name(String genre_name) {
        this.genre_name = genre_name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
