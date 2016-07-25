package com.sga.master.sgamaster;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.threed.jpct.Object3D;
import com.threed.jpct.Texture;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

/**
 * Created by Mario Salierno on 20/07/2016.
 */
public class JSONEncoder {

    private String TAG = "JSONEncoder";
    private MainMasterActivity act;


    public JSONEncoder(MainMasterActivity act){
        this.act=act;
    }



    public String encodeObject3DNewNew(/*String textureFile,*/String basename, int textureID, Object3D obj3D)
    {
        //PrintWriter pw = null;
        try{


            //Bitmap bm = BitmapFactory.decodeResource(act.getResources(), textureID);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            BitmapFactory.decodeResource(act.getResources(), textureID).compress(Bitmap.CompressFormat.JPEG, 100, stream);
            //Texture texture = new Texture(bm);
            //byte byteArray [] = byteBuffer.array();
            //Log.e(TAG,"byteArray len:"+byteArray.length);
            //TextureManager.getInstance().addTexture(basename, texture);

            //ByteArrayOutputStream ba = loadFile(textureFile);

            //Converting byte[] to base64 string
            //NOTE: Always remember to encode your base 64 string in utf8 format other wise you may always get problems on browser.

            //String fileBase64String = org.apache.commons.codec.binary.StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(/*ba.toByteArray()*/byteArray));


            //Object3D serialization stuff
            Object3DManager o3dM = new Object3DManager(act);

            //Object3D tmpObj3d = o3dM.createObject3D();
            //String obj3Dbase64 = org.apache.commons.codec.binary.StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(Object3DManager.serializeObject3D(obj3D)));

            //writing json
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("successful", true);
            //jsonObject.put("basename", basename);
            jsonObject.put("file", org.apache.commons.codec.binary.
                    StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(stream.toByteArray())));
            jsonObject.put("obj3d",org.apache.commons.codec.binary.
                    StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(Object3DManager.serializeObject3D(obj3D))));


            return jsonObject.toString();

        }
        catch(Exception ex) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("successful",false);
                jsonObject.put("message",ex.getMessage());
                return jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("successful",false);
            jsonObject.put("message","generic error");
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String encodeObject3DNew(/*String textureFile,*/String basename, int textureID, Object3D obj3D)
    {
        //PrintWriter pw = null;
        try{
            //pw = response.getWriter();


            /*
            String file_extension=".jpg";
            String basename="";
            StringTokenizer sToken = new StringTokenizer(textureFile,".");
            int ntok = sToken.countTokens();
            for(int i = 0; i<ntok; i++){
                if(i==ntok-1) {
                    file_extension = sToken.nextToken();
                    break;
                }
                else
                    basename=basename+sToken.nextToken();
            }
            */

            Bitmap bm = BitmapFactory.decodeResource(act.getResources(), textureID);

            Texture texture = new Texture(bm);
            //byte byteArray [] = byteBuffer.array();
            //Log.e(TAG,"byteArray len:"+byteArray.length);
            //TextureManager.getInstance().addTexture(basename, texture);

            //ByteArrayOutputStream ba = loadFile(textureFile);

            //Converting byte[] to base64 string
            //NOTE: Always remember to encode your base 64 string in utf8 format other wise you may always get problems on browser.

            //String fileBase64String = org.apache.commons.codec.binary.StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(/*ba.toByteArray()*/byteArray));


            //Object3D serialization stuff
            Object3DManager o3dM = new Object3DManager(act);

            //Object3D tmpObj3d = o3dM.createObject3D();
            //String obj3Dbase64 = org.apache.commons.codec.binary.StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(Object3DManager.serializeObject3D(obj3D)));

            //writing json
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("successful", true);
            //jsonObject.put("basename", basename);
            jsonObject.put("file", org.apache.commons.codec.binary.
                    StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(/*ba.toByteArray()*/Object3DManager.serializeTexture(texture))));
            jsonObject.put("obj3d",org.apache.commons.codec.binary.
                    StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(Object3DManager.serializeObject3D(obj3D))));


            /*
            PrintStream jsonOut = new PrintStream(jsonString);
            jsonOut.println("{");
            jsonOut.println("\"successful\": true,");
            //jsonOut.println("\"basename\": "+basename+",");
            //jsonOut.println("\"extension\": "+file_extension+",");
            jsonOut.println("\"basename\": "+basename+",");
            jsonOut.println("\"file\": \""+fileBase64String+"\"");
            jsonOut.println("\"obj3d\": \""+obj3Dbase64+"\"");
            jsonOut.println("}");
            */
            return jsonObject.toString();

        }
        catch(Exception ex) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("successful",false);
                jsonObject.put("message",ex.getMessage());
                return jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /*
            try {

                jsonOut.println("{");
                jsonOut.println("\"successful\": false,");
                jsonOut.println("\"message\": \""+ex.getMessage()+"\",");
                jsonOut.println("}");
                return jsonString;
            }
            */

        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("successful",false);
            jsonObject.put("message","generic error");
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }


    public String encodeObject3D(/*String textureFile,*/String basename, int textureID, Object3D obj3D)
    {
        //PrintWriter pw = null;
        try{
            //pw = response.getWriter();


            /*
            String file_extension=".jpg";
            String basename="";
            StringTokenizer sToken = new StringTokenizer(textureFile,".");
            int ntok = sToken.countTokens();
            for(int i = 0; i<ntok; i++){
                if(i==ntok-1) {
                    file_extension = sToken.nextToken();
                    break;
                }
                else
                    basename=basename+sToken.nextToken();
            }
            */

            Bitmap bm = BitmapFactory.decodeResource(act.getResources(), textureID);
            //int width = bm.getWidth();
            //int height = bm.getHeight();
            int size = bm.getRowBytes() * bm.getHeight();
            ByteBuffer byteBuffer = ByteBuffer.allocate(size);
            bm.copyPixelsToBuffer(byteBuffer);
            byte byteArray [] = byteBuffer.array();
            Log.e(TAG,"byteArray len:"+byteArray.length);
            //Texture texture = new Texture(bm);
            //TextureManager.getInstance().addTexture(basename, texture);

            //ByteArrayOutputStream ba = loadFile(textureFile);

            //Converting byte[] to base64 string
            //NOTE: Always remember to encode your base 64 string in utf8 format other wise you may always get problems on browser.

            //String fileBase64String = org.apache.commons.codec.binary.StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(/*ba.toByteArray()*/byteArray));


            //Object3D serialization stuff
            Object3DManager o3dM = new Object3DManager(act);

            //Object3D tmpObj3d = o3dM.createObject3D();
            //String obj3Dbase64 = org.apache.commons.codec.binary.StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(Object3DManager.serializeObject3D(obj3D)));

            //writing json
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("successful", true);
            jsonObject.put("basename", basename);
            jsonObject.put("file", org.apache.commons.codec.binary.
                            StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(/*ba.toByteArray()*/byteArray)));
            jsonObject.put("obj3d",org.apache.commons.codec.binary.
                    StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(Object3DManager.serializeObject3D(obj3D))));


            /*
            PrintStream jsonOut = new PrintStream(jsonString);
            jsonOut.println("{");
            jsonOut.println("\"successful\": true,");
            //jsonOut.println("\"basename\": "+basename+",");
            //jsonOut.println("\"extension\": "+file_extension+",");
            jsonOut.println("\"basename\": "+basename+",");
            jsonOut.println("\"file\": \""+fileBase64String+"\"");
            jsonOut.println("\"obj3d\": \""+obj3Dbase64+"\"");
            jsonOut.println("}");
            */
            return jsonObject.toString();

        }
        catch(Exception ex) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("successful",false);
                jsonObject.put("message",ex.getMessage());
                return jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /*
            try {

                jsonOut.println("{");
                jsonOut.println("\"successful\": false,");
                jsonOut.println("\"message\": \""+ex.getMessage()+"\",");
                jsonOut.println("}");
                return jsonString;
            }
            */

        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("successful",false);
            jsonObject.put("message","generic error");
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String encodeObject3DOLD(/*String textureFile,*/String basename, int textureID, Object3D obj3D)
    {
        //PrintWriter pw = null;
        try{
            //pw = response.getWriter();


            /*
            String file_extension=".jpg";
            String basename="";
            StringTokenizer sToken = new StringTokenizer(textureFile,".");
            int ntok = sToken.countTokens();
            for(int i = 0; i<ntok; i++){
                if(i==ntok-1) {
                    file_extension = sToken.nextToken();
                    break;
                }
                else
                    basename=basename+sToken.nextToken();
            }
            */

            Bitmap bm = BitmapFactory.decodeResource(act.getResources(), textureID);
            //int width = bm.getWidth();
            //int height = bm.getHeight();
            int size = bm.getRowBytes() * bm.getHeight();
            ByteBuffer byteBuffer = ByteBuffer.allocate(size);
            bm.copyPixelsToBuffer(byteBuffer);
            byte byteArray [] = byteBuffer.array();
            //texture = new Texture(bm);
            //TextureManager.getInstance().addTexture(basename, texture);

            //ByteArrayOutputStream ba = loadFile(textureFile);

            //Converting byte[] to base64 string
            //NOTE: Always remember to encode your base 64 string in utf8 format other wise you may always get problems on browser.

            //String fileBase64String = org.apache.commons.codec.binary.StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(/*ba.toByteArray()*/byteArray));


            //Object3D serialization stuff
            Object3DManager o3dM = new Object3DManager(act);

            //Object3D tmpObj3d = o3dM.createObject3D();
            //String obj3Dbase64 = org.apache.commons.codec.binary.StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(Object3DManager.serializeObject3D(obj3D)));

            //writing json

            String jsonString = "";
            jsonString = jsonString + "{";
            jsonString = jsonString + "\"successful\": \"true\",";
            jsonString = jsonString + "\"basename\": \""+basename+"\",";

            //encode texture file in Base64
            jsonString = jsonString + "\"file\": \""+org.apache.commons.codec.binary.
                                                    StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(/*ba.toByteArray()*/byteArray))/*fileBase64String*/+"\"";
            //System.gc();
            //encode Object3D in Base64
            jsonString = jsonString + "\"obj3d\": \""+org.apache.commons.codec.binary.
                                                    StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(Object3DManager.serializeObject3D(obj3D)))+"\"";
            //System.gc();
            jsonString = jsonString + "}";

            /*
            PrintStream jsonOut = new PrintStream(jsonString);
            jsonOut.println("{");
            jsonOut.println("\"successful\": true,");
            //jsonOut.println("\"basename\": "+basename+",");
            //jsonOut.println("\"extension\": "+file_extension+",");
            jsonOut.println("\"basename\": "+basename+",");
            jsonOut.println("\"file\": \""+fileBase64String+"\"");
            jsonOut.println("\"obj3d\": \""+obj3Dbase64+"\"");
            jsonOut.println("}");
            */
            return jsonString;

        }
        catch(Exception ex) {
            String jsonString = "";
            PrintStream jsonOut = null;
            try {
                jsonOut = new PrintStream(jsonString);
                jsonOut.println("{");
                jsonOut.println("\"successful\": false,");
                jsonOut.println("\"message\": \""+ex.getMessage()+"\",");
                jsonOut.println("}");
                return jsonString;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return jsonString;

        }
    }


    public String encodeFile(String fname)
    {
        //PrintWriter pw = null;
        try{
            //pw = response.getWriter();

            String file_extension=".jpg";
            String basename="";
            StringTokenizer sToken = new StringTokenizer(fname,".");
            int ntok = sToken.countTokens();
            for(int i = 0; i<ntok; i++){
                if(i==ntok-1) {
                    file_extension = sToken.nextToken();
                    break;
                }
                else
                    basename=basename+sToken.nextToken();
            }


            ByteArrayOutputStream ba= loadFile(fname);

            //Converting byte[] to base64 string
            //NOTE: Always remember to encode your base 64 string in utf8 format other wise you may always get problems on browser.

            String fileBase64String =
                    org.apache.commons.codec.binary.StringUtils.newStringUtf8(org.apache.
                            commons.codec.binary.Base64.encodeBase64(ba.toByteArray()));

            //writing json

            String jsonString = "";
            PrintStream jsonOut = new PrintStream(jsonString);
            jsonOut.println("{");
            jsonOut.println("\"successful\": true,");
            jsonOut.println("\"basename\": "+basename+",");
            jsonOut.println("\"extension\": "+file_extension+",");
            jsonOut.println("\"file\": \""+fileBase64String+"\"");
            jsonOut.println("}");
            return jsonString;
        }catch(Exception ex)
        {
            String jsonString = "";
            PrintStream jsonOut = null;
            try {
                jsonOut = new PrintStream(jsonString);
                jsonOut.println("{");
                jsonOut.println("\"successful\": false,");
                jsonOut.println("\"message\": \""+ex.getMessage()+"\",");
                jsonOut.println("}");
                return jsonString;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return jsonString;

        }
    }

    /*
    public void doSomething(String fname)
    {
        //PrintWriter pw = null;
        try{
            //pw = response.getWriter();

            String file_extension=".jpg";
            String basename="";
            StringTokenizer sToken = new StringTokenizer(fname,".");
            int ntok = sToken.countTokens();
            for(int i = 0; i<ntok; i++){
                if(i==ntok-1) {
                    file_extension = sToken.nextToken();
                    break;
                }
                else
                    basename=basename+sToken.nextToken();
            }

            ByteArrayOutputStream ba= loadFile(fname);

            //Converting byte[] to base64 string
            //NOTE: Always remember to encode your base 64 string in utf8 format other wise you may always get problems on browser.

            String fileBase64String =
                    org.apache.commons.codec.binary.StringUtils.newStringUtf8(org.apache.
                            commons.codec.binary.Base64.encodeBase64(ba.toByteArray()));

            //writing json

            ByteArrayOutputStream
            pw.println("{");
            pw.println("\"successful\": true,");
            pw.println("\"basename\": "+basename+",");
            pw.println("\"extension\": "+file_extension+",");
            pw.println("\"file\": \""+fileBase64String+"\"");
            pw.println("}");
            return;
        }catch(Exception ex)
        {
            pw.println("{");
            pw.println("\"successful\": false,");
            pw.println("\"message\": \""+ex.getMessage()+"\",");
            pw.println("}");
            return;
        }
    }
    */

    private ByteArrayOutputStream loadFile(String fileName)
    {
        try {
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buf = new byte[1024];

            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum); //no doubt here is 0
            }

            return bos;

        } catch (FileNotFoundException fnf) {
            fnf.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
