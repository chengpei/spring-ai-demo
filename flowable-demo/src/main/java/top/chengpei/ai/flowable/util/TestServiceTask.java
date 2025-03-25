package top.chengpei.ai.flowable.util;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

@Slf4j
public class TestServiceTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        log.info("调用服务任务 top.chengpei.ai.flowable.util.TestServiceTask");
    }
}
