package com.github.stephanenicolas.afterburner.inserts;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.github.stephanenicolas.afterburner.AfterBurner;
import com.github.stephanenicolas.afterburner.TestCounter;
import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;

public class InsertableMethodBuilderTest {

    private InsertableMethod.Builder builder;
    private SignatureExtractor signatureExtractorMock;
    private AfterBurner afterBurnerMock;
    
    @Before
    public void setUp() {
        afterBurnerMock = EasyMock.createNiceMock(AfterBurner.class);
        builder = new InsertableMethod.Builder(afterBurnerMock, null);
    }
    
    @Test
    public void testDoIt_calls_afterburner() throws CannotCompileException, AfterBurnerImpossibleException {
        //GIVEN
        afterBurnerMock = EasyMock.createMock(AfterBurner.class);
        afterBurnerMock.addOrInsertMethod((InsertableMethod) EasyMock.anyObject());
        EasyMock.replay(afterBurnerMock);

        builder = new InsertableMethod.Builder(afterBurnerMock, null);

        CtClass classToInsertInto = CtClass.intType;
        String targetMethod = "";
        String insertionAfterMethod = "";
        String fullMethod = "";
        String body = "";

        //WHEN
        builder
            .insertIntoClass(classToInsertInto)
            .inMethodIfExists(targetMethod)
            .afterACallTo(insertionAfterMethod)
            .withBody(body)
            .elseCreateMethodIfNotExists(fullMethod);

        builder.doIt();

        //THEN
        EasyMock.verify(afterBurnerMock);
    }

    @Test
    public void testDoIt_calls_afterburner_with_override() throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        //GIVEN
        afterBurnerMock = EasyMock.createMock(AfterBurner.class);
        afterBurnerMock.addOrInsertMethod((InsertableMethod) EasyMock.anyObject());
        EasyMock.replay(afterBurnerMock);
        signatureExtractorMock = EasyMock.createMock(SignatureExtractor.class);
        EasyMock.expect(signatureExtractorMock.invokeSuper((CtMethod) EasyMock.anyObject())).andReturn("super.foo()");
        EasyMock.expect(signatureExtractorMock.extractSignature((CtMethod) EasyMock.anyObject())).andReturn("public void foo()");
        EasyMock.replay(signatureExtractorMock);

        CtClass targetClassAncestor = ClassPool.getDefault().makeClass(
                "TargetAncestor" + TestCounter.testCounter++);
        targetClassAncestor.addMethod(CtNewMethod.make("public void foo() { }", targetClassAncestor));

        CtClass targetClass = ClassPool.getDefault().makeClass(
                "Target" + TestCounter.testCounter++);
        targetClass.setSuperclass(targetClassAncestor);
        targetClass.addMethod(CtNewMethod.make("public void foo() { super.foo(); }", targetClass));

        //WHEN
        builder = new InsertableMethod.Builder(afterBurnerMock, signatureExtractorMock);

        CtClass classToInsertInto = targetClass;
        String targetMethod = "foo";
        String body = "";
        builder
            .insertIntoClass(classToInsertInto)
            .afterOverrideMethod(targetMethod)
            .withBody(body);

        builder.doIt();

        //THEN
        EasyMock.verify(afterBurnerMock);
    }

    @Test
    public void testCheckAllFields_should_succeed_with_insert_after_method_defined() throws AfterBurnerImpossibleException {
        //GIVEN
        CtClass classToInsertInto = CtClass.intType;
        String targetMethod = "";
        String insertionAfterMethod = "";
        String fullMethod = "";
        String body = "";
        builder
            .insertIntoClass(classToInsertInto)
            .inMethodIfExists(targetMethod)
            .afterACallTo(insertionAfterMethod)
            .withBody(body)
            .elseCreateMethodIfNotExists(fullMethod);

        //WHEN
        InsertableMethod method = builder.createInsertableMethod();

        //THEN
        assertNotNull(method);
    }

    @Test
    public void testCheckAllFields_should_succeed_with_insert_before_method_defined() throws AfterBurnerImpossibleException {
        //GIVEN
        CtClass classToInsertInto = CtClass.intType;
        String targetMethod = "";
        String insertionBeforeMethod = "";
        String fullMethod = "";
        String body = "";
        builder
            .insertIntoClass(classToInsertInto)
            .inMethodIfExists(targetMethod)
            .beforeACallTo(insertionBeforeMethod)
            .withBody(body)
            .elseCreateMethodIfNotExists(fullMethod);

        //WHEN
        InsertableMethod method = builder.createInsertableMethod();

        //THEN
        assertNotNull(method);
    }

    @Test(expected = AfterBurnerImpossibleException.class)
    public void testCheckAllFields_should_throw_exceptions_if_no_class_defined() throws AfterBurnerImpossibleException {
        //GIVEN
        String targetMethod = "";
        String insertionAfterMethod = "";
        String fullMethod = "";
        String body = "";
        builder
            .inMethodIfExists(targetMethod)
            .afterACallTo(insertionAfterMethod)
            .withBody(body)
            .elseCreateMethodIfNotExists(fullMethod);

        //WHEN
        builder.checkFields();

        //THEN
        fail("Should have thrown exception");
    }

    @Test(expected = AfterBurnerImpossibleException.class)
    public void testCheckAllFields_should_throw_exceptions_if_no_targetMethod_defined() throws AfterBurnerImpossibleException {
        //GIVEN
        CtClass classToInsertInto = CtClass.intType;
        String insertionAfterMethod = "";
        String fullMethod = "";
        String body = "";
        builder
            .insertIntoClass(classToInsertInto)
            .afterACallTo(insertionAfterMethod)
            .withBody(body)
            .elseCreateMethodIfNotExists(fullMethod);

        //WHEN
        builder.checkFields();

        //THEN
        fail("Should have thrown exception");
    }

    @Test(expected = AfterBurnerImpossibleException.class)
    public void testCheckAllFields_should_throw_exceptions_if_no_insertion_defined() throws AfterBurnerImpossibleException {
        //GIVEN
        CtClass classToInsertInto = CtClass.intType;
        String targetMethod = "";
        String fullMethod = "";
        String body = "";
        builder
            .insertIntoClass(classToInsertInto)
            .inMethodIfExists(targetMethod)
            .withBody(body)
            .elseCreateMethodIfNotExists(fullMethod);

        //WHEN
        builder.checkFields();

        //THEN
        fail("Should have thrown exception");
    }

    @Test(expected = AfterBurnerImpossibleException.class)
    public void testCheckAllFields_should_throw_exceptions_if_no_body_defined() throws AfterBurnerImpossibleException {
        //GIVEN
        CtClass classToInsertInto = CtClass.intType;
        String targetMethod = "";
        String insertionBeforeMethod = "";
        String fullMethod = "";
        builder
            .insertIntoClass(classToInsertInto)
            .inMethodIfExists(targetMethod)
            .beforeACallTo(insertionBeforeMethod)
            .elseCreateMethodIfNotExists(fullMethod);

        //WHEN
        builder.checkFields();

        //THEN
        fail("Should have thrown exception");
    }

    @Test(expected = AfterBurnerImpossibleException.class)
    public void testCheckAllFields_should_throw_exceptions_if_no_full_method_defined() throws AfterBurnerImpossibleException {
        //GIVEN
        CtClass classToInsertInto = CtClass.intType;
        String targetMethod = "";
        String insertionBeforeMethod = "";
        String body = "";
        builder
            .insertIntoClass(classToInsertInto)
            .inMethodIfExists(targetMethod)
            .beforeACallTo(insertionBeforeMethod)
            .withBody(body);

        //WHEN
        builder.checkFields();

        //THEN
        fail("Should have thrown exception");
    }
}
