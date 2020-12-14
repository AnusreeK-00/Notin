
        package com.example.notin.Student;

        import android.Manifest;
        import android.annotation.SuppressLint;
        import android.app.Activity;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Color;
        import android.graphics.drawable.GradientDrawable;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.provider.MediaStore;
        import android.provider.Settings;
        import android.speech.RecognitionListener;
        import android.speech.RecognizerIntent;
        import android.speech.SpeechRecognizer;
        import android.util.Log;
        import android.view.MotionEvent;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.SeekBar;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.content.ContextCompat;
        import androidx.core.content.FileProvider;

        import com.example.notin.BuildConfig;
        import com.example.notin.R;
        import com.example.notin.database.NotesDatabase;
        import com.example.notin.entities.Note;
        import com.google.android.material.bottomsheet.BottomSheetBehavior;

        import java.io.File;
        import java.io.IOException;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.Locale;

        import static android.view.MotionEvent.*;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText inputNoteTitle, inputNoteText;
    private TextView textDateTime;

    private Note alreadyAvailableNote;

    //Add camera btn
    ImageView camera;

    //Seek bar for text size
    SeekBar seekBar;
    TextView txtSeekBar;
    int textSize = 1;
    int saveProgress;

    //For camera
    static final int REQUEST_IMAGE_CAPTURE=100;
    static final int IMAGE_DISPLAY=22;

    int i=1;
    private View viewTitle;
    private String selectedColor;

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        checkPermission();


        //for carousel of images
        // ViewPager viewPager=findViewById(R.id.viewpager);
        //  ImageAdapter adapter2=new ImageAdapter(this);
        //  viewPager.setAdapter(adapter2);

        //back button
        ImageView imageBack = findViewById(R.id.ImageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onBackPressed();
                startActivity(new Intent(CreateNoteActivity.this,MainActivity.class));
            }
        });

        //--scroll desc
        EditText et = findViewById(R.id.description_text);

        et.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                if (view.getId() == R.id.description_text) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & ACTION_MASK) {
                        case ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });


        //back button
        // ImageView imageBack = findViewById(R.id.ImageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onBackPressed();
                startActivity(new Intent(CreateNoteActivity.this,MainActivity.class));
            }
        });

        inputNoteTitle = findViewById(R.id.Create_Title);
        inputNoteText = findViewById(R.id.description_text);
        textDateTime = findViewById(R.id.DateTime);
        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date())
        );


        camera = findViewById(R.id.camera_btn);
        Button save = findViewById(R.id.Save_btn);


        viewTitle = findViewById(R.id.NoteColorIndicator);
        //For seek bar
        txtSeekBar = (TextView) findViewById(R.id.description_text);
        txtSeekBar.setTextScaleX(textSize);
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textSize = textSize + (progress - saveProgress);
                saveProgress = progress;
                txtSeekBar.setTextSize(textSize);
            }
        });


        //Camera BUTTON FUNCTION
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dispatchTakePictureIntent();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //IT CLEARS TEXT FOR NOW ON CLICKING SAVE
                /*
                TextView desctext = findViewById(R.id.description_text);
                TextView Title=findViewById(R.id.Create_Title);

                Title.setText("");
                desctext.setText("");
                */
                saveNote();
            }
        });

        //Speech to text

        final SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());

        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                //getting all the matches
                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //displaying the first match
                if (matches != null)
                    inputNoteText.setText(inputNoteText.getText()+"\n"+matches.get(0)+"\n");
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });



        findViewById(R.id.audio_btn).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case ACTION_UP:
                        mSpeechRecognizer.stopListening();
                        inputNoteText.setHint("You will see input here");
                        break;

                    case ACTION_DOWN:
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        //inputNoteText.setText("");
                        inputNoteText.setHint("Listening...");
                        break;
                }
                return false;
            }
        });


        selectedColor = "#808080";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
        initMiscellaneous();
        setTitleIndicatorColor();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {



        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            displayImage();
        }
    }

    private void setViewOrUpdateNote() {
        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNoteText.setText(alreadyAvailableNote.getNoteText());
        displayImage();
    }

    private void saveNote() {
        //validations
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note title can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        } else if (inputNoteText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        //saving to db
        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setColor(selectedColor);


        //getting id of new note from already available note,as OnConflictStrategy is set to "replace" in noteDao,hence it will be updated
        if (alreadyAvailableNote != null) {
            note.setId(alreadyAvailableNote.getId());
        }

        //use async task to save in room db

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteTask().execute();
    }

    public void initMiscellaneous() {
        final LinearLayout layoutMisc = findViewById(R.id.layout_Miscellaneous);
        final BottomSheetBehavior BottomSheet = BottomSheetBehavior.from(layoutMisc);
        layoutMisc.findViewById(R.id.MiscText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BottomSheet.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    BottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    BottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        //TO change color of note
        final ImageView imageColor1 = layoutMisc.findViewById(R.id.imageColor1);
        final ImageView imageColor2 = layoutMisc.findViewById(R.id.imageColor2);
        final ImageView imageColor3 = layoutMisc.findViewById(R.id.imageColor3);
        final ImageView imageColor4 = layoutMisc.findViewById(R.id.imageColor4);

        layoutMisc.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#808080";
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                setTitleIndicatorColor();
            }
        });

        layoutMisc.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#3a52fc";
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor1.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                setTitleIndicatorColor();
            }
        });
        layoutMisc.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#fd8e38";
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor4.setImageResource(0);
                setTitleIndicatorColor();
            }
        });
        layoutMisc.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#ff4842";
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor1.setImageResource(0);
                setTitleIndicatorColor();
            }
        });

        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()) {
            switch (alreadyAvailableNote.getColor()) {
                case "#808080":
                    layoutMisc.findViewById(R.id.viewColor1).performClick();
                    break;
                case "#3a52fc":
                    layoutMisc.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#fd8e38":
                    layoutMisc.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#ff4842":
                    layoutMisc.findViewById(R.id.viewColor4).performClick();

            }
        }

    }

    //Function to change note color
    private void setTitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewTitle.getBackground();
        gradientDrawable.setColor((Color.parseColor(selectedColor)));
    }

    //To display saved photos
    protected void displayImage(){


        String ExternalStorageDirectoryPath = Environment
               .getExternalStorageDirectory()
                .getAbsolutePath();

       // String targetPath = ExternalStorageDirectoryPath + "/DCIM/App";
        String targetPath = ExternalStorageDirectoryPath + "/DCIM/Notin/"+inputNoteTitle.getText().toString();
        ArrayList<String> images = new ArrayList<String>();
        File targetDirector = new File(targetPath);
        LinearLayout layout = (LinearLayout) findViewById(R.id.imageLayout);
        layout.removeAllViews();
        File[] files = targetDirector.listFiles();


        for (File file : files) {

            String filepath = file.getAbsolutePath();
            Bitmap bmp = BitmapFactory.decodeFile(filepath);

            ImageView image = new ImageView(this);
            image.setLayoutParams(new android.view.ViewGroup.LayoutParams(1000, 1000));
            image.setMaxHeight(500);
            image.setMaxWidth(500);
            // Adds the view to the layout
            layout.addView(image);

            image.setImageBitmap(bmp);
        }
    }

    //Set up the camera intent
    private void dispatchTakePictureIntent() throws InterruptedException {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter a Title pls!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      //  startActivityForResult(takePictureIntent, IMAGE_DISPLAY);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {


            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this,"THis is not working!",
                        Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID +".provider",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);


            }

        }
    }



    //Create Image File
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir= new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM),"Notin/"+inputNoteTitle.getText().toString());
        if(!storageDir.exists()){

            boolean s = new File(storageDir.getPath()).mkdirs();

            if(!s){
                Toast.makeText(this,"NOt created!",
                        Toast.LENGTH_SHORT).show();
                Log.v("not", "not created");
            }
            else{
                Log.v("cr","directory created");
            }
        }
        else{
            Log.v("directory", "directory exists");
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String currentPhotoPath = image.getAbsolutePath();
        Uri uriOfImage = Uri.parse(image.getPath());
        return image;

    }
}

