package com.github.stephanenicolas.afterburner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import javassist.CannotCompileException;
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
            .withBody(body);
        builder.doIt();

        //THEN
        EasyMock.verify(afterBurnerMock);
    }

    @Test
    public void testCheckAllFields_should_succeed_with_all_fields_defined() throws AfterBurnerImpossibleException {
        //GIVEN
        CtClass classToInsertInto = CtClass.intType;
        String body = "";
        builder
            .insertIntoClass(classToInsertInto)
            .withBody(body);

        //WHEN
        InsertableConstructor constructor = builder.createInsertableConstructor();

        //THEN
        assertNotNull(constructor);
    }

    @Test(expected = AfterBurnerImpossibleException.class)
    public void testCheckAllFields_should_throw_exceptions_if_no_targetClass_defined() throws AfterBurnerImpossibleException {
        //GIVEN
        builder.withBody("");

        //WHEN
        builder.checkFields();

        //THEN
        fail("Should have thrown exception");
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
