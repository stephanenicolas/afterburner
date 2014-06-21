package com.github.stephanenicolas.afterburner.inserts;

import com.github.stephanenicolas.afterburner.AfterBurner;
import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public abstract class InsertableMethod extends Insertable {

    public InsertableMethod(CtClass classToInsertInto) {
        super(classToInsertInto);
    }

    public String getInsertionBeforeMethod() {
        return null;
    }

    public String getInsertionAfterMethod() {
        return null;
    }

    public abstract String getFullMethod() throws AfterBurnerImpossibleException;

    public abstract String getBody() throws AfterBurnerImpossibleException;

    public abstract String getTargetMethodName() throws AfterBurnerImpossibleException;

    public static class Builder {

        private static final String BODY_TAG = "==BODY==";
        private String targetMethod;
        private CtClass classToInsertInto;
        protected String fullMethod;
        protected String body;
        protected String insertionBeforeMethod;
        protected String insertionAfterMethod;
        private AfterBurner afterBurner;
        private SignatureExtractor signatureExtractor;

        public Builder(AfterBurner afterBurner) {
            this(afterBurner, null);
        }

        public Builder(AfterBurner afterBurner, SignatureExtractor signatureExtractor) {
            this.afterBurner = afterBurner;
            this.signatureExtractor = signatureExtractor;
        }

        public Builder insertIntoClass(Class<?> clazzToInsertInto) throws NotFoundException {
            this.classToInsertInto = ClassPool.getDefault().get(clazzToInsertInto.getName());
            return this;
        }

        public Builder insertIntoClass(CtClass classToInsertInto) {
            this.classToInsertInto = classToInsertInto;
            return this;
        }

        public Builder inMethodIfExists(String targetMethod) {
            this.targetMethod = targetMethod;
            return this;
        }

        public Builder beforeACallTo(String insertionBeforeMethod) {
            this.insertionBeforeMethod = insertionBeforeMethod;
            return this;
        }

        public Builder afterACallTo(String insertionAfterMethod) {
            this.insertionAfterMethod = insertionAfterMethod;
            return this;
        }

        public Builder withBody(String body) {
            this.body = body;
            return this;
        }

        public Builder elseCreateMethodIfNotExists(String fullMethod) {
            this.fullMethod = fullMethod;
            return this;
        }
        
        public Builder afterOverrideMethod(String targetMethod) throws NotFoundException {
            this.targetMethod = targetMethod;
            this.insertionAfterMethod = targetMethod;
            CtMethod overridenMethod = classToInsertInto.getDeclaredMethod(targetMethod);
            fullMethod = signatureExtractor.extractSignature(overridenMethod) + " { \n"
                    + signatureExtractor.invokeSuper(overridenMethod) + "\n"
                    + BODY_TAG + "}\n";

            return this;
        }

        public void doIt() throws CannotCompileException,
                AfterBurnerImpossibleException {

            InsertableMethod method = createInsertableMethod();
            afterBurner.addOrInsertMethod(method);
        }

        private void doInsertBodyInFullMethod() {
            if (fullMethod != null) {
                fullMethod = fullMethod.replace(BODY_TAG, body);
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
}
