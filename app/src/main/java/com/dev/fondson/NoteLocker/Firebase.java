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

    static public DatabaseReference getToDoRef() {
        return FirebaseDatabase.getInstance().getReference().child("todo").child(MainActivity.userEmail.replace(".","\"(dot)\""));
    }

    static public DatabaseReference getCompletedRef() {
        return FirebaseDatabase.getInstance().getReference().child("completed").child(MainActivity.userEmail.replace(".","\"(dot)\""));
    }

    static public void writeNewToDoItem(String name, boolean selected){
        String key= MainActivity.toDoDatabase.push().getKey();
        UserItem item = new UserItem(key, name, selected);
        Map<String, Object> itemValues = item.toMap();
        Map<String,Object> childUpdates = new HashMap<>();
        childUpdates.put(key, itemValues);
        MainActivity.toDoDatabase.updateChildren(childUpdates);
    }

    static public void removeToDoItem(String key){
        Map<String,Object> childUpdates = new HashMap<>();
        childUpdates.put(key, null);
        MainActivity.toDoDatabase.updateChildren(childUpdates);
    }

    static public void updateToDoItem(UserItem item) {
        Map<String, Object> itemValues = item.toMap();
        Map<String,Object> childUpdates = new HashMap<>();
        childUpdates.put(item.getKey(), itemValues);
        MainActivity.toDoDatabase.updateChildren(childUpdates);
    }

    static public void writeNewCompletedItem(String name, boolean selected){
        String key= MainActivity.completedDatabase.push().getKey();
        UserItem item = new UserItem(key, name, selected);
        Map<String, Object> itemValues = item.toMap();
        Map<String,Object> childUpdates = new HashMap<>();
        childUpdates.put(key, itemValues);
        MainActivity.completedDatabase.updateChildren(childUpdates);
    }

    static public void removeCompletedItem(String key){
        Map<String,Object> childUpdates = new HashMap<>();
        childUpdates.put(key, null);
        MainActivity.completedDatabase.updateChildren(childUpdates);
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
                        // the auth state listener will be notified and logic to handle the
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
