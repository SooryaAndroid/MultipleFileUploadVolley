package com.android.multiplefileupload;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button pick,upload1;
    private int PICK_FILE_REQUEST = 1;

    String fileName;
    String extension;
    byte[] bytes;
    ArrayList<File> up_file;
    ArrayList<byte[]> up_byte;

    private List<Fileupload> upload  = new ArrayList();
    private FileuploadAdapter mAdapter;


    RecyclerView recyclerView;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView)findViewById(R.id.recycler);

        pd = new ProgressDialog(MainActivity.this);

        up_file = new ArrayList<>();
        up_byte = new ArrayList<>();



        upload1 = (Button) findViewById(R.id.btn_upload);
        pick = (Button) findViewById(R.id.btn_pick);
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_FILE_REQUEST);
            }
        });

       //---------------upload the file --------------
        upload1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                upload_file_multipart();

            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();

            Fileupload fileupload = new Fileupload();
            try {
                File file = new File(SelectedFilePath.getPath(getApplicationContext(), filePath));
                fileName = file.getName();
                extension = fileName.substring(fileName.lastIndexOf("."));
                bytes = loadFile(file);
                up_file.add(file);
                up_byte.add(bytes);

                fileupload.setName(fileName);
                fileupload.setType(extension);

                upload.add(fileupload);
                mAdapter = new FileuploadAdapter(upload,MainActivity.this);

                Log.d("objl",upload.toString());
                Log.d("objd",up_file.toString());

             RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setAdapter(mAdapter);


            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "ERROR " + e.getMessage() + "\n" + e.getCause(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }


    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int)length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        is.close();
        return bytes;
    }


    public void delete_file(int position)
    {
        upload.remove(position);
        up_file.remove(position);
        up_byte.remove(position);
        mAdapter.notifyDataSetChanged();
        Toast.makeText(this, ""+position, Toast.LENGTH_SHORT).show();
    }


    //--------------multipart request-------
    public void upload_file_multipart()
    {
        final int filecount = up_file.size();


        pd.setMessage("Loading");
        pd.show();
        String url = "http://192.168.1.112/fileupload/php/ads.php";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {

                try {
                    Toast.makeText(getApplicationContext(),""+response.toString(),Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pd.cancel();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.cancel();
                Toast.makeText(getApplicationContext(),error.getCause()+" \n"+error.getMessage(),Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("uname", "razik");
                params.put("filesize", filecount+"");
//                params.put("name", mNameInput.getText().toString());
//                params.put("location", mLocationInput.getText().toString());
//                params.put("about", mAvatarInput.getText().toString());
//                params.put("contact", mContactInput.getText().toString());
                return params;
            }



            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> up_params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
//                params.put("avatar", new DataPart("file_avatar.jpg", AppHelper.getFileDataFromDrawable(getBaseContext(), mAvatarImage.getDrawable()), "image/jpeg"));
             //   up_params.put("file1", new DataPart(fileName+""+extension,bytes, "*/*"));
              //  up_params.put("file2", new DataPart(fileName+"RRR"+extension,bytes, "*/*"));
                for (int i=0; i<filecount; i++){
                    String filenames = up_file.get(i).getName();
                    String extensions = filenames.substring(filenames.lastIndexOf("."));
                    up_params.put("file"+i+"", new DataPart(filenames+""+extensions,up_byte.get(i), "*/*"));
                }
                return up_params;
            }
        };
        // multipartRequest.setOnProgressListener(this);
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
    }
}