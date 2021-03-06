package com.example.coordinator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity implements PostAdapter.OnCardClickListener, DialogCategory.GategoryListener {
    FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private Query query;
    private DialogCategory dialogCategory;
    private PostAdapter adapter;
    private String choosen;
    private final int LIMIT = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            signInAnonymously();
        }
        initCaller();
    }

    private void initCaller() {
        choosen = "Main";
        initFirestore();
        setUpPostAdapter();
        FloatingActionButton button_post = findViewById(R.id.button_floating_post);
        button_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPost();
            }
        });
        setUpFloatCategory();
    }

    private void initFirestore() {
        firestore = FirebaseFirestore.getInstance();
        query = firestore.collection("posts17_2")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(LIMIT);
    }

    private void startPost() {
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, PostActivity.class);
            startActivity(intent);
        }
        else {
            DialogNotSigned dialogNotSigned = new DialogNotSigned();
            dialogNotSigned.show(getSupportFragmentManager(), "Not signed");
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("success", "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("fail", "signInAnonymously:failure", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onCardClick(DocumentSnapshot documentSnapshot) {
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra("document_id", documentSnapshot.getId());
        if (documentSnapshot.getLong("color") != null) {
            intent.putExtra("post_color", documentSnapshot.getLong("color").intValue());
        }
        else intent.putExtra("post_color", 0);
        startActivity(intent);
    }

    private void setUpPostAdapter() {
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class).build();
        adapter = new PostAdapter(options, this, this);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void changeQuery() {
        if (!choosen.equals("Main")) {
            query = firestore.collection("posts17_2").whereEqualTo("tag", choosen)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(LIMIT);
        }
        else {
            query = firestore.collection("posts17_2")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(LIMIT);
        }
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class).build();
        adapter.updateOptions(options);
    }

    private void setUpChooseCa() {
        dialogCategory = new DialogCategory();
        dialogCategory.show(getSupportFragmentManager(), "Dialog choose");
    }

    private void setUpFloatCategory() {
        FloatingActionButton button = findViewById(R.id.button_floating_category);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpChooseCa();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void listenChange(String type) {
        if (!choosen.equals(type)) {
            choosen = type;
            changeQuery();
        }
    }
}
