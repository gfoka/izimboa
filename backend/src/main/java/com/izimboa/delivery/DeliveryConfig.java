package com.izimboa.delivery;

import com.izimboa.delivery.dispatch.DispatchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({DeliveryProperties.class, DispatchProperties.class})
class DeliveryConfig {}
