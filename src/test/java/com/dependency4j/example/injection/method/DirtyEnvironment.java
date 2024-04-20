package com.dependency4j.example.injection.method;

import com.dependency4j.Managed;
import com.dependency4j.Pull;
import com.dependency4j.Strategy;

@Managed(strategy = @Strategy({"dirtyStrategy"}))
public class DirtyEnvironment implements Environment {

    private JavaVersionConfig javaVersionConfig;
    private NonManagedType nonManagedType;

    public DirtyEnvironment() {}

    public @Pull void setDirtyJavaVersionConfig(JavaVersionConfig javaVersionConfig,
                                                NonManagedType nonManagedType)
    {
        this.javaVersionConfig = javaVersionConfig;
        this.nonManagedType    = nonManagedType;

        if (this.nonManagedType == null) /* we are sure that this will be null tho when injection occurs. */
            this.nonManagedType = NonManagedType.DUMMY; /* for test case */

    }

    @Override
    public JavaVersionConfig getJavaVersionConfig() {
        return javaVersionConfig;
    }

    @Override
    public NonManagedType getNonManagedType() {
        return nonManagedType;
    }

}
