package com.handcoding.oauth;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.OAuth2AuthorizationServerConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.handcoding.oauth.filter.AccessLogFilter;

@EnableAuthorizationServer
@SpringBootApplication
public class OAuth2Application extends WebMvcConfigurerAdapter {

	public static void main(String[] args) {
		SpringApplication.run(OAuth2Application.class, args);
	}
	
	/**
	 * AccessLog filter 설정
	 * @return
	 */
	@Bean
	public FilterRegistrationBean accessLogFilterBean() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean(new AccessLogFilter());
		return registrationBean;
	}
	
}

@Configuration
class OAuth2Configuration {
	
	/**
	 * 토큰을 저장할 db접속정보 지정
	 * @param dataSource
	 * @return
	 */
    @Bean
    public TokenStore jdbcTokenStore(DataSource dataSource) {
        return new JdbcTokenStore(dataSource);
        // jwt 토큰시
//        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    /**
     * client_id와 client_secret을 비교하기위해 쓸 db접속정보 등록
     * @param dataSource
     * @return
     */
    @Bean
    @Primary
    public JdbcClientDetailsService jdbcClientDetailsService(DataSource dataSource) {
        return new JdbcClientDetailsService(dataSource);
    }
    
    // jwt 토큰시
//    @Bean
//    public JwtAccessTokenConverter jwtAccessTokenConverter() {
//        return new JwtAccessTokenConverter();
//    }
    
}

@Configuration
class JwtOAuth2AuthorizationServerConfiguration extends OAuth2AuthorizationServerConfiguration {

	@Autowired
	private ClientDetailsService clientDetailsService;
	
	public JwtOAuth2AuthorizationServerConfiguration(BaseClientDetails details,
			AuthenticationManager authenticationManager, ObjectProvider<TokenStore> tokenStore,
			ObjectProvider<AccessTokenConverter> tokenConverter, AuthorizationServerProperties properties) {
		super(details, authenticationManager, tokenStore, tokenConverter, properties);
	}

	/**
	 * DB에 저장된 사용자 정보로 검증처리 : client_id, client_secret
	 */
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(clientDetailsService);
	}
    
}