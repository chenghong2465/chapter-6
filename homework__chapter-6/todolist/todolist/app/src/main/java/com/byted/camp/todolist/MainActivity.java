package com.byted.camp.todolist;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;

    private TodoDbHelper mTodoHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(ContextCompat.checkSelfPermission(this,"android.permission.READ_EXTERNAL_STORAGE")!=0 &&
                ContextCompat.checkSelfPermission(this,"android.permission.WRITE_EXTERNAL_STORAGE")!=0){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }
        }

        mTodoHelper = new TodoDbHelper(getBaseContext());
        db = mTodoHelper.getReadableDatabase();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        mTodoHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans

        if(db == null) return Collections.emptyList();
        List<Note> result = new LinkedList<>();
        String[] projection = {
                BaseColumns._ID,
                TodoContract.TodoEntry.COLUMN_NAME_CONTENT,
                TodoContract.TodoEntry.COLUMN_NAME_DATE,
                TodoContract.TodoEntry.COLUMN_NAME_STATE
        };
        Cursor cursor = null;
        try{
            cursor = db.query(TodoContract.TodoEntry.TABLE_NAME,
                    projection, null, null,
                    null,null,
                    TodoContract.TodoEntry.COLUMN_NAME_DATE + " DESC");
            while(cursor.moveToNext()){
                String content = cursor.getString(cursor.getColumnIndex(
                        TodoContract.TodoEntry.COLUMN_NAME_CONTENT));
                long dateM = cursor.getLong(cursor.getColumnIndex(
                        TodoContract.TodoEntry.COLUMN_NAME_DATE));
                int state = cursor.getInt(cursor.getColumnIndex(
                        TodoContract.TodoEntry.COLUMN_NAME_STATE));
                long id = cursor.getLong(cursor.getColumnIndex(
                        TodoContract.TodoEntry._ID));
                Note note = new Note(id);
                note.setContent(content);
                note.setDate(new Date(dateM));
                note.setState(State.from(state));
                result.add(note);
            }
        }
        finally {
            if(cursor!=null) cursor.close();
        }
        //String selection = TodoContract.TodoEntry.

        return result;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        String selection = TodoContract.TodoEntry._ID + " LIKE ?";
        String[] arg = {String.valueOf(note.id)};

        if(db == null) return ;
        else {
            int deleteRow = db.delete(TodoContract.TodoEntry.TABLE_NAME,
                    selection,arg);
            if(deleteRow!=-1){
                NoteListAdapter na = (NoteListAdapter) recyclerView.getAdapter();
                na.refresh(loadNotesFromDatabase());
            }
            else{

            }
        }


    }

    private void updateNode(Note note) {
        // 更新数据
        int newState = note.getState().intValue;
        ContentValues value = new ContentValues();
        value.put(TodoContract.TodoEntry.COLUMN_NAME_STATE,newState);

        String selection = TodoContract.TodoEntry._ID + " LIKE ?";
        String[] arg = {String.valueOf(note.id)};

        int n = db.update(
                TodoContract.TodoEntry.TABLE_NAME,
                value,selection,arg);
        if(n!=-1){
            NoteListAdapter na = (NoteListAdapter) recyclerView.getAdapter();
            na.refresh(loadNotesFromDatabase());
        }

    }
}
