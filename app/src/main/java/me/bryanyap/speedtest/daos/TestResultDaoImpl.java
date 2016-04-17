package me.bryanyap.speedtest.daos;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Date;

import me.bryanyap.speedtest.constants.ApplicationConstants;
import me.bryanyap.speedtest.models.TestResult;

public class TestResultDaoImpl implements TestResultDao, ApplicationConstants {
    private Firebase fb = new Firebase(FIREBASE);

    @Override
    public boolean write(TestResult testResult) {
        Firebase resultsRef = fb.child(RESULTS);
        Firebase newResultRef = resultsRef.push();

        newResultRef.setValue(testResult);
        return false;
    }

    @Override
    public boolean isAuthenticated() {
        AuthData authData = fb.getAuth();
        if (authData == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean authenticate(String email, String password) {
        fb.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {

            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {

            }
        });

        return true;
    }

}
