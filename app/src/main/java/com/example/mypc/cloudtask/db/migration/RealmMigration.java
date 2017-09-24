package com.example.mypc.cloudtask.db.migration;

import io.realm.DynamicRealm;
import io.realm.RealmSchema;

/**
 * Created by hotun on 23.09.2017.
 */
//класс нужен для превращения java класса realmMigration
public class RealmMigration implements io.realm.RealmMigration{
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        //получаем схему нашей бд
        RealmSchema schema = realm.getSchema();

        //создаем изминение в бд
        if (oldVersion == 0){
            //создаем модель задачи
            //Task - имя сущности которую создаем
            schema.create("TaskRealmModel")
                    //добавим одно поле наш заголовок
                    .addField("title", String.class);
            //увеличиваем версию
            oldVersion++;
        }
    }
}
