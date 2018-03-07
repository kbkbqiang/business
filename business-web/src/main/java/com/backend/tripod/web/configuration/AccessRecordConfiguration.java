package com.backend.tripod.web.configuration;

import com.backend.tripod.web.component.AccessRecorder;
import com.backend.tripod.web.component.MongoAccessRecorder;
import com.backend.tripod.web.properties.AccessRecordProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;

@Configuration
@ConditionalOnProperty(prefix = "access-record", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AccessRecordConfiguration {

    @Configuration
    @ConditionalOnClass({MongoTemplate.class})
    @EnableConfigurationProperties({AccessRecordProperties.class})
    public static class MongoAccessRecordConfiguration {

        @Autowired
        private AccessRecordProperties properties;
        @Autowired
        private Environment environment;

        @Bean
        @ConditionalOnMissingBean
        public AccessRecorder mongoAccessRecorder(MongoTemplate mongoTemplate) {
            String collectionName = properties.getTableName();
            if (StringUtils.isBlank(collectionName)) {
                String app = environment.getProperty("spring.application.name", "unknown-application");
                String[] activeProfiles = environment.getActiveProfiles();
                collectionName = app + Arrays.toString(activeProfiles);
            }
            return new MongoAccessRecorder(collectionName, mongoTemplate);
        }

    }

}
