package com.dsb.eb2.framework.log;


import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspect {
    @Around(value = "@within(com.dsb.eb2.framework.log.Loggable) || @annotation(com.dsb.eb2.framework.log.Loggable)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {


        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        Loggable loggableMethod = method.getAnnotation(Loggable.class);

        Loggable loggableClass = proceedingJoinPoint.getTarget().getClass().getAnnotation(Loggable.class);

        //get current log level
        LogLevel logLevel = loggableMethod != null ? loggableMethod.value() : loggableClass.value();

        String star = "**********";
        //before
        LogWritter.write(proceedingJoinPoint.getTarget().getClass(), logLevel, star + method.getName() + "() start execution" + star);


        //show params
        boolean showParams = loggableMethod != null ? loggableMethod.params() : loggableClass.params();
        if (showParams) {

            if (proceedingJoinPoint.getArgs() != null && proceedingJoinPoint.getArgs().length > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < proceedingJoinPoint.getArgs().length; i++) {
                    sb.append(method.getParameterTypes()[i].getName() + ":" + proceedingJoinPoint.getArgs()[i]);
                    if (i < proceedingJoinPoint.getArgs().length - 1)
                        sb.append(", ");
                }

                LogWritter.write(proceedingJoinPoint.getTarget().getClass(), logLevel, method.getName() + "() args " + sb);
            }

        }

        long startTime = System.currentTimeMillis();
        //start method execution
        Object result = proceedingJoinPoint.proceed();

        long endTime = System.currentTimeMillis();

        //show results
        if (result != null) {
            boolean showResults = loggableMethod != null ? loggableMethod.result() : loggableClass.result();
            if (showResults) {
                LogWritter.write(proceedingJoinPoint.getTarget().getClass(), logLevel, method.getName() + "() Result : " + result);
            }
        }

        //show after
        LogWritter.write(proceedingJoinPoint.getTarget().getClass(), logLevel, star + method.getName() + "() finished execution and takes "+(endTime-startTime)+" millis time to execute " + star);

        return result;
    }
}
