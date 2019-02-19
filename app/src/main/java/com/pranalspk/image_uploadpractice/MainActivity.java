package com.pranalspk.image_uploadpractice;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.lang.ref.Reference;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

    private  static final int PICK_IMAGE_REQUEST =1;

    private Button choosefileBTN;
    private Button uploadBTN;
    private EditText descriptionET;
    private ProgressBar progressPB;
    private ImageView uploadedimageIV;

    private Uri mImageUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choosefileBTN = findViewById(R.id.choosefileBTN);
        uploadBTN = findViewById(R.id.uploadBTN);
        descriptionET = findViewById(R.id.descriptionET);
        progressPB = findViewById(R.id.progressPB);
        uploadedimageIV = findViewById(R.id.uploadedimageIV);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        choosefileBTN.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        uploadBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUploadTask != null && mUploadTask.isInProgress()){
                    Toast.makeText(MainActivity.this,"UPLOAD IN PROGRESS",Toast.LENGTH_SHORT).show();
                }
                else {
                    uploadFile();
                }
            }
        });

    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE_REQUEST && resultCode == RESULT_OK
        && data != null && data.getData() != null){
            mImageUri = data.getData();

            uploadedimageIV.setImageURI(mImageUri);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));

    }

    private void uploadFile(){
        if(mImageUri != null){

            final StorageReference fileRef = mStorageRef.child(System.currentTimeMillis()
                    +"."+getFileExtension(mImageUri));
            mUploadTask = fileRef.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressPB.setProgress(0);
                                }
                            },5000);

                            Toast.makeText(MainActivity.this,"UPLOAD SUCCESSFULL",Toast.LENGTH_LONG).show();

                            UploadModel upload = new UploadModel(descriptionET.getText().toString(),
                                    fileRef.getDownloadUrl().toString());
                            String uploadID = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadID).setValue(upload);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                            progressPB.setProgress((int) progress);
                        }
                    });

        }
        else{
            Toast.makeText(this,"NO IMAGE SELECTED", Toast.LENGTH_SHORT).show();
        }

    }
}
