package io.github.dependency4j;

/**
 *
 * An object that implements <b>QueryableProxy</b> acts like a Proxy to the
 * {@link DependencySearchTree}. An example of implementation is {@link DependencyManager},
 * which in the future, can be used to index query results of {@link DependencySearchTree}
 * to optimize user queries.
 *
 * @author daviddev16
 * @version 1.0.4
 *
 **/
public interface QueryableProxy {

    /**
     * Acts like a proxy to {@link DependencySearchTree#query(Class, QueryOptions)}.
     *
     * Given a {@code classType} and {@code queryOptions}, this function should
     * return a correspondent singleton instance of the specified class type.
     * {@code queryOptions} can be used to specify certain criteria to the search.
     * <p>
     * When {@link QueryOptions#retrieveAnyways()} is true, the function will bring
     * the most non-null value, which means that if you specify a filter that searches
     * by the name of the Managed instance, and the name was not found, the query
     * function still returns some instance if it exists in the tree hierarchy.
     *
     * @param classType    The class type to be searched in the tree.
     * @param queryOptions Optional configuration to the search.
     *
     * @return A child or itself instance of the class type passed in {code classType}.
     *         If no correspondent was found, the function will return null.
     *
     * @since 1.0.4
     *
     **/
    <T> T query(Class<? extends T> classType, QueryOptions queryOptions);

    /**
     * Acts like a proxy to {@link DependencySearchTree#query(Class, QueryOptions)}.
     *
     * Given a {@code classType} and {@code queryOptions}, this function should
     * return a correspondent singleton instance of the specified class type.
     * {@code queryOptions} can be used to specify certain criteria to the search.
     * <p>
     * When {@link QueryOptions#retrieveAnyways()} is true, the function will bring
     * the most non-null value, which means that if you specify a filter that searches
     * by the name of the Managed instance, and the name was not found, the query
     * function still returns some instance if it exists in the tree hierarchy.
     *
     * @param classType    The class type to be searched in the tree.
     *
     * @return A child or itself instance of the class type passed in {code classType}.
     *         If no correspondent was found, the function will return null.
     *
     * @since 1.0.4
     *
     **/
    <T> T query(Class<? extends T> classType);

}
