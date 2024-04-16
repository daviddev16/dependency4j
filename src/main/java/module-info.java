module dependency4j {

    requires java.base;

    opens example;

    exports example;
    exports com.dependency4j;
    exports com.dependency4j.node;
    exports com.dependency4j.util;

}
