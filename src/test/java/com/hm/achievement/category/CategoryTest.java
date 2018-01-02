package com.hm.achievement.category;

import org.junit.Test;

import static org.junit.Assert.*;

public class CategoryTest {

    /**
     * Test for interface functionality.
     *
     * I wasn't sure how enums interact with interfaces so I made this test.
     * ~ Rsl1122
     */
    @Test
    public void testImplementationFunctionality() {
        Category category = NormalAchievements.ANVILS;
        callMethods(category);

        category = MultipleAchievements.BREAKS;
        callMethods(category);
    }

    private void callMethods(Category category) {
        String[] methodResults = {
                category.toConfigComment(),
                category.toDBName(),
                category.toLangDefault(),
                category.toLangName(),
                category.toPermName()
        };
        for (String result : methodResults) {
            assertNotNull(result);
        }
    }
}