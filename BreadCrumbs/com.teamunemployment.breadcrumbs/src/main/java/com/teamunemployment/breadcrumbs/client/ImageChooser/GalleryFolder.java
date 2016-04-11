package com.teamunemployment.breadcrumbs.client.ImageChooser;

import java.util.ArrayList;

/**
 * Created by jek40 on 6/04/2016.
 */
public class GalleryFolder {

    public int NumberOfPhotos;
    public final String FolderName;
    public ArrayList<String> Images = new ArrayList<>();
    public GalleryFolder(int numberOfPhotos, String folderName) {
        NumberOfPhotos = numberOfPhotos;
        FolderName = folderName;
    }

}
