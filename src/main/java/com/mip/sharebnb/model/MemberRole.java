package com.mip.sharebnb.model;

import lombok.Getter;

@Getter
public enum MemberRole {

    ADMIN("ROLE_ADMIN"), Member("ROLE_MEMBER");

    private final String roleName;

    MemberRole(String roleName) {
        this.roleName = roleName;
    }
}
