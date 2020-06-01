package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;

import java.util.Date;
import java.util.List;

public class NoteActivity extends AppCompatActivity {

    private EditText editText;
    private Button addBtn;
    private TodoDbHelper mTodoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.take_a_note);
        mTodoHelper = new TodoDbHelper(getBaseContext());
        editText = findViewById(R.id.edit_text);
        editText.setFocusable(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }

        addBtn = findViewById(R.id.btn_add);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = saveNote2Database(content.toString().trim());
                if (succeed) {
                    Toast.makeText(NoteActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(NoteActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTodoHelper.close();
    }

    private boolean saveNote2Database(String content) {

        SQLiteDatabase db = mTodoHelper.getWritableDatabase();
        ContentValues value = new ContentValues();

        value.put(TodoContract.TodoEntry.COLUMN_NAME_STATE,0);
        value.put(TodoContract.TodoEntry.COLUMN_NAME_CONTENT,content);
        value.put(TodoContract.TodoEntry.COLUMN_NAME_DATE,new Date().getTime());
        long newRowId = db.insert(TodoContract.TodoEntry.TABLE_NAME,null,value);
        if(newRowId == -1) return false;
        else return true;

    }
}
