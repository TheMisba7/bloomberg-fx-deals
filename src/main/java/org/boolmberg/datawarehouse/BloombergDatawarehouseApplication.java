package org.boolmberg.datawarehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@SpringBootApplication
public class BloombergDatawarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(BloombergDatawarehouseApplication.class, args);
    }

}
