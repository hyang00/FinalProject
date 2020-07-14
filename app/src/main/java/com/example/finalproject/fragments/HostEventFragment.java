package com.example.finalproject.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.models.Event;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;


public class HostEventFragment extends Fragment {

    public static final  String TAG = "Event Fragment";

    private EditText etTitle;
    private EditText etDescription;
    private EditText etAddress;
    private EditText etDate;
    private EditText etTime;
    private Button btnPost;

    FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;


    public HostEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_host_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        etAddress = view.findViewById(R.id.etAddress);
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        btnPost = view.findViewById(R.id.btnPost);

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = etTitle.getText().toString();
                String description = etDescription.getText().toString();
                String address = etAddress.getText().toString();
                String date = etDate.getText().toString();
                String time = etTime.getText().toString();
                if (description.isEmpty()){
                    Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                postEvent(uid, title, description, address, time);
            }
        });
    }
    public void postEvent(String author, String title, String description, String location, String time){
        Event event = new Event(author, title, description, location, time);
        String key = mDatabase.child("Posts").push().getKey();
        mDatabase.child("Posts").child(key).setValue(event).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "posted successfully");
            }
        });
    }
}