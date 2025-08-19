package com.AutoSales_Agent.Identity;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppIdentityService {
    private final AppIdentityProps props;

    public AppIdentityDto getIdentity() {
        AppIdentityDto d = new AppIdentityDto();
        d.setCompanyName(props.getCompanyName());
        d.setSenderName(props.getSenderName());
        d.setSenderEmail(props.getSenderEmail());
        return d;
    }
}
