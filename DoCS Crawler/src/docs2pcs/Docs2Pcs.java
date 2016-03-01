
package docs2pcs;


public class Docs2Pcs {
    public Docs2Pcs() {
        super();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        DocsFileOperation client = new DocsFileOperation();
        if (client.Initialize()) {
            client.start();
        }
        System.exit(0);
    }
}
