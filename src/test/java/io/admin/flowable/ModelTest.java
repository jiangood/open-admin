package io.admin.flowable;

import jakarta.annotation.Resource;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ModelTest {

    @Resource
    RepositoryService repositoryService;


    @Test
    public void test(){
        Model model = repositoryService.newModel();

        repositoryService.saveModel(model);
    }

}
