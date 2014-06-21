package com.github.stephanenicolas.afterburner.inserts;

import com.github.stephanenicolas.afterburner.AfterBurner;
import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Base class of all insertable methods through AfterBurner.
 * Inserts code into a given method. It will inject code using an "insertion point", i.e. 
 * a method call inside the target method, either before or after it.
 * If there is no method to insert into, fully create the target method.
 * Due to limitations in javassist (https://github.com/jboss-javassist/javassist/issues/9),
 * one of the overloads of the target method is chosen arbitrarily to insert code.  
 * @author SNI
 */
public abstract class InsertableMethod extends Insertable {
    public static final String BODY_TAG = "==BODY==";

    public InsertableMethod(CtClass classToInsertInto) {
        super(classToInsertInto);
    }

    public String getInsertionBeforeMethod() {
        return null;
    }

    public String getInsertionAfterMethod() {
        return null;
    }

    /**
     * Return the full method (signature + body) to add to the classToInsertInto.
     * A special mechanism allow to replace the tag #BODY_TAG by the result of #getBody().
     * @return the full method (signature + body) to add to the classToInsertInto.
     * @throws AfterBurnerImpossibleException in case something goes wrong. Wrap all exceptions into it.
     */
    public abstract String getFullMethod() throws AfterBurnerImpossibleException;

    /**
     * Return the java statements to insert.
     * @return the instructions (no signature) to insert;
     * @throws AfterBurnerImpossibleException in case something goes wrong. Wrap all exceptions into it.
     */
    public abstract String getBody() throws AfterBurnerImpossibleException;

    /**
     * Return the name of the method to insert code into.
     * @return the name of the method to insert code into.
     * @throws AfterBurnerImpossibleException in case something goes wrong. Wrap all exceptions into it.
     */
    public abstract String getTargetMethodName() throws AfterBurnerImpossibleException;

    /**
     * Almost a DSL/builder to ease creating an InsertableMethod.
     * Needs more intermediate states.
     * @author SNI
     */
    public static class Builder {

        private String targetMethod;
        private CtClass classToInsertInto;
        protected String fullMethod;
        protected String body;
        protected String insertionBeforeMethod;
        protected String insertionAfterMethod;
        private AfterBurner afterBurner;
        private CtMethodJavaWriter signatureExtractor;

        public Builder(AfterBurner afterBurner) {
            this(afterBurner, null);
        }

        public Builder(AfterBurner afterBurner, CtMethodJavaWriter signatureExtractor) {
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
            fullMethod = signatureExtractor.createJavaSignature(overridenMethod) + " { \n"
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
