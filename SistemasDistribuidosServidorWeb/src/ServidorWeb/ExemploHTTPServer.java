package ServidorWeb;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class ExemploHTTPServer {
    Socket s;
    BufferedReader in;
    PrintWriter out;
    ServerSocket ss;
    DataOutputStream outDataStream;
    List validPages = Arrays.asList("", "index.html", "contato.html", "sobre.html");
    
    public void setup(){
        try {
            ss = new ServerSocket(8091);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void waitClient(){
        try {            
            s = ss.accept();
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String receiveRequest(){
        try {
          return in.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    private String findHtmlPage(String page) throws FileNotFoundException, IOException{
        File file = null;
        String str;
        StringBuilder bldr = new StringBuilder();
        
        if (validPages.contains(page)) {
            file = new File(System.getProperty("user.dir") + "\\src\\ServidorWeb\\pages\\" + (page.isEmpty() ? "index.html" : page));
            in = new BufferedReader(new FileReader(file));
        }else {
            file = new File(System.getProperty("user.dir") + "\\src\\ServidorWeb\\pages\\erro.html");
            in = new BufferedReader(
                new FileReader(file));
    	}
        
    	if (in != null) {
            while((str = in.readLine())!= null)
      	      bldr.append(str);
            in.close();
    	}
    	
    	return bldr.toString();
    }
    
    private byte[] findFileServer(String page, String fileName) throws IOException{
        File fi;
        if (page.endsWith(".ico") || page.endsWith(".css")) {
            fi = new File(System.getProperty("user.dir") + "\\src\\ServidorWeb\\pages\\" + page);
        }else {
            fi = new File(System.getProperty("user.dir") + "\\src\\ServidorWeb\\pages\\imgs\\" + fileName);
        }

        byte[] fileContent = Files.readAllBytes(fi.toPath());
        return fileContent;
    }
    
    private String findContentTypeRequest(String page, String fileName){
        String contentType = "";
        
        if (fileName.endsWith(".jpg")) {
            contentType = "image/jpeg";
        }else if (fileName.endsWith(".png")) {
            contentType = "image/png";
        }else if (fileName.endsWith(".ico")) {
            contentType = "image/x-icon";
        }else if(page.endsWith(".gif")) {
            contentType = "image/gif";
        }else if(page.endsWith(".css")) {            
            contentType = "text/css";
        }else{
            contentType = "text/html";
        }
        
        return contentType;
    }
    
    private void sendResponseInBytesToClient(String contentType, int length, byte[] content) throws IOException {
        outDataStream = new DataOutputStream(s.getOutputStream());
        outDataStream.writeBytes("HTTP/1.1 200 OK\n" +
                "Content-Type: " + contentType + "\n" +
                "Content-Length: " + length + "\n" +
                "Connection: close\n" +
                "\n");

        outDataStream.write(content);
        outDataStream.flush();
        outDataStream.close();
    }
    
    private void sendResponseInStringToClient(String contentType, int length, String content){
        String data = "HTTP/1.1 200 OK\n" +
            "Content-Type: " + contentType + "\n" +
            "Content-Length: " + length + "\n" +
            "Server: Sist.Dist. Server 1.0\n" +
            "Connection: close\n" +
            "\n";

        out.println(data);
        out.println(content);
        out.flush();
        out.close();
    }
    
    public void sendReply(String page, String fileName) throws IOException{
        String content = "";
        byte[] contentBytes = null;
        
        if (page.contains("imgs") || page.endsWith(".ico") || page.endsWith(".css")) {
            contentBytes = findFileServer(page, fileName);
        }else {
            content = findHtmlPage(page);
        }
        
        String contentType = findContentTypeRequest(page, fileName);
        
        if (contentBytes != null) {
            sendResponseInBytesToClient(contentType, contentBytes.length, contentBytes);
        }
        
        if (content != "") {
            sendResponseInStringToClient(contentType, content.length(), content);
        }
    }
    
    private String findDataPage(String request, boolean findOnlyFileName) {
        String[] page = request.split("/");
        String[] name = null;
        
        if (findOnlyFileName) {
            name = page[2].split(" ");
        }else {
            name = page[1].split(" ");
        }

    	return name[0];
    }
    
    /**
     * @param args the command line arguments
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        ExemploHTTPServer server = new ExemploHTTPServer();
        server.setup();
        
        while(true) {
            server.waitClient();
            String request = server.receiveRequest();
            
            if (request != null && !request.equals("")) {
                String page = server.findDataPage(request, false);
                String fileName = server.findDataPage(request, true);

                server.sendReply(page, fileName);  
            }
        }
    }
}