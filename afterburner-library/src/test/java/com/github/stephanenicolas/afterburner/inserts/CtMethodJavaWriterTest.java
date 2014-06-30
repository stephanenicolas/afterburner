package com.github.stephanenicolas.afterburner.inserts;

import static org.junit.Assert.assertEquals;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.junit.Before;
import org.junit.Test;

import com.github.stephanenicolas.afterburner.TestCounter;

public class CtMethodJavaWriterTest {

    CtMethodJavaWriter signatureExtractor;
    
    @Before
    public void setUp() throws Exception {
        signatureExtractor = new CtMethodJavaWriter();
    }

    @Test
    public void testExtractSignature_simple() throws CannotCompileException, NotFoundException {
        //GIVEN
        CtClass targetClass = ClassPool.getDefault().makeClass(
                "Target" + TestCounter.testCounter++);
        CtMethod fooMethod = CtNewMethod.make("public void foo() { }", targetClass);
        targetClass.addMethod(fooMethod);

        //WHEN
        String extractSignature = signatureExtractor.createJavaSignature(fooMethod);
        
        //THEN
        assertEquals("public void foo()", extractSignature);
    }

    @Test
    public void testExtractSignature_with_return_type() throws CannotCompileException, NotFoundException {
        //GIVEN
        CtClass targetClass = ClassPool.getDefault().makeClass(
                "Target" + TestCounter.testCounter++);
        CtMethod fooMethod = CtNewMethod.make("public int foo() { return 0; }", targetClass);
        targetClass.addMethod(fooMethod);

        //WHEN
        String extractSignature = signatureExtractor.createJavaSignature(fooMethod);

        //THEN
        assertEquals("public int foo()", extractSignature);
    }

    @Test
    public void testExtractSignature_with_params() throws CannotCompileException, NotFoundException {
        //GIVEN
        CtClass targetClass = ClassPool.getDefault().makeClass(
                "Target" + TestCounter.testCounter++);
        CtMethod fooMethod = CtNewMethod.make("public void foo(int a, String b) {}", targetClass);
        targetClass.addMethod(fooMethod);

        //WHEN
        String extractSignature = signatureExtractor.createJavaSignature(fooMethod);

        //THEN
        assertEquals("public void foo(int p0, java.lang.String p1)", extractSignature);
    }

    @Test
    public void testExtractSignature_with_throws() throws CannotCompileException, NotFoundException {
        //GIVEN
        CtClass targetClass = ClassPool.getDefault().makeClass(
                "Target" + TestCounter.testCounter++);
        CtMethod fooMethod = CtNewMethod.make("public void foo() throws Exception, Throwable {}", targetClass);
        targetClass.addMethod(fooMethod);

        //WHEN
        String extractSignature = signatureExtractor.createJavaSignature(fooMethod);

        //THEN
        assertEquals("public void foo() throws java.lang.Exception, java.lang.Throwable", extractSignature);
    }
}
