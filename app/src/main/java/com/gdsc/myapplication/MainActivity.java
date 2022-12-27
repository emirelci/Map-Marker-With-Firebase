package com.gdsc.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db;
    EditText Coffename;
    EditText xCord;
    EditText yCord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Coffename = findViewById(R.id.coffename);
        xCord = findViewById(R.id.xcord);
        yCord = findViewById(R.id.ycord);

    }

    public void Mapclicks(View view){
        Intent intent = new Intent(MainActivity.this,MapsActivity.class);
        startActivity(intent);

    }

    public void saveClick(View view){

        if(Coffename.getText() == null || xCord.getText() == null || yCord.getText() == null){

            Toast.makeText(this, "boşlukları doldurunuz!!", Toast.LENGTH_LONG).show();

        }else{

            db = FirebaseFirestore.getInstance();

            HashMap<String, Object> Coffea1 = new HashMap<>();
            Coffea1.put("Caffe name",Coffename.getText().toString());
            Coffea1.put("x",xCord.getText().toString());
            Coffea1.put("y",yCord.getText().toString());

            db.collection("Coffeas").add(Coffea1).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(MainActivity.this, "Başarı ile veri kaydedildi", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "beklenmedik bir hata oldu!", Toast.LENGTH_SHORT).show();
                }
            });

        }


    }

}