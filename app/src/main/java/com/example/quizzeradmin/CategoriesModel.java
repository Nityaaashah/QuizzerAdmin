package com.example.quizzeradmin;

import java.util.List;

public class CategoriesModel {
    private String name, url;
    private List<String> sets;
    String key;



    public CategoriesModel(String name, List<String> sets, String url, String key) {
        this.name = name;
        this.url = url;
        this.sets = sets;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getSets() {
        return sets;
    }

    public void setSets(List<String> sets) {
        this.sets = sets;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
