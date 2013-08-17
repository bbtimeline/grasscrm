package com.gcrm.action;

import com.gcrm.service.IUserService;
import com.opensymphony.xwork2.ActionSupport;

public class ForgetPasswordAction extends ActionSupport {

    private static final long serialVersionUID = 1L;

    private IUserService baseService;
    private String username;
    private String email;
    private String notification = null;

    @Override
    public String execute() throws Exception {
        String subject = this.getText("login.forgetPassword.email.subject");
        String content = this.getText("login.forgetPassword.email.content");
        boolean flag = baseService.forgetPassword(username, email, subject,
                content);
        if (flag) {
            notification = this.getText("login.forgetPassword.success");
        } else {
            notification = this.getText("login.forgetPassword.fail");
        }
        return SUCCESS;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     *            the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email
     *            the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the baseService
     */
    public IUserService getBaseService() {
        return baseService;
    }

    /**
     * @param baseService
     *            the baseService to set
     */
    public void setBaseService(IUserService baseService) {
        this.baseService = baseService;
    }

    /**
     * @return the notification
     */
    public String getNotification() {
        return notification;
    }

    /**
     * @param notification
     *            the notification to set
     */
    public void setNotification(String notification) {
        this.notification = notification;
    }

}