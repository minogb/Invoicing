package invoiceserver;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URL;
import java.sql.*;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.net.URLDecoder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;
/**
 *
 * @author Brad Minogue
 */
public class IndexHandler  implements HttpHandler{

    @Override
    public void handle(HttpExchange he) throws IOException {
        //Begin proccessing of incoming request
        String response = "Empty Body";
        try{
            //Need to parse stream to string to jsonobject
            StringBuilder out = new StringBuilder();
            BufferedReader  br = new BufferedReader (new InputStreamReader(he.getRequestBody()));
            String read= br.readLine();
            while(read != null){
                out.append(read);
                read = br.readLine();
            }
            //For debuging
            System.out.println(out.toString());
            JSONObject incomingRequest = new JSONObject(out.toString());
            //Check of validity
            if(!incomingRequest.has("action")){
                response = "Missing Action";
                response = add(incomingRequest);
            }
            else{
                //switch to our correct function
                response = incomingRequest.toString();
                switch(incomingRequest.getString("action")){
                    case "new":
                        response = add(incomingRequest);
                        break;
                    case "update":
                        response = update(incomingRequest);
                        break;
                    case "search":
                        response = searchContact(incomingRequest);
                        break;
                    case "getInvoice":
                        response = getInvoice(incomingRequest);
                        break;
                    case "getXmlInvoice":
                        response = getXmlInvoice(incomingRequest);
                        break;
                    case "addPreXml":
                        response = addExampleXmlInvoice();
                        break;
                    case "addXml":
                        response = addXmlInvoice(incomingRequest);
                        break;
                    default:
                        response = "Unkown action";
                        break;
                }
            }
        }
        catch(Exception e){
            System.out.println(e);
            System.out.println("FAILED at " + he.getRequestURI() + " FROM: " + he.getRemoteAddress());
        }
        finally{
            he.sendResponseHeaders(200, response.length());
            OutputStream oout = he.getResponseBody();
            oout.write(response.getBytes());
            oout.close();
        }
    }
    private String addExampleXmlInvoice() throws Exception{
        String retVal = "Something exploded";
        URL url = new URL("http://localhost/exampleInvoiceXml.xml");
        Scanner s = new Scanner(url.openStream());
        String value= "";
        while(s.hasNext()){
            value+=s.nextLine();
        }
        return addXml(value);
    }
    private String addXmlInvoice(JSONObject obj){
        String retVal = "Invalid";
        String value;
        try{
            retVal = addXml(URLDecoder.decode(obj.getString("data"),"UTF-8"));
        }
        catch(Exception e){
            retVal = "invalid xml syntax";
        }
        return retVal;
    }
    private String useLedesXml(Element root){
        JSONObject retVal = new JSONObject();
        
        JSONArray contacts = new JSONArray();
        
        NodeList nList = root.getElementsByTagName("lf_billing_contact");
        Element elem = (Element)nList.item(0);
        JSONObject current = new JSONObject();
        
        current.put("name", elem.getElementsByTagName("contact_fname").item(0).getTextContent() 
                + " " + elem.getElementsByTagName("contact_lname").item(0).getTextContent());
        current.put("email", elem.getElementsByTagName("contact_email").item(0).getTextContent());
        current.put("phone", elem.getElementsByTagName("contact_phone").item(0).getTextContent());
        
        nList = root.getElementsByTagName("lf_address");
        elem = (Element)nList.item(0);
        current.put("addresslineone", elem.getElementsByTagName("address_1").item(0).getTextContent());
        current.put("addresslinetwo","");
        current.put("city", elem.getElementsByTagName("address_1").item(0).getTextContent());
        current.put("state", elem.getElementsByTagName("country").item(0).getTextContent());
        current.put("zip", elem.getElementsByTagName("zip_postal_code").item(0).getTextContent());
        
        contacts.put(current);
        current = new JSONObject();
         
        nList = root.getElementsByTagName("lf_billing_contact");
        elem = (Element)nList.item(0);
        
        current.put("email", elem.getElementsByTagName("contact_email").item(0).getTextContent());
        current.put("phone", elem.getElementsByTagName("contact_phone").item(0).getTextContent());
        
        nList = root.getElementsByTagName("client");
        elem = (Element)nList.item(0);
        current.put("name", elem.getElementsByTagName("cl_name").item(0).getTextContent());
        
        nList = elem.getElementsByTagName("cl_address");
        elem = (Element)nList.item(0);
        current.put("addresslineone", elem.getElementsByTagName("address_1").item(0).getTextContent());
        current.put("addresslinetwo", "");
        current.put("state", elem.getElementsByTagName("state_province").item(0).getTextContent());
        current.put("city", elem.getElementsByTagName("city").item(0).getTextContent());
        current.put("zip", elem.getElementsByTagName("zip_postal_code").item(0).getTextContent());
        contacts.put(current);
        
        retVal.put("contacts", contacts);
        
        nList = root.getElementsByTagName("tax");
        String[] tax = new String[nList.getLength()];
        for(int i = 0; i < nList.getLength(); i++){
            elem = (Element)nList.item(i);
            tax[Integer.parseInt(elem.getElementsByTagName("tx_id").item(0).getTextContent())-1] = elem.getElementsByTagName("tax_rate_percent").item(0).getTextContent();
        }
        
        JSONArray items = new JSONArray();
        nList = root.getElementsByTagName("fee");
        for(int i = 0; i < nList.getLength(); i++){
            current = new JSONObject();
            elem = (Element)nList.item(i);
            current.put("name", elem.getElementsByTagName("tk_id").item(0).getTextContent());
            current.put("description", elem.getElementsByTagName("tk_level").item(0).getTextContent());
            current.put("price", elem.getElementsByTagName("rate").item(0).getTextContent());
            current.put("qnty", elem.getElementsByTagName("units").item(0).getTextContent());
            Element itm = (Element) elem.getElementsByTagName("tax_item_fee").item(0);
            current.put("tax", tax[Integer.parseInt(itm.getElementsByTagName("tx_id").item(0).getTextContent())-1]);
            items.put(current);
        }
        
        nList = root.getElementsByTagName("expense");
        for(int i = 0; i < nList.getLength(); i++){
            current = new JSONObject();
            elem = (Element)nList.item(i);
            current.put("name", elem.getElementsByTagName("expense_code").item(0).getTextContent());
            current.put("description", elem.getElementsByTagName("charge_desc").item(0).getTextContent());
            current.put("price", elem.getElementsByTagName("rate").item(0).getTextContent());
            current.put("qnty", elem.getElementsByTagName("units").item(0).getTextContent());
            Element itm = (Element) elem.getElementsByTagName("tax_item_expense").item(0);
            current.put("tax", tax[Integer.parseInt(itm.getElementsByTagName("tx_id").item(0).getTextContent())-1]);
            items.put(current);
        }
        
        retVal.put("items",items);
        
        return add(retVal);
    }
    private String addXml(String data) throws Exception{
            
        java.sql.Statement statement;
        MysqlDataSource datasrc = new MysqlDataSource();
        datasrc.setUser("user");
        datasrc.setServerName("localhost");
        datasrc.setPort(3306);
        datasrc.setDatabaseName("invoice");
        java.sql.Connection conn = datasrc.getConnection();
        java.util.Date d = new java.util.Date();
        statement = conn.createStatement();
        java.sql.Timestamp date = new java.sql.Timestamp(d.getTime());
        int id = getNewInv(statement, date);
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
        Document doc = dbBuilder.parse(new InputSource(new ByteArrayInputStream(data.getBytes())));
        doc.getDocumentElement().normalize();
        
        Element root = doc.getDocumentElement();
        if(((NodeList)root.getElementsByTagName("firm")).getLength() > 0){
            return useLedesXml(root);
        }
        NodeList nList = root.getElementsByTagName("customer");
        
        Node node;
        Element elem = (Element)nList.item(0);
        insertContact(elem.getElementsByTagName("name").item(0).getTextContent(),
                elem.getElementsByTagName("email").item(0).getTextContent(),
                elem.getElementsByTagName("phone").item(0).getTextContent(),
                elem.getElementsByTagName("line1").item(0).getTextContent(), 
                elem.getElementsByTagName("line2").item(0).getTextContent(),
                elem.getElementsByTagName("city").item(0).getTextContent(),
                elem.getElementsByTagName("state").item(0).getTextContent(),
                elem.getElementsByTagName("zipcode").item(0).getTextContent(), id, statement);
        
        nList = root.getElementsByTagName("job");
        elem = (Element)nList.item(0);
        insertContact(elem.getElementsByTagName("name").item(0).getTextContent(),
                elem.getElementsByTagName("email").item(0).getTextContent(),
                elem.getElementsByTagName("phone").item(0).getTextContent(),
                elem.getElementsByTagName("line1").item(0).getTextContent(), 
                elem.getElementsByTagName("line2").item(0).getTextContent(),
                elem.getElementsByTagName("city").item(0).getTextContent(),
                elem.getElementsByTagName("state").item(0).getTextContent(),
                elem.getElementsByTagName("zipcode").item(0).getTextContent(), id, statement);
        
        nList = root.getElementsByTagName("item");
        for(int i = 0; i < nList.getLength(); i++){
            node = nList.item(i);
            elem = (Element)node;
            insertItem(elem.getElementsByTagName("name").item(0).getTextContent(),
                    elem.getElementsByTagName("description").item(0).getTextContent(),
                    elem.getElementsByTagName("qnty").item(0).getTextContent(),
                    elem.getElementsByTagName("price").item(0).getTextContent(),
                    elem.getElementsByTagName("tax").item(0).getTextContent(),id,statement);
        }
        return "success";
    }
    private String getXmlInvoice(JSONObject obj) throws Exception{
        String retVal = "failed to load invoice";
        retVal = jsonToXml(getInvoice(obj));
        return retVal;
    }
    private String jsonToXml(String strObj){
        String retVal = "failed";
        JSONObject obj = new JSONObject(strObj);
 
        JSONArray contacts = new JSONArray(obj.get("contacts").toString());
        JSONObject customer = new JSONObject(contacts.get(0).toString());
        JSONObject job = new JSONObject(contacts.get(1).toString());

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("invoice");
            doc.appendChild(rootElement);
            
            {
                Element contact = doc.createElement("customer");
                rootElement.appendChild(contact);

                Element el = doc.createElement("name");
                el.appendChild(doc.createTextNode(customer.getString("name")));
                contact.appendChild(el);
                
                el = doc.createElement("email");
                el.appendChild(doc.createTextNode(customer.getString("email")));
                contact.appendChild(el);
                
                el = doc.createElement("phone");
                el.appendChild(doc.createTextNode(customer.getString("phone")));
                contact.appendChild(el);
                
                Element address = doc.createElement("address");
                contact.appendChild(address);
                
                el = doc.createElement("line1");
                el.appendChild(doc.createTextNode(customer.getString("addresslineone")));
                el = doc.createElement("line2");
                el.appendChild(doc.createTextNode(customer.getString("addresslinetwo")));
                el = doc.createElement("city");
                el.appendChild(doc.createTextNode(customer.getString("city")));
                el = doc.createElement("state");
                el.appendChild(doc.createTextNode(customer.getString("state")));
                el = doc.createElement("zipcode");
                el.appendChild(doc.createTextNode(customer.getString("zipcode")));
            }
            {
                Element contact = doc.createElement("job");
                rootElement.appendChild(contact);

                Element el = doc.createElement("name");
                el.appendChild(doc.createTextNode(job.getString("name")));
                contact.appendChild(el);
                
                el = doc.createElement("email");
                el.appendChild(doc.createTextNode(job.getString("email")));
                contact.appendChild(el);
                
                el = doc.createElement("phone");
                el.appendChild(doc.createTextNode(job.getString("phone")));
                contact.appendChild(el);
                
                Element address = doc.createElement("address");
                contact.appendChild(address);
                
                el = doc.createElement("line1");
                el.appendChild(doc.createTextNode(job.getString("addresslineone")));
                el = doc.createElement("line2");
                el.appendChild(doc.createTextNode(job.getString("addresslinetwo")));
                el = doc.createElement("city");
                el.appendChild(doc.createTextNode(job.getString("city")));
                el = doc.createElement("state");
                el.appendChild(doc.createTextNode(job.getString("state")));
                el = doc.createElement("zipcode");
                el.appendChild(doc.createTextNode(job.getString("zipcode")));
            }
            
            JSONArray items = new JSONArray(obj.get("items").toString());
            for(int i =0; i < items.length(); i++){
                JSONObject current = items.getJSONObject(i);
                
                Element item = doc.createElement("item");
                rootElement.appendChild(item);
                
                Element el = doc.createElement("name");
                el.appendChild(doc.createTextNode(current.getString("name")));
                item.appendChild(el);
                
                el = doc.createElement("description");
                el.appendChild(doc.createTextNode(current.getString("description")));
                item.appendChild(el);
                
                el = doc.createElement("qnty");
                el.appendChild(doc.createTextNode(current.getString("qnty")));
                item.appendChild(el);
                
                el = doc.createElement("price");
                el.appendChild(doc.createTextNode(current.getString("price")));
                item.appendChild(el);
                
                el = doc.createElement("tax");
                el.appendChild(doc.createTextNode(current.getString("tax")));
                item.appendChild(el);
            }
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            retVal = writer.getBuffer().toString().replaceAll("\n|\r", "");
        }
        catch(Exception e){
            retVal = "failed to load xml";
            System.out.println(e);
        }
        return retVal;
    }
    //pulls all information from the database about a given invoice id
    private String getInvoice(JSONObject obj) throws Exception{
        JSONObject retVal = new JSONObject();
        String id = obj.getString("id");
        JSONArray contacts = new JSONArray();
        
        java.sql.Statement statement;
        MysqlDataSource datasrc = new MysqlDataSource();
        datasrc.setUser("user");
        datasrc.setServerName("localhost");
        datasrc.setPort(3306);
        datasrc.setDatabaseName("invoice");
        java.sql.Connection conn = datasrc.getConnection();
        java.util.Date d = new java.util.Date();
        statement = conn.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM `contact` WHERE invoice = \"" + id + "\";");
        //get both contact information, first is always customer, second is job
        while(set.next()){
            JSONObject current = new JSONObject();
            current.put("name", set.getString("name"));
            current.put("email", set.getString("email"));
            current.put("phone", set.getString("phone"));
            current.put("addresslineone", set.getString("addresslineone"));
            current.put("addresslinetwo", set.getString("addresslinetwo"));
            current.put("city", set.getString("city"));
            current.put("state", set.getString("state"));
            current.put("zipcode", set.getString("zipcode"));
            contacts.put(current);
        }
        retVal.put("contacts", contacts);
        //get all items
        JSONArray items = new JSONArray();
        set = statement.executeQuery("SELECT * FROM `item` WHERE invoice = \"" + id + "\";");
        
        while(set.next()){
            JSONObject current = new JSONObject();
            current.put("name", set.getString("name"));
            current.put("description", set.getString("description"));
            current.put("qnty", set.getString("qnty"));
            current.put("price", set.getString("price"));
            current.put("tax", set.getString("tax"));
            items.put(current);
        }
        retVal.put("items",items);
        return retVal.toString();
    }
    //get invoice id based on contact information
    //returns a list as multiple contacts can have an invoice
    private String searchContact(JSONObject obj) throws Exception{
        String retVal = "No Results";
        String name = obj.getString("name");
        
        java.sql.Statement statement;
        MysqlDataSource datasrc = new MysqlDataSource();
        datasrc.setUser("user");
        datasrc.setServerName("localhost");
        datasrc.setPort(3306);
        datasrc.setDatabaseName("invoice");
        java.sql.Connection conn = datasrc.getConnection();
        statement = conn.createStatement();
        ResultSet set;
        
        set = statement.executeQuery("SELECT * FROM `contact` WHERE name = \"" + name + "\";");
        ArrayList<String> listOfInvoiceMatch = new ArrayList();
        //store the potential invoice id's in a more accessable java array list
        while(set.next()){
            String invoiceId =set.getString("invoice");
            if(!listOfInvoiceMatch.contains(invoiceId)){
                listOfInvoiceMatch.add(invoiceId);
            }
        }
        //add information for each potential invoice to the array
        JSONArray array = new JSONArray();
        for(int i = 0; i < listOfInvoiceMatch.size(); i++){
            JSONObject current = new JSONObject();
            set = statement.executeQuery("SELECT * FROM `invoice` WHERE id = \"" +
                    listOfInvoiceMatch.get(i) + "\";");
            while(set.next()){
                current.put("date", set.getString("date"));
                current.put("id", set.getString("id"));
            }
            array.put(current);
        }
        retVal = new JSONObject().put("results", array).toString();
        return retVal;
    }
    //update an invoice based on invoice id and incoming information
    private String update(JSONObject obj){
        String retVal = "";
        return retVal;
    }
    //Add a new invoice to the database
    private String add(JSONObject obj){
        String retVal = "";
        try{
            JSONArray contacts = new JSONArray(obj.get("contacts").toString());
            JSONObject customer = new JSONObject(contacts.get(0).toString());
            JSONObject job = new JSONObject(contacts.get(1).toString());
            
            ArrayList<JSONObject> itemList = new ArrayList();
            JSONArray items = new JSONArray(obj.get("items").toString());
            for(int i =0; i < items.length(); i++){
                itemList.add(items.getJSONObject(i));
            }
            
            java.sql.Statement statement;
            MysqlDataSource datasrc = new MysqlDataSource();
            datasrc.setUser("user");
            datasrc.setServerName("localhost");
            datasrc.setPort(3306);
            datasrc.setDatabaseName("invoice");
            java.sql.Connection conn = datasrc.getConnection();
            java.util.Date d = new java.util.Date();
            statement = conn.createStatement();
            java.sql.Timestamp date = new java.sql.Timestamp(d.getTime());
            int id = getNewInv(statement, date);
            insertContact(customer,id,statement);
            insertContact(job,id,statement);
            for(JSONObject itemInList : itemList){
                insertItem(itemInList, id, statement);
            }
        }
        catch(Exception e){
            retVal = "Could not save to server!";
            System.out.println(e);
            System.out.println(obj.toString());
        }
        finally{
            return retVal;
        }
    }
    int getNewInv( java.sql.Statement statement, java.sql.Timestamp date) throws Exception{
        int i = 0;
        while(true){
            try{
            statement.execute("INSERT INTO `invoice` " + 
                "(`id`, `date`) VALUES ('" + i +"', '"  + date + "');");
            break;
            }
            catch(Exception e){
                if(e.getMessage().contains("Duplicate entry")){
                    i++;
                    continue;
                }
                System.out.println(e);
                throw new Exception(e);
            }
        }
        return i;
    }
    //inserrt in a contact in to the database
    private void insertContact(JSONObject obj, int id, java.sql.Statement statement) throws Exception{
        insertContact(obj.getString("name"), obj.getString("email"), 
                obj.getString("phone"), obj.getString("addresslineone"),
                obj.getString("addresslinetwo"), obj.getString("city"), 
                obj.getString("state"), obj.getString("zip"), id, statement);
    }
    private void insertContact(String name, String email, String phone, 
            String aLineOne, String aLineTwo, String city, String state, 
            String zip, int id, java.sql.Statement statement) throws Exception{
        
        statement.execute("INSERT INTO `contact` (`name`, `email`,`phone`,`addresslineone`,`addresslinetwo`,`city`,`state`,`zipcode`,`invoice`)"
                +"VALUES ('" + name + "','" + email + "','" + phone +"','" +
                aLineOne + "','" + aLineTwo + "','" + city
                + "','" + state + "','" + zip+ "','" + id + "');");
    }
    //insert an item into the database
    private void insertItem(JSONObject obj, int id, java.sql.Statement statement) throws Exception{
        
        statement.execute("INSERT INTO `item`(`name`,`description`,`qnty`,`price`,`tax`,`invoice`)" +
                "VALUES ('" + obj.getString("name") + "','" + obj.getString("description") +
                "','" + obj.getString("qnty")+ "','" + obj.getString("price") + "','" +
                obj.getString("tax")+ "','" + id+ "');");
    }
    private void insertItem(String name, String desc, String qnty, String price,
                    String tax, int id, java.sql.Statement statement) throws Exception{
        
        statement.execute("INSERT INTO `item`(`name`,`description`,`qnty`,`price`,`tax`,`invoice`)" +
            "VALUES ('" + name + "','" + desc + "','" + qnty + "','" + price
            + "','" +  tax + "','" + id+ "');");
    }
}