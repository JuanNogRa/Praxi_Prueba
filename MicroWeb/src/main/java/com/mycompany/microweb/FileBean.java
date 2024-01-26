/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.microweb;

/**
 *
 * @author Prodesk
 */

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.apache.commons.io.IOUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.json.JSONException;
import org.primefaces.model.UploadedFile;
import java.util.Base64;
        
@ManagedBean(name="fileBean")
@SessionScoped
public class FileBean {
    UploadedFile file;
    JsonObject json_text;
    static{ nu.pattern.OpenCV.loadShared(); }
    
    /* utilizar las API de cliente Jersey para crear un cliente 
    RESTful Java para realizar "GET" y "POST" solicitudes a servicio REST*/
    private transient Client client;
    
    public JsonObject getJson_text() {
        return json_text;
	}
	
    public void setJson_text(JsonObject json_text) {
            this.json_text = json_text;
    }
        
    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }
    
    public static String matToJson(Mat mat){        
        JsonObject obj = new JsonObject();

        if(mat.isContinuous()){
            int cols = mat.cols();
            int rows = mat.rows();
            int elemSize = (int) mat.elemSize();    

            byte[] data = new byte[cols * rows * elemSize];

            mat.get(0, 0, data);

            obj.addProperty("rows", mat.rows()); 
            obj.addProperty("cols", mat.cols()); 
            obj.addProperty("type", mat.type());

            // We cannot set binary data to a json object, so:
            // Encoding data byte array to Base64.
            String dataString = Base64.getEncoder().encodeToString(data);
            obj.addProperty("data", dataString);            
            System.out.println("Datos :: "+dataString);
            Gson gson = new Gson();
            String json = gson.toJson(obj);

            return json;
        } else {
            System.out.println("Mat not continuous.");
        }
        return "{}";
    }
    public void fileUploadListener(FileUploadEvent e) throws IOException, JSONException{
        
        // Get uploaded file from the FileUploadEvent
        this.file = e.getFile();
        // Print out the information of the file
        System.out.println("Uploaded File Name Is :: "+file.getFileName()+" :: Uploaded File Size :: "+file.getSize());
        byte[] foto = IOUtils.toByteArray(file.getInputstream());
        Mat mat = Imgcodecs.imdecode(new MatOfByte(foto), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        String matJson = matToJson(mat);
        
        if (client == null) {
            client = Client.create();
        }
        WebResource webResource = client.resource("http://127.0.0.1:8000/Json2Mat");
        webResource.accept("application/json").type("application/json").post(
                ClientResponse.class, matJson);
        URL url = new URL("http://127.0.0.1:8000/MicroInference");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (con.getResponseCode() == 200) {
            Reader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8);
            System.out.println("GET InputStreamReader Code :: " + reader);
            JsonElement element = new JsonParser().parse(reader);
            json_text = element.getAsJsonObject();
            System.out.println("GET Json :: " + json_text);
          }
    }
}
