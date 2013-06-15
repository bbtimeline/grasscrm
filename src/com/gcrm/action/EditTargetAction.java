/**
 * Copyright (C) 2012, Grass CRM Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gcrm.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.task.TaskExecutor;

import com.gcrm.domain.Account;
import com.gcrm.domain.ChangeLog;
import com.gcrm.domain.Lead;
import com.gcrm.domain.Salutation;
import com.gcrm.domain.Target;
import com.gcrm.domain.TargetList;
import com.gcrm.domain.User;
import com.gcrm.security.AuthenticationSuccessListener;
import com.gcrm.service.IBaseService;
import com.gcrm.service.IOptionService;
import com.gcrm.service.ITargetService;
import com.gcrm.util.BeanUtil;
import com.gcrm.util.CommonUtil;
import com.gcrm.util.Constant;
import com.gcrm.util.security.UserUtil;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.Preparable;

/**
 * Edits Target
 * 
 */
public class EditTargetAction extends BaseEditAction implements Preparable {

    private static final long serialVersionUID = -2404576552417042445L;

    private ITargetService baseService;
    private IBaseService<Account> accountService;
    private IBaseService<Lead> leadService;
    private IBaseService<User> userService;
    private IBaseService<TargetList> targetListService;
    private IOptionService<Salutation> salutationService;
    private IBaseService<ChangeLog> changeLogService;
    private TaskExecutor taskExecutor;
    private List<Salutation> salutations;
    private Target target;
    private Lead lead;
    private Integer accountID = null;
    private Integer salutationID = null;

    /**
     * Saves the entity.
     * 
     * @return the SUCCESS result
     */
    public String save() throws Exception {
        Target originalTarget = saveEntity();
        final Collection<ChangeLog> changeLogs = changeLog(originalTarget,
                target);
        target = getBaseService().makePersistent(target);
        this.setId(target.getId());
        this.setSaveFlag("true");
        if (changeLogs != null) {
            taskExecutor.execute(new Runnable() {
                public void run() {
                    batchInserChangeLogs(changeLogs);
                }
            });
        }
        return SUCCESS;
    }

    private void batchInserChangeLogs(Collection<ChangeLog> changeLogs) {
        this.getChangeLogService().batchUpdate(changeLogs);
    }

    /**
     * Gets the entity.
     * 
     * @return the SUCCESS result
     */
    public String get() throws Exception {
        if (this.getId() != null) {
            target = baseService.getEntityById(Target.class, this.getId());
            Account account = target.getAccount();
            if (account != null) {
                accountID = account.getId();
            }

            Salutation salutation = target.getSalutation();
            if (salutation != null) {
                salutationID = salutation.getId();
            }

            Integer leadID = target.getLead_id();
            if (leadID != null) {
                try {
                    lead = this.getLeadService().getEntityById(Lead.class,
                            leadID);
                } catch (Exception e) {
                    // in case the converted lead is deleted
                    lead = null;
                }
            }

            User assignedTo = target.getAssigned_to();
            if (assignedTo != null) {
                this.setAssignedToID(assignedTo.getId());
                this.setAssignedToText(assignedTo.getName());
            }
            this.getBaseInfo(target, Target.class.getSimpleName(),
                    Constant.CRM_NAMESPACE);
        } else {
            this.initBaseInfo();
        }
        return SUCCESS;
    }

    /**
     * Mass update entity record information
     */
    public String massUpdate() throws Exception {
        saveEntity();
        String[] fieldNames = this.massUpdate;
        if (fieldNames != null) {
            String[] selectIDArray = this.seleteIDs.split(",");
            Collection<Target> targets = new ArrayList<Target>();
            User loginUser = this.getLoginUser();
            User user = userService
                    .getEntityById(User.class, loginUser.getId());
            Collection<ChangeLog> allChangeLogs = new ArrayList<ChangeLog>();
            for (String IDString : selectIDArray) {
                int id = Integer.parseInt(IDString);
                Target targetInstance = this.baseService.getEntityById(
                        Target.class, id);
                Target originalTarget = targetInstance.clone();
                for (String fieldName : fieldNames) {
                    Object value = BeanUtil.getFieldValue(target, fieldName);
                    BeanUtil.setFieldValue(targetInstance, fieldName, value);
                }
                targetInstance.setUpdated_by(user);
                targetInstance.setUpdated_on(new Date());
                Collection<ChangeLog> changeLogs = changeLog(originalTarget,
                        targetInstance);
                allChangeLogs.addAll(changeLogs);
                targets.add(targetInstance);
            }
            final Collection<ChangeLog> changeLogsForSave = allChangeLogs;
            if (targets.size() > 0) {
                this.baseService.batchUpdate(targets);
                taskExecutor.execute(new Runnable() {
                    public void run() {
                        batchInserChangeLogs(changeLogsForSave);
                    }
                });
            }
        }
        return SUCCESS;
    }

    /**
     * Saves entity field
     * 
     * @throws Exception
     */
    private Target saveEntity() throws Exception {
        Target originalTarget = null;
        if (target.getId() == null) {
            UserUtil.permissionCheck("create_target");
        } else {
            UserUtil.permissionCheck("update_target");
            originalTarget = baseService.getEntityById(Target.class,
                    target.getId());
            target.setTargetLists(originalTarget.getTargetLists());
        }

        Account account = null;
        if (accountID != null) {
            account = accountService.getEntityById(Account.class, accountID);
        }
        target.setAccount(account);

        Salutation salutation = null;
        if (salutationID != null) {
            salutation = salutationService.getEntityById(Salutation.class,
                    salutationID);
        }
        target.setSalutation(salutation);

        User assignedTo = null;
        if (this.getAssignedToID() != null) {
            assignedTo = userService.getEntityById(User.class,
                    this.getAssignedToID());
        }
        target.setAssigned_to(assignedTo);
        User owner = null;
        if (this.getOwnerID() != null) {
            owner = userService.getEntityById(User.class, this.getOwnerID());
        }
        target.setOwner(owner);

        if ("TargetList".equals(this.getRelationKey())) {
            TargetList targetList = targetListService.getEntityById(
                    TargetList.class, Integer.valueOf(this.getRelationValue()));
            Set<TargetList> targetLists = target.getTargetLists();
            if (targetLists == null) {
                targetLists = new HashSet<TargetList>();
            }
            targetLists.add(targetList);
        }
        super.updateBaseInfo(target);
        return originalTarget;
    }

    /**
     * Converts the lead
     * 
     * @return the SUCCESS result
     */
    public String convert() throws Exception {

        this.getBaseService().convert(this.getTarget().getId());
        this.setSaveFlag(Target.STATUS_CONVERTED);
        return SUCCESS;
    }

    private Collection<ChangeLog> changeLog(Target originalTarget, Target target) {
        Collection<ChangeLog> changeLogs = null;
        if (originalTarget != null) {
            ActionContext context = ActionContext.getContext();
            Map<String, Object> session = context.getSession();
            String entityName = Target.class.getSimpleName();
            Integer recordID = target.getId();
            User loginUser = (User) session
                    .get(AuthenticationSuccessListener.LOGIN_USER);
            changeLogs = new ArrayList<ChangeLog>();

            String oldSalutation = getOptionValue(originalTarget
                    .getSalutation());
            String newSalutation = getOptionValue(target.getSalutation());
            if (!oldSalutation.equals(newSalutation)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "menu.salutation.title", oldSalutation, newSalutation,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldFirstName = CommonUtil.fromNullToEmpty(originalTarget
                    .getFirst_name());
            String newFirstName = CommonUtil.fromNullToEmpty(target
                    .getFirst_name());
            if (!oldFirstName.equals(newFirstName)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.first_name.label", oldFirstName, newFirstName,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldLastName = CommonUtil.fromNullToEmpty(originalTarget
                    .getLast_name());
            String newLastName = CommonUtil.fromNullToEmpty(target
                    .getLast_name());
            if (!oldLastName.equals(newLastName)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.last_name.label", oldLastName, newLastName,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldOfficePhone = CommonUtil.fromNullToEmpty(originalTarget
                    .getOffice_phone());
            String newOfficePhone = CommonUtil.fromNullToEmpty(target
                    .getOffice_phone());
            if (!oldOfficePhone.equals(newOfficePhone)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.office_phone.label", oldOfficePhone,
                        newOfficePhone, loginUser);
                changeLogs.add(changeLog);
            }

            String oldTitle = CommonUtil.fromNullToEmpty(originalTarget
                    .getTitle());
            String newTitle = CommonUtil.fromNullToEmpty(target.getTitle());
            if (!oldTitle.equals(newTitle)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.title.label", oldTitle, newTitle, loginUser);
                changeLogs.add(changeLog);
            }

            String oldMobile = CommonUtil.fromNullToEmpty(originalTarget
                    .getMobile());
            String newMobile = CommonUtil.fromNullToEmpty(target.getMobile());
            if (!oldMobile.equals(newMobile)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.mobile.label", oldMobile, newMobile, loginUser);
                changeLogs.add(changeLog);
            }

            String oldDepartment = CommonUtil.fromNullToEmpty(originalTarget
                    .getDepartment());
            String newDepartment = CommonUtil.fromNullToEmpty(target
                    .getDepartment());
            if (!oldDepartment.equals(newDepartment)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.department.label", oldDepartment,
                        newDepartment, loginUser);
                changeLogs.add(changeLog);
            }

            String oldFax = CommonUtil.fromNullToEmpty(originalTarget.getFax());
            String newWFax = CommonUtil.fromNullToEmpty(target.getFax());
            if (!oldFax.equals(newWFax)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.fax.label", oldFax, newWFax, loginUser);
                changeLogs.add(changeLog);
            }

            String oldPrimaryStreet = CommonUtil.fromNullToEmpty(originalTarget
                    .getPrimary_street());
            String newPrimaryStreet = CommonUtil.fromNullToEmpty(target
                    .getPrimary_street());
            if (!oldPrimaryStreet.equals(newPrimaryStreet)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.primary_street.label", oldPrimaryStreet,
                        newPrimaryStreet, loginUser);
                changeLogs.add(changeLog);
            }

            String oldPrimaryState = CommonUtil.fromNullToEmpty(originalTarget
                    .getPrimary_state());
            String newPrimaryState = CommonUtil.fromNullToEmpty(target
                    .getPrimary_state());
            if (!oldPrimaryState.equals(newPrimaryState)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.primary_state.label", oldPrimaryState,
                        newPrimaryState, loginUser);
                changeLogs.add(changeLog);
            }

            String oldPrimaryPostalCode = CommonUtil
                    .fromNullToEmpty(originalTarget.getPrimary_postal_code());
            String newPrimaryPostalCode = CommonUtil.fromNullToEmpty(target
                    .getPrimary_postal_code());
            if (!oldPrimaryPostalCode.equals(newPrimaryPostalCode)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.primary_postal_code.label",
                        oldPrimaryPostalCode, newPrimaryPostalCode, loginUser);
                changeLogs.add(changeLog);
            }

            String oldPrimaryCountry = CommonUtil
                    .fromNullToEmpty(originalTarget.getPrimary_country());
            String newPrimaryCountry = CommonUtil.fromNullToEmpty(target
                    .getPrimary_country());
            if (!oldPrimaryCountry.equals(newPrimaryCountry)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.primary_country.label", oldPrimaryCountry,
                        newPrimaryCountry, loginUser);
                changeLogs.add(changeLog);
            }

            String oldOtherStreet = CommonUtil.fromNullToEmpty(originalTarget
                    .getOther_street());
            String newOtherStreet = CommonUtil.fromNullToEmpty(target
                    .getOther_street());
            if (!oldOtherStreet.equals(newOtherStreet)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.other_street.label", oldOtherStreet,
                        newOtherStreet, loginUser);
                changeLogs.add(changeLog);
            }

            String oldOtherState = CommonUtil.fromNullToEmpty(originalTarget
                    .getOther_state());
            String newOtherState = CommonUtil.fromNullToEmpty(target
                    .getOther_state());
            if (!oldOtherState.equals(newOtherState)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.other_state.label", oldOtherState,
                        newOtherState, loginUser);
                changeLogs.add(changeLog);
            }

            String oldOtherPostalCode = CommonUtil
                    .fromNullToEmpty(originalTarget.getOther_postal_code());
            String newOtherPostalCode = CommonUtil.fromNullToEmpty(target
                    .getOther_postal_code());
            if (!oldOtherPostalCode.equals(newOtherPostalCode)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.other_postal_code.label", oldOtherPostalCode,
                        newOtherPostalCode, loginUser);
                changeLogs.add(changeLog);
            }

            String oldOtherCountry = CommonUtil.fromNullToEmpty(originalTarget
                    .getOther_country());
            String newOtherCountry = CommonUtil.fromNullToEmpty(target
                    .getOther_country());
            if (!oldOtherCountry.equals(newOtherCountry)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.other_country.label", oldOtherCountry,
                        newOtherCountry, loginUser);
                changeLogs.add(changeLog);
            }

            String oldEmail = CommonUtil.fromNullToEmpty(originalTarget
                    .getEmail());
            String newEmail = CommonUtil.fromNullToEmpty(target.getEmail());
            if (!oldEmail.equals(newEmail)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.email.label", oldEmail, newEmail, loginUser);
                changeLogs.add(changeLog);
            }

            String oldDescription = CommonUtil.fromNullToEmpty(originalTarget
                    .getDescription());
            String newDescription = CommonUtil.fromNullToEmpty(target
                    .getDescription());
            if (!oldDescription.equals(newDescription)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.description.label", oldDescription,
                        newDescription, loginUser);
                changeLogs.add(changeLog);
            }

            String oldNotes = CommonUtil.fromNullToEmpty(originalTarget
                    .getNotes());
            String newNotes = CommonUtil.fromNullToEmpty(target.getNotes());
            if (!oldNotes.equals(newNotes)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.notes.label", oldNotes, newNotes, loginUser);
                changeLogs.add(changeLog);
            }

            String oldAccountName = "";
            Account oldAccount = originalTarget.getAccount();
            if (oldAccount != null) {
                oldAccountName = CommonUtil.fromNullToEmpty(oldAccount
                        .getName());
            }
            String newAccountName = "";
            Account newAccount = target.getAccount();
            if (newAccount != null) {
                newAccountName = CommonUtil.fromNullToEmpty(newAccount
                        .getName());
            }
            if (oldAccountName != newAccountName) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.account.label", oldAccountName, newAccountName,
                        loginUser);
                changeLogs.add(changeLog);
            }

            boolean oldNotCall = originalTarget.isNot_call();

            boolean newNotCall = target.isNot_call();
            if (oldNotCall != newNotCall) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.not_call.label", String.valueOf(oldNotCall),
                        String.valueOf(newNotCall), loginUser);
                changeLogs.add(changeLog);
            }

            String oldAssignedToName = "";
            User oldAssignedTo = originalTarget.getAssigned_to();
            if (oldAssignedTo != null) {
                oldAssignedToName = oldAssignedTo.getName();
            }
            String newAssignedToName = "";
            User newAssignedTo = target.getAssigned_to();
            if (newAssignedTo != null) {
                newAssignedToName = newAssignedTo.getName();
            }
            if (oldAssignedToName != newAssignedToName) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.assigned_to.label",
                        CommonUtil.fromNullToEmpty(oldAssignedToName),
                        CommonUtil.fromNullToEmpty(newAssignedToName),
                        loginUser);
                changeLogs.add(changeLog);
            }
        }
        return changeLogs;
    }

    /**
     * Prepares the list
     * 
     */
    public void prepare() throws Exception {
        ActionContext context = ActionContext.getContext();
        Map<String, Object> session = context.getSession();
        String local = (String) session.get("locale");
        this.salutations = salutationService.getOptions(
                Salutation.class.getSimpleName(), local);
    }

    /**
     * @return the accountService
     */
    public IBaseService<Account> getAccountService() {
        return accountService;
    }

    /**
     * @param accountService
     *            the accountService to set
     */
    public void setAccountService(IBaseService<Account> accountService) {
        this.accountService = accountService;
    }

    /**
     * @return the userService
     */
    public IBaseService<User> getUserService() {
        return userService;
    }

    /**
     * @param userService
     *            the userService to set
     */
    public void setUserService(IBaseService<User> userService) {
        this.userService = userService;
    }

    /**
     * @return the target
     */
    public Target getTarget() {
        return target;
    }

    /**
     * @param target
     *            the target to set
     */
    public void setTarget(Target target) {
        this.target = target;
    }

    /**
     * @return the accountID
     */
    public Integer getAccountID() {
        return accountID;
    }

    /**
     * @param accountID
     *            the accountID to set
     */
    public void setAccountID(Integer accountID) {
        this.accountID = accountID;
    }

    /**
     * @return the targetListService
     */
    public IBaseService<TargetList> getTargetListService() {
        return targetListService;
    }

    /**
     * @param targetListService
     *            the targetListService to set
     */
    public void setTargetListService(IBaseService<TargetList> targetListService) {
        this.targetListService = targetListService;
    }

    /**
     * @return the salutations
     */
    public List<Salutation> getSalutations() {
        return salutations;
    }

    /**
     * @param salutations
     *            the salutations to set
     */
    public void setSalutations(List<Salutation> salutations) {
        this.salutations = salutations;
    }

    /**
     * @return the salutationID
     */
    public Integer getSalutationID() {
        return salutationID;
    }

    /**
     * @param salutationID
     *            the salutationID to set
     */
    public void setSalutationID(Integer salutationID) {
        this.salutationID = salutationID;
    }

    /**
     * @return the leadService
     */
    public IBaseService<Lead> getLeadService() {
        return leadService;
    }

    /**
     * @param leadService
     *            the leadService to set
     */
    public void setLeadService(IBaseService<Lead> leadService) {
        this.leadService = leadService;
    }

    /**
     * @return the baseService
     */
    public ITargetService getBaseService() {
        return baseService;
    }

    /**
     * @param baseService
     *            the baseService to set
     */
    public void setBaseService(ITargetService baseService) {
        this.baseService = baseService;
    }

    /**
     * @return the lead
     */
    public Lead getLead() {
        return lead;
    }

    /**
     * @param lead
     *            the lead to set
     */
    public void setLead(Lead lead) {
        this.lead = lead;
    }

    /**
     * @return the salutationService
     */
    public IOptionService<Salutation> getSalutationService() {
        return salutationService;
    }

    /**
     * @param salutationService
     *            the salutationService to set
     */
    public void setSalutationService(
            IOptionService<Salutation> salutationService) {
        this.salutationService = salutationService;
    }

    public IBaseService<ChangeLog> getChangeLogService() {
        return changeLogService;
    }

    public void setChangeLogService(IBaseService<ChangeLog> changeLogService) {
        this.changeLogService = changeLogService;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

}
