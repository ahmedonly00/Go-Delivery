package com.goDelivery.goDelivery.shared.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface CustomUserDetails extends UserDetails {
    Long getId();
    String getFullName();
}
