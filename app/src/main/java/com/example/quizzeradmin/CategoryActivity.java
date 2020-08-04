package com.example.quizzeradmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryActivity extends AppCompatActivity
{
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference myRef = firebaseDatabase.getReference();
    private RecyclerView recyclerView;
    public static List<CategoriesModel> list;
    private CircleImageView addimage;
    private EditText categoryname;
    private Button  addbtn;
    private Dialog loadingdialog , categorydialog;
    private Uri image;
    private CategoryAdapter adapter;
    private String downloadurl;

    StorageReference storageReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");

        loadingdialog = new Dialog(this);
        loadingdialog.setContentView(R.layout.loading);
        loadingdialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corner));
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.setCancelable(false);

        setcategorydialog();

        recyclerView = findViewById(R.id.rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);

        list = new ArrayList<>();
         adapter = new CategoryAdapter(list, new CategoryAdapter.Deletelistener() {
             @Override
             public void onDelete(final String key , final int position) {
                 new AlertDialog.Builder(CategoryActivity.this , R.style.Theme_AppCompat_Light_Dialog_Alert)
                         .setTitle("Delete Category")
                         .setMessage("Are You sure,You want to remove this category?")
                         .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 loadingdialog.show();
                                 myRef.child("Categories").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                         if(task.isSuccessful())
                                         {
                                             for(String setid :list.get(position).getSets())
                                             {
                                                 myRef.child("SETS").child(list.get(position).getName()).removeValue();
                                             }
                                             list.remove(position);
                                             adapter.notifyDataSetChanged();
                                             loadingdialog.dismiss();
                                         }
                                         else{
                                             Toast.makeText(CategoryActivity.this,"Failed to remove",Toast.LENGTH_LONG).show();
                                             loadingdialog.dismiss();
                                         }
                                     }
                                 });
                             }
                         })
                         .setNegativeButton("cancel",null)
                         .setIcon(android.R.drawable.ic_dialog_alert)
                         .show();
             }
         });

        recyclerView.setAdapter(adapter);

        loadingdialog.show();
        myRef.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                {
                    List<String> sets = new ArrayList<>();
                    for(DataSnapshot dataSnapshot2:dataSnapshot1.child("sets").getChildren()){
                        sets.add(dataSnapshot2.getKey());
                    }
                    list.add(new CategoriesModel(dataSnapshot1.child("name").getValue().toString(),
                            sets,
                            dataSnapshot1.child("url").getValue().toString(),
                            dataSnapshot1.getKey()  ));
                }
                adapter.notifyDataSetChanged();
                loadingdialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CategoryActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
                loadingdialog.dismiss();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == R.id.add) {
            //dialog show//
            categorydialog.show();
        }
        if(item.getItemId() == R.id.logout)
        {
            new  AlertDialog.Builder(CategoryActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle("Are You sure you want to logout!!")
                    .setMessage("Are You sure,You want to remove this question?")
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadingdialog.show();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(CategoryActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("Enjoy the Quiz", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();


        }
        return super.onOptionsItemSelected(item);
    }

    private void setcategorydialog(){
        categorydialog = new Dialog(this);
        categorydialog.setContentView(R.layout.add_category_dialog);
        categorydialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_box));
        categorydialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        categorydialog.setCancelable(true);

        addimage = categorydialog.findViewById(R.id.image);
        categoryname = categorydialog.findViewById(R.id.categoryname);
        addbtn = categorydialog.findViewById(R.id.add_btn);


        addimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryintent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryintent,101);
            }
        });

        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(categoryname.getText().toString().isEmpty() || categoryname.getText()==null )
                {
                    categoryname.setError("CategoryName is Required");
                    return;
                }for(CategoriesModel model :list){
                    if(categoryname.getText().toString().equals(model.getName())){
                        categoryname.setError("category name already exist");
                        return;
                    }
                }
                if(image == null)
                {
                    Toast.makeText(CategoryActivity.this,"Please upload an image",Toast.LENGTH_LONG).show();
                    return;
                }
                categorydialog.dismiss();
                //upload data//
                uploadData();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101)
        {
            if(resultCode == RESULT_OK)
            {
                 image = data.getData();
                addimage.setImageURI(image);
            }
        }
    }

    private void uploadData() {
        loadingdialog.show();
         StorageReference storageReference = FirebaseStorage.getInstance().getReference();

         //Creating the refrence where the file is to be upload//
         final StorageReference  imagerefernce = storageReference.child("Categories").child(image.getLastPathSegment());

         UploadTask uploadTask = imagerefernce.putFile(image);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imagerefernce.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            downloadurl = task.getResult().toString();
                            uploadcategoryName();
                        }
                        else
                        {
                            loadingdialog.dismiss();
                            Toast.makeText(CategoryActivity.this,"something went wrong",Toast.LENGTH_LONG);
                        }
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    // Handle failures
                    // ...
                    loadingdialog.dismiss();
                    Toast.makeText(CategoryActivity.this,"something went wrong",Toast.LENGTH_LONG);

                }
            }
        });
    }

    private void uploadcategoryName(){
        Map<String, Object> map = new HashMap< >();
        map.put("name",categoryname.getText().toString());
        map.put("sets",0);
        map.put("url",downloadurl);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final String id = UUID.randomUUID().toString();
        firebaseDatabase.getReference().child("Categories").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                     list.add(new CategoriesModel(categoryname.getText().toString(),new ArrayList<String>(),downloadurl,id));
                     adapter.notifyDataSetChanged();
                }
                else{
                    Toast.makeText(CategoryActivity.this,"something went wrong",Toast.LENGTH_LONG);

                }
                loadingdialog.dismiss();
            }
        });
    }
}
