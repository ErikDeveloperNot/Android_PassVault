package com.erikdeveloper.passvault;

import android.util.Log;

import com.passvault.util.DefaultRandomPasswordGenerator;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;


/**
 * Created by erik.manor on 5/16/17.
 */

public class AndroidDefaultRandomPasswordGenerator extends DefaultRandomPasswordGenerator {

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

		/*
		System.out.println("Algorithm: " + ((SecureRandom)random).getAlgorithm() + ",  Provider: " +
				((SecureRandom)random).getProvider().getName() + ", " + ((SecureRandom)random).getProvider().getInfo() +
				", " + ((SecureRandom)random).getProvider().getVersion());
		*/

        //IntStream is = random.ints();
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
