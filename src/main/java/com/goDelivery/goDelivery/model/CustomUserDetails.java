package com.goDelivery.goDelivery.model;

import org.springframework.security.core.userdetails.UserDetails;

public interface CustomUserDetails extends UserDetails {
    Long getId();
    String getFullName();
}
