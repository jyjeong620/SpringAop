package com.jeongjy.springaoptest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class HMACAop {
    private static final String RESPONSE_BODY_KEY = "body";
    private static final String SHA_256 = "SHA-256";
    private static final String MD5 = "MD5";
    private final ObjectMapper om;

    public HMACAop(ObjectMapper om) {
        this.om = om;
    }


    @AfterReturning(value = "execution(* com.jeongjy.springaoptest.TestController..*(..)) && !target(com.jeongjy.springaoptest.IndexController)", returning = "response")
    public void responseHashToHMAC(final Object response) {
        final var requestAttributes = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes());
        final var servletRequest = requestAttributes.getRequest();
        final var servletResponse = requestAttributes.getResponse();
        final var authToken = servletRequest.getHeader("X-Auth-Token");

        try {
            String stringResponse = this.om.writeValueAsString(response);
            if (response instanceof ResponseEntity) {
                final var mapResponse = this.om.readValue(
                        stringResponse,
                        new TypeReference<Map<String, Object>>() {
                        });

                if (mapResponse.containsKey(RESPONSE_BODY_KEY)) {
                    stringResponse = this.om.writeValueAsString(mapResponse.get(RESPONSE_BODY_KEY));
                }
            }

            // StatusCode MD5 Hash
            final var md5HashedStatusCode = this.hashToMD5(String.valueOf(servletResponse.getStatus()));

            // Response SHA-256 Hash
            final var sha256HashedResponse = this.hashToSHA256(
                    authToken,
                    HexUtils.toHexString(md5HashedStatusCode) + stringResponse);

            // HMAC Hash
            final var hmacResponse = HexUtils.toHexString(sha256HashedResponse);

            servletResponse.setHeader("X-Authorization", hmacResponse);
        } catch (NoSuchAlgorithmException | JsonProcessingException e) {
            log.error("해시처리에 실패하였습니다", e);
        }
    }

    private byte[] hashToMD5(final String statusCode) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(MD5)
                .digest(statusCode.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] hashToSHA256(final String salt, final String response)
            throws NoSuchAlgorithmException {
        final var messageDigest = MessageDigest.getInstance(SHA_256);
        messageDigest.update(salt.getBytes(StandardCharsets.UTF_8));

        return messageDigest.digest(response.getBytes(StandardCharsets.UTF_8));
    }

}
