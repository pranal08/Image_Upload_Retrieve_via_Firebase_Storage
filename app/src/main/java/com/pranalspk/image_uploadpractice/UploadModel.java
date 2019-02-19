package com.pranalspk.image_uploadpractice;

public class UploadModel {

    private String imageURL;
    private String description;

    public UploadModel() {
    }

    public UploadModel(String description, String imageURL) {
        this.imageURL = imageURL;
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getDescription() {
        return description;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
