//variables:
var customer = {
  name:"",
  addLineOne:"",
  addLineTwo:"",
  addCity:"",
  addState:"",
  addZip:"",
  email:"",
  phone:""
};
var job = {
  name:"",
  addLineOne:"",
  addLineTwo:"",
  addCity:"",
  addState:"",
  addZip:"",
  email:"",
  phone:""
};
var numItems = 1;
var Item = function (name,description,qnty,price,tax){
    this.name = name;
    this.description = description;
    this.qnty = qnty;
    this.price = price;
    this.tax = tax;
    console.log("item made");
}
var items =[];
//functions:
//add an additional item input for webpage
function newItem(){
    numItems++;
    document.getElementById("items").innerHTML +="<div id =\"" + (numItems-1) + "item\"style=\"float:left; margin:1%; background-color:graytext; width: 175px; height:200px;\">"+
                    "Name<br>"+
                    "<input type=\"text\" id=\"" + (numItems-1) + "itemName\"><br>"+
                    "Description<br>"+
                    "<input type=\"text\" id=\"" + (numItems-1) + "itemDescription\"><br>"+
                    "Quantity<br>"+
                    "<input type=\"number\" id=\"" + (numItems-1) + "itemQuantity\" value=\"0\"><br>"+
                    "Price<br>"+
                    "<input type=\"number\" id=\"" + (numItems-1) + "itemPrice\" value=\"0\"><br>"+
                    "Tax<br>"+
                    "<input type=\"number\" id=\"" + (numItems-1) + "itemTax\" value=\"0\"><br>"+
                "</div>";
}
//remove last added item slot
function removeItem(){
    if(numItems > 1)
    {
        var element = document.getElementById("" + --numItems + "item");
        element.parentNode.removeChild(element);
    }
}
//add item to the items array
function addItem(name,description,qnty,price,tax){
    items.push(new item(name,description,qnty,price,tax));
}
//enable/disable use of job information
function toggleUseJob(){
    var inv = document.invoice;
    inv.jobName.disabled=!inv.jobName.disabled;
    inv.jAddLineOne.disabled= !inv.jAddLineOne.disabled;
    inv.jAddLineTwo.disabled= !inv.jAddLineTwo.disabled;
    inv.jAddCity.disabled= !inv.jAddCity.disabled;
    inv.jAddState.disabled= !inv.jAddState.disabled;
    inv.jAddZip.disabled= !inv.jAddZip.disabled;
    inv.jobPhone.disabled= !inv.jobPhone.disabled;
    inv.jobEmail.disabled= !inv.jobEmail.disabled;
}
//sends all information of invoice to server for save and creates a printable window
function submitForm(){
    var inv = document.invoice;
    if(inv.customerName.value.length < 1){
        alert("missing customer name");
        return;
    }
    if(inv.cAddLineOne.value.length < 1){
        alert("missing customer address");
        return;
    }
    if(inv.cAddCity.value.length < 1){
        alert("missing customer city");
        return;
    }
    if(inv.cAddState.value.length < 1){
        alert("missing customer state");
        return;
    }
    if(inv.cAddZip.value.length < 1){
        alert("missing customer city");
        return;
    }
    customer.name = inv.customerName.value;
    customer.email = inv.customerEmail.value;
    customer.phone = inv.customerPhone.value;
    customer.addCity = inv.cAddCity.value;
    customer.addLineOne = inv.cAddLineOne.value;
    customer.addLineTwo = inv.cAddLineTwo.value;
    customer.addState = inv.cAddState.value;
    customer.addZip = inv.cAddZip.value;
    if(inv.jobName.disabled){
        job.name = customer.name;
        job.email = customer.email;
        job.phone = customer.phone;
        job.addCity = customer.addCity;
        job.addLineOne = customer.addLineOne;
        job.addLineTwo = customer.addLineTwo;
        job.addState = customer.addState;
        job.addZip = customer.addZip;
    }
    else{
        if(inv.jobName.value.length < 1){
            alert("missing job name");
            return;
        }
        if(inv.jAddLineOne.value.length < 1){
            alert("missing job address");
            return;
        }
        if(inv.jAddCity.value.length < 1){
            alert("missing job city");
            return;
        }
        if(inv.jAddState.value.length < 1){
            alert("missing job state");
            return;
        }
        if(inv.jAddZip.value.length < 1){
            alert("missing job city");
            return;
        } 
        job.name = inv.jobName.value;
        job.email = inv.jobEmail.value;
        job.phone = inv.jobPhone.value;
        job.addCity = inv.jAddCity.value;
        job.addLineOne = inv.jAddLineOne.value;
        job.addLineTwo = inv.jAddLineTwo.value;
        job.addState = inv.jAddState.value;
        job.addZip = inv.jAddZip.value;
    }
    for(var i =0; i < numItems; i++){
        if(document.getElementById(i+"itemQuantity").value < 1){
            alert("missing qnty of an item");
            return;
        }
        if(document.getElementById(i+"itemTax") < 0){
            alert("invalid tax option");
            return;
        }
        if(document.getElementById(i+"itemTax") < 0){
            alert("invalid price option");
            return;
        }
        items.push(new Item(document.getElementById(i+"itemName").value,
            document.getElementById(i+"itemDescription").value,
            document.getElementById(i+"itemQuantity").value,
            document.getElementById(i+"itemPrice").value,
            document.getElementById(i+"itemTax").value));
    }
    var json = '{"contacts":[{ "name":"' + customer.name + '","email":"' + customer.email
            + '", "phone":"'+ customer.phone + '", "addLineOne":"'+ customer.addLineOne
            + '", "addLineTwo":"'+ customer.addLineTwo + '", "city":"' + customer.addCity 
            + '", "state":"' + customer.addState + '", "zip":"' + customer.addZip
            + '"},' 
            + '{ "name":"' + job.name + '","email":"' + job.email
            + '", "phone":"'+ job.phone + '", "addLineOne":"'+ job.addLineOne
            + '", "addLineTwo":"'+ job.addLineTwo + '", "city":"' + job.addCity 
            + '", "state":"' + job.addState + '", "zip":"' + job.addZip
            + '"}],'
            +'"items":[';
    for(var i = 0; i < numItems;i++){
        if(i > 0){
            json +=',';
        }
        json+='{"name":"' + items[i].name + '","description":"' + items[i].description
            + '", "qnty":"' + items[i].qnty + '", "price":"' + items[i].price
            +'", "tax":"' + items[i].tax + '"}';
    }
    json+=']}';
    request(json);
}
var xmlHttp = null;
//save invoice and then open print window
function request(jsonData){
    var Url = "/api";
    xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = processRequest;
    xmlHttp.open("POST",Url,true);
    xmlHttp.send(jsonData);
    createPrintWindow();
}
//validate save
function processRequest(){
    if(xmlHttp.readyState == 4 && xmlHttp.status == 200){
        if(xmlHttp.responseText.length > 0)
            alert(xmlHttp.responseText);
    }
    else if(xmlHttp.readyState == 4){
        alert("Bad Server Communication");
    }
}
//Create print window based on global variables
function createPrintWindow(){
    var printWindow= window.open(null,null,'width=200px');
    
    var format = "<div style=\"width:100%; float:left; border-style: double;\">";
    format += "<div style=\"width:49%; float:left; border-right-style:solid;\">";
    format += "Customer: " + customer.name + "<br>";
    format += "Email: " + customer.email + "<br>";
    format += "Phone: " + customer.phone + "<br>";
    format += "Address<br>";
    format += customer.addLineOne + "<br>";
    if(!customer.addLineTwo.length > 1)
        format += customer.addLineTwo  + "<br>";
    else
        format += "<br>";
    format += customer.addCity + "<br>";
    format += customer.addCity + " " + customer.addState;
    format += "</div>";

    format += "<div style=\"width:50%; float:left;\">";
    format += "job: " + job.name + "<br>";
    format += "Email: " + job.email + "<br>";
    format += "Phone: " + job.phone + "<br>";
    format += "Address<br>";
    format += job.addLineOne + "<br>";
    if(!job.addLineTwo.length > 1)
        format += job.addLineTwo  + "<br>";
    else
        format += "<br>";
    format += job.addCity + "<br>";
    format += job.addCity + " " + job.addState;
    format += "</div>";
    
    format +="<div style=\"width=100%;float:left;border-style=dashed;\">";
    var total = 0;
    for(var i = 0; i < numItems; i++){
        format +="<div style=\"width=25%;float:left;border-style:double; margin:5px;\">"
        format += items[i].name + "<br>" + items[i].description + "<br>" +
                "$" + items[i].price + "x" + items[i].qnty + "<br>" +
                "tax total: " + items[i].tax/100 * items[i].qnty * items[i].price + "<br>"; 
        total += items[i].tax/100 * items[i].qnty * items[i].price + items[i].qnty * items[i].price;
        format +="</div>"
    }
    format += "</div>";
    format += "</div>";
    format += "<div style=\"width=25%;float:left;border-style:double; margin:5px;\">Total: $"+total+"</div>";
    format += "</div>";
    printWindow.document.write(format);
    printWindow.focus();
}
//querry the server for invoices based on contact name
function searchInvoices(){
    var jsonData = "{\"action\":\"search\", \"name\":\"";
    jsonData += document.getElementById("searchInvoiceBar").value;
    jsonData += "\"}";
    var Url = "/api";
    xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = responseQuarry;
    xmlHttp.open("POST",Url,true);
    xmlHttp.send(jsonData);
}
//give response to user for additional input
function responseQuarry(){
    if(xmlHttp.readyState == 4 && xmlHttp.status == 200){
        var format = "<div style=\"width:100%\">";
        var results = JSON.parse(xmlHttp.responseText);
        for(var i = 0; i < results.results.length; i++){
            format += "<a href=\"javascript:selectId(" +results.results[i].id + ")\">";
            format += "<div style=\"border-style:double;width:10%; float:left; margin:5px;\">";
            format += results.results[i].date;
        format+= "</div></a>";
        }
        format+= "</div>";
        document.write(format);
    }
}
//pull invoice based on id from searchInvoice/responseQuarry 
function selectId(id){
    var jsonData = "{\"action\":\"getInvoice\", \"id\":\"";
    jsonData += id;
    jsonData += "\"}";
    var Url = "/api";
    xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = showSearchedInvoice;
    xmlHttp.open("POST",Url,true);
    xmlHttp.send(jsonData);
    
    jsonData = "{\"action\":\"getXmlInvoice\", \"id\":\"";
    jsonData += id;
    jsonData += "\"}";
    Url = "/api";
    jmlHttp = new XMLHttpRequest();
    jmlHttp.onreadystatechange = showXmlInvoice;
    jmlHttp.open("POST",Url,true);
    jmlHttp.send(jsonData);
}
function showXmlInvoice(){
    
    if(jmlHttp.readyState == 4 && jmlHttp.status == 200){
        var printWindow= window.open(null,null,'width=200px');
        printWindow.document.write("<textarea style='width:100%; height:200px;'>" + jmlHttp.responseText + "</textarea>");
    }
}
//save informational globally then create printable window
function showSearchedInvoice(){
    if(xmlHttp.readyState == 4 && xmlHttp.status == 200){
        var results = JSON.parse(xmlHttp.responseText);
        customer.name = results.contacts[0].name;
        customer.email = results.contacts[0].email;
        customer.phone = results.contacts[0].phone;
        customer.addLineOne = results.contacts[0].addresslinetwo;
        customer.addLineTwo = results.contacts[0].addresslineone;
        customer.addState = results.contacts[0].state;
        customer.addCity = results.contacts[0].city;
        customer.addZip= results.contacts[0].zipcode;
        
        job.name = results.contacts[1].name;
        job.email = results.contacts[1].email;
        job.phone = results.contacts[1].phone;
        job.addLineOne = results.contacts[1].addresslinetwo;
        job.addLineTwo = results.contacts[1].addresslineone;
        job.addState = results.contacts[1].state;
        job.addCity = results.contacts[1].city;
        job.addZip= results.contacts[1].zipcode;
        
        items =[];
        numItems = 0;
        for(var i = 0; i < results.items.length; i++){
            items.push(new Item(results.items[i].name,
            results.items[i].description,
            results.items[i].qnty,
            results.items[i].price,
            results.items[i].tax));
            numItems++;
        }
        createPrintWindow();
    }
}
function uploadXML(){
    alert("exmaple xml can be found /exampleInvoiceXml.xml");
    var jsonData = "{\"action\":\"addXml\", \"data\":\"";
    jsonData += encodeURI(document.getElementById("xmlInput").value);
    jsonData += "\"}"
    var Url = "/api";
    xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = useXmlResponse;
    xmlHttp.open("POST",Url,true);
    xmlHttp.send(jsonData);
}
function usePreXML(){
    alert("exmaple xml can be found /exampleInvoiceXml.xml");
    var jsonData = "{\"action\":\"addPreXml\"}";
    var Url = "/api";
    xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = useXmlResponse;
    xmlHttp.open("POST",Url,true);
    xmlHttp.send(jsonData);
}
function useXmlResponse(){
    if(xmlHttp.readyState == 4 && xmlHttp.status == 200){
        var success = xmlHttp.responseText;
        if(success == 'true' || success == ""){
            alert("Succesfully added");
        }
        else{
            alert(xmlHttp.responseText);
        }
    }
}