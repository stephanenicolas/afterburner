package com.github.stephanenicolas.afterburner;

import static org.junit.Assert.*;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;
import com.github.stephanenicolas.afterburner.inserts.InsertableConstructor;

public class InsertableConstructorBuilderTest {

    private InsertableConstructorBuilder builder;
    private AfterBurner afterBurnerMock;

    @Before
    public void setUp() {
        afterBurnerMock = EasyMock.createNiceMock(AfterBurner.class);
        builder = new InsertableConstructorBuilder(afterBurnerMock);
    }

    @Test
    public void testDoIt_calls_afterburner() throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        //GIVEN
        afterBurnerMock = EasyMock.createMock(AfterBurner.class);
        afterBurnerMock.insertConstructor((InsertableConstructor) EasyMock.anyObject());
        EasyMock.replay(afterBurnerMock);

        builder = new InsertableConstructorBuilder(afterBurnerMock);

        CtClass classToInsertInto = CtClass.intType;
        String body = "";

        //WHEN
        builder
            .insertIntoClass(classToInsertInto)
            .withBody(body)
            .doIt();

        //THEN
        EasyMock.verify(afterBurnerMock);
    }

    @Test
    public void testCheckAllFields_should_succeed_with_all_fields_defined() throws AfterBurnerImpossibleException {
        //GIVEN
        CtClass classToInsertInto = CtClass.intType;
        String body = "";

        //WHEN
        InsertableConstructor constructor = builder
                .insertIntoClass(classToInsertInto)
                .withBody(body)
                .createInsertableConstructor();

        //THEN
        assertNotNull(constructor);
        assertEquals(classToInsertInto, constructor.getClassToInsertInto());
        assertEquals(body, constructor.getConstructorBody(null));
        assertTrue(constructor.acceptParameters(null));
    }

    @Test
    public void testCheckAllFields_should_succeed_with_all_fields_defined_using_class() throws Exception {
        //GIVEN
        String body = "";
        Class<?> classToInsertInto = String.class;
        ;

        //WHEN
        InsertableConstructor constructor = builder
                .insertIntoClass(classToInsertInto )
                .withBody(body)
                .createInsertableConstructor();

        //THEN
        assertNotNull(constructor);
        assertEquals(ClassPool.getDefault().get(String.class.getName()), constructor.getClassToInsertInto());
        assertEquals(body, constructor.getConstructorBody(null));
        assertTrue(constructor.acceptParameters(null));
    }


    @Test(expected = AfterBurnerImpossibleException.class)
    public void testCheckAllFields_should_throw_exceptions_if_no_body_defined() throws AfterBurnerImpossibleException {
        //GIVEN
        CtClass classToInsertInto = CtClass.intType;
        builder.insertIntoClass(classToInsertInto);

        //WHEN
        builder.checkFields();

        //THEN
        fail("Should have thrown exception");
    }

}
