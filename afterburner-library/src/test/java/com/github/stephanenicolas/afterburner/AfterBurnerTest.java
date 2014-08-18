package com.github.stephanenicolas.afterburner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;
import com.github.stephanenicolas.afterburner.inserts.InsertableConstructor;
import com.github.stephanenicolas.afterburner.inserts.InsertableMethod;
import com.github.stephanenicolas.afterburner.inserts.SimpleInsertableConstructor;
import com.github.stephanenicolas.afterburner.inserts.SimpleInsertableMethod;

public class AfterBurnerTest {

    private AfterBurner afterBurner;
    private CtClass target;
    private Class<?> targetClass;
    private Object targetInstance;

    @Before
    public void setUp() {
        afterBurner = new AfterBurner();
        target = ClassPool.getDefault().makeClass(
                "Target" + TestCounter.testCounter);
        TestCounter.testCounter++;
    }

    @Test
    public void testAddMethod() throws Exception {
        // GIVEN
        InsertableMethod insertableMethod = new SimpleInsertableMethod(target, "foo", null, null, null, "public boolean foo() { return true; }");

        // WHEN
        afterBurner.addOrInsertMethod(insertableMethod);

        // THEN
        targetClass = target.toClass();
        targetInstance = targetClass.newInstance();
        assertHasFooMethodWithReturnValue(target, true);
    }

    @Test
    public void testInsertMethod_after() throws Exception {
        // GIVEN
        target.addMethod(CtNewMethod.make("public void bar() { }", target));
        target.addMethod(CtNewMethod.make("public boolean foo() { bar(); return false; }", target));
        target.addField(new CtField(CtClass.intType, "foo", target));
        InsertableMethod insertableMethod = new SimpleInsertableMethod(target, "foo", null, "bar", "foo = 2;", null );

        // WHEN
        afterBurner.addOrInsertMethod(insertableMethod);

        // THEN
        targetClass = target.toClass();
        targetInstance = targetClass.newInstance();
        assertHasFooMethodWithReturnValue(target, false);
        assertHasFooFieldWithValue(target, 2);
    }

    @Test
    public void testInsertMethod_before() throws Exception {
        // GIVEN
        target.addMethod(CtNewMethod.make("public void bar() { }", target));
        target.addMethod(CtNewMethod.make("public boolean foo() { bar(); return false; }", target));
        target.addField(new CtField(CtClass.intType, "foo", target));
        InsertableMethod insertableMethod = new SimpleInsertableMethod(target, "foo", "bar", null, "foo = 2;", null);

        // WHEN
        afterBurner.addOrInsertMethod(insertableMethod);

        // THEN
        targetClass = target.toClass();
        targetInstance = targetClass.newInstance();
        assertHasFooMethodWithReturnValue(target, false);
        assertHasFooFieldWithValue(target, 2);
    }

    @Test(expected=AfterBurnerImpossibleException.class)
    public void testInsertMethod_not_before_not_after() throws Exception {
        // GIVEN
        target.addMethod(CtNewMethod.make("public void bar() { }", target));
        target.addMethod(CtNewMethod.make("public boolean foo() { bar(); return false; }", target));
        target.addField(new CtField(CtClass.intType, "foo", target));
        InsertableMethod insertableMethod = new SimpleInsertableMethod(target, "foo", null, null, "foo = 2;", null);

        // WHEN
        afterBurner.addOrInsertMethod(insertableMethod);

        // THEN
        fail();
    }

    @Test
    public void testInsertConstructor() throws Exception {
        // GIVEN
        target.addConstructor(CtNewConstructor.make("public Target() {}", target));
        target.addField(new CtField(CtClass.intType, "foo", target));
        InsertableConstructor insertableConstructor = new SimpleInsertableConstructor(target, "foo = 2;", true);

        // WHEN
        afterBurner.insertConstructor(insertableConstructor);

        // THEN
        targetClass = target.toClass();
        targetInstance = targetClass.newInstance();
        assertHasFooFieldWithValue(target, 2);
    }

    @Test(expected=AfterBurnerImpossibleException.class)
    public void testInsertConstructor_with_no_constructor() throws Exception {
        // GIVEN
        target.addConstructor(CtNewConstructor.make("public Target() {}", target));
        target.addField(new CtField(CtClass.intType, "foo", target));
        InsertableConstructor insertableConstructor = new SimpleInsertableConstructor(target, "foo = 2;", false);

        // WHEN
        afterBurner.insertConstructor(insertableConstructor);

        // THEN
        fail();
    }

    @Test
    public void testBeforeOverride() throws Exception {
        // GIVEN
        target.addMethod(CtNewMethod.make("public void foo() { }", target));
        afterBurner = EasyMock.createMockBuilder(AfterBurner.class).withConstructor().addMockedMethod("addOrInsertMethod").createMock();
        Capture<InsertableMethod> captured = new Capture<InsertableMethod>();
        afterBurner.addOrInsertMethod(EasyMock.capture(captured));
        EasyMock.expectLastCall().once();
        EasyMock.replay(afterBurner);

        String targetMethodName = "foo";
        String body = "foo = 2;";

        // WHEN
        afterBurner.beforeOverrideMethod(target, targetMethodName, body);

        // THEN
        EasyMock.verify(afterBurner);
        assertEquals(body, captured.getValue().getBody());
        assertEquals(targetMethodName, captured.getValue().getInsertionBeforeMethod());
        assertNull(captured.getValue().getInsertionAfterMethod());
        assertEquals(target, captured.getValue().getClassToInsertInto());
    }

    @Test
    public void testAfterOverride() throws Exception {
        // GIVEN
        target.addMethod(CtNewMethod.make("public void foo() { }", target));
        afterBurner = EasyMock.createMockBuilder(AfterBurner.class).withConstructor().addMockedMethod("addOrInsertMethod").createMock();
        Capture<InsertableMethod> captured = new Capture<InsertableMethod>();
        afterBurner.addOrInsertMethod(EasyMock.capture(captured));
        EasyMock.expectLastCall().once();
        EasyMock.replay(afterBurner);

        String targetMethodName = "foo";
        String body = "foo = 2;";

        // WHEN
        afterBurner.afterOverrideMethod(target, targetMethodName, body);

        // THEN
        EasyMock.verify(afterBurner);
        assertEquals(body, captured.getValue().getBody());
        assertNull(captured.getValue().getInsertionBeforeMethod());
        assertEquals(targetMethodName, captured.getValue().getInsertionAfterMethod());
        assertEquals(target, captured.getValue().getClassToInsertInto());
    }

    private void assertHasFooMethodWithReturnValue(CtClass clazz, boolean returnValue) throws Exception {
        CtMethod fooMethod = clazz.getDeclaredMethod("foo");
        assertNotNull(fooMethod);
        // we also need to check if code has been copied

        Method realFooMethod = targetInstance.getClass().getMethod("foo");
        assertEquals(returnValue, realFooMethod.invoke(targetInstance));
    }

    private void assertHasFooFieldWithValue(CtClass clazz, int value) throws Exception {
        CtField fooField = clazz.getField("foo");
        assertNotNull(fooField);
        CtClass fooFieldType = fooField.getType();
        assertEquals(CtClass.intType, fooFieldType);

        Field realFooField = targetInstance.getClass().getDeclaredField("foo");
        realFooField.setAccessible(true);
        assertEquals(value, realFooField.get(targetInstance));
    }

}
