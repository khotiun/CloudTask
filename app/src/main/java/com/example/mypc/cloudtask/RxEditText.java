package com.example.mypc.cloudtask;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by hotun on 23.09.2017.
 */

public class RxEditText {

    //Слушает когда данные в эдит текст изменяются
    public static Observable<String> getTextWatcherObservable(@NonNull final EditText editText){

        ///когда данные получены в PublishSubject он отдает их всем кто подписан на него в данный момент
        final PublishSubject<String> subject = PublishSubject.create();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //рассылаем данные из эдит текст
                subject.onNext(s.toString());
            }
        });
        return subject;
    }
}
