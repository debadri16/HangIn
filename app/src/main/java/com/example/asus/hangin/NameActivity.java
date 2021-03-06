package com.example.asus.hangin;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NameActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mName;
    private Button mSaveBtn;

    private DatabaseReference mNameDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mNameDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current_uid);

        mToolbar = (Toolbar) findViewById(R.id.name_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Name");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String name_value = getIntent().getStringExtra("name_value");

        mName = (TextInputLayout) findViewById(R.id.name_input);
        mName.getEditText().setText(name_value);

        mSaveBtn = (Button) findViewById(R.id.name_save_btn);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //progress
                mProgress = new ProgressDialog(NameActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we save changes...");
                mProgress.show();

                String name = mName.getEditText().getText().toString();

                mNameDatabase.child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            mProgress.dismiss();
                            Toast.makeText(getApplicationContext(), "Name updated", Toast.LENGTH_LONG).show();

                        }
                        else{

                            Toast.makeText(getApplicationContext(), "There is some error in saving changes", Toast.LENGTH_LONG).show();

                        }

                    }
                });

            }
        });

    }
}
