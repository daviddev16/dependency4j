package com.dependency4j;

import java.util.*;

public class Main {

    public static void main(String[] args) {


        Class<MongoDBRepository> mongoDBRepositoryClass = MongoDBRepository.class;

        MongoDBRepository mongoDBRepository = new MongoDBRepository();
        PostgreSQLRepository postgreSQLRepository = new PostgreSQLRepository();
        DependencySearchTree dependencySearchTree = new DependencySearchTree();

        dependencySearchTree.insert(mongoDBRepository);
        dependencySearchTree.insert(postgreSQLRepository);
        dependencySearchTree.insert(new CassandraDBRepository());

        D4JUtil.printDependencySearchTree(dependencySearchTree);

        A postgreSQLRepository1 = dependencySearchTree.query(TstAbs.class, QueryOptions
                .builder()
                .build());

        System.err.println(postgreSQLRepository1.getClass().getSimpleName());

    }

    public static String toString(Set<SingletonNode> st) {
        StringJoiner jn = new StringJoiner(", ");
        st.stream().forEach(s -> jn.add(s.getNodeName()));
        return "[" + jn.toString().strip() + "]";
    }

}
