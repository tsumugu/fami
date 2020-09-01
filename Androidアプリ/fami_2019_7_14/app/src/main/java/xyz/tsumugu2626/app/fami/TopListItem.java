package xyz.tsumugu2626.app.fami;

public class TopListItem {

    /*
     * TopListAdapterのItem
     */

    private String mThumbnail = null;
    private String mName = null;
    private String mUid = null;
    private boolean isGettingOut = false;
    private boolean isINVISIBLE = false;

    public TopListItem() {};

    public TopListItem(String thumbnail, String name, String uid, boolean mbg, boolean mib) {
        mThumbnail = thumbnail;
        mName = name;
        mUid = uid;
        isGettingOut = mbg;
        isINVISIBLE = mib;
    }

    public void setThumbnail(String thumbnail) {
        mThumbnail = thumbnail;
    }

    public void setmName(String name) {
        mName = name;
    }

    public void setmUid(String uid) {
        mUid = uid;
    }

    public void setIsGettingOut(boolean mbg) {
        isGettingOut = mbg;
    }

    public void setIsINVISIBLE(boolean mib) {
        isINVISIBLE = mib;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public String getName() {
        return mName;
    }

    public String getUid() {
        return mUid;
    }

    public boolean getIsGettingOut() {
        return isGettingOut;
    }

    public boolean getIsINVISIBLE() {
        return isINVISIBLE;
    }

}
