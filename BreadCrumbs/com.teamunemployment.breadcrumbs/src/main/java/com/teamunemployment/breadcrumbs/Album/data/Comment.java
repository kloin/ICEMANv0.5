package com.teamunemployment.breadcrumbs.Album.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Josiah Kendall.
 */
public class Comment {

    @SerializedName("UserId")
    @Expose
    private String userId;
    @SerializedName("CommentText")
    @Expose
    private String commentText;
    @SerializedName("EntityId")
    @Expose
    private String entityId;

    @SerializedName("Id")
    @Expose
    private String id;


    public String getCommentText() {
        return commentText;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getUserId() {
        return userId;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
