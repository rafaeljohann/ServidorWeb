package ServidorWeb;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
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
            outDataStream = new DataOutputStream(s.getOutputStream());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String receiveRequest(){
        String line = "";
        try {
            while((line = in.readLine()).length() > 0){
                System.out.println(line);
                if (line.contains("GET")) {
                    return line;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	return line;
    }
    
    public void sendReply(String page, String fileName) throws IOException{
    	StringBuilder bldr = new StringBuilder();
    	String str;
    	BufferedReader in = null;
        
    	if (validPages.contains(page)) {
    		in = new BufferedReader(
                    new FileReader(System.getProperty("user.dir") + "\\src\\ServidorWeb\\pages\\" + (page.isEmpty() ? "index.html" : page)));
    	}else {
    		in = new BufferedReader(
                    new FileReader(System.getProperty("user.dir") + "\\src\\ServidorWeb\\pages\\erro.html"));
    	}
    	
    	if (in != null) {
            while((str = in.readLine())!= null)
      	      bldr.append(str);
            in.close();
    	}
    	
    	String content = bldr.toString();
    	
    	System.out.println(page);
        System.out.println("enviar resposta");
        
        String data = "";
        
        if (page.contains("imgs")) {
            File fi = new File("D:\\" + fileName);
            byte[] fileContent = Files.readAllBytes(fi.toPath());

            outDataStream.writeBytes("HTTP/1.0 200 OK\n" +
                    "Content-Type: image/jpeg\n" +
                    "Content-Length: " + fileContent.length + "\n" +
                    "\n");

            outDataStream.write(fileContent);
            outDataStream.flush();
               
       }else {
            data = "HTTP/1.1 200 OK\n" +
                            "Content-Type: text/html;\n" +
                            "Server: Sist.Dist. Server 1.0\n" +
                            "Connection: close\n" +
                            "\n" +
                            content;
            
            out.println(data);
            out.flush();
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
            String page = server.findDataPage(request, false);
            String fileName = server.findDataPage(request, true);
            
            server.sendReply(page, fileName);
        }
    }
}