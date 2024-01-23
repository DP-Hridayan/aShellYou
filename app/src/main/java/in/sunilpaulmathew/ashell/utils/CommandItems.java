package in.sunilpaulmathew.ashell.utils;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 05, 2022
 */
public class CommandItems implements Serializable {

    private final String mTitle, mSummary, mExample;

    public CommandItems(String title, String summary, String example) {
        this.mTitle = title;
        this.mSummary = summary;
        this.mExample = example;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSummary() {
        return mSummary;
    }

    public String getExample() {
        return mExample;
    }

}