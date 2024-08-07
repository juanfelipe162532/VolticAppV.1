package com.lhdevelopment.voltic;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class EmailValidation {

    @Test
    public void testIsValidEmail() {
        // Corrección de emails válidos
        Assert.assertTrue(isValidEmail("user@gmail.com"));
        Assert.assertTrue(isValidEmail("user@hotmail.com"));
        Assert.assertTrue(isValidEmail("user@yahoo.com"));
        Assert.assertTrue(isValidEmail("user@yahoo.es"));
        Assert.assertTrue(isValidEmail("user@outlook.com"));

        // Corrección de emails no válidos
        Assert.assertFalse(isValidEmail("user@invalid.com"));
        Assert.assertFalse(isValidEmail("user@gmail"));
        Assert.assertFalse(isValidEmail("user@.com"));
        Assert.assertFalse(isValidEmail("user.com"));
    }

    private boolean isValidEmail(String email) {
        Pattern emailPattern = Pattern.compile(
                "^[A-Za-z0-9._%+-]+@(hotmail\\.com|gmail\\.com|yahoo\\.com|yahoo\\.es|outlook\\.com)$"
        );
        return emailPattern.matcher(email).matches();
    }
}

