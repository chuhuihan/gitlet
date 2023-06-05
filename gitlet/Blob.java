package gitlet;

import java.io.File;
import java.io.Serializable;

/** handles the contents of the files? Not sure if needed
 * @author Chuhui Han
 */

public class Blob implements Serializable {

    /** The blob's filename. */
    private String _fileName;
    /** The blob's filename. */
    private String _fileContentsString;
    /** The blob's filename. */
    private byte[] _fileContents;
    /** The blob's filename. */
    private String _sha1;

    public Blob(File file) {
        _fileName = file.getName();
        _fileContentsString = Utils.readContentsAsString(file);
        _fileContents = Utils.readContents(file);
        _sha1 = Utils.sha1(_fileName + _fileContentsString);
    }

    public String getName() {
        return _fileName;
    }

    public byte[] getFileContents() {
        return _fileContents;
    }

    public String getFileContentsString() {
        return _fileContentsString;
    }

    public String getSha1() {
        return _sha1;
    }

}
