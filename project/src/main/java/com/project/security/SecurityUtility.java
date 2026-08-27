package com.project.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtility {
    public static String getCurrentUserEmail(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication==null || !authentication.isAuthenticated()){
            throw new RuntimeException("User is not authenticated");
        }

        return authentication.getName();

    }
}
