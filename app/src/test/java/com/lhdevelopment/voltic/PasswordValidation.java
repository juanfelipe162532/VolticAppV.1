package com.lhdevelopment.voltic;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class PasswordValidation {

    @Test
    public void testIsValidPassword() {
        // Corrección de contraseñas válidas
        Assert.assertTrue(isValidPassword("P@ssw0rd"));
        Assert.assertTrue(isValidPassword("Secure1!"));
        Assert.assertTrue(isValidPassword("Password123!"));

        // Corrección de contraseñas no válidas
        Assert.assertFalse(isValidPassword("password"));
        Assert.assertFalse(isValidPassword("PASSWORD123"));
        Assert.assertFalse(isValidPassword("pass1234"));
        Assert.assertFalse(isValidPassword("1234!@"));
    }

    private boolean isValidPassword(String password) {
        Pattern passwordPattern = Pattern.compile(
                "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\\$%^&+=!]).{8,12}$"
        );
        return passwordPattern.matcher(password).matches();
    }
}

