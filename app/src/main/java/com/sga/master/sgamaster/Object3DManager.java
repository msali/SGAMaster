package com.sga.master.sgamaster;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Mario Salierno on 15/07/2016.
 */
public class Object3DManager {

    private Activity activity;
    private String TAG = "Object3DManager";


    public Object3DManager(Activity act){

        this.activity=act;

    }


    public Object3D createObject3D(){
        TexturedObject tObj = new TexturedObject("chair.3ds",//id
                R.drawable.chair,//p.e R.drawable.bigoffice//textureID
                0.025f,//scale
                -4,//x
                0,//y
                -2,//z
                1);//dim

        tObj.obj3D.rotateX((float)Math.toRadians(90.0));

        return tObj.obj3D;

    }

    public static byte[] serializeObject3D(Object3D obj3d) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        serializeObject3D(out, obj3d);

        byte[] serObj = out.toByteArray();

        // close the stream
        out.close();

        return serObj;
    }


    public static Object3D deserializeObject3D(byte[] serObj) throws IOException, ClassNotFoundException {

        ByteArrayInputStream in = new ByteArrayInputStream(serObj);

        return deserializeObject3D(in);

    }




    public static void serializeObject3D(OutputStream out, Object3D obj3d) throws IOException {


        ObjectOutputStream oout = new ObjectOutputStream(out);

        // write something in the file
        oout.writeObject(obj3d);

        // close the stream
        oout.close();

    }


    public static Object3D deserializeObject3D(InputStream in) throws IOException, ClassNotFoundException {


        ObjectInputStream input = new ObjectInputStream(in);

        Object3D obj3d = (Object3D) input.readObject();

        return obj3d;

    }

    //http://stackoverflow.com/questions/15298130/load-3d-models-with-jpct-ae
    public class TexturedObject{

        public String modelFileName;
        public String basename = "";
        public String model_format = "";
        public int textureId;
        public float x,y,z;
        public int dimension;
        public float scale;
        public Texture texture;
        public Object3D obj3D;

        //public String texturePath;

        public TexturedObject(String id,
                              int textureId,///*p.e R.drawable.bigoffice*/
                              //String texturePath,
                              float scale,
                              float x,
                              float y,
                              float z,
                              int dim){
            this.modelFileName=modelFileName;
            this.textureId = textureId;
            //this.texturePath=texturePath; //path = assets/
            this.scale=scale;
            this.x=x;
            this.y=y;
            this.z=z;
            dimension=dim;
            obj3D = loadObject(id, textureId, scale);
        }


        //texture and model file has to match the same basename p.e. chair.3ds and chair.jpg
        private Object3D loadObject(String modName, int textureID/*p.e R.drawable.bigoffice*/, float thingScale /*= 1*/)
        {

            StringTokenizer sToken = new StringTokenizer(modName,".");
            int ntok = sToken.countTokens();
            for(int i = 0; i<ntok; i++){
                if(i==ntok-1) {
                    model_format = sToken.nextToken();
                    break;
                }
                else
                    basename=basename+sToken.nextToken();
            }
            Log.e(TAG, "basename:"+basename);
            Log.e(TAG, "mod_form:"+model_format);


            Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), textureID);
            texture = new Texture(bm);
            TextureManager.getInstance().addTexture(basename, texture);

            try {

                Log.e(TAG, "model file name:"+modName);
                Object3D objT = loadModel(basename, model_format, thingScale);

                objT.build();
                objT.setTexture(basename);
                objT.setName(basename);
                objT.translate(x, y, z);

                return objT;
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }


        //http://stackoverflow.com/questions/15298130/load-3d-models-with-jpct-ae
        private Object3D loadModel(String filename, String format, float scale) throws UnsupportedEncodingException {

            //InputStream stream = new ByteArrayInputStream(filename.getBytes("UTF-8"));
            //InputStream stream = mContext.getAssets().open("FILENAME.3DS")
            InputStream stream = null;
            try {
                stream = activity.getApplicationContext().getAssets().open(filename+"."+format);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Object3D[] model=null;
            switch (format){
                case "3ds":
                    model = Loader.load3DS(stream, scale);
                    break;
                case "3DS":
                    model = Loader.load3DS(stream, scale);
                    break;
                case "obj":
                    Loader.loadOBJ(stream,null,scale);
                    break;
                case "OBJ":
                    Loader.loadOBJ(stream,null,scale);
                    break;
                case "md2":
                    Loader.loadMD2(stream,scale);
                    break;
                case "MD2":
                    Loader.loadMD2(stream,scale);
                    break;
                case "asc":
                    Loader.loadASC(stream,scale,false);
                    break;
                case "ASC":
                    Loader.loadASC(stream,scale,false);
                    break;
                default:
                    /*
                    Loads an object in serialized format.
                    This format can't be created by jPCT-AE but by using the desktop version of jPCT by using the DeSerializer.
                    This is the fastest and least memory intense way to load an object in jPCT-AE.
                    It doesn't matter the extension. You can name the file as you like, because you are loading them via an input stream anyway.
                    Just keep in mind that Android has some limits for the size of various file types.
                    If it complains about a file being too large, just name it .mp3... Yes, seriously...
                     */
                    Loader.loadSerializedObject(stream);
                /*
                default:
                    return null;
                */
            }

            if(model==null)return null;

            Object3D o3d = new Object3D(0);
            Object3D temp = null;
            for (int i = 0; i < model.length; i++) {
                temp = model[i];
                temp.setCenter(SimpleVector.ORIGIN);
                temp.rotateX((float)( -.5*Math.PI));
                temp.rotateMesh();
                temp.setRotationMatrix(new Matrix());
                o3d = Object3D.mergeObjects(o3d, temp);
                o3d.build();
            }
            return o3d;
        }

        /*
        //http://www.jpct.net/forum2/index.php/topic,2168.15.html?PHPSESSID=2963dbbdcd6472ebe013778ea71482ec
        ////txtrName for example for a named bman.3ds, it would be just bman
        private Object3D load3DSObject(String txtrName, int textureID, float thingScale)
        {

            //TextureManager.getInstance().addTexture(txtrName + ".jpg", new Texture("res/" + txtrName + ".jpg"));
            // Create a texture out of the icon...:-)
            //texture = new Texture(BitmapHelper.rescale(BitmapHelper.convert(activity.getResources().getDrawable(R.drawable.ic_launcher)), 64, 64));
            Log.e("TEXTUREchairID","chair:"+textureID);
            //Drawable image = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.bigoffice , null);

            //texture = new Texture(image);

            //Bitmap bmp = BitmapFactory.decodeFile( "/drawable/" );
            Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), textureID);
            //texture = new Texture(activity.getResources().openRawResource(textureID));
            texture = new Texture(bm);
            //texture = new Texture(200,200);
            //texture = new Texture(BitmapHelper.convert(image));
            TextureManager.getInstance().addTexture(txtrName, texture);


            try {
                String fname = txtrName+".3ds";
                //Log.e(TAG, "model file name:"+fname);
                Object3D objT = loadModel(fname, thingScale);
                //Primitives.getCube(10);
                //cube.calcTextureWrapSpherical();
                //cube.setTexture("texture");
                //cube.strip();
                objT.build();

                return objT;
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            return null;
        }


        //http://stackoverflow.com/questions/15298130/load-3d-models-with-jpct-ae
        private Object3D loadModel(String filename, float scale) throws UnsupportedEncodingException {

            //InputStream stream = new ByteArrayInputStream(filename.getBytes("UTF-8"));
            //InputStream stream = mContext.getAssets().open("FILENAME.3DS")
            InputStream stream = null;
            try {
                stream = activity.getApplicationContext().getAssets().open(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Object3D[] model;
            if(filename.endsWith(".3ds")){
                model = Loader.load3DS(stream, scale);
            }
            else
                return null;

            Object3D o3d = new Object3D(0);
            Object3D temp = null;
            for (int i = 0; i < model.length; i++) {
                temp = model[i];
                temp.setCenter(SimpleVector.ORIGIN);
                temp.rotateX((float)( -.5*Math.PI));
                temp.rotateMesh();
                temp.setRotationMatrix(new Matrix());
                o3d = Object3D.mergeObjects(o3d, temp);
                o3d.build();
            }
            return o3d;
        }
        */
    }




}
