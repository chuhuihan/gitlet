package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Chuhui Han
 */
public class Main {
    /** a new repository. */
    private static Repo repository = new Repo();
    /** a new repository file in the directory. */
    private static File repoFile = new File(".gitlet/repo.txt");

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        loadRepo();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (args[0].equals("init")) {
            if (repoFile.exists()) {
                System.out.println("A Gitlet version-control system already "
                        + "exists in the current directory.");
            } else {
                repoFile.createNewFile();
                repository.init();
                saveRepo();
            }
        } else if (!repoFile.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
        } else {
            if (args[0].equals("add")) {
                if (args.length >= 2) {
                    repository.add(args[1]);
                } else {
                    System.out.println("Please enter a file name");
                }
            } else if (args[0].equals("commit")) {
                if (args.length >= 2) {
                    repository.commitStaged(args[1], null);
                }
            } else if (args[0].equals("log")) {
                repository.log();
            } else if (args[0].equals("global-log")) {
                repository.globalLog();
            } else if (args[0].equals("find")) {
                repository.find(args[1]);
            } else if (args[0].equals("checkout")) {
                if (args[1].equals("--")) {
                    repository.checkout(args[2]);
                } else if (args.length == 2) {
                    repository.checkoutBranch(args[1]);
                } else if (args[2].equals("--")) {
                    repository.checkout(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                }
            } else if (args[0].equals("reset")) {
                repository.reset(args[1]);
            } else if (args[0].equals("status")) {
                repository.status();
            } else if (args[0].equals("rm")) {
                repository.rm(args[1]);
            } else if (args[0].equals("branch")) {
                repository.createBranch(args[1]);
            } else if (args[0].equals("rm-branch")) {
                repository.removeBranch(args[1]);
            } else if (args[0].equals("merge")) {
                repository.merge(args[1]);
            } else {
                System.out.println("No command with that name exists.");
            }
            saveRepo();
        }
    }

    public Repo getRepository() {
        return repository;
    }

    public File getRepoFile() {
        return repoFile;
    }

    public static void saveRepo() {
        Utils.writeObject(repoFile, repository);
    }

    public static void loadRepo() {
        if (repoFile.exists()) {
            repository = Utils.readObject(repoFile, Repo.class);
        }
    }

}
