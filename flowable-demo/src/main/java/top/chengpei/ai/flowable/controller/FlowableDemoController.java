package top.chengpei.ai.flowable.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class FlowableDemoController {

    @Resource
    private ProcessEngine processEngine;

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @GetMapping("/flowable/start")
    public String start(String name, Integer days, String reason) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("staffName", name);
        variables.put("days", days);
        variables.put("reason", reason);
        // 这是一个业务key，可以用来关联业务系统
        String businessKey = "testBusinessKey1";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("askForLeave", businessKey, variables);
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).taskAssignee(name).singleResult();
        taskService.complete(task.getId());
        return processInstance.getId();
    }

    @GetMapping("/flowable/queryTask")
    public String queryTask(String name, String processId) {
        // 这是一个业务key，可以用来关联业务系统
        String businessKey = "testBusinessKey1";
        List<Task> list = taskService.createTaskQuery().processInstanceId(processId).processInstanceBusinessKey(businessKey).taskAssignee(name).list();
        return list.toString();
    }

    @GetMapping("/flowable/completeTask")
    public String doTask(String taskId, String outcome) {
        Task singleResult = taskService.createTaskQuery().taskId(taskId).singleResult();
        log.info(singleResult.toString());
        Map<String, Object> variables = new HashMap<>();
        if (StringUtils.isBlank(outcome)) {
            variables.put("outcome", "通过");
        } else {
            variables.put("outcome", outcome);
        }
        List<String> hrList = new ArrayList<>();
        hrList.add("hr1");
        hrList.add("hr2");
        hrList.add("hr3");
        variables.put("hrList", hrList);
        taskService.complete(singleResult.getId(), variables);
        return "success";
    }

    /**
     * 生成流程图
     *
     * @param processId 任务ID
     */
    @RequestMapping(value = "/flowable/processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        //流程走完的不显示图
        if (pi == null) {
            IOUtils.write("流程已经结束", httpServletResponse.getOutputStream());
            return;
        }
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(processId)
                .list();
        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration processEngineConfiguration = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        byte[] buf = new byte[1024];
        int legth;
        try (InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows,
                processEngineConfiguration.getActivityFontName(),
                processEngineConfiguration.getLabelFontName(),
                processEngineConfiguration.getAnnotationFontName(),
                processEngineConfiguration.getClassLoader(), 1.0, true); OutputStream out = httpServletResponse.getOutputStream()) {
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        }
    }
}
