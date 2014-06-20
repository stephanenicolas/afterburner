package com.github.stephanenicolas.afterburner;

import javassist.CannotCompileException;
import javassist.CtClass;

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

        private String targetMethod;
        private CtClass classToInsertInto;
        protected String fullMethod;
        protected String body;
        protected String insertionBeforeMethod;
        protected String insertionAfterMethod;
        private AfterBurner afterBurner;

        public Builder(AfterBurner afterBurner) {
            this.afterBurner = afterBurner;
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

        public void doIt() throws CannotCompileException,
                AfterBurnerImpossibleException {
            boolean hasInsertionMethod = insertionBeforeMethod != null
                    || insertionAfterMethod != null;
            if (classToInsertInto == null || targetMethod == null
                    || !hasInsertionMethod || body == null
                    || fullMethod == null) {
                throw new AfterBurnerImpossibleException(
                        "Builder was not used as intended. A field is null.");
            }

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
            afterBurner.addOrInsertMethod(method);
        }
    }
}
