
public class Gitlet {
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("Please enter a command");
            }
            String command = args[0];
            String[] tokens = new String[args.length - 1];
            System.arraycopy(args, 1, tokens, 0, tokens.length);
            Git g = Serialize.load(); //load gitlet if there is one
            if (command.equals("init")) {
                if (g != null) {
                    System.out.println("A gitlet version control system already 
                                        exists in the current directory.");
                } else {
                    g = new Git();
                }
            } else if (g == null) {
                System.out.println("You must initialize first");
            } else {
                switch (command) {                     
                    case "add":
                        g.add(tokens); 
                        break;
                    case "commit":
                        g.commit(tokens);
                        break;
                    case "rm":
                        g.removeFile(tokens);                       
                        break;
                    case "log":
                        g.log();
                        break;
                    case "global-log":
                        g.globalLog();
                        break;
                    case "find":
                        g.findIds(tokens);
                        break;
                    case "status":
                        g.getStatus();
                        break;
                    case "checkout":
                        g.checkout(tokens);
                        break;
                    case "branch":
                        g.createBranch(tokens);
                        break;
                    case "rm-branch":
                        g.removeBranch(tokens);
                        break;
                    case "reset":
                        g.reset(tokens);
                        break;
                    case "merge":
                        g.merge(tokens);
                        break;
                    case "rebase":
                        g.rebase(tokens, false);
                        break;
                    case "i-rebase":
                        g.rebase(tokens, true);
                        break;
                    default:
                        System.out.println("Invalid command.");  
                }
            }
            Serialize.serializeGit(g); //always serialize after a command
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
