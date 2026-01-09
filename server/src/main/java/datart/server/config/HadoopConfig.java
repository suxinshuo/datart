package datart.server.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author suxinshuo 
 * @date 2026/1/9 15:01
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "hadoop")
public class HadoopConfig {

    private String username;

    private Map<String, String> properties;

    @Bean("hdfsFileSystem")
    public FileSystem createFs() throws Exception {
        System.setProperty("HADOOP_USER_NAME", username);
        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
        properties.forEach(conf::set);
        log.info("===============【hadoop configuration info start.】===============");
        log.info("【hadoop conf】: size:{}, {}", conf.size(), conf);
        log.info("【fs.defaultFS】: {}", conf.get("fs.defaultFS"));
        log.info("【fs.hdfs.impl】: {}", conf.get("fs.hdfs.impl"));
        FileSystem fs = FileSystem.newInstance(conf);
        log.info("【fileSystem scheme】: {}", fs.getScheme());
        log.info("===============【hadoop configuration info end.】===============");
        return fs;
    }

}
