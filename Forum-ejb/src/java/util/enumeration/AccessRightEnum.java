/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.enumeration;

/**
 *
 * @author valenciateh
 */
public enum AccessRightEnum {
    
    NORMAL_USER("User"),
    ADMIN("Administrator"),
    BLOCKED("Blocked");
    
    private String status;
    
    private AccessRightEnum(String status) {
        this.status = status;
    }
    
    private String getStatus() {
        return status;
    }
    
}
