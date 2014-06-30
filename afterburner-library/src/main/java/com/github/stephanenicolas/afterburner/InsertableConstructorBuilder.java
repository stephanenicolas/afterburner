package com.github.stephanenicolas.afterburner;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;
import com.github.stephanenicolas.afterburner.inserts.InsertableConstructor;

/**
 * Almost a DSL/builder to ease creating an {@link InsertableConstructor}.
 * Needs more intermediate states.
 * @author SNI
 */
public class InsertableConstructorBuilder {

    private CtClass classToInsertInto;
    protected String body;
    private AfterBurner afterBurner;
    
    public InsertableConstructorBuilder(AfterBurner afterBurner) {
        this.afterBurner = afterBurner;
    }

    public InsertableConstructorBuilder insertIntoClass(Class<?> clazzToInsertInto) throws NotFoundException {
        this.classToInsertInto = ClassPool.getDefault().get(clazzToInsertInto.getName());
        return this;
    }

    public InsertableConstructorBuilder insertIntoClass(CtClass clazzToInsertInto) {
        this.classToInsertInto = clazzToInsertInto;
        return this;
    }

    public InsertableConstructorBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    public void doIt() throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        InsertableConstructor method = createInsertableConstructor();
        afterBurner.insertConstructor(method);
    }

    protected void checkFields() throws AfterBurnerImpossibleException {
        if (classToInsertInto == null || body == null) {
            throw new AfterBurnerImpossibleException(
                    "Builder was not used as intended. A field is null.");
        }
    }

    public InsertableConstructor createInsertableConstructor() throws AfterBurnerImpossibleException {
        checkFields();

        InsertableConstructor constructor = new InsertableConstructor(classToInsertInto) {

            @Override
            public String getConstructorBody(CtClass[] paramClasses) throws AfterBurnerImpossibleException {
                return body;
            }

            @Override
            public boolean acceptParameters(CtClass[] paramClasses) throws AfterBurnerImpossibleException {
                return true;
            }
        };
        return constructor;
    }
}
