package Homepage.practice.Config;

import Homepage.practice.Exception.GlobalApiResponse;
import Homepage.practice.User.JWT.JwtAuthFilter;
import Homepage.practice.User.OAuth.OAuthFailureHandler;
import Homepage.practice.User.OAuth.OAuthSuccessHandler;
import Homepage.practice.User.OAuth.OAuthUserService;
import Homepage.practice.User.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserService userService;
    private final JwtAuthFilter jwtAuthFilter;
    private final OAuthUserService oAuthUserService;
    private final OAuthSuccessHandler oAuthSuccessHandler;
    private final OAuthFailureHandler oAuthFailureHandler;

    /** 순환 참조를 막기 위한 지연 로딩 */
    public SecurityConfig (@Lazy UserService userService,
                           @Lazy JwtAuthFilter jwtAuthFilter,
                           @Lazy OAuthUserService oAuthUserService,
                           @Lazy OAuthSuccessHandler oAuthSuccessHandler,
                           @Lazy OAuthFailureHandler oAuthFailureHandler
        ) {
        this.userService = userService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.oAuthUserService = oAuthUserService;
        this.oAuthSuccessHandler = oAuthSuccessHandler;
        this.oAuthFailureHandler = oAuthFailureHandler;
    }

    /** 필터 체인 설정 */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(source()))                   // 명시적인 CORS 설정 추가
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()     // OPTIONS 요청(Preflight) 허용
                        .requestMatchers(
                                "/public/**",
                                "/error/",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuthSuccessHandler)
                        .failureHandler(oAuthFailureHandler)
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(oAuthUserService)))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(entryPoint()) // 401
                        .accessDeniedHandler(deniedHandler())   // 403
                )
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))             // 세션 사용 비활성화
                .authenticationProvider(provider())                                          // 커스텀 사용자 인증 방식
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // JWT 토큰 요청 시 jwtAuthFilter를 먼저 거치도록 설정
        return httpSecurity.build();
    }

    /** 비밀번호 암호화 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 사용자 인증을 처리 */
    @Bean
    public AuthenticationProvider provider() {
        DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
        dao.setUserDetailsService(userService);   // 사용자 정보 로드
        dao.setPasswordEncoder(passwordEncoder());// 비밀번호 비교
        return dao;
    }

    /** AuthenticationManager가 자동으로 Bean으로 노출되지 않음, AuthService에서 사용하기 위함 */
    @Bean
    public AuthenticationManager manager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /** CORS 설정을 위한 CorsConfigurationSource 빈 생성 */
    @Bean
    public CorsConfigurationSource source() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://leoan.p-e.kr", "https://leoan.p-e.kr","http://localhost:8081",
                "https://localhost:8081", "http://localhost:3000", "https://localhost:3000"));
                // *:와일드 카드 URL을 사용하려면 OriginPatterns를 사용해야 합니다.
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private final ObjectMapper mapper = new ObjectMapper();

    private void write(HttpServletResponse response, int status, GlobalApiResponse<?> body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        mapper.writeValue(response.getWriter(), body);
    }

    /** 401 Unauthorized 처리 */
    @Bean
    public AuthenticationEntryPoint entryPoint() {
        return (request, response, authException) ->
                write(response, HttpServletResponse.SC_UNAUTHORIZED,
                        GlobalApiResponse.fail("인증이 필요합니다.", "401"));
    }

    /** 403 Forbidden 처리 */
    @Bean
    public AccessDeniedHandler deniedHandler() {
        return (request, response, accessDeniedException) ->
                write(response, HttpServletResponse.SC_FORBIDDEN,
                        GlobalApiResponse.fail("접근 권한이 없습니다.", "403"));
    }
}
