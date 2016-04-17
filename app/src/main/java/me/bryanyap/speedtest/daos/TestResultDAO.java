package me.bryanyap.speedtest.daos;

import me.bryanyap.speedtest.models.TestResult;

public interface TestResultDao {
    /**
     * Write to TestResult to Firebase server.
     * @param testResult
     * @return success
     */
    boolean write(TestResult testResult);

    /**
     * Authenticate with Firebase server using email and password.
     * @param email
     * @param password
     * @return success
     */
    boolean authenticate(String email, String password);

    /**
     * Check if device is authenticated with Firebase server.
     * @return authenticated
     */
    boolean isAuthenticated();
}
