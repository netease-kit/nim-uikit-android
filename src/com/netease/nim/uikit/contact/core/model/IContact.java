package com.netease.nim.uikit.contact.core.model;

public interface IContact {

    interface Type {

        /**
         * TYPE USER
         */
        int Friend = 0x1;

        /**
         * TYPE TEAM
         */
        int Team = 0x2;

        /**
         * TYPE TEAM MEMBER
         */
        int TeamMember = 0x03;
    }

    /**
     * get contact id
     *
     * @return
     */
    String getContactId();

    /**
     * get contact type {@link Type}
     *
     * @return
     */
    int getContactType();

    /**
     * get contact's display name to show to user
     *
     * @return
     */
    String getDisplayName();
}
