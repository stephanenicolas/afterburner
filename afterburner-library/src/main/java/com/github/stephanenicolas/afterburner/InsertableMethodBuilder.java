package com.github.stephanenicolas.afterburner;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;
import com.github.stephanenicolas.afterburner.inserts.CtMethodJavaWriter;
import com.github.stephanenicolas.afterburner.inserts.InsertableMethod;

/**
 * Almost a DSL/builder to ease creating an {@link InsertableMethod}.
 * Needs more intermediate states.
 * @author SNI
 */
public class InsertableMethodBuilder {

    private String targetMethod;
    private CtClass classToInsertInto;
    protected String fullMethod;
    protected String body;
    protected String insertionBeforeMethod;
    protected String insertionAfterMethod;
    private AfterBurner afterBurner;
    private CtMethodJavaWriter signatureExtractor;

    public InsertableMethodBuilder(AfterBurner afterBurner) {
        this(afterBurner, null);
    }

    public InsertableMethodBuilder(AfterBurner afterBurner, CtMethodJavaWriter signatureExtractor) {
        this.afterBurner = afterBurner;
        this.signatureExtractor = signatureExtractor;
    }

    public InsertableMethodBuilder insertIntoClass(Class<?> clazzToInsertInto) throws NotFoundException {
        this.classToInsertInto = ClassPool.getDefault().get(clazzToInsertInto.getName());
        return this;
    }

    public InsertableMethodBuilder insertIntoClass(CtClass classToInsertInto) {
        this.classToInsertInto = classToInsertInto;
        return this;
    }

    public InsertableMethodBuilder inMethodIfExists(String targetMethod) {
        this.targetMethod = targetMethod;
        return this;
    }

    public InsertableMethodBuilder beforeACallTo(String insertionBeforeMethod) {
        this.insertionBeforeMethod = insertionBeforeMethod;
        return this;
    }

    public InsertableMethodBuilder afterACallTo(String insertionAfterMethod) {
        this.insertionAfterMethod = insertionAfterMethod;
        return this;
    }

    public InsertableMethodBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    public InsertableMethodBuilder elseCreateMethodIfNotExists(String fullMethod) {
        this.fullMethod = fullMethod;
        return this;
    }

    public InsertableMethodBuilder beforeOverrideMethod(String targetMethod) throws NotFoundException {
        this.targetMethod = targetMethod;
        this.insertionBeforeMethod = targetMethod;
        CtMethod overridenMethod = classToInsertInto.getDeclaredMethod(targetMethod);
        fullMethod = signatureExtractor.createJavaSignature(overridenMethod) + " { \n"
                + InsertableMethod.BODY_TAG + "\n"
                + signatureExtractor.invokeSuper(overridenMethod) + "}\n";
        return this;
    }

    public InsertableMethodBuilder afterOverrideMethod(String targetMethod) throws NotFoundException {
        this.targetMethod = targetMethod;
        this.insertionAfterMethod = targetMethod;
        CtMethod overridenMethod = classToInsertInto.getDeclaredMethod(targetMethod);
        fullMethod = signatureExtractor.createJavaSignature(overridenMethod) + " { \n"
                + signatureExtractor.invokeSuper(overridenMethod) + "\n"
                + InsertableMethod.BODY_TAG + "}\n";
        return this;
    }
    
    public void doIt() throws CannotCompileException,
    AfterBurnerImpossibleException {

        InsertableMethod method = createInsertableMethod();
        afterBurner.addOrInsertMethod(method);
    }

    private void doInsertBodyInFullMethod() {
        if (fullMethod != null) {
            fullMethod = fullMethod.replace(InsertableMethod.BODY_TAG, body);
        }
    }

    protected void checkFields() throws AfterBurnerImpossibleException {
        boolean hasInsertionMethod = insertionBeforeMethod != null
                || insertionAfterMethod != null;
        if (classToInsertInto == null || targetMethod == null
                || !hasInsertionMethod || body == null
                || fullMethod == null) {
            throw new AfterBurnerImpossibleException(
                    "Builder was not used as intended. A field is null.");
        }
    }

    public InsertableMethod createInsertableMethod() throws AfterBurnerImpossibleException {
        checkFields();
        doInsertBodyInFullMethod();

        InsertableMethod method = new InsertableMethod(classToInsertInto) {
            @Override
            public String getFullMethod() {
                return fullMethod;
            }

            @Override
            public String getBody() {
                return body;
            }

            @Override
            public String getTargetMethodName() throws AfterBurnerImpossibleException {
                return targetMethod;
            }

            @Override
            public String getInsertionBeforeMethod() {
                return insertionBeforeMethod;
            }

            @Override
            public String getInsertionAfterMethod() {
                return insertionAfterMethod;
            }
        };
        return method;
    }
}
