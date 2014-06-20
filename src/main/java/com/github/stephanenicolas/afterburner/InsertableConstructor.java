package com.github.stephanenicolas.afterburner;

import javassist.CtClass;

public abstract class InsertableConstructor extends Insertable {

    public InsertableConstructor(CtClass classToInsertInto) {
        super(classToInsertInto);
    }

    public abstract String getConstructorBody(CtClass paramClass) throws AfterBurnerImpossibleException;
    
    public abstract boolean acceptParameters(CtClass[] paramClasses) throws AfterBurnerImpossibleException;

}
