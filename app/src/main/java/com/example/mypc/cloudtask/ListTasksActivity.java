package com.example.mypc.cloudtask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListTasksActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;//ссылка на данные в нашей базе данных
    FirebaseUser user = mAuth.getInstance().getCurrentUser();//получаем текущего пользователя

//    FirebaseListAdapter mAdapter;

    //    ListView listViewTask;
    private Button btnNewTask;
    private EditText etNewTask;

    private static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView mTitleTask;
        Button mDel;

        public TaskViewHolder(View itemView) {
            super(itemView);
            mTitleTask = (TextView) itemView.findViewById(R.id.tv_item);
            mDel = (Button) itemView.findViewById(R.id.btn_item_del);
        }
    }



    //В первом параметре тип нашей модели второй это наш класс вью холдера
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_tasks);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();//получаем экземляр FirebaseDatabase и из него ссылку на базу данных
//        listViewTask = (ListView) findViewById(R.id.list_view_user_tasks);
//        mAdapter = new FirebaseListAdapter<String>(this, String.class, android.R.layout.simple_list_item_1, mDatabaseReference.child(user.getUid()).child("Tasks")) {//mDatabaseReference.child(user.getUid()).child("Tasks") - находим ссылку на бд, сначала по пользователю, затем его задачи
//            @Override
//            protected void populateView(View v, String s, int position) {//position - позиция в файле макета
//                //метод будет вызываться каждый раз когда данные в нашем списки будут изменены
//                //в этом методе мы устанавливаем новые данные для изменения нашего списка
//                TextView text = (TextView) v.findViewById(android.R.id.text1);
//                text.setText(s);
//            }
//        };
//        listViewTask.setAdapter(mAdapter);
        etNewTask = (EditText) findViewById(R.id.edit_text_new_task);
        btnNewTask = (Button) findViewById(R.id.btn_new_task);
        btnNewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseReference.child(user.getUid()).child("Tasks").push().setValue(etNewTask.getText().toString());
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_user_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));//как будут распологатся элементы в списке
        recyclerView.setHasFixedSize(true);

        FirebaseRecyclerAdapter<String, TaskViewHolder> adapter;//адаптер для нашего списка
        adapter = new FirebaseRecyclerAdapter<String, TaskViewHolder>(
                String.class,//модель
                R.layout.item_task,//наш макет
                TaskViewHolder.class,//вью холдер
                mDatabaseReference.child(user.getUid()).child("Tasks")//ссылка на бд
        ) {
            @Override
            protected void populateViewHolder(TaskViewHolder viewHolder, String title, final int position) {
                //будет вызываться при инициализации и при изменении данных по ссылке которую мы указали
                viewHolder.mTitleTask.setText(title);
                viewHolder.mDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference itemRef = getRef(position);//getRef - ссылка на задачу которую нужно удалить
                        itemRef.removeValue();
                    }
                });
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ListTasksActivity.this,DetailTaskActivity.class);
                        intent.putExtra("Reference",getRef(position).getKey().toString());//ключ по которому идентифицируем нашу задачу, понадобится для составления имени изображения
                        startActivity(intent);
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }
}
