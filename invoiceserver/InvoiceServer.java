
package invoiceserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
/**
 *
 * @author Brad Minogue
 */
public class InvoiceServer {
    public static int port = 8855;
    private static final String[] CONTEXT_LIST = {"/"};
    private static final HttpHandler[] CONTEXT_HANDLER_LIST = {
        new IndexHandler()
    };
    public static void main(String[] args) {
        if(args.length > 0)
        {
            try{
                port = Integer.parseInt(args[0]);
                System.out.println("Using port: " + port);
            }
            catch(Exception e){
                System.err.println("Cannot use port: " + port);
                return;
            }
                
        }
        else
            System.out.println("Using default port: " + port);
        HttpServer webServer = null;
        try{
            webServer = HttpServer.create(
                    new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
        }
        catch(Exception e)
        {
            System.err.println("Failed in creating server");
            return;
        }
        try{
            for(int i = 0; i < CONTEXT_LIST.length; i++)
            {
                webServer.createContext(CONTEXT_LIST[i], CONTEXT_HANDLER_LIST[i]);
            }
            webServer.setExecutor(null);
            webServer.start();
        }
        catch(Exception e)
        {
            System.err.println("Failed in creating server");
            return;
        }
    }
}
