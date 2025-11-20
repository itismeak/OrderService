package com.microservice.order_service.common.security;

import com.microservice.order_service.common.dto.CurrentUser;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUserUtil {
    public static CurrentUser get(){
        return (CurrentUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getDetails();
    }
}