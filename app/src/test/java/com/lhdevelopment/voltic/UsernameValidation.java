package com.lhdevelopment.voltic;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class UsernameValidation {

    @Test
    public void testIsValidUsername() {
        // Corrección de nombres de usuario válidos
        Assert.assertTrue(isValidUsername("user123"));
        Assert.assertTrue(isValidUsername("user@name"));
        Assert.assertTrue(isValidUsername("user#name"));
        Assert.assertTrue(isValidUsername("username"));

        // Corrección de nombres de usuario no válidos
        Assert.assertFalse(isValidUsername("us"));
        Assert.assertFalse(isValidUsername("12345678901"));
        Assert.assertFalse(isValidUsername("user name"));
    }
    public static boolean isValidUsername(String username) {
        Pattern usernamePattern = Pattern.compile(
                "^[A-Za-z][A-Za-z0-9@#\\$%^&+=!]{4,11}$"
        );
        return usernamePattern.matcher(username).matches();
    }
}

