package io.github.dependency4j.exception;

import java.lang.reflect.Member;

/**
 * @author Daviddev16
 **/
public class MemberInjectionFailedException extends RuntimeException {

    public MemberInjectionFailedException(Member member, Class<?> classType, Throwable cause) {
        super("Failed to inject singleton in \"%s\" of class type \"%s\"."
                .formatted(member.getName(), classType.getName()), cause);
    }
}
