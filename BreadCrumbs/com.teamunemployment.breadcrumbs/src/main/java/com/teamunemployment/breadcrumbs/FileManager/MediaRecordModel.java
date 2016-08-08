package com.teamunemployment.breadcrumbs.FileManager;

/**
 * Created by jek40 on 7/08/2016.
 */
public class MediaRecordModel {

    private String id;
    private float size; // Size in bytes
    private int databaseId;

    public MediaRecordModel(String id, float size) {
        this.id = id;
        this.size = size;
        this.databaseId = -1;
    }

    public MediaRecordModel(String id, float size, int databaseId) {
        this.id = id;
        this.size = size;
        this.databaseId = databaseId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public int getReferenceId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

}
