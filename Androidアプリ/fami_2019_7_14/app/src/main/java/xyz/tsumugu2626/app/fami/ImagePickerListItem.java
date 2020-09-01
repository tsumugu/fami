package xyz.tsumugu2626.app.fami;

public class ImagePickerListItem {

    /*
     * ImagePickerListAdapterのItem
     */

    private String mMainImage = null;
    private float mGrayImageAlpha = 0;

    public ImagePickerListItem() {};

    public ImagePickerListItem(String main_image, float gray_img_alpha) {
        mMainImage = main_image;
        mGrayImageAlpha = gray_img_alpha;
    }

    public void setGrayImageAlpha(float gray_img_alpha) {
        mGrayImageAlpha = gray_img_alpha;
    }

    public void setMainImage(String main_image) {
        mMainImage = main_image;
    }

    public String getMainImage() {
        return mMainImage;
    }

    public float getGrayImageAlpha() {
        return mGrayImageAlpha;
    }

}
