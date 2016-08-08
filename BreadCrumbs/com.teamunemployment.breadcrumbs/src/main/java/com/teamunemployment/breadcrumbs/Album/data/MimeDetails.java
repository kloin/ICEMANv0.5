package com.teamunemployment.breadcrumbs.Album.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Josiah Kendall
 */
public class MimeDetails {

    @SerializedName("Id")
    @Expose
    private String id;

    @SerializedName("Extension")
    @Expose
    private String extension;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
