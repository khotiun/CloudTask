package com.example.mypc.cloudtask;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func2;

public class EmailPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText etEmail, etPassword;
    private Button btnSignIn, btnRegistration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();//инициализация обьекта
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            //слушает состояние пользователя,
            //пользователь либо вошел в систему либо вышел
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();//получаем текущего пользователя
                if (user != null) {
                    // User is signed in, пользователь вошел
                    Intent intent = new Intent(EmailPasswordActivity.this, ListTasksActivity.class);//при авторизации пользователя запускается активность с задачами
                    startActivity(intent);
                } else {
                    // User is signed out, пользователь вышел

                }
            }
        };
        etEmail = (EditText) findViewById(R.id.et_email);
        etPassword = (EditText) findViewById(R.id.et_password);
        btnSignIn = (Button) findViewById(R.id.btn_sign_in);
        btnSignIn.setOnClickListener(this);
        btnRegistration = (Button) findViewById(R.id.btn_registration);
        btnRegistration.setOnClickListener(this);

        FirebaseUser user = mAuth.getCurrentUser();//получаем текущего пользователя
        if (user != null) {
            // User is signed in, пользователь вошел
            Intent intent = new Intent(EmailPasswordActivity.this, ListTasksActivity.class);
            startActivity(intent);
        }

        btnSignIn.setEnabled(false);
        btnRegistration.setEnabled(false);

        //излучаю данные используя RxEditText
        Observable<String> emailObservable = RxEditText.getTextWatcherObservable(etEmail);
        Observable<String> passwordObservable = RxEditText.getTextWatcherObservable(etPassword);
        //combineLatest - служит для обьединения Observable
        Observable.combineLatest(emailObservable, passwordObservable, new Func2<String, String, Boolean>() {
            @Override
            public Boolean call(String s, String s2) {//приходят строка эмейла и пароля
                //если значения пусты возвращаем фолс
                if (s.isEmpty() || s2.isEmpty()) {

                    return false;
                } else return true;
            }
        }).subscribe(new Action1<Boolean>() {//добавление подписчика, в котором будем блакировать наши кнопки регестрации и авторизации в зависимости от значения валидации
            @Override
            public void call(Boolean aBoolean) {
                btnSignIn.setEnabled(aBoolean);
                btnRegistration.setEnabled(aBoolean);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_sign_in) {
            signInAccount(etEmail.getText().toString(), etPassword.getText().toString());

        } else if (v.getId() == R.id.btn_registration) {
            registrationAccount(etEmail.getText().toString(), etPassword.getText().toString());
        }
    }

    public void signInAccount(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {//слушатель выполненого входа
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {//отрабатывает при попытке авторизации
                if (task.isSuccessful()) {
                    Toast.makeText(EmailPasswordActivity.this, "Авторизация успешна", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EmailPasswordActivity.this, "Авторизация провалена", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void registrationAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(EmailPasswordActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EmailPasswordActivity.this, "Регистрация провалена", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
