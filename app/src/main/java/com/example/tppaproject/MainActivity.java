package com.example.tppaproject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.chinalwb.are.AREditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SaveDialog.DialogListener{

    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final int READ_REQUEST_CODE = 42;
    private static final String TAG = MainActivity.class.getSimpleName();
   // EditText editText;

    AREditor arEditor;
    String LoadFileName = "";

    Bundle savedInstanceSt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceSt = savedInstanceState;
        setContentView(R.layout.activity_main);
        Log.i(TAG, "in method onCreate");

        arEditor = this.findViewById(R.id.areditor);
        arEditor.setExpandMode(AREditor.ExpandMode.FULL);
        arEditor.setHideToolbar(false);
        arEditor.setToolbarAlignment(AREditor.ToolbarAlignment.BOTTOM);


       // editText = findViewById(R.id.editText);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "in method onStart");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.i(TAG, "in method onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "in method onPause");
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "in method onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "in method onDestroy");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("SavedText",arEditor.getHtml());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        arEditor.fromHtml(savedInstanceState.getString("SavedText"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_STORAGE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission granted!", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this,"Permission denied!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String readFile(String input){
        File file = new File(Environment.getExternalStorageDirectory(), input);
        StringBuilder text = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null){
                text.append(line);
                text.append("\n");
            }
            br.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }

        return text.toString();
    }


    private void performFileSearch(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            if(data != null){
                Uri uri = data.getData();
                String path = null;
                if (uri != null) {
                    path = uri.getPath();
                }
                if (path != null) {
                    path = path.substring(path.indexOf(":") + 1);
                }
                //editText.setText(readFile(path));
                arEditor.fromHtml(readFile(path));
                if (path != null) {
                    LoadFileName = path.substring(path.indexOf("/") + 1);
                }
                Toast.makeText(this, LoadFileName,Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void save(String filename) {


        String State = Environment.getExternalStorageState();
        filename = filename + ".txt";
        if(Environment.MEDIA_MOUNTED.equals(State)){
            File root = Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/APad");
            if(!dir.exists()) dir.mkdir();
            File file = new  File(dir,filename);
            String text = arEditor.getHtml();
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(text.getBytes());
                fileOutputStream.close();
                arEditor.getARE().getText().clear();

                Toast.makeText(getApplicationContext(),"File saved",Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"SD card not found",Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.apad_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.Load:
                arEditor.getARE().getText().clear();
                performFileSearch();
                Toast.makeText(this, "Load selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.Save:
                if(!LoadFileName.equals("")){
                    save(LoadFileName);
                    LoadFileName = "";
                }
                else{
                    openDialog();
                }
                Toast.makeText(this, "Save selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.SaveAs:
                openDialog();
                Toast.makeText(this, "Save As selected", Toast.LENGTH_SHORT).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void applyText(String fileName) {
        save(fileName);
    }

    public void openDialog(){
        SaveDialog saveDialog = new SaveDialog();
        saveDialog.show(getSupportFragmentManager(), "Save As");
    }


}
