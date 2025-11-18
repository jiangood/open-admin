package io.admin.modules.flowable.admin.controller;


import io.admin.modules.flowable.core.FlowableService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("admin/flowable/instance")
@AllArgsConstructor
public class InstanceController {


    private FlowableService flowableService;

    private HistoryService historyService;


    @GetMapping("img")
    public void instanceImg(String businessKey, String id, HttpServletResponse response) throws IOException {
        if (StringUtils.isNotEmpty(businessKey)) {
            HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
            query.processInstanceBusinessKey(businessKey);
            query.notDeleted().orderByProcessInstanceStartTime()
                    .desc();
            List<HistoricProcessInstance> list = query
                    .listPage(0, 1);
            Assert.state(!list.isEmpty(), "暂无流程信息");
            HistoricProcessInstance instance = list.get(0);

            id = instance.getId();
        }

        BufferedImage image = flowableService.drawImage(id);
        ImageIO.write(image, "jpg", response.getOutputStream());
    }


}
