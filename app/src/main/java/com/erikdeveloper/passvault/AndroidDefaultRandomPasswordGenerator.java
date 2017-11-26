package com.erikdeveloper.passvault;

import android.util.Log;

import com.passvault.util.DefaultRandomPasswordGenerator;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;


/**
 * Created by erik.manor on 5/16/17.
 */

public class AndroidDefaultRandomPasswordGenerator extends DefaultRandomPasswordGenerator implements  Serializable {

    private static final String TAG = "DefaultRandomPasswdGen";

    private AndroidDefaultRandomPasswordGenerator(Integer length, Boolean lower, Boolean upper, Boolean special, Boolean digits) {
        super(length, lower, upper, special, digits);
    }

    public static AndroidDefaultRandomPasswordGenerator getInstance() {
        return new AndroidDefaultRandomPasswordGenerator(32, true, true, true, true);
    }


    public static AndroidDefaultRandomPasswordGenerator getInstance(int length, boolean lower, boolean upper,
                                                                    boolean special, boolean digits) {
        return new AndroidDefaultRandomPasswordGenerator(length, lower, upper, special, digits);
    }


    public AndroidDefaultRandomPasswordGenerator(DefaultRandomPasswordGenerator generator) {
        List<Character> allowedChars = generator.getAllowedCharactres();
        this.clearAllowedCharacters();
        this.setAllowedCharacters(allowedChars);
        this.setCheckDigits(generator.isCheckDigits());
        this.setCheckLower(generator.isCheckLower());
        this.setCheckSpecial(generator.isCheckSpecial());
        this.setCheckUpper(generator.isCheckUpper());
        this.setLength(generator.getLength());
    }


    @Override
    public String generatePassword(int length) {
        StringBuilder stringBuilder = null;
        Random random = null;

        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Not able to use SecureRandom, using Random instead !!!!");
            random = new Random();
        }

        PasswordConstraints constraints = null;

        do {
            constraints = new PasswordConstraints();
            int x = 0;
            stringBuilder = new StringBuilder();

            while (++x <= length) {
                int next=random.nextInt();
                char checkChar = allowedCharacters.get(Math.abs(next%allowedCharacters.size()));
                stringBuilder.append(checkChar);
                constraints.checkValue(checkChar);
            }

        } while (!constraints.constraintsEnforced());

        return stringBuilder.toString();
    }
}
