package com.example.quizzeradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.example.quizzeradmin.QuestionActivity.list;

public class SetsActivity extends AppCompatActivity {

    private GridView gridView;
    private GridAdapter adapter;
    private Dialog loadingdialog;
  private String categoryname;
  private DatabaseReference myref;
  private List<String> sets;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadingdialog = new Dialog(this);
        loadingdialog.setContentView(R.layout.loading);
        loadingdialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corner));
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.setCancelable(false);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        categoryname = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(categoryname);

        gridView = findViewById(R.id.gridview);
        myref = FirebaseDatabase.getInstance().getReference();

        sets =CategoryActivity.list.get(getIntent().getIntExtra("position", 0)).getSets();
        adapter = new GridAdapter(sets, getIntent().getStringExtra("title"), new GridAdapter.Gridlistener() {
            @Override
            public void addset() {
                loadingdialog.show();
                final String id = UUID.randomUUID().toString();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                firebaseDatabase.getReference().child("Categories").child(getIntent().getStringExtra("key")).child("sets").child(id).setValue("SET ID").addOnCompleteListener(new  OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                          sets.add(id);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(SetsActivity.this,"Something wentwrong",Toast.LENGTH_LONG);
                        }
                        loadingdialog.dismiss();
                    }
                });
            }

            @Override
    public void onlongclick(final String setid,int position) {
       new  AlertDialog.Builder(SetsActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert)
          .setTitle("Delete Set"+position)
               .setMessage("Are You sure,You want to remove this question?")
               .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       loadingdialog.show();
                       myref.child("SETS").child(setid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if (task.isSuccessful()) {
                                   myref.child("Categories").child(CategoryActivity.list.get(getIntent().getIntExtra("position", 0)).getKey()).
                                           child("sets").child(setid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if(task.isSuccessful())
                                           {
                                               sets.remove(setid);
                                               adapter.notifyDataSetChanged();
                                           }
                                           else{
                                               Toast.makeText(SetsActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                                           }
                                           loadingdialog.dismiss();
                                       }
                                   });
                                   loadingdialog.dismiss();
                               } else {
                                   Toast.makeText(SetsActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                               }
                               loadingdialog.show();
                           }
                       });
                   }
               })


                        .setNegativeButton("cancel", null)
                               .setIcon(android.R.drawable.ic_dialog_alert)
                               .show();

            }
        });
        gridView.setAdapter(adapter);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
