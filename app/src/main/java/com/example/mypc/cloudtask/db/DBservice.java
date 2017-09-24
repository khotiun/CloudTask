package com.example.mypc.cloudtask.db;

import com.example.mypc.cloudtask.db.migration.RealmMigration;
import com.example.mypc.cloudtask.db.models.TaskRealmModel;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;

import rx.Observable;

/**
 * Created by hotun on 23.09.2017.
 */
//находятся наши запросы к бд
public class DBservice {

    //RealmConfiguration - с пмощью этого обьекта запускается миграция бд при ее изменении
    private RealmConfiguration mConfig = new RealmConfiguration.Builder()
            .schemaVersion(1)//указываем текущую версию бд
            .migration(new RealmMigration())//запускаем миграцию создаем обьект
            .build();

    //функция сохранения записи
    //функция возвращает типизированый Observable на который мы в следствии будем подписываться
    //функция принимает обьект для сохранения и класс этого обьекта
    public <T extends RealmObject> Observable<T> save(T object, Class<T> clazz) {
        Realm realm = Realm.getInstance(mConfig);

        long id;

        try {
            id = realm.where(clazz).max("id").intValue() + 1;
        } catch (Exception e) {
            id = 0L;
        }

        ((TaskRealmModel) object).setId(id);

        //Observable - испускает данные, и к нему можно приминять все rx операторы
        return Observable.just(object)
                .flatMap(t -> Observable.just(t)//flatMap - возьмет все переданное в нее и привратит в Observable, t - это обьект нашего класса
                        .doOnSubscribe(realm::beginTransaction)//перед подпиской начинаем транзакцию в realm
                        .doOnUnsubscribe(() -> {//после подписки комитим транзакцию
                            realm.commitTransaction();
                            realm.close();//realm - закрываем
                        })
                        .doOnNext(realm::copyToRealm)//передаем подписчикам на нас данные, copyToRealm - метод для сохранения данных в бд
                );
    }

    //функция возврата всех данных из бд
    //функция возвращает Observable типизированы й листом наших Realm обьектов
    //на вход функция получает класс тех обьектов, которые мы хотим получить
    public <T extends RealmObject> Observable<List<T>> getAll (Class<T> clazz) {
        Realm realm = Realm.getInstance(mConfig);

        return Observable.just(clazz)
                .flatMap(t-> Observable.just(t)
                        .doOnSubscribe(realm::beginTransaction)
                        .doOnUnsubscribe(() -> {
                            realm.commitTransaction();
                            realm.close();
                        })
                        .map(type -> realm.where(type).findAll())//для возврата листа с нашими realm обджектами, type - обьектов каких мы хотим получить, realm.where - запрос на поиск всех элементов данного типа
                );
    }
}
