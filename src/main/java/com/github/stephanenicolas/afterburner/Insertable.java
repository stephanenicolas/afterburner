package com.github.stephanenicolas.afterburner;

import javassist.CtClass;

public class Insertable {

    private CtClass classToInsertInto;

    public Insertable(CtClass classToInsertInto) {
        this.classToInsertInto = classToInsertInto;
    }
    
    public CtClass getClassToInsertInto() {
        return classToInsertInto;
    }

}
