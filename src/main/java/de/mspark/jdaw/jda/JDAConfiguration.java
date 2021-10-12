package de.mspark.jdaw.jda;

import java.util.List;
import java.util.stream.Stream;

import javax.security.auth.login.LoginException;

import org.jooq.lambda.Unchecked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

@Configuration
public class JDAConfiguration {

    @Bean
    public JDA[] jdas(JDAWConfig conf, List<JDAConfigurationVisitor> jdaVisitor) throws LoginException {
        List<JDABuilder> jdaBuilderList = Stream.of(conf.apiTokens()).map(JDABuilder::createDefault).toList();
        jdaBuilderList.forEach(a -> jdaVisitor.forEach(j -> j.visit(a)));
        return jdaBuilderList.stream().map(Unchecked.function(JDABuilder::build)).toArray(JDA[]::new);
    }

}
