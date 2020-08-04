package com.example.quizzeradmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class QuestionActivity extends AppCompatActivity {

    private Button add_btn, execel_btn;
    RecyclerView recyclerView;
    private QuestionAdapter adapter;
    public static List<QuestionModel> list;
    private Dialog loadingdialog;
    private QuestionModel questionModel;
    private DatabaseReference myRef;
    private String setid;
    private TextView loadingtext;
    String  categoryname;
    private static final int CELL_COUNT =6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myRef = FirebaseDatabase.getInstance().getReference();

        loadingdialog = new Dialog(this);
        loadingdialog.setContentView(R.layout.loading);
        loadingdialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corner));
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.setCancelable(false);
        loadingtext = (TextView)findViewById(R.id.loadingtv);
         categoryname = getIntent().getStringExtra("category");
        setid = getIntent().getStringExtra("setid"); //22min32sec?//
        getSupportActionBar().setTitle(categoryname);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        add_btn = findViewById(R.id.add_btn);
        execel_btn = findViewById(R.id.excel_btn);
        recyclerView = findViewById(R.id.question_recycler_view);
        loadingtext = (TextView)findViewById(R.id.loadingtv);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(linearLayoutManager);

        loadingdialog.show();
        list = new ArrayList<>();

        adapter = new QuestionAdapter(list, categoryname, new QuestionAdapter.Deletelistener() {
            @Override
            public void onlongclick(final int position, final String id) {
                new AlertDialog.Builder(QuestionActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                        .setTitle("Delete Question")
                        .setMessage("Are You sure,You want to remove this category?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingdialog.show();
                                myRef.child("SETS").child(setid).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            list.remove(position);
                                            adapter.notifyItemRemoved(position);
                                        } else {
                                            Toast.makeText(QuestionActivity.this, "Failed to remove", Toast.LENGTH_LONG).show();
                                            loadingdialog.dismiss();
                                        }
                                        loadingdialog.dismiss();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("cancel", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });

        recyclerView.setAdapter(adapter);
        getData(categoryname, setid);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addquestion = new Intent(QuestionActivity.this, AddQuestionActivity.class);
                addquestion.putExtra("categoryname", categoryname);
                addquestion.putExtra("setid", setid);
                startActivity(addquestion);

            }
        });
        execel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if permission is there
                if(ActivityCompat.checkSelfPermission(QuestionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    selectfile();
                }
                //else request permission//
                else{
                    ActivityCompat.requestPermissions(QuestionActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
                }
            }
        });
    }
    //check whether the user denied or allowed.//
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectfile();
            } else {
                Toast.makeText(this, "Please Allow Permission!", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void selectfile()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent,"Select File"),102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==102)
        {
            if(resultCode==RESULT_OK)
            {
                String filepath = data.getData().getPath();
                if(filepath.endsWith(".xlsx"))
                {
                    //Toast.makeText(this,"File Selected",Toast.LENGTH_LONG);
                    readfile(data.getData());
                }
                else{
                    Toast.makeText(this,"Select an Excel File",Toast.LENGTH_LONG).show();
            }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getData(String categoryname, final String setid){
        loadingdialog.show();
        myRef.child("SETS").child(setid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                {
                    String id = dataSnapshot1.getKey();
                    String question = dataSnapshot1.child("question").getValue().toString();
                    String a = dataSnapshot1.child("optionA").getValue().toString();
                    String b = dataSnapshot1.child("optionB").getValue().toString();
                    String c = dataSnapshot1.child("optionC").getValue().toString();
                    String d = dataSnapshot1.child("optionD").getValue().toString();
                    String correctAns = dataSnapshot1.child("correctAns").getValue().toString();



                    list.add(new QuestionModel(id,question,a,b,c,d,correctAns,setid));
                }
                loadingdialog.dismiss();
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QuestionActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                loadingdialog.dismiss();
                finish();
            }
        });
    }

    protected void readfile(final Uri fileuri) {

     //   loadingtext.setText("Scanningtext");

        loadingdialog.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final HashMap<String, Object> parentMap = new HashMap<>();
                final List<QuestionModel> templist= new ArrayList<>();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(fileuri);
                    XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                    XSSFSheet sheet = workbook.getSheetAt(0);
                    //to convert data into string
                    FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    //to count no of rows in excel sheet and create an object of each question //
                    int rowcount = sheet.getPhysicalNumberOfRows();
                    if (rowcount > 0) {
                        //Run for loop for each row//
                        for (int r = 0; r < rowcount; r++) {
                            Row row = sheet.getRow(r);
                            //Here we are taking cellcount as 6 because each as question format
                            //1st question,4 optiion  and 1 correct ans//so in total 6things//
                            if (row.getPhysicalNumberOfCells() == CELL_COUNT) {
                                //Here question will be in 1st cell so cellposition 0//
                                String question = getCellData(row, 0, formulaEvaluator);
                                String a = getCellData(row, 1, formulaEvaluator);
                                String b = getCellData(row, 2, formulaEvaluator);
                                String c = getCellData(row, 3, formulaEvaluator);
                                String d = getCellData(row, 4, formulaEvaluator);
                                String correctAns = getCellData(row, 5, formulaEvaluator);

                                if (correctAns.equals(a) || correctAns.equals(b) || correctAns.equals(c) || correctAns.equals(d)) {
                                    HashMap<String, Object> questionMap = new HashMap<>();
                                    questionMap.put("question", question);
                                    questionMap.put("optionA", a);
                                    questionMap.put("optionB", b);
                                    questionMap.put("optionC", c);
                                    questionMap.put("optionD", d);
                                    questionMap.put("correctAns", correctAns);
                                    questionMap.put("setid",setid);

                                    String id = UUID.randomUUID().toString();
                                    parentMap.put(id, questionMap);
                                    templist.add(new QuestionModel(id, question, a, b, c, d, correctAns, setid));
                                } else {
                                    final int finalR1 = r;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                          //  loadingtext.setText("Loading....");
                                            loadingdialog.dismiss();
                                            Toast.makeText(QuestionActivity.this, "Row" + (finalR1 + 1) + "has no correct option", Toast.LENGTH_LONG).show();
                                            return;

                                        }
                                    });
                                    return;
                                }

                            } else {
                                final int finalR = r;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //loadingtext.setText("Loading....");
                                        loadingdialog.dismiss();
                                        Toast.makeText(QuestionActivity.this, "Row no" + (finalR + 1) + "has incorrect data", Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                              //  loadingtext.setText("uploading.....");
                                FirebaseDatabase.getInstance().getReference()
                                        .child("SETS").child(setid).updateChildren(parentMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //add all templist data in mainlist
                                            list.addAll(templist);
                                            adapter.notifyDataSetChanged();

                                        } else {
                                           // loadingtext.setText("Loading....");
                                            loadingdialog.dismiss();
                                            Toast.makeText(QuestionActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                                        }
                                        loadingdialog.dismiss();

                                    }
                                });

                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                               // loadingtext.setText("Loading....");
                                loadingdialog.dismiss();
                                Toast.makeText(QuestionActivity.this, "File is empty", Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    }

                }catch (final FileNotFoundException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                          //  loadingtext.setText("Loading....");
                            Toast.makeText(QuestionActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                     catch (final IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //loadingtext.setText("Loading....");
                            Toast.makeText(QuestionActivity.this, e
                                    .getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
    }

    private String getCellData(Row row, int cellposition, FormulaEvaluator formulaEvaluator)
    {
        String value ="";
        Cell cell = row.getCell(cellposition);
        switch(cell.getCellType())
        {
            case Cell.CELL_TYPE_BOOLEAN:
                return value + cell.getBooleanCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                return value +cell.getNumericCellValue();
            case Cell.CELL_TYPE_STRING:
                return value + cell.getStringCellValue();
            default: return value;
        }
    }
}