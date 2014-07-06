package com.github.stephanenicolas.afterburner;

import static org.junit.Assert.*;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;
import com.github.stephanenicolas.afterburner.inserts.CtMethodJavaWriter;
import com.github.stephanenicolas.afterburner.inserts.InsertableMethod;

public class InsertableMethodBuilderTest {

    private InsertableMethodBuilder builder;
    private CtMethodJavaWriter signatureExtractorMock;
    private AfterBurner afterBurnerMock;

    @Before
    public void setUp() {
        afterBurnerMock = EasyMock.createNiceMock(AfterBurner.class);
        builder = new InsertableMethodBuilder(afterBurnerMock, null);
    }

    @Test
    public void testConstructor_with_afterburner_only() {
        //GIVEN
        builder = new InsertableMethodBuilder(afterBurnerMock);

        //WHEN

        //THEN
        assertNotNull(builder);
    }

    @Test
    public void testDoIt_calls_afterburner() throws CannotCompileException, AfterBurnerImpossibleException {
        //GIVEN
        afterBurnerMock = EasyMock.createMock(AfterBurner.class);
        afterBurnerMock.addOrInsertMethod((InsertableMethod) EasyMock.anyObject());
        EasyMock.replay(afterBurnerMock);

        builder = new InsertableMethodBuilder(afterBurnerMock, null);

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
            .elseCreateMethodIfNotExists(fullMethod)
            .doIt();

        //THEN
        EasyMock.verify(afterBurnerMock);
    }

    @Test
    public void testDoIt_calls_afterburner_with_after_override() throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        //GIVEN
        afterBurnerMock = EasyMock.createMock(AfterBurner.class);
        afterBurnerMock.addOrInsertMethod((InsertableMethod) EasyMock.anyObject());
        EasyMock.replay(afterBurnerMock);
        signatureExtractorMock = EasyMock.createMock(CtMethodJavaWriter.class);
        EasyMock.expect(signatureExtractorMock.invokeSuper((CtMethod) EasyMock.anyObject())).andReturn("super.foo()");
        EasyMock.expect(signatureExtractorMock.createJavaSignature((CtMethod) EasyMock.anyObject())).andReturn("public void foo()");
        EasyMock.replay(signatureExtractorMock);

        CtClass targetClassAncestor = ClassPool.getDefault().makeClass(
                "TargetAncestor" + TestCounter.testCounter++);
        targetClassAncestor.addMethod(CtNewMethod.make("public void foo() { }", targetClassAncestor));

        CtClass targetClass = ClassPool.getDefault().makeClass(
                "Target" + TestCounter.testCounter++);
        targetClass.setSuperclass(targetClassAncestor);
        targetClass.addMethod(CtNewMethod.make("public void foo() { super.foo(); }", targetClass));

        //WHEN
        builder = new InsertableMethodBuilder(afterBurnerMock, signatureExtractorMock);

        CtClass classToInsertInto = targetClass;
        String targetMethod = "foo";
        String body = "";
        builder
            .insertIntoClass(classToInsertInto)
            .afterOverrideMethod(targetMethod)
            .withBody(body)
            .doIt();

        //THEN
        EasyMock.verify(afterBurnerMock);
    }
    
    @Test
    public void testDoIt_calls_afterburner_with_after_override_when_no_override() throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        //GIVEN
        afterBurnerMock = EasyMock.createMock(AfterBurner.class);
        afterBurnerMock.addOrInsertMethod((InsertableMethod) EasyMock.anyObject());
        EasyMock.replay(afterBurnerMock);
        signatureExtractorMock = EasyMock.createMock(CtMethodJavaWriter.class);
        EasyMock.expect(signatureExtractorMock.invokeSuper((CtMethod) EasyMock.anyObject())).andReturn("super.foo()");
        EasyMock.expect(signatureExtractorMock.createJavaSignature((CtMethod) EasyMock.anyObject())).andReturn("public void foo()");
        EasyMock.replay(signatureExtractorMock);

        CtClass targetClassAncestor = ClassPool.getDefault().makeClass(
                "TargetAncestor" + TestCounter.testCounter++);
        targetClassAncestor.addMethod(CtNewMethod.make("public void foo() { }", targetClassAncestor));

        CtClass targetClass = ClassPool.getDefault().makeClass(
                "Target" + TestCounter.testCounter++);
        targetClass.setSuperclass(targetClassAncestor);

        //WHEN
        builder = new InsertableMethodBuilder(afterBurnerMock, signatureExtractorMock);

        CtClass classToInsertInto = targetClass;
        String targetMethod = "foo";
        String body = "";
        builder
            .insertIntoClass(classToInsertInto)
            .afterOverrideMethod(targetMethod)
            .withBody(body)
            .doIt();

        //THEN
        EasyMock.verify(afterBurnerMock);
    }
    
    @Test(expected=NotFoundException.class)
    public void testDoIt_calls_afterburner_with_after_override_when_no_method() throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        //GIVEN
        afterBurnerMock = EasyMock.createMock(AfterBurner.class);

        CtClass targetClass = ClassPool.getDefault().makeClass(
                "Target" + TestCounter.testCounter++);

        //WHEN
        builder = new InsertableMethodBuilder(afterBurnerMock, signatureExtractorMock);

        CtClass classToInsertInto = targetClass;
        String targetMethod = "foo";
        String body = "";

        builder
        .insertIntoClass(classToInsertInto.toClass())
        .afterOverrideMethod(targetMethod)
        .withBody(body)
        .doIt();

        //THEN
        fail();
    }

    @Test
    public void testDoIt_calls_afterburner_with_before_override() throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        //GIVEN
        afterBurnerMock = EasyMock.createMock(AfterBurner.class);
        afterBurnerMock.addOrInsertMethod((InsertableMethod) EasyMock.anyObject());
        EasyMock.replay(afterBurnerMock);
        signatureExtractorMock = EasyMock.createMock(CtMethodJavaWriter.class);
        EasyMock.expect(signatureExtractorMock.invokeSuper((CtMethod) EasyMock.anyObject())).andReturn("super.foo()");
        EasyMock.expect(signatureExtractorMock.createJavaSignature((CtMethod) EasyMock.anyObject())).andReturn("public void foo()");
        EasyMock.replay(signatureExtractorMock);

        CtClass targetClassAncestor = ClassPool.getDefault().makeClass(
                "TargetAncestor" + TestCounter.testCounter++);
        targetClassAncestor.addConstructor(CtNewConstructor.make("public " + targetClassAncestor.getName()+ "() { }", targetClassAncestor));
        targetClassAncestor.addMethod(CtNewMethod.make("public void foo() { }", targetClassAncestor));

        CtClass targetClass = ClassPool.getDefault().makeClass(
                "Target" + TestCounter.testCounter++);
        targetClass.setSuperclass(targetClassAncestor);
        targetClass.addMethod(CtNewMethod.make("public void foo() { super.foo(); }", targetClass));

        targetClassAncestor.toClass();

        //WHEN
        builder = new InsertableMethodBuilder(afterBurnerMock, signatureExtractorMock);

        CtClass classToInsertInto = targetClass;
        String targetMethod = "foo";
        String body = "";

        builder
        .insertIntoClass(classToInsertInto.toClass())
        .beforeOverrideMethod(targetMethod)
        .withBody(body)
        .doIt();

        //THEN
        EasyMock.verify(afterBurnerMock);
    }
    
    @Test
    public void testDoIt_calls_afterburner_with_before_override_when_no_override() throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        //GIVEN
        afterBurnerMock = EasyMock.createMock(AfterBurner.class);
        afterBurnerMock.addOrInsertMethod((InsertableMethod) EasyMock.anyObject());
        EasyMock.replay(afterBurnerMock);
        signatureExtractorMock = EasyMock.createMock(CtMethodJavaWriter.class);
        EasyMock.expect(signatureExtractorMock.invokeSuper((CtMethod) EasyMock.anyObject())).andReturn("super.foo()");
        EasyMock.expect(signatureExtractorMock.createJavaSignature((CtMethod) EasyMock.anyObject())).andReturn("public void foo()");
        EasyMock.replay(signatureExtractorMock);

        CtClass targetClassAncestor = ClassPool.getDefault().makeClass(
                "TargetAncestor" + TestCounter.testCounter++);
        targetClassAncestor.addConstructor(CtNewConstructor.make("public " + targetClassAncestor.getName()+ "() { }", targetClassAncestor));
        targetClassAncestor.addMethod(CtNewMethod.make("public void foo() { }", targetClassAncestor));

        CtClass targetClass = ClassPool.getDefault().makeClass(
                "Target" + TestCounter.testCounter++);
        targetClass.setSuperclass(targetClassAncestor);

        targetClassAncestor.toClass();

        //WHEN
        builder = new InsertableMethodBuilder(afterBurnerMock, signatureExtractorMock);

        CtClass classToInsertInto = targetClass;
        String targetMethod = "foo";
        String body = "";

        builder
        .insertIntoClass(classToInsertInto.toClass())
        .beforeOverrideMethod(targetMethod)
        .withBody(body)
        .doIt();

        //THEN
        EasyMock.verify(afterBurnerMock);
    }
    
    @Test(expected=NotFoundException.class)
    public void testDoIt_calls_afterburner_with_before_override_when_no_method() throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        //GIVEN
        afterBurnerMock = EasyMock.createMock(AfterBurner.class);

        CtClass targetClass = ClassPool.getDefault().makeClass(
                "Target" + TestCounter.testCounter++);

        //WHEN
        builder = new InsertableMethodBuilder(afterBurnerMock, signatureExtractorMock);

        CtClass classToInsertInto = targetClass;
        String targetMethod = "foo";
        String body = "";

        builder
        .insertIntoClass(classToInsertInto.toClass())
        .beforeOverrideMethod(targetMethod)
        .withBody(body)
        .doIt();

        //THEN
        fail();
    }


    @Test
    public void testCheckAllFields_should_succeed_with_insert_after_method_defined() throws AfterBurnerImpossibleException {
        //GIVEN
        CtClass classToInsertInto = CtClass.intType;
        String targetMethod = "";
        String insertionAfterMethod = "";
        String fullMethod = "";
        String body = "";

        //WHEN
        InsertableMethod method = builder
                .insertIntoClass(classToInsertInto)
                .inMethodIfExists(targetMethod)
                .afterACallTo(insertionAfterMethod)
                .withBody(body)
                .elseCreateMethodIfNotExists(fullMethod)
                .createInsertableMethod();

        //THEN
        assertNotNull(method);
        assertEquals(classToInsertInto, method.getClassToInsertInto());
        assertEquals(targetMethod, method.getTargetMethodName());
        assertNull(method.getInsertionBeforeMethod());
        assertEquals(insertionAfterMethod, method.getInsertionAfterMethod());
        assertEquals(fullMethod, method.getFullMethod());
        assertEquals(body, method.getBody());
    }

    @Test
    public void testCheckAllFields_should_succeed_with_insert_before_method_defined() throws AfterBurnerImpossibleException {
        //GIVEN
        CtClass classToInsertInto = CtClass.intType;
        String targetMethod = "target";
        String insertionBeforeMethod = "insertionBeforeMethod";
        String fullMethod = "fullMethod";
        String body = "body";
        

        //WHEN
        InsertableMethod method = builder
                .insertIntoClass(classToInsertInto)
                .inMethodIfExists(targetMethod)
                .beforeACallTo(insertionBeforeMethod)
                .withBody(body)
                .elseCreateMethodIfNotExists(fullMethod)
                .createInsertableMethod();

        //THEN
        assertNotNull(method);
        assertEquals(classToInsertInto, method.getClassToInsertInto());
        assertEquals(targetMethod, method.getTargetMethodName());
        assertEquals(insertionBeforeMethod, method.getInsertionBeforeMethod());
        assertNull(method.getInsertionAfterMethod());
        assertEquals(fullMethod, method.getFullMethod());
        assertEquals(body, method.getBody());
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
