package com.dev.fondson.NoteLocker;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Fondson on 2016-10-20.
 */

public class Firebase {

    private static FirebaseDatabase firebaseDatabase;
    private static DatabaseReference toDoDatabase;
    private static DatabaseReference completedDatabase;

    static public FirebaseDatabase getDatabaseInstance(){
        if (firebaseDatabase == null){
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);
        }
        return firebaseDatabase;
    }

    static public DatabaseReference getToDoRef() {
        toDoDatabase = firebaseDatabase.getReference().child("todo").child(MainActivity.userEmail.replace(".","\"(dot)\""));
        return toDoDatabase;
    }

    static public DatabaseReference getCompletedRef() {
        completedDatabase = firebaseDatabase.getReference().child("completed").child(MainActivity.userEmail.replace(".","\"(dot)\""));
        return completedDatabase;
    }

    static public String writeNewToDoItem(String name, boolean selected){
        if (toDoDatabase != null) {
            String key = toDoDatabase.push().getKey();
            UserItem item = new UserItem(key, name, selected);
            Map<String, Object> itemValues = item.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(key, itemValues);
            toDoDatabase.updateChildren(childUpdates);
            return key;
        }
        return null;
    }

    static public void removeToDoItem(String key){
        if (toDoDatabase != null) {
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(key, null);
            toDoDatabase.updateChildren(childUpdates);
        }
    }

    static public void updateToDoItem(UserItem item) {
        if (toDoDatabase != null) {
            Map<String, Object> itemValues = item.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(item.getKey(), itemValues);
            toDoDatabase.updateChildren(childUpdates);
        }
    }

    static public void writeNewCompletedItem(String name, boolean selected){
        if (completedDatabase != null) {
            String key = completedDatabase.push().getKey();
            UserItem item = new UserItem(key, name, selected);
            Map<String, Object> itemValues = item.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(key, itemValues);
            completedDatabase.updateChildren(childUpdates);
        }
    }

    static public void removeCompletedItem(String key){
        if (completedDatabase != null) {
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(key, null);
            completedDatabase.updateChildren(childUpdates);
        }
    }

    static public void authWithGoogle(GoogleSignInAccount acct, FirebaseAuth mAuth, final Context context) {
        Log.d("firebasetag", "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("firebasetag", "signInWithCredential:onComplete:" + task.isSuccessful());

                        MainActivity.loggingOut = false;
                        Toast.makeText(context, "Log in successful.",
                                Toast.LENGTH_SHORT).show();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth itemlist_textview listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("firebasetag", "signInWithCredential", task.getException());
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
