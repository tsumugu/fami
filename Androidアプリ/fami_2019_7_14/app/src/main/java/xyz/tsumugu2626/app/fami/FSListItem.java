package xyz.tsumugu2626.app.fami;

public class FSListItem {

    /*
     * FSListAdapterのItem
     */

    private String mMainImage = null;
    private String mNameText = null;
    private String mSubText = null;

    public FSListItem() {};

    public FSListItem(String main_image, String name_text, String sub_text) {
        mMainImage = main_image;
        mNameText = name_text;
        mSubText = sub_text;
    }

    public void setMainImage(String main_image) {
        mMainImage = main_image;
    }
    public void setNameText(String name_text) {
        mNameText = name_text;
    }
    public void setSubText(String sub_text) {
        mSubText = sub_text;
    }

    public String getMainImage() {
        return mMainImage;
    }
    public String getNameText() {
        return mNameText;
    }
    public String getSubText() {
        return mSubText;
    }

}
