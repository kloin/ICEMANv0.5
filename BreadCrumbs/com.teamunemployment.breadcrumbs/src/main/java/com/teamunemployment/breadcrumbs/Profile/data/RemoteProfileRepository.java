package com.teamunemployment.breadcrumbs.Profile.data;

import com.teamunemployment.breadcrumbs.RESTApi.NodeService;

import java.util.ArrayList;

/**
 * Created by jek40 on 30/06/2016.
 */
public class RemoteProfileRepository implements ProfileRepositoryContract {

    NodeService nodeService;

    public RemoteProfileRepository(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public String getUserName(long userId) {
        return nodeService.getStringProperty(Long.toString(userId), "Username");
    }

    @Override
    public String getUserAbout(long userId) {
        return nodeService.getStringProperty(Long.toString(userId), "About");
    }

    @Override
    public String getUserWeb(long userId) {
        return nodeService.getStringProperty(Long.toString(userId), "Web");
    }

    @Override
    public String getProfilePictureId(long userId) {
        return null;
    }

    @Override
    public ArrayList<String> getUserTrailIds(long userId) {
        return null;
    }

    @Override
    public void saveUserName(String username, long userId) {
        nodeService.setStringProperty(Long.toString(userId), "Username", username);
    }

    @Override
    public void saveUserAbout(String about, long userId) {
        nodeService.setStringProperty(Long.toString(userId), "About", about);
    }

    @Override
    public void saveUserWeb(String website, long userId) {
        nodeService.setStringProperty(Long.toString(userId), "Web", website);
    }

    @Override
    public void saveProfilePictureId(String profilePicId, long userId) {

    }
}
