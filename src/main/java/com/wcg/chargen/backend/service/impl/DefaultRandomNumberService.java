package com.wcg.chargen.backend.service.impl;

import com.wcg.chargen.backend.service.RandomNumberService;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Simple class for generating random integers.
 *
 * By making this a class, it can be mocked in unit tests, allowing for certain test cases to be run.
 */
@Component
public class DefaultRandomNumberService implements RandomNumberService {
    private final Random rng;

    public DefaultRandomNumberService() {
        rng = new Random();
    }

    @Override
    public int getIntFromRange(int start, int end) {
        return rng.nextInt(start, end + 1);
    }
}
