package io.admin.modules.flowable.core.assignment.impl;


import io.admin.modules.flowable.core.assignment.AssignmentTypeProvider;
import io.admin.modules.system.service.SysUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CandidateUserProvider extends BaseUserProvider  {


    @Override
    public int getOrder() {
        return 2;
    }


    @Override
    public boolean isMultiple() {
        return true;
    }

    @Override
    public String getCode() {
        return "CandidateUser";
    }

    @Override
    public String getLabel() {
        return "候选用户";
    }



    @Override
    public XmlAttribute getXmlAttribute() {
        return XmlAttribute.candidateUsers;
    }



}
