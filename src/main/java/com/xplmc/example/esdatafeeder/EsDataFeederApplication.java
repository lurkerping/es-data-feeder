package com.xplmc.example.esdatafeeder;

import com.xplmc.example.esdatafeeder.feeder.NetEasyFeeder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * es-data-feeder
 *
 * @author luke
 */
@SpringBootApplication
public class EsDataFeederApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsDataFeederApplication.class, args);
    }

    /**
     * feed NetEasy data
     *
     * @param netEasyFeeder netEasyFeeder bean
     * @return CommandLineRunner that's runs NetEasyFeeder
     */
    @Profile("dev")
    @Bean
    public CommandLineRunner netEasyFeederRunner(final NetEasyFeeder netEasyFeeder) {
        return (args) -> netEasyFeeder.feed();
    }

}

