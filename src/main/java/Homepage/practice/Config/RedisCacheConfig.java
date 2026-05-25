package Homepage.practice.Config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@Profile("!test") // 통합테스트에서는 Redis 제외
// Redis 저장소를 사용하도록 설정
public class RedisCacheConfig {
    @Bean
    // CacheManager : 캐시 매니저, RedisConnectionFactory : yml의 redis 설정을 통한 redis 연결 객체
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        GenericJackson2JsonRedisSerializer redisSerializer =
                GenericJackson2JsonRedisSerializer.builder()
                        .typeHintPropertyName("@class")         // 원래의 클래스 타입을 식별할 수 있도록 설정
                        .build()                                                  // serializer 인스턴스 생성
                        .configure(objectMapper -> {
                            objectMapper.registerModule(new JavaTimeModule());                                          // 시간 관련 클래스 직렬화
                            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);   // 날짜를 타임스탬프가 아닌 ISO-8601 문자열 형식으로 저장
                            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);                // 빈 객체 직렬화 시 예외 발생 방지, 프록시 객체 캐시 시 문제 방지
                        });

        // 기본 정책 만들기
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()  //조회 결과 Null이면 저장하지 않음
                .serializeKeysWith(               // Redis key를 문자열로 저장
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(            // Redis value를 JSON으로 저장
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                redisSerializer
                        )
                )
                .entryTtl(Duration.ofMinutes(5)); // 기본 TTL (Time To Live) : 캐시 유지 시간 (5분)

        // 캐시 이름별로 다른 설정을 넣기 위한 Map
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put("getRootCategory",
                defaultConfig.entryTtl(Duration.ofHours(12)));

        cacheConfigurations.put("getChildCategory",
                defaultConfig.entryTtl(Duration.ofHours(12)));

        cacheConfigurations.put("getItem",
                defaultConfig.entryTtl(Duration.ofMinutes(3)));

        cacheConfigurations.put("getAllItem",
                defaultConfig.entryTtl(Duration.ofMinutes(1)));

        cacheConfigurations.put("getItemsByCategory",
                defaultConfig.entryTtl(Duration.ofMinutes(1)));

        cacheConfigurations.put("getReview",
                defaultConfig.entryTtl(Duration.ofMinutes(3)));

        cacheConfigurations.put("getItemReview",
                defaultConfig.entryTtl(Duration.ofMinutes(3)));

        return RedisCacheManager.builder(redisConnectionFactory)    // RedisConnectionFactory로 Redis 연결
                .cacheDefaults(defaultConfig)                                          // 기본 캐시 정책은 defaultConfig 사용
                .withInitialCacheConfigurations(cacheConfigurations)     // 특정 캐시 정책은 cacheConfigurations TTL 사용
                .transactionAware()                                                         // DB 트랜잭션 성공 이후 캐시 무효화가 반영되도록
                .build();
    }
}

