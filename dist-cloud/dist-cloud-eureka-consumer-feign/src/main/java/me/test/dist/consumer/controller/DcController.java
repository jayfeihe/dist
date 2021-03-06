package me.test.dist.consumer.controller;

import me.test.dist.consumer.FeignClient.DcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RefreshScope
@RestController
public class DcController implements DcClient{

    @GetMapping("/consumer")
    public String dc() {
        return "";
    }

    @Override
    public String consumer() {
        return "true";
    }
}