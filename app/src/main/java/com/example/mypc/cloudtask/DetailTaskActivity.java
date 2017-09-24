package com.example.mypc.cloudtask;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailTaskActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mIVpicture;

    private Button mBTNaddPicture;

    private File mTempPhoto; //фаил для сохранения фото с камеры

    private String mImageUri = "";//Uri файла

    private String mReference = "";//ключ, который мы получили со списка

    private static final int REQUEST_CODE_PERMISSION_RECEIVE_CAMERA = 102;//константа для работы с камерой
    private static final int REQUEST_CODE_TAKE_PHOTO = 103;//константв для работы с галереей
    final private String TAG = "DetailTaskActivityTag";
    final private String TAM = "vvvvvvvv";
    private StorageReference mStorageRef;//ссылка на файловое хранилище фаербейс

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_task);
        mIVpicture = (ImageView) findViewById(R.id.iv_piture);
        mBTNaddPicture = (Button) findViewById(R.id.btn_add_picture);
        mBTNaddPicture.setOnClickListener(this);

        //так как мы хотим что бы фотография грузилась сразу из нашего хранилища при создании активити, мы должны выполнить загрузку из нашего файлового хранилища
        File localFile = null;

        mReference = getIntent().getStringExtra("Reference");//получение ключа по нашей задаче
        mStorageRef = FirebaseStorage.getInstance().getReference();//инициализация ссылки на Storage

        try {
            localFile = createTempImageFile(getExternalCacheDir());//(getExternalCacheDir() - получим обсолютный путь к директории нашего приложения
            final File finalLocalFile = localFile;
            mStorageRef.child("images/" + mReference).getFile(localFile)//получим фаил из папки images с именем mRefereces
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {//слушатель успешно выполненых задач
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG, finalLocalFile.toString());

                            Picasso.with(DetailTaskActivity.this)
                                    .load(Uri.fromFile(finalLocalFile))//fromFile - получим Uri из только что скаченого файла
                                    .into(mIVpicture);

                        }
                    }).addOnFailureListener(new OnFailureListener() {//слушатель для ошибок
                @Override
                public void onFailure(@NonNull Exception e) {//если при загрузке файла будет ошибка
                    Log.i("LoadonFailure", e.getMessage());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Метод для добавления фото
    private void addPhoto() {
        Log.d(TAM, "addPhoto");
        //Проверяем разрешение на работу с камерой
        boolean isCameraPermissionGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        //Проверяем разрешение на работу с внешнем хранилещем телефона
        boolean isWritePermissionGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        //Если разрешения != true
        if (!isCameraPermissionGranted || !isWritePermissionGranted) {

            String[] permissions;//Разрешения которые хотим запросить у пользователя

            if (!isCameraPermissionGranted && !isWritePermissionGranted) {
                Log.d(TAM, "!isCameraPermissionGranted && !isWritePermissionGranted");
                permissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            } else if (!isCameraPermissionGranted) {
                Log.d(TAM, "!isCameraPermissionGranted");
                permissions = new String[]{android.Manifest.permission.CAMERA};
            } else {
                Log.d(TAM, "!isWritePermissionGranted");
                permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
            //Запрашиваем разрешения у пользователя
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION_RECEIVE_CAMERA);
        } else {
            //Если все разрешения получены
            try {
                Log.d(TAM, "else");
                mTempPhoto = createTempImageFile(getExternalCacheDir());//(getExternalCacheDir() - получим обсолютный путь до директории нашего приложения
                Log.d(TAM, mTempPhoto.toString());
                mImageUri = mTempPhoto.getAbsolutePath();
                Log.d(TAM, mImageUri.toString());

                //Создаём лист с интентами для работы с изображениями
                List<Intent> intentList = new ArrayList<>();
                Intent chooserIntent = null;


                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //Принцип ACTION_PICK заключается в том, чтобы запустить активность, отображающую список элементов. После этого активность должна предоставлять пользователю возможность выбора элемента из этого списка. Когда пользователь выберет элемент,
                // активность возвратит URI выбранного элемента вызывающей стороне. Таким образом, можно многократно использовать функцию UI для выбора нескольких элементов определенного типа.
                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//стандартный интент для вызова камеры

                takePhotoIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempPhoto));

                intentList = addIntentsToList(this, intentList, pickIntent);
                intentList = addIntentsToList(this, intentList, takePhotoIntent);

                if (!intentList.isEmpty()) {
                    chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1), "Choose your image source");
                    //Intent.createChooser() создает диалог выбора соответствующего приложения.
                    // Если необходимо разшарить медиа контент (изображение/видео), то нужно указать соответствующий тип контента в методе setType, например mailIntent.setType(«image/png»);, а также добавить сам контент в extra интента:
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
                }

                /*После того как пользователь закончит работу с приложеним(которое работает с изображениями)
                 будет вызван метод onActivityResult
                */
                Log.d(TAM, "finish" + mTempPhoto.toString());
                startActivityForResult(chooserIntent, REQUEST_CODE_TAKE_PHOTO);
            } catch (IOException e) {
                Log.e("ERROR", e.getMessage(), e);
            }
        }
    }

    //Получаем абсолютный путь файла из Uri
    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        //Без аннотации компилятор выдаст предупреждение о том, что локальная переменная s не используется. С аннотацией компилятор игнорирует это предупреждение для локального модуля foo. При этом данное предупреждение по-прежнему будет выдаваться для других методов этого же модуля компиляции или проекта.
        //deprecation - отключение предупреждений, связанных с устареванием
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int columnIndex = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

    /*
      File storageDir -  абсолютный путь к каталогу конкретного приложения на
      основном общем /внешнем устройстве хранения, где приложение может размещать
      файлы кеша, которыми он владеет.
     */
    public static File createTempImageFile(File storageDir) throws IOException {

        // Генерируем имя файла
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());//получаем время
        String imageFileName = "photo_" + timeStamp;//состовляем имя файла

        //Создаём файл
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    /*
    Метод для добавления интента в лист интентов
    */
    public static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_add_picture) {
            addPhoto();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAM, " onActivityResult");
        switch (requestCode) {
            case REQUEST_CODE_TAKE_PHOTO://если было выбрано какое-то фото
                if (resultCode == RESULT_OK) {
                    if (data != null && data.getData() != null) {//если фото было выбрано из галереи
                        mImageUri = getRealPathFromURI(data.getData());//получаем Uri из пришедших данных
                        Log.d(TAM, "Uri    " + mImageUri);
                        Log.d(TAM, mTempPhoto.toString());

                        Picasso.with(getBaseContext())//сетим изображение в ImageView
                                .load(data.getData())
                                .into(mIVpicture);
                        uploadFileInFireBaseStorage(data.getData());//задача на загрузку нашего изображения
                    } else if (mImageUri != null) {//если фото было сделано спомощью камеры
                        if (mTempPhoto == null) {//вообще не понятно почему так
                            Log.d(TAM, "null");
                            Log.d(TAM, "111" + mImageUri);
                        }
                        mImageUri = Uri.fromFile(mTempPhoto).toString();//получим Uri в которое пишется изображение снятое с камеры

                        Picasso.with(this)
                                .load(mImageUri)
                                .into(mIVpicture);
                        uploadFileInFireBaseStorage(Uri.fromFile((mTempPhoto)));//задача на загрузку нашего изображения в cloudStorage
                    }
                }
                break;
        }
    }


    private void uploadFileInFireBaseStorage(Uri uri) {
        UploadTask uploadTask = mStorageRef.child("images/" + mReference).putFile(uri);//создадим задачу загрузки по определенной ссылки, и изображение будет иметь имя нашей задачи
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {//запустим нашу задачу по загрузке изображения, addOnProgressListener - слушательпрогресса загрузки
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred());
                Log.i("Load", "Upload is " + progress + "% done");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {//слушатель успешной загрузки файлв
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri donwoldUri = taskSnapshot.getMetadata().getDownloadUrl();// Uri только что загруженного файла
                Log.i("Load", "Uri donwlod" + donwoldUri);
            }
        });
    }
}
