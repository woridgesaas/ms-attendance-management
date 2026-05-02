package org.woridge.ms_attendance_management.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.woridge.ms_attendance_management.messaging.event.TenantReadyEvent;
import org.woridge.ms_attendance_management.service.TenantProvisioningService;

@RequiredArgsConstructor
@Component
@Slf4j
public class TenantReadyConsumer {
    private final TenantProvisioningService provisioningService;


    @RabbitListener(queues = "${rabbitmq.provisioning-queue}")
    public void handleMessage(TenantReadyEvent event){
        log.info("Recieve a message for tenant {} with service key {}" , event.getSubdomain() , event.getServiceKey());
        this.provisioningService.runMigrationsForTenant(event.getSubdomain()  , event.getServiceKey());
    }
}
