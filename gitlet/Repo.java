package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/***
 * This class contains everything that a git repo needs to run, such
 * as a staging area, a commit tree, and is where the program pulls
 * files from.
 * @author Chuhui Han
 */

public class Repo implements Serializable {

    /** a dictionary of Sha1 Strings and fileContents String
     *  that make up name and blob. */
    private Hashtable<String, Blob> nameBlob = new Hashtable<String, Blob>();

    /** HashTable of commits. */
    private Hashtable<String, Commit> _commits = new Hashtable<>();

    /** Linked List of commit Sha1 names. */
    private LinkedList<String> sha1Commits = new LinkedList<>();

    /** arrayList of all branches, or "pointers" to certain head commits. */
    private ArrayList<Branch> _branches = new ArrayList<>();

    /** the head of the commit tree, changes according to commits. */
    private Commit head;

    /** Master branch. */
    private Branch master;

    /** the current working directory. */
    private File cwd = new File(System.getProperty("user.dir"));

    /** the directory where all of .gitlet files are stored. */
    private File f = new File(".gitlet/");

    /** the directory where all of .gitlet files are stored. */
    private File stagingArea = new File(".gitlet/stagingArea/");

    /** the directory where all commit files are stored. */
    private File allCommits = new File(".gitlet/allCommits/");

    /** the directory where all blob files are stored. */
    private File blobs = new File(".gitlet/blobs/");

    /** the directory where all remove files are stored. */
    private File removeArea = new File(".gitlet/removeArea/");

    /** the directory where head file is stored. */
    private File commitHead = new File(".gitlet/HEAD.txt");

    public Repo() {
        f.mkdir();
    }

    /** initializes the repository in the working directory
     * creates a new repo object. */
    public void init() throws IOException {

        stagingArea.mkdir();
        allCommits.mkdir();
        removeArea.mkdir();
        blobs.mkdir();

        Commit initialCommit = new Commit("initial commit", null, null);

        sha1Commits.add(initialCommit.getSha1());

        _commits.put(initialCommit.getSha1(), initialCommit);

        master = new Branch("master", initialCommit, true);
        _branches.add(master);

        serialize(".gitlet/allCommits/"
                + initialCommit.getSha1() + ".txt", initialCommit);
        serialize(".gitlet/HEAD.txt", getInUseHead());
        serialize(".gitlet/master.txt", master);
    }

    /** adds a file to the staging area.
     * @param name is file name. */
    public void add(String name) throws IOException {
        File cwdFile = new File(cwd, name);

        File removeFile = new File(removeArea, name);

        if (!cwdFile.exists()) {
            System.out.println("File does not exist.");
        } else {
            File toStage = new File(stagingArea, name);
            TreeMap<String, String> headTracked
                    = getInUseHead().getTrackedFiles();
            Blob testBlob = new Blob(cwdFile);
            String fileContents = Utils.readContentsAsString(cwdFile);
            String sha1 = Utils.sha1(name + fileContents);

            if (removeFile.exists() && (removeFile.getName()).equals(name)) {
                removeFile.delete();
            }

            if (headTracked.containsValue(sha1)) {
                toStage.delete();
            } else {
                if (!toStage.exists()) {
                    toStage.createNewFile();
                }
                Utils.writeContents(toStage, fileContents);
            }
        }
    }

    /** removes a file from the staging area,
     * adds removed file to list of removed files.
     * @param name is file name. */
    public void rm(String name) throws IOException {
        File cwdFile = new File(cwd, name);
        File stagingFile = new File(stagingArea,  name);
        File[] stagingArray = stagingArea.listFiles();
        Blob rmReference = null;
        if (cwdFile.exists()) {
            rmReference = new Blob(cwdFile);
        }
        String status = "can go";

        for (File file: stagingArray) {
            Blob stageReference = new Blob(file);
            if (rmReference != null && stageReference.getSha1().equals
                    (rmReference.getSha1())) {
                file.delete();
                status = "stop";
            }
        }

        if (status.equals("can go")
                && getInUseHead().getTrackedFiles().size() != 0) {
            if (Arrays.asList(stagingArray).contains(cwdFile)
                    && !getInUseHead().getTrackedFiles().containsKey(name)) {
                stagingFile.delete();
            } else if (getInUseHead().getTrackedFiles().containsKey(name)) {
                File blobFile = new File(blobs,
                        getInUseHead().getTrackedFiles().get(name) + ".txt");
                Blob reference = Utils.readObject(blobFile, Blob.class);
                if (cwdFile.exists()) {
                    File removeFile = new File(removeArea, name);
                    removeFile.createNewFile();
                    Utils.writeContents(removeFile,
                            reference.getFileContentsString());
                    Utils.restrictedDelete(cwdFile);
                } else {
                    File removeFile = new File(removeArea, name);
                    removeFile.createNewFile();
                    Utils.writeContents(removeFile,
                            reference.getFileContentsString());
                }
            } else if (getBranchInUse()
                    .getTrackedInBranch().containsKey(name)) {
                File blobFile = new File(blobs, getBranchInUse()
                        .getTrackedInBranch().get(name) + ".txt");
                Blob reference = Utils.readObject(blobFile, Blob.class);
                File removeFile = new File(removeArea, name);
                removeFile.createNewFile();
                Utils.writeContents(removeFile,
                        reference.getFileContentsString());
            } else {
                System.out.println("No reason to remove the file.");
            }
        } else {
            if (Arrays.asList(stagingArray).contains(stagingFile)) {
                stagingFile.delete();
            } else {
                System.out.println("No reason to remove the file.");
            }
        }
    }

    /** Prints the status of the repo at this point in time. */
    public void status() {
        System.out.println("=== Branches ===");
        for (Branch branch: _branches) {
            if (branch.isInuse()) {
                System.out.println("*" + branch.getName());
            } else {
                System.out.println(branch.getName());
            }
        }

        System.out.println("\n=== Staged Files ===");
        for (File file: stagingArea.listFiles()) {
            System.out.println(file.getName());
        }

        System.out.println("\n=== Removed Files ===");
        for (File file: removeArea.listFiles()) {
            System.out.println(file.getName());
        }

        System.out.println("\n=== Modifications Not Staged For Commit ===");

        System.out.println("\n=== Untracked Files ===");

    }

    /** adds dictionary of name -> blob to commit tree.
     * @param m is the commit message.
     * @param secondParent is the second parent */
    public void commitStaged(String m, Commit secondParent) throws IOException {

        Commit newCommit = new Commit(m, getInUseHead(), secondParent);
        File commitFile = new File(allCommits, newCommit.getSha1() + ".txt");
        File[] stageList = stagingArea.listFiles();
        File[] removeList = removeArea.listFiles();
        if (m == null || m.equals("")) {
            System.out.println("Please enter a commit message.");
        } else if (stageList.length == 0 && removeList.length == 0) {
            System.out.println("No changes added to the commit.");
        } else {
            for (File file : stageList) {
                Blob toStage = new Blob(file);
                File blobFile = new File(blobs, toStage.getSha1() + ".txt");
                nameBlob.put(toStage.getSha1(), toStage);
                if (getInUseHead().getTrackedFiles().containsKey(file.getName())
                    && !getInUseHead().getTrackedFiles().get
                        (file.getName()).equals(toStage.getSha1())) {
                    newCommit.changeTracked(file.getName(), toStage.getSha1());
                } else {
                    newCommit.addTracked(file.getName(), toStage.getSha1());
                }

                getBranchInUse().addTracked(file.getName(), toStage.getSha1());

                blobFile.createNewFile();
                Utils.writeObject(blobFile, toStage);

                file.delete();
            }

            for (File file: removeList) {
                Blob toRemove = new Blob(file);
                if (getInUseHead().getTrackedFiles().containsKey(file.getName())
                        && getInUseHead().getTrackedFiles().get
                        (file.getName()).equals(toRemove.getSha1())) {
                    newCommit.removeTracked(file.getName(), toRemove.getSha1());
                }
                file.delete();
            }
        }

        commitFile.createNewFile();
        Utils.writeObject(commitFile, newCommit);
        _commits.put(newCommit.getSha1(), newCommit);
        sha1Commits.add(newCommit.getSha1());
        Utils.writeObject(commitHead, getInUseHead());

        getBranchInUse().changeHead(newCommit);
        head = getInUseHead();
    }

    public void log() {

        Commit current = getInUseHead();
        int counter = 0;
        while (current != null) {
            if (counter == 0) {
                System.out.println("===");
            } else {
                System.out.println("\n===");
            }
            System.out.println("commit " + current.getSha1());
            if (current.getSecondParent() == null) {
                System.out.println("Date: " + current.getTime());
                System.out.println(current.getMessage());
                current = current.getParent();
            } else {
                System.out.println("Merge: "
                        + current.getParent().getSha1().substring(0, 7) + " "
                        + current.getSecondParent().getSha1().substring(0, 7));
                System.out.println("Date: " + current.getTime());
                System.out.println(current.getMessage());
                current = current.getParent();
            }

            counter++;
        }
    }

    public void globalLog() {
        int counter = 0;
        Commit current;
        for (String name: Utils.plainFilenamesIn(allCommits)) {
            current = _commits.get(name.replace(".txt", ""));
            if (counter == 0) {
                System.out.println("===");
            } else {
                System.out.println("\n===");
            }
            System.out.println("commit " + current.getSha1());
            System.out.println("Date: " + current.getTime());
            System.out.println(current.getMessage());
            counter++;
        }
    }

    public void find(String m) {
        Commit current;
        boolean printed = false;
        for (String name: Utils.plainFilenamesIn(allCommits)) {
            current = _commits.get(name.replace(".txt", ""));
            if (current.getMessage().equals(m)) {
                System.out.println(current.getSha1());
                printed = true;
            }
        }
        if (!printed) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void checkout(String name) throws IOException {
        Commit current = getInUseHead();
        File cwdFile = new File(cwd, name);
        if (current.getTrackedFiles().containsKey(name)) {
            String blobSha1 = current.getTrackedFiles().get(name);
            File blobFile = new File(blobs, blobSha1 + ".txt");
            Blob newBlob = Utils.readObject(blobFile, Blob.class);
            if (cwdFile.exists()) {
                Utils.writeContents(cwdFile, newBlob.getFileContentsString());
            } else {
                cwdFile.createNewFile();
                Utils.writeContents(cwdFile, newBlob.getFileContentsString());
            }
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public void checkout(String commitID, String name) throws IOException {
        if (getRegularID(commitID) != null) {
            Commit current = _commits.get(getRegularID(commitID));
            File cwdFile = new File(cwd, name);
            if (current.getTrackedFiles().containsKey(name)) {
                String blobSha1 = current.getTrackedFiles().get(name);
                File blobFile = new File(blobs, blobSha1 + ".txt");
                Blob newBlob = Utils.readObject(blobFile, Blob.class);
                if (cwdFile.exists()) {
                    Utils.writeContents(cwdFile,
                            newBlob.getFileContentsString());
                } else {
                    cwdFile.createNewFile();
                    Utils.writeContents(cwdFile,
                            newBlob.getFileContentsString());
                }
            } else {
                System.out.println("File does not exist in that commit.");
            }
        } else {
            System.out.println("No commit with that id exists.");
        }

    }

    public void checkoutBranch(String branchName) throws IOException {
        File[] cwdFiles = cwd.listFiles();
        String status = "";

        for (File file: cwdFiles) {
            if (!file.isDirectory()) {
                Blob testBlob = new Blob(file);
                if (!getInUseHead().getTrackedFiles()
                        .containsValue(testBlob.getSha1())) {
                    status = "untracked file in the way";
                }
            }
        }

        Branch toCheckout = null;
        for (Branch branch: _branches) {
            if (branch.getName().equals(branchName)) {
                toCheckout = branch;
            }
        }

        if (toCheckout == null) {
            System.out.println("No such branch exists.");
        } else if (toCheckout.isInuse()) {
            System.out.println("No need to checkout the current branch.");
        } else if (status.equals("untracked file in the way")) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first");
        } else {
            Commit branchHead = toCheckout.getHead();
            overwrite(branchHead.getTrackedFiles());
            getBranchInUse().changeInUse(false);
            toCheckout.changeInUse(true);
        }
    }

    public void overwrite(TreeMap<String, String> tracked) throws IOException {
        File[] cwdFiles = cwd.listFiles();
        if (tracked.size() == 0) {
            for (File file: cwdFiles) {
                file.delete();
            }
        } else {
            for (File file: cwdFiles) {
                file.delete();
            }

            for (Map.Entry<String, String> entry: tracked.entrySet()) {
                String trackedName = entry.getKey();
                String trackedSha1 = entry.getValue();
                Blob trackedBlob = nameBlob.get(trackedSha1);
                File newFile = new File(cwd, trackedName);
                newFile.createNewFile();
                Utils.writeContents(newFile,
                        trackedBlob.getFileContentsString());

            }
        }
    }

    public void reset(String commitID) throws IOException {

        File[] cwdFiles = cwd.listFiles();
        String status = "";
        File[] staged = stagingArea.listFiles();

        ArrayList<String> allTracked = trackedAllBranches();
        for (File file: staged) {
            Blob newBlob = new Blob(file);
            allTracked.add(newBlob.getSha1());
        }

        for (File file: cwdFiles) {
            if (!file.isDirectory()) {
                Blob newBlob = new Blob(file);
                if (!allTracked.contains(newBlob.getSha1())) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    break;
                }
            }
        }

        if (getRegularID(commitID) != null) {
            Commit toReset = _commits.get(getRegularID(commitID));
            overwrite(toReset.getTrackedFiles());
            getBranchInUse().changeHead(toReset);

            for (File file: staged) {
                file.delete();
            }
        } else {
            System.out.println("No commit with that id exists.");
        }

    }

    public void merge(String branchName) throws IOException {
        String error = errorCases(branchName);
        if (error.equals("untracked file in the way")) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        } else if (error.equals("uncommitted")) {
            System.out.println("You have uncommitted changes.");
        } else if (error.equals("DNE")) {
            System.out.println("A branch with that name does not exist.");
        } else if (error.equals("cannot merge itself")) {
            System.out.println("Cannot merge a branch with itself.");
        } else {
            Branch toMerge = null;
            for (Branch branch : _branches) {
                if (branch.getName().equals(branchName)) {
                    toMerge = branch;
                }
            }
            Commit splitPoint = getSplitPoint(toMerge);
            specialMerge(splitPoint, toMerge);
            regularMerge(splitPoint, toMerge);
        }
    }

    public void regularMerge(Commit split, Branch toMerge) throws IOException {
        TreeMap<String, String> headFiles = getInUseHead().getTrackedFiles();
        TreeMap<String, String> giveFiles = toMerge.getHead().getTrackedFiles();
        TreeMap<String, String> splitFiles = split.getTrackedFiles();
        TreeMap<String, String> allUniqueFiles = new TreeMap<>();
        allUniqueFiles.putAll(giveFiles);
        allUniqueFiles.putAll(headFiles);
        allUniqueFiles.putAll(splitFiles);
        for (Map.Entry<String, String> fileRun : allUniqueFiles.entrySet()) {
            String path = fileRun.getKey();
            String givenC = giveFiles.get(path);
            String headC = headFiles.get(path);
            String splitC = splitFiles.get(path);
            if (givenC == null) {
                givenC = "deleted";
            }
            if (splitC == null) {
                splitC = "deleted";
            }
            if (headC == null) {
                headC = "deleted";
            }
            if (splitC.equals("deleted") && headC.equals("deleted")
                    && !givenC.equals("deleted")) {
                Blob newBlob = Utils.readObject(new File(blobs + "/"
                        + givenC + ".txt"), Blob.class);
                File file = new File(path);
                Utils.writeContents(file, newBlob.getFileContentsString());
                add(file.getName());
            } else if (headC.equals(splitC) && givenC.equals("deleted")) {
                File file = new File(path);
                rm(file.getName());
            } else if (!givenC.equals(splitC) && headC.equals(splitC)) {
                Blob newBlob = Utils.readObject(new File(blobs + "/"
                        + givenC + ".txt"), Blob.class);
                File file = new File(path);
                Utils.writeContents(file, newBlob.getFileContentsString());
                add(file.getName());
            } else if (!givenC.equals(splitC) && !headC.equals(splitC)) {
                if (!givenC.equals(headC)) {
                    Blob givenBlob = null, headBlob = null;
                    if (!givenC.equals("deleted")) {
                        givenBlob = Utils.readObject(new File(blobs
                                + "/" + givenC + ".txt"), Blob.class);
                    }
                    if (!headC.equals("deleted")) {
                        headBlob = Utils.readObject(new File(blobs
                                + "/" + headC + ".txt"), Blob.class);
                    }
                    File file = new File(path);
                    conflictFile(path, givenBlob, headBlob);
                    add(file.getName());
                    System.out.println("Encountered a merge conflict.");
                }
            }
        }
        commitStaged("Merged " + toMerge.getName() + " into "
                        + getBranchInUse().getName() + ".", toMerge.getHead());
    }

    /** Creates a confict file where its content follows conflicted format.
     * @param filePath Path string.
     * @param targetBlob Target string content.
     * @param headBlob Head branch string content.
     */
    public static void conflictFile(String filePath,
                                    Blob targetBlob,
                                    Blob headBlob) {
        File conflictedFile = new File(filePath);
        String conflictContent = "<<<<<<< HEAD\n";
        if (headBlob != null) {
            conflictContent += headBlob.getFileContentsString();
        }
        conflictContent += "=======\n";
        if (targetBlob != null) {
            conflictContent += targetBlob.getFileContentsString();
        }
        conflictContent += ">>>>>>>\n";
        Utils.writeContents(conflictedFile, conflictContent);
    }

    private void specialMerge(Commit splitPoint, Branch branch)
            throws IOException {
        if (splitPoint.equals(getInUseHead())) {
            checkoutBranch(branch.getName());
            System.out.println("Current branch fast-forwarded");
            System.exit(0);
        }
        if (splitPoint.equals(branch.getHead())) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            System.exit(0);
        }
    }

    public ArrayList<Commit> getCommitsBranch(Branch branch) {
        ArrayList<Commit> newList = new ArrayList<>();
        Commit temp = branch.getHead();
        while (temp != null) {
            newList.add(temp);
            temp = temp.getParent();
        }
        return newList;
    }

    public Commit getSplitPoint(Branch branch) {
        Commit currentHead = getInUseHead();
        Commit branchHead = branch.getHead();

        Graph allCommitGraph = new Graph(sha1Commits);

        ArrayList<Commit> allSplitPoints = new ArrayList<>();

        allCommitGraph.breathFirstSearch(currentHead.getSha1(), "blue");
        allCommitGraph.breathFirstSearch(branchHead.getSha1(), "red");

        for (Node node: allCommitGraph.getAllNodes()) {
            if (node.getColor().equals("red")) {
                ArrayList<Node> parents = node.getParents();
                for (Node parentNode: parents) {
                    parentNode.setCount(1);
                }
            }
        }

        for (Node node: allCommitGraph.getAllNodes()) {
            if (node.getColor().equals("red") && node.getCount() == 0) {
                allSplitPoints.add(_commits.get(node.getLabel()));
            }
        }

        return allSplitPoints.get(0);
    }

    public String errorCases(String branchName) {
        File[] cwdFiles = cwd.listFiles();
        File[] stageArray = stagingArea.listFiles();
        File[] removeArray = removeArea.listFiles();
        String status = "PASSES";

        ArrayList<String> allTracked = trackedAllBranches();

        for (File file: stageArray) {
            Blob newBlob = new Blob(file);
            allTracked.add(newBlob.getSha1());
        }

        for (File file: cwdFiles) {
            if (!file.isDirectory()) {
                Blob newBlob = new Blob(file);
                if (!allTracked.contains(newBlob.getSha1())) {
                    status = "untracked file in the way";
                }
            }
        }

        if (status.equals("untracked file in the way")) {
            return status;
        } else if (stageArray.length != 0 || removeArray.length != 0) {
            status = "uncommitted";
        } else {
            Branch toMerge = null;
            for (Branch branch : _branches) {
                if (branch.getName().equals(branchName)) {
                    toMerge = branch;
                }
            }

            if (toMerge == null) {
                status = "DNE";
            } else if (toMerge.equals(getBranchInUse())) {
                status = "cannot merge itself";
            }
        }
        return status;
    }

    public void createBranch(String name) {
        for (Branch branch : _branches) {
            if (branch.getName().equals(name)) {
                System.out.println("A branch with that name already exists.");
                break;
            }
        }
        Branch newBranch = new Branch(name, getInUseHead(), false);
        _branches.add(newBranch);
    }

    public void removeBranch(String name) {
        String status = "noName";
        Branch toRemove = null;
        for (Branch branch : _branches) {
            if (branch.getName().equals(name) && branch.isInuse()) {
                System.out.println("Cannot remove the current branch.");
                status = "current";
            } else if (branch.getName().equals(name) && !branch.isInuse()) {
                toRemove = branch;
                status = "removed";
            }
        }
        if (status.equals("removed")) {
            _branches.remove(toRemove);
        }
        if (status.equals("noName")) {
            System.out.println("A branch with that name does not exist.");
        }
    }

    public Branch getBranchInUse() {
        for (Branch branch: _branches) {
            if (branch.isInuse()) {
                return branch;
            }
        }
        return null;
    }

    public Commit getInUseHead() {
        return getBranchInUse().getHead();
    }

    public ArrayList<String> trackedAllBranches() {
        ArrayList<String> newList = new ArrayList<>();
        for (Branch branch: _branches) {
            for (Map.Entry<String, String> set
                    :branch.getTrackedInBranch().entrySet()) {
                newList.add(set.getValue());
            }
        }
        return newList;
    }

    /** @param uid is the abbreviated id
     *  @return the original commit sha1 or null */
    public String getRegularID(String uid) {
        Set<String> setOfKeys = _commits.keySet();
        for (String key: setOfKeys) {
            if (key.contains(uid)) {
                return key;
            }
        }
        return null;
    }

    public void serialize(String filePath, Serializable toSerialize)
            throws IOException {
        File file = new File(filePath);

        file.createNewFile();

        Utils.writeObject(file, toSerialize);
    }
}
