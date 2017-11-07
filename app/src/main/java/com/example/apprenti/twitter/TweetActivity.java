package com.example.apprenti.twitter;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TweetActivity extends AppCompatActivity {

    private EditText input_name,input_details, input_date;
    private ListView list_data;
    private ProgressBar circular_progress;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private List<TweetModel> list_events = new ArrayList<>();

    private TweetModel selectedEvent = null; //hold event when we select item in listView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        //Add Toolbar
        Toolbar toolbar =(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Formulaire
        input_name = (EditText)findViewById(R.id.name);
        input_details = (EditText)findViewById(R.id.details);
        input_date = (EditText)findViewById(R.id.date);

        //Liste d'events
        list_data = (ListView)findViewById(R.id.list_data);
        list_data.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TweetModel event = (TweetModel) adapterView.getItemAtPosition(i);
                selectedEvent = event;
                input_name.setText(event.getName());
                input_details.setText(event.getDetails());
                input_date.setText(event.getDate());
            }
        });

        input_date.setFocusable(false);

        //Date Picker
        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                input_date.setText(sdf.format(myCalendar.getTime()));
            }
        };

        input_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog date =
                        new DatePickerDialog(TweetActivity.this, datePickerListener,
                                myCalendar.get(Calendar.YEAR),
                                myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH));
                date.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);//Désactive les jours précédents
                date.show();

            }
        });

        //Firebase
        initFirebase();
        addEventFirebaseListener();
    }

    private void initFirebase() {
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
    }

    private void addEventFirebaseListener() {

        mDatabaseReference.child("events").orderByChild("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (list_events.size() > 0)
                    list_events.clear();
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    TweetModel event = postSnapshot.getValue(TweetModel.class);
                    list_events.add(event);
                }
                ListTweetAdapter adapter = new ListTweetAdapter(TweetActivity.this,list_events);
                list_data.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Clic on item menu (toolbar)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add)
        {
            createEvent();
        }
        else if(item.getItemId() == R.id.menu_save)
        {
            if (selectedEvent != null){
                TweetModel event = new TweetModel(selectedEvent.getEid(),input_name.getText().toString(),
                        input_details.getText().toString(), input_date.getText().toString());
                updateEvent(event);
            }

        }
        else if(item.getItemId() == R.id.menu_remove)
        {
            deleteEvent(selectedEvent);
        }
        return true;
    }

    //Create Event
    private void createEvent() {
        if (input_name.getText().toString().isEmpty() || input_details.getText().toString().isEmpty() || input_date.getText().toString().isEmpty()) {
            Toast.makeText(TweetActivity.this, getResources().getString(R.string.messToast), Toast.LENGTH_SHORT).show();
        } else {
            TweetModel event = new TweetModel(UUID.randomUUID().toString(), input_name.getText().toString(),
                    input_details.getText().toString(), input_date.getText().toString());
            mDatabaseReference.child("events").child(event.getEid()).setValue(event);
            clearEditText();
        }
    }

    //Update Event
    private void updateEvent(TweetModel event) {
        if (input_name.getText().toString().isEmpty() || input_details.getText().toString().isEmpty() || input_date.getText().toString().isEmpty()) {
            Toast.makeText(TweetActivity.this, getResources().getString(R.string.messToast), Toast.LENGTH_SHORT).show();
        } else {
            mDatabaseReference.child("events").child(event.getEid()).child("name").setValue(event.getName());
            mDatabaseReference.child("events").child(event.getEid()).child("details").setValue(event.getDetails());
            mDatabaseReference.child("events").child(event.getEid()).child("date").setValue(event.getDate());
            clearEditText();
        }
    }

    //Delete Event
    private void deleteEvent(TweetModel selectedEvent) {
        if (input_name.getText().toString().isEmpty() || input_details.getText().toString().isEmpty() || input_date.getText().toString().isEmpty()) {
            Toast.makeText(TweetActivity.this, getResources().getString(R.string.messToast), Toast.LENGTH_SHORT).show();
        } else {
            mDatabaseReference.child("events").child(selectedEvent.getEid()).removeValue();
            clearEditText();
        }
    }

    private void clearEditText() {
        input_name.setText("");
        input_details.setText("");
        input_date.setText("");
    }
}
