package com.github.stephanenicolas.afterburner.inserts;

import static org.junit.Assert.*;
import javassist.CtClass;

import org.junit.Before;
import org.junit.Test;

import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;

public class InsertableMethodTest {

    InsertableMethodUnderTest insertableMethodUnderTest;

    @Before
    public void setUp() throws Exception {
        insertableMethodUnderTest = new InsertableMethodUnderTest(null);
    }

    @Test
    public void testGetInsertionBeforeMethod_returns_null_by_default() {
        //GIVEN

        //WHEN
        String insertionBeforeMethod = insertableMethodUnderTest.getInsertionBeforeMethod();

        //THEN
        assertNull(insertionBeforeMethod);
    }

    @Test
    public void testGetInsertionAfterMethod_returns_null_by_default() {
        //GIVEN

        //WHEN
        String insertionAfterMethod = insertableMethodUnderTest.getInsertionAfterMethod();

        //THEN
        assertNull(insertionAfterMethod);
    }

    private class InsertableMethodUnderTest extends InsertableMethod {

        public InsertableMethodUnderTest(CtClass classToInsertInto) {
            super(classToInsertInto);
        }

        @Override
        public String getFullMethod() throws AfterBurnerImpossibleException {
            return null;
        }

        @Override
        public String getBody() throws AfterBurnerImpossibleException {
            return null;
        }

        @Override
        public String getTargetMethodName() throws AfterBurnerImpossibleException {
            return null;
        }
    }
}
