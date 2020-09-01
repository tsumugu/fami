package xyz.tsumugu2626.app.fami;

public class MVMListItem {

    /*
     * MVMListAdapterのItem
     */

    private String mFromText = null;
    private String mToText = null;
    private String mFileUrl = null;
    private String mFileName = null;
    private String mParams = null;

    public MVMListItem() {};

    public MVMListItem(String from_text, String to_text, String file_url, String file_name, String params) {
        mFromText = from_text;
        mToText = to_text;
        mFileUrl = file_url;
        mFileName = file_name;
        mParams = params;
    }

    public void setFromText(String from_text) {
        mFromText = from_text;
    }

    public void setToText(String to_text) {
        mToText = to_text;
    }

    public void setFileUrl(String file_url) {
        mFileUrl = file_url;
    }

    public void setFileName(String file_name) {
        mFileName = file_name;
    }

    public void setParams(String params) {
        mParams = params;
    }

    public String getFromText() {
        return mFromText;
    }

    public String getToText() {
        return mToText;
    }

    public String getFileUrl() {
        return mFileUrl;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getParams() {
        return mParams;
    }
}
