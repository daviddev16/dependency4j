package example;


import com.dependency4j.DependencyManager;
import com.dependency4j.QueryOptions;
import com.dependency4j.util.D4JUtil;

public class Main {

    public static void main(String[] args) {

        DependencyManager dependencyManager = new DependencyManager();
        dependencyManager.install("example");

        IHomeController controller = dependencyManager
                .getDependencySearchTree().query(IHomeController.class, QueryOptions.none());

        System.out.println(controller.helloMessage());

    }

}
