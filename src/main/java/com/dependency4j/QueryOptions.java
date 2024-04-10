package com.dependency4j;

public record QueryOptions(boolean retrieveAnyways, String filteredClassName) {

    public static final class QueryOptionsBuilder {

        private String filteredClassName = "";
        private boolean retrieveAnyways  = true;

        private QueryOptionsBuilder() {}

        public QueryOptionsBuilder disableRetrieveAnyways() {
            this.retrieveAnyways = false;
            return this;
        }

        public QueryOptionsBuilder filterByName(String filteredClassName) {
            this.filteredClassName = Checks.nonNull(filteredClassName,
                    "Using QueryOptionsBuilder#filterByName the \"filteredClassName\" must not be null.");
            return this;
        }

        public QueryOptions build() {
            return new QueryOptions(retrieveAnyways, filteredClassName);
        }

    }

    public static QueryOptions none() {
        return QueryOptions.builder().build();
    }

    public static QueryOptionsBuilder builder() {
        return new QueryOptionsBuilder();
    }

}
