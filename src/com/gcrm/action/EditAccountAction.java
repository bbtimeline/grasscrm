/**
 * Copyright (C) 2012 - 2013, Grass CRM Inc
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
import com.gcrm.domain.AccountType;
import com.gcrm.domain.Campaign;
import com.gcrm.domain.ChangeLog;
import com.gcrm.domain.Document;
import com.gcrm.domain.Industry;
import com.gcrm.domain.TargetList;
import com.gcrm.domain.User;
import com.gcrm.security.AuthenticationSuccessListener;
import com.gcrm.service.IBaseService;
import com.gcrm.service.IOptionService;
import com.gcrm.util.BeanUtil;
import com.gcrm.util.CommonUtil;
import com.gcrm.util.Constant;
import com.gcrm.util.security.UserUtil;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.Preparable;

/**
 * Edits Account
 * 
 */
public class EditAccountAction extends BaseEditAction implements Preparable {

    private static final long serialVersionUID = -2404576552417042445L;

    private IBaseService<Account> baseService;
    private IOptionService<AccountType> accountTypeService;
    private IOptionService<Industry> industryService;
    private IBaseService<User> userService;
    private IBaseService<Campaign> campaignService;
    private IBaseService<TargetList> targetListService;
    private IBaseService<Document> documentService;
    private IBaseService<ChangeLog> changeLogService;
    private TaskExecutor taskExecutor;
    private Account account;
    private List<AccountType> types;
    private List<Industry> industries;
    private Integer typeID = null;
    private Integer industryID = null;
    private Integer campaignID = null;
    private Integer managerID = null;
    private String managerText = null;

    /**
     * Saves the entity.
     * 
     * @return the SUCCESS result
     */
    public String save() throws Exception {
        Account originalAccount = saveEntity();

        if ("TargetList".equals(this.getRelationKey())) {
            TargetList targetList = targetListService.getEntityById(
                    TargetList.class, Integer.valueOf(this.getRelationValue()));
            Set<TargetList> targetLists = account.getTargetLists();
            if (targetLists == null) {
                targetLists = new HashSet<TargetList>();
            }
            targetLists.add(targetList);
        } else if ("Document".equals(this.getRelationKey())) {
            Document document = documentService.getEntityById(Document.class,
                    Integer.valueOf(this.getRelationValue()));
            Set<Document> documents = account.getDocuments();
            if (documents == null) {
                documents = new HashSet<Document>();
            }
            documents.add(document);
        }
        final Collection<ChangeLog> changeLogs = changeLog(originalAccount,
                account);
        account = getBaseService().makePersistent(account);
        this.setId(account.getId());
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
     * Mass update entity record information
     */
    public String massUpdate() throws Exception {
        saveEntity();
        String[] fieldNames = this.massUpdate;
        if (fieldNames != null) {
            String[] selectIDArray = this.seleteIDs.split(",");
            Collection<Account> accounts = new ArrayList<Account>();
            User loginUser = this.getLoginUser();
            User user = userService
                    .getEntityById(User.class, loginUser.getId());
            Collection<ChangeLog> allChangeLogs = new ArrayList<ChangeLog>();
            for (String IDString : selectIDArray) {
                int id = Integer.parseInt(IDString);
                Account accountInstance = this.baseService.getEntityById(
                        Account.class, id);
                Account originalAccount = accountInstance.clone();
                for (String fieldName : fieldNames) {
                    Object value = BeanUtil.getFieldValue(account, fieldName);
                    BeanUtil.setFieldValue(accountInstance, fieldName, value);
                }
                accountInstance.setUpdated_by(user);
                accountInstance.setUpdated_on(new Date());
                Collection<ChangeLog> changeLogs = changeLog(originalAccount,
                        accountInstance);
                allChangeLogs.addAll(changeLogs);
                accounts.add(accountInstance);
            }
            final Collection<ChangeLog> changeLogsForSave = allChangeLogs;
            if (accounts.size() > 0) {
                this.baseService.batchUpdate(accounts);
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
     * @return original account record
     * @throws Exception
     */
    private Account saveEntity() throws Exception {
        Account originalAccount = null;
        if (account.getId() == null) {
            UserUtil.permissionCheck("create_account");
        } else {
            UserUtil.permissionCheck("update_account");
            originalAccount = baseService.getEntityById(Account.class,
                    account.getId());
            account.setTargetLists(originalAccount.getTargetLists());
            account.setDocuments(originalAccount.getDocuments());
        }
        AccountType type = null;
        if (typeID != null) {
            type = accountTypeService.getEntityById(AccountType.class, typeID);
        }
        account.setAccount_type(type);

        Industry industry = null;
        if (industryID != null) {
            industry = industryService
                    .getEntityById(Industry.class, industryID);
        }
        account.setIndustry(industry);

        User assignedTo = null;
        if (this.getAssignedToID() != null) {
            assignedTo = userService.getEntityById(User.class,
                    this.getAssignedToID());
        }
        account.setAssigned_to(assignedTo);

        User owner = null;
        if (this.getOwnerID() != null) {
            owner = userService.getEntityById(User.class, this.getOwnerID());
        }
        account.setOwner(owner);

        Account manager = null;
        if (managerID != null) {
            manager = baseService.getEntityById(Account.class, managerID);
        }
        account.setManager(manager);
        super.updateBaseInfo(account);
        return originalAccount;
    }

    private Collection<ChangeLog> changeLog(Account originalAccount,
            Account account) {
        Collection<ChangeLog> changeLogs = null;
        if (originalAccount != null) {
            ActionContext context = ActionContext.getContext();
            Map<String, Object> session = context.getSession();
            String entityName = Account.class.getSimpleName();
            Integer recordID = account.getId();
            User loginUser = (User) session
                    .get(AuthenticationSuccessListener.LOGIN_USER);
            changeLogs = new ArrayList<ChangeLog>();

            String oldName = CommonUtil.fromNullToEmpty(originalAccount
                    .getName());
            String newName = CommonUtil.fromNullToEmpty(account.getName());
            if (!oldName.equals(newName)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.name.label", oldName, newName, loginUser);
                changeLogs.add(changeLog);
            }

            String oldOfficePhone = CommonUtil.fromNullToEmpty(originalAccount
                    .getOffice_phone());
            String newOfficePhone = CommonUtil.fromNullToEmpty(account
                    .getOffice_phone());
            if (!oldOfficePhone.equals(newOfficePhone)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.office_phone.label", oldOfficePhone,
                        newOfficePhone, loginUser);
                changeLogs.add(changeLog);
            }

            String oldWebsite = CommonUtil.fromNullToEmpty(originalAccount
                    .getWebsite());
            String newWebsite = CommonUtil
                    .fromNullToEmpty(account.getWebsite());
            if (!oldWebsite.equals(newWebsite)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.website.label", oldWebsite, newWebsite,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldFax = CommonUtil
                    .fromNullToEmpty(originalAccount.getFax());
            String newWFax = CommonUtil.fromNullToEmpty(account.getFax());
            if (!oldFax.equals(newWFax)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.fax.label", oldFax, newWFax, loginUser);
                changeLogs.add(changeLog);
            }

            String oldBillStreet = CommonUtil.fromNullToEmpty(originalAccount
                    .getBill_street());
            String newBillStreet = CommonUtil.fromNullToEmpty(account
                    .getBill_street());
            if (!oldBillStreet.equals(newBillStreet)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.billing_street.label", oldBillStreet,
                        newBillStreet, loginUser);
                changeLogs.add(changeLog);
            }

            String oldBillState = CommonUtil.fromNullToEmpty(originalAccount
                    .getBill_state());
            String newBillState = CommonUtil.fromNullToEmpty(account
                    .getBill_state());
            if (!oldBillState.equals(newBillState)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.billing_state.label", oldBillState,
                        newBillState, loginUser);
                changeLogs.add(changeLog);
            }

            String oldBillPostalCode = CommonUtil
                    .fromNullToEmpty(originalAccount.getBill_postal_code());
            String newBillPostalCode = CommonUtil.fromNullToEmpty(account
                    .getBill_postal_code());
            if (!oldBillPostalCode.equals(newBillPostalCode)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.billing_postal_code.label", oldBillPostalCode,
                        newBillPostalCode, loginUser);
                changeLogs.add(changeLog);
            }

            String oldBillCountry = CommonUtil.fromNullToEmpty(originalAccount
                    .getBill_country());
            String newBillCountry = CommonUtil.fromNullToEmpty(account
                    .getBill_country());
            if (!oldBillCountry.equals(newBillCountry)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.billing_country.label", oldBillCountry,
                        newBillCountry, loginUser);
                changeLogs.add(changeLog);
            }

            String oldShipStreet = CommonUtil.fromNullToEmpty(originalAccount
                    .getShip_street());
            String newShipStreet = CommonUtil.fromNullToEmpty(account
                    .getShip_street());
            if (!oldShipStreet.equals(newShipStreet)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.shipping_street.label", oldShipStreet,
                        newShipStreet, loginUser);
                changeLogs.add(changeLog);
            }

            String oldShipState = CommonUtil.fromNullToEmpty(originalAccount
                    .getShip_state());
            String newShipState = CommonUtil.fromNullToEmpty(account
                    .getShip_state());
            if (!oldShipState.equals(newShipState)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.shipping_state.label", oldShipState,
                        newShipState, loginUser);
                changeLogs.add(changeLog);
            }

            String oldShipPostalCode = CommonUtil
                    .fromNullToEmpty(originalAccount.getShip_postal_code());
            String newShipPostalCode = CommonUtil.fromNullToEmpty(account
                    .getShip_postal_code());
            if (!oldShipPostalCode.equals(newShipPostalCode)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.shipping_postal_code.label", oldShipPostalCode,
                        newShipPostalCode, loginUser);
                changeLogs.add(changeLog);
            }

            String oldShipCountry = CommonUtil.fromNullToEmpty(originalAccount
                    .getShip_country());
            String newShipCountry = CommonUtil.fromNullToEmpty(account
                    .getShip_country());
            if (!oldShipCountry.equals(newShipCountry)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.shipping_country.label", oldShipCountry,
                        newShipCountry, loginUser);
                changeLogs.add(changeLog);
            }

            String oldEmail = CommonUtil.fromNullToEmpty(originalAccount
                    .getEmail());
            String newEmail = CommonUtil.fromNullToEmpty(account.getEmail());
            if (!oldEmail.equals(newEmail)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.email.label", oldEmail, newEmail, loginUser);
                changeLogs.add(changeLog);
            }

            String oldDescription = CommonUtil.fromNullToEmpty(originalAccount
                    .getDescription());
            String newDescription = CommonUtil.fromNullToEmpty(account
                    .getDescription());
            if (!oldDescription.equals(newDescription)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.description.label", oldDescription,
                        newDescription, loginUser);
                changeLogs.add(changeLog);
            }

            String oldNotes = CommonUtil.fromNullToEmpty(originalAccount
                    .getNotes());
            String newNotes = CommonUtil.fromNullToEmpty(account.getNotes());
            if (!oldNotes.equals(newNotes)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.notes.label", oldNotes, newNotes, loginUser);
                changeLogs.add(changeLog);
            }

            String oldAccountType = getOptionValue(originalAccount
                    .getAccount_type());
            String newAccountType = getOptionValue(account.getAccount_type());
            if (!oldAccountType.equals(newAccountType)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.type.label", oldAccountType, newAccountType,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldIndustry = getOptionValue(originalAccount.getIndustry());
            String newIndustry = getOptionValue(account.getIndustry());
            if (!oldIndustry.equals(newIndustry)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "menu.industry.title", oldIndustry, newIndustry,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldAnnualRevenue = CommonUtil
                    .fromNullToEmpty(originalAccount.getAnnual_revenue());
            String newAnnualRevenue = CommonUtil.fromNullToEmpty(account
                    .getAnnual_revenue());
            if (!oldAnnualRevenue.equals(newAnnualRevenue)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "account.annual_revenue.label", oldAnnualRevenue,
                        newAnnualRevenue, loginUser);
                changeLogs.add(changeLog);
            }

            String oldMarketValue = CommonUtil.fromNullToEmpty(originalAccount
                    .getMarket_value());
            String newMarketValue = CommonUtil.fromNullToEmpty(account
                    .getMarket_value());
            if (!oldMarketValue.equals(newMarketValue)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "account.market_value.label", oldMarketValue,
                        newMarketValue, loginUser);
                changeLogs.add(changeLog);
            }

            String oldEmployees = CommonUtil.fromNullToEmpty(originalAccount
                    .getEmployees());
            String newEmployees = CommonUtil.fromNullToEmpty(account
                    .getEmployees());
            if (!oldMarketValue.equals(newMarketValue)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "account.employees.label", oldEmployees, newEmployees,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldSicCode = CommonUtil.fromNullToEmpty(originalAccount
                    .getSic_code());
            String newSicCode = CommonUtil.fromNullToEmpty(account
                    .getSic_code());
            if (!oldSicCode.equals(newSicCode)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "account.sic_code.label", oldSicCode, newSicCode,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldTicketSymbol = CommonUtil.fromNullToEmpty(originalAccount
                    .getTicket_symbol());
            String newTicketSymbol = CommonUtil.fromNullToEmpty(account
                    .getTicket_symbol());
            if (!oldTicketSymbol.equals(newTicketSymbol)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "account.ticket_symbol.label", oldTicketSymbol,
                        newTicketSymbol, loginUser);
                changeLogs.add(changeLog);
            }

            String oldManagerName = "";
            Account oldManager = originalAccount.getManager();
            if (oldManager != null) {
                oldManagerName = CommonUtil.fromNullToEmpty(oldManager
                        .getName());
            }
            String newManagerName = "";
            Account newManager = account.getManager();
            if (newManager != null) {
                newManagerName = CommonUtil.fromNullToEmpty(newManager
                        .getName());
            }
            if (oldManagerName != newManagerName) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "account.manager.label", oldManagerName,
                        newManagerName, loginUser);
                changeLogs.add(changeLog);
            }

            String oldOwnship = CommonUtil.fromNullToEmpty(originalAccount
                    .getOwnship());
            String newOwnship = CommonUtil
                    .fromNullToEmpty(account.getOwnship());
            if (!oldOwnship.equals(newOwnship)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "account.ownship.label", oldOwnship, newOwnship,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldRating = CommonUtil.fromNullToEmpty(originalAccount
                    .getRating());
            String newRating = CommonUtil.fromNullToEmpty(account.getRating());
            if (!oldRating.equals(newRating)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "account.rating.label", oldRating, newRating, loginUser);
                changeLogs.add(changeLog);
            }

            String oldAssignedToName = "";
            User oldAssignedTo = originalAccount.getAssigned_to();
            if (oldAssignedTo != null) {
                oldAssignedToName = oldAssignedTo.getName();
            }
            String newAssignedToName = "";
            User newAssignedTo = account.getAssigned_to();
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
     * Gets the entity.
     * 
     * @return the SUCCESS result
     */
    public String get() throws Exception {
        if (this.getId() != null) {
            UserUtil.permissionCheck("view_account");
            account = baseService.getEntityById(Account.class, this.getId());
            UserUtil.scopeCheck(account, "scope_account");
            AccountType type = account.getAccount_type();
            if (type != null) {
                typeID = type.getId();
            }
            Industry industry = account.getIndustry();
            if (industry != null) {
                industryID = industry.getId();
            }

            User assignedTo = account.getAssigned_to();
            if (assignedTo != null) {
                this.setAssignedToID(assignedTo.getId());
                this.setAssignedToText(assignedTo.getName());
            }

            Account manager = account.getManager();
            if (manager != null) {
                managerID = manager.getId();
                managerText = manager.getName();
            }
            this.getBaseInfo(account, Account.class.getSimpleName(),
                    Constant.CRM_NAMESPACE);
        } else {
            this.initBaseInfo();
        }
        return SUCCESS;
    }

    /**
     * Prepares the list
     * 
     */
    public void prepare() throws Exception {
        ActionContext context = ActionContext.getContext();
        Map<String, Object> session = context.getSession();
        String local = (String) session.get("locale");
        this.types = accountTypeService.getOptions(
                AccountType.class.getSimpleName(), local);
        this.industries = industryService.getOptions(
                Industry.class.getSimpleName(), local);
    }

    /**
     * @return the baseService
     */
    public IBaseService<Account> getBaseService() {
        return baseService;
    }

    /**
     * @param baseService
     *            the baseService to set
     */
    public void setBaseService(IBaseService<Account> baseService) {
        this.baseService = baseService;
    }

    /**
     * @return the accountTypeService
     */
    public IOptionService<AccountType> getAccountTypeService() {
        return accountTypeService;
    }

    /**
     * @param accountTypeService
     *            the accountTypeService to set
     */
    public void setAccountTypeService(
            IOptionService<AccountType> accountTypeService) {
        this.accountTypeService = accountTypeService;
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
     * @return the account
     */
    public Account getAccount() {
        return account;
    }

    /**
     * @param account
     *            the account to set
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * @return the types
     */
    public List<AccountType> getTypes() {
        return types;
    }

    /**
     * @param types
     *            the types to set
     */
    public void setTypes(List<AccountType> types) {
        this.types = types;
    }

    /**
     * @return the industries
     */
    public List<Industry> getIndustries() {
        return industries;
    }

    /**
     * @param industries
     *            the industries to set
     */
    public void setIndustries(List<Industry> industries) {
        this.industries = industries;
    }

    /**
     * @return the typeID
     */
    public Integer getTypeID() {
        return typeID;
    }

    /**
     * @param typeID
     *            the typeID to set
     */
    public void setTypeID(Integer typeID) {
        this.typeID = typeID;
    }

    /**
     * @return the industryID
     */
    public Integer getIndustryID() {
        return industryID;
    }

    /**
     * @param industryID
     *            the industryID to set
     */
    public void setIndustryID(Integer industryID) {
        this.industryID = industryID;
    }

    /**
     * @return the campaignID
     */
    public Integer getCampaignID() {
        return campaignID;
    }

    /**
     * @param campaignID
     *            the campaignID to set
     */
    public void setCampaignID(Integer campaignID) {
        this.campaignID = campaignID;
    }

    /**
     * @return the manageID
     */
    public Integer getManagerID() {
        return managerID;
    }

    /**
     * @param manageID
     *            the manageID to set
     */
    public void setManagerID(Integer managerID) {
        this.managerID = managerID;
    }

    /**
     * @return the campaignService
     */
    public IBaseService<Campaign> getCampaignService() {
        return campaignService;
    }

    /**
     * @param campaignService
     *            the campaignService to set
     */
    public void setCampaignService(IBaseService<Campaign> campaignService) {
        this.campaignService = campaignService;
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
     * @return the documentService
     */
    public IBaseService<Document> getDocumentService() {
        return documentService;
    }

    /**
     * @param documentService
     *            the documentService to set
     */
    public void setDocumentService(IBaseService<Document> documentService) {
        this.documentService = documentService;
    }

    /**
     * @return the managerText
     */
    public String getManagerText() {
        return managerText;
    }

    /**
     * @param managerText
     *            the managerText to set
     */
    public void setManagerText(String managerText) {
        this.managerText = managerText;
    }

    /**
     * @return the industryService
     */
    public IOptionService<Industry> getIndustryService() {
        return industryService;
    }

    /**
     * @param industryService
     *            the industryService to set
     */
    public void setIndustryService(IOptionService<Industry> industryService) {
        this.industryService = industryService;
    }

    /**
     * @return the changeLogService
     */
    public IBaseService<ChangeLog> getChangeLogService() {
        return changeLogService;
    }

    /**
     * @param changeLogService
     *            the changeLogService to set
     */
    public void setChangeLogService(IBaseService<ChangeLog> changeLogService) {
        this.changeLogService = changeLogService;
    }

    /**
     * @return the taskExecutor
     */
    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    /**
     * @param taskExecutor
     *            the taskExecutor to set
     */
    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

}
