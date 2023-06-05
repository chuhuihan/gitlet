package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Date;

/***
 * This class stores the commit tree and does the logic for each commit.
 * @author Chuhui Han
 */

public class Commit implements Serializable {

    /** The blob's filename. */
    private String _message;
    /** The blob's filename. */
    private String _author;
    /** The blob's filename. */
    private String _time;
    /** The blob's filename. */
    private ArrayList<File> _files;
    /** The blob's filename. */
    private TreeMap<String, String> trackedFiles = new TreeMap<>();
    /** The blob's filename. */
    private TreeMap<String, String> trackedBlobs = new TreeMap<>();
    /** The blob's filename. */
    private Commit _parent;
    /** The blob's filename. */
    private String _parentSha1;
    /** The blob's filename. */
    private String _sha1;
    /** The second parent, only happens if merged. */
    private Commit _secondParent;


    public Commit(String message, Commit parent, Commit secondParent) {
        _message = message;
        _parent = parent;
        _secondParent = secondParent;

        if (parent == null) {
            _time = "Wed Dec 31 16:00:00 1969 -0800";
            _sha1 = Utils.sha1(_message + _time);
        } else {
            _parentSha1 = parent.getSha1();
            trackedFiles =
                    new TreeMap<>(parent.getTrackedFiles());
            _sha1 = Utils.sha1(_message + _time + _parentSha1);

            Date tempTime = new java.util.Date();
            SimpleDateFormat newDate =
                    new SimpleDateFormat("\"EEE MMM d HH:mm:ss yyyy Z\"");

            _time = newDate.format(tempTime).replaceAll("\"", "");
        }
    }

    public String getMessage() {
        return _message;
    }

    public void addTracked(String name, String sha1) {
        trackedFiles.put(name, sha1);
    }

    public void changeTracked(String name, String sha1) {
        trackedFiles.remove(name, trackedBlobs.get(name));
        trackedFiles.put(name, sha1);
    }

    public void removeTracked(String name, String sha1) {
        trackedFiles.remove(name, sha1);
    }

    public String getAuthor() {
        return _author;
    }

    public String getTime() {
        return _time;
    }

    public void setMessage(String m) {
        _message = m;
    }

    public void setTime(String t) {
        _time = t;
    }

    public ArrayList<File> getFiles() {
        return _files;
    }

    public TreeMap<String, String> getTrackedFiles() {
        return trackedFiles;
    }

    public String getSha1() {
        return _sha1;
    }

    public Commit getParent() {
        return _parent;
    }

    public Commit getSecondParent() {
        return _secondParent;
    }

}
