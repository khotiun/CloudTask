package com.example.mypc.cloudtask.history;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.mypc.cloudtask.R;
import com.example.mypc.cloudtask.db.DBservice;
import com.example.mypc.cloudtask.db.models.TaskRealmModel;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private HistoryRVAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Realm.init(this);
        DBservice dBservice = new DBservice();

        mAdapter = new HistoryRVAdapter(this);

        //получаем список задач
        dBservice.getAll(TaskRealmModel.class)
                //в подписке получаем весь список realm моделей
                .subscribe(taskRealmModels -> {
                    //список, который будем передавать в адаптер
                    List<String> historyList = new ArrayList<String>();

                    //заполняем список
                    for (TaskRealmModel model : taskRealmModels) {
                        historyList.add(model.getTitle());
                    }
                    //передаем в адаптер наш список
                    mAdapter.setList(historyList);
                });

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_content);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
    }
}
