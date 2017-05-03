package com.example.mypc.cloudtask;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListTasksActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;//ссылка на данные в нашей базе данных
    FirebaseUser user = mAuth.getInstance().getCurrentUser();//получаем текущего пользователя
    FirebaseListAdapter mAdapter;

    ListView listViewTask;
    private Button btnNewTask;
    private EditText etNewTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_tasks);

        listViewTask = (ListView) findViewById(R.id.list_view_user_tasks);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();//получаем экземляр FirebaseDatabase и из него ссылку на базу данных
        mAdapter = new FirebaseListAdapter<String>(this, String.class, android.R.layout.simple_list_item_1, mDatabaseReference.child(user.getUid()).child("Tasks")) {//mDatabaseReference.child(user.getUid()).child("Tasks") - находим ссылку на бд, сначала по пользователю, затем его задачи
            @Override
            protected void populateView(View v, String s, int position) {//position - позиция в файле макета
                //метод будет вызываться каждый раз когда данные в нашем списки будут изменены
                //в этом методе мы устанавливаем новые данные для изменения нашего списка
                TextView text = (TextView) v.findViewById(android.R.id.text1);
                text.setText(s);
            }
        };
        listViewTask.setAdapter(mAdapter);
        etNewTask = (EditText) findViewById(R.id.edit_text_new_task);
        btnNewTask = (Button) findViewById(R.id.btn_new_task);
        btnNewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseReference.child(user.getUid()).child("Tasks").push().setValue(etNewTask.getText().toString());
            }
        });
    }
}
