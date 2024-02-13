package com.jeongjy.springaoptest;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class ParameterAop {

    @Before("execution(* com.jeongjy.springaoptest.TestController..*(..))")
    public void before(JoinPoint joinPoint) {
        //실행되는 함수 이름을 가져오고 출력
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        log.info("method: {}", method.getName());

        //메서드에 들어가는 매개변수 배열을 읽어옴
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            log.info("arg: {}", arg);
        }
    }

    @AfterReturning(value = "execution(* com.jeongjy.springaoptest.TestController..*(..))", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        //실행되는 함수 이름을 가져오고 출력
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        log.info("method: {}", method.getName());

        //리턴값을 출력
        log.info("result: {}", result);
    }

}
