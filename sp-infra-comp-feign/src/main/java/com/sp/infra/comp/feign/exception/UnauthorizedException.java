package com.sp.infra.comp.feign.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luchao
 * @create 2020/10/26.
 */
@Data
@NoArgsConstructor
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

}
