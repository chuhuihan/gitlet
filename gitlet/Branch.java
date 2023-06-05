package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class Branch implements Serializable {

    /** The blob's filename. */
    private Commit _HEAD;
    /** The blob's filename. */
    private String _name;
    /** The blob's filename. */
    private Boolean _inUse;
    /** The blob's filename. */
    private HashMap<String, String> trackedInBranch = new HashMap<>();
    /** The second parent, only happens if merged. */
    private File allBlobDirectory = new File(".gitlet/blobs/");

    public Branch(String name, Commit head, Boolean inUse) {
        _name = name;
        _HEAD = head;
        _inUse = inUse;
    }

    public void changeHead(Commit head) {
        _HEAD = head;
    }

    public String getName() {
        return _name;
    }

    public Commit getHead() {
        return _HEAD;
    }

    public boolean isInuse() {
        return _inUse;
    }

    public void changeInUse(boolean now) {
        _inUse = now;
    }

    public void addTracked(String name, String sha1) {
        if (!trackedInBranch.containsKey(name)
                && !trackedInBranch.containsValue(sha1)) {
            trackedInBranch.put(name, sha1);
        } else if (trackedInBranch.containsKey(name)
                && !trackedInBranch.get(name).equals(sha1)) {
            trackedInBranch.put(name, sha1);
        } else if (!trackedInBranch.containsKey(name)
                && trackedInBranch.containsValue(sha1)) {
            trackedInBranch.put(name, sha1);
        }
    }

    public HashMap<String, String> getTrackedInBranch() {
        return trackedInBranch;
    }

}
