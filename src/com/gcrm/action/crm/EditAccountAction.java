/**
 * Copyright (C) 2012 - 2013, Grass CRM Studio
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
package com.gcrm.action.crm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.springframework.core.task.TaskExecutor;

import com.gcrm.domain.Account;
import com.gcrm.domain.AccountLevel;
import com.gcrm.domain.AccountNature;
import com.gcrm.domain.AccountType;
import com.gcrm.domain.AnnualRevenue;
import com.gcrm.domain.Campaign;
import com.gcrm.domain.Capital;
import com.gcrm.domain.ChangeLog;
import com.gcrm.domain.CompanySize;
import com.gcrm.domain.Currency;
import com.gcrm.domain.Document;
import com.gcrm.domain.Industry;
import com.gcrm.domain.TargetList;
import com.gcrm.domain.User;
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
    private IOptionService<AccountLevel> accountLevelService;
    private IOptionService<Capital> capitalService;
    private IOptionService<AnnualRevenue> annualRevenueService;
    private IOptionService<CompanySize> companySizeService;
    private IOptionService<AccountNature> accountNatureService;
    private IOptionService<Industry> industryService;
    private IBaseService<Currency> currencyService;
    private IBaseService<User> userService;
    private IBaseService<Campaign> campaignService;
    private IBaseService<TargetList> targetListService;
    private IBaseService<Document> documentService;
    private IBaseService<ChangeLog> changeLogService;
    private TaskExecutor taskExecutor;
    private Account account;
    private List<AccountType> types;
    private List<AccountLevel> accountLevels;
    private List<Currency> currencies;
    private List<Capital> capitals;
    private List<AnnualRevenue> annualRevenues;
    private List<CompanySize> companySizes;
    private List<AccountNature> accountNatures;
    private List<Industry> industries;
    private Integer typeID = null;
    private Integer accountLevelID = null;
    private Integer currencyID = null;
    private Integer capitalID = null;
    private Integer annualRevenueID = null;
    private Integer companySizeID = null;
    private Integer accountNatureID = null;
    private Integer industryID = null;
    private Integer campaignID = null;
    private Integer managerID = null;
    private String managerText = null;
    private String createDate = null;

    /**
     * Saves the entity.
     * 
     * @return the SUCCESS result
     */
    public String save() throws Exception {
        final Account originalAccount = saveEntity();

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
        User loginUser = this.getLoginUser();
        final Collection<ChangeLog> changeLogs = changeLog(originalAccount,
                account, loginUser);
        account = getBaseService().makePersistent(account);
        this.setId(account.getId());
        this.setSaveFlag("true");

        if (originalAccount != null) {
            taskExecutor.execute(new Runnable() {
                public void run() {
                    batchInserChangeLogs(changeLogs);
                }
            });
        }
        return SUCCESS;
    }

    /**
     * Batch update change log
     * 
     * @param changeLogs
     *            change log collection
     */
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
            final User loginUser = this.getLoginUser();
            User user = userService
                    .getEntityById(User.class, loginUser.getId());
            final List<Account> originalAccounts = new ArrayList<Account>();
            final List<Account> currentAccounts = new ArrayList<Account>();
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

                originalAccounts.add(originalAccount);
                currentAccounts.add(accountInstance);
                accounts.add(accountInstance);
            }

            if (accounts.size() > 0) {
                this.baseService.batchUpdate(accounts);
                final Collection<ChangeLog> allChangeLogs = genBAChangeLog(
                        originalAccounts, currentAccounts, loginUser);
                taskExecutor.execute(new Runnable() {
                    public void run() {
                        batchInserChangeLogs(allChangeLogs);
                    }
                });
            }
        }
        return SUCCESS;
    }

    /**
     * Generates change log for batch account update
     * 
     * @param originalAccount
     *            original account
     * @param account
     *            current accounts
     * @param loginUser
     *            current login user
     * @return change log collection
     * 
     */
    private Collection<ChangeLog> genBAChangeLog(
            List<Account> originalAccounts, List<Account> currentAccounts,
            User loginUser) {
        Collection<ChangeLog> allChangeLogs = new ArrayList<ChangeLog>();
        Account originalAccount = null;
        Account currentAccount = null;
        for (int i = 0; i < originalAccounts.size(); i++) {
            originalAccount = originalAccounts.get(i);
            currentAccount = currentAccounts.get(i);
            Collection<ChangeLog> changeLogs = changeLog(originalAccount,
                    currentAccount, loginUser);
            allChangeLogs.addAll(changeLogs);
        }
        return allChangeLogs;
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
            account.setCreated_on(originalAccount.getCreated_on());
            account.setCreated_by(originalAccount.getCreated_by());
        }
        AccountType type = null;
        if (typeID != null) {
            type = accountTypeService.getOptionById(AccountType.class, typeID);
        }
        account.setAccount_type(type);

        AccountLevel accountLevel = null;
        if (accountLevelID != null) {
            accountLevel = accountLevelService.getOptionById(
                    AccountLevel.class, accountLevelID);
        }
        account.setAccount_level(accountLevel);

        Currency currency = null;
        if (currencyID != null) {
            currency = currencyService
                    .getEntityById(Currency.class, currencyID);
        }
        account.setCurrency(currency);

        Capital capital = null;
        if (capitalID != null) {
            capital = capitalService.getOptionById(Capital.class, capitalID);
        }
        account.setCapital(capital);

        AnnualRevenue annualRevenue = null;
        if (annualRevenueID != null) {
            annualRevenue = annualRevenueService.getOptionById(
                    AnnualRevenue.class, annualRevenueID);
        }
        account.setAnnual_revenue(annualRevenue);

        CompanySize companySize = null;
        if (companySizeID != null) {
            companySize = companySizeService.getOptionById(CompanySize.class,
                    companySizeID);
        }
        account.setCompany_size(companySize);

        AccountNature accountNature = null;
        if (accountNatureID != null) {
            accountNature = accountNatureService.getOptionById(
                    AccountNature.class, accountNatureID);
        }
        account.setAccount_nature(accountNature);

        Industry industry = null;
        if (industryID != null) {
            industry = industryService
                    .getOptionById(Industry.class, industryID);
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

        SimpleDateFormat dateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);
        Date create_date = null;
        if (!CommonUtil.isNullOrEmpty(createDate)) {
            create_date = dateFormat.parse(createDate);
        }
        account.setCreate_date(create_date);
        super.updateBaseInfo(account);
        return originalAccount;
    }

    /**
     * Creates change log
     * 
     * @param originalAccount
     *            original account record
     * @param account
     *            current account record
     * @param loginUser
     *            current login user
     * @return change log collections
     */
    private Collection<ChangeLog> changeLog(Account originalAccount,
            Account account, User loginUser) {
        Collection<ChangeLog> changeLogs = null;
        if (originalAccount != null) {
            String entityName = Account.class.getSimpleName();
            Integer recordID = account.getId();
            changeLogs = new ArrayList<ChangeLog>();

            String oldName = CommonUtil.fromNullToEmpty(originalAccount
                    .getName());
            String newName = CommonUtil.fromNullToEmpty(account.getName());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.name.label", oldName, newName, loginUser);

            String oldAccountLevel = getOptionValue(originalAccount
                    .getAccount_level());
            String newAccountLevel = getOptionValue(account.getAccount_level());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.account_level.label", oldAccountLevel,
                    newAccountLevel, loginUser);

            String oldCurrencyName = "";
            Currency oldCurrency = originalAccount.getCurrency();
            if (oldCurrency != null) {
                oldCurrencyName = CommonUtil.fromNullToEmpty(oldCurrency
                        .getName());
            }
            String newCurrencyName = "";
            Currency newCurrency = account.getCurrency();
            if (newCurrency != null) {
                newCurrencyName = CommonUtil.fromNullToEmpty(newCurrency
                        .getName());
            }
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.currency.label", oldCurrencyName, newCurrencyName,
                    loginUser);

            String oldCapital = getOptionValue(originalAccount.getCapital());
            String newCapital = getOptionValue(account.getCapital());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.capital.label", oldCapital, newCapital, loginUser);

            String oldAnnualRevenue = getOptionValue(originalAccount
                    .getAnnual_revenue());
            String newAnnualRevenue = getOptionValue(account
                    .getAnnual_revenue());
            createChangeLog(changeLogs, entityName, recordID,
                    "menu.annualRevenue.title", oldAnnualRevenue,
                    newAnnualRevenue, loginUser);

            String oldCompanySize = getOptionValue(originalAccount
                    .getCompany_size());
            String newCompanySize = getOptionValue(account.getCompany_size());
            createChangeLog(changeLogs, entityName, recordID,
                    "menu.companySize.title", oldCompanySize, newCompanySize,
                    loginUser);

            String oldAccountType = getOptionValue(originalAccount
                    .getAccount_type());
            String newAccountType = getOptionValue(account.getAccount_type());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.type.label", oldAccountType, newAccountType,
                    loginUser);

            String oldIndustry = getOptionValue(originalAccount.getIndustry());
            String newIndustry = getOptionValue(account.getIndustry());
            createChangeLog(changeLogs, entityName, recordID,
                    "menu.industry.title", oldIndustry, newIndustry, loginUser);

            String oldOfficePhone = CommonUtil.fromNullToEmpty(originalAccount
                    .getOffice_phone());
            String newOfficePhone = CommonUtil.fromNullToEmpty(account
                    .getOffice_phone());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.office_phone.label", oldOfficePhone,
                    newOfficePhone, loginUser);

            String oldWebsite = CommonUtil.fromNullToEmpty(originalAccount
                    .getWebsite());
            String newWebsite = CommonUtil
                    .fromNullToEmpty(account.getWebsite());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.website.label", oldWebsite, newWebsite, loginUser);

            String oldFax = CommonUtil
                    .fromNullToEmpty(originalAccount.getFax());
            String newWFax = CommonUtil.fromNullToEmpty(account.getFax());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.fax.label", oldFax, newWFax, loginUser);

            String oldBillStreet = CommonUtil.fromNullToEmpty(originalAccount
                    .getBill_street());
            String newBillStreet = CommonUtil.fromNullToEmpty(account
                    .getBill_street());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.billing_street.label", oldBillStreet,
                    newBillStreet, loginUser);

            String oldBillState = CommonUtil.fromNullToEmpty(originalAccount
                    .getBill_state());
            String newBillState = CommonUtil.fromNullToEmpty(account
                    .getBill_state());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.billing_state.label", oldBillState, newBillState,
                    loginUser);

            String oldBillPostalCode = CommonUtil
                    .fromNullToEmpty(originalAccount.getBill_postal_code());
            String newBillPostalCode = CommonUtil.fromNullToEmpty(account
                    .getBill_postal_code());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.billing_postal_code.label", oldBillPostalCode,
                    newBillPostalCode, loginUser);

            String oldBillCountry = CommonUtil.fromNullToEmpty(originalAccount
                    .getBill_country());
            String newBillCountry = CommonUtil.fromNullToEmpty(account
                    .getBill_country());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.billing_country.label", oldBillCountry,
                    newBillCountry, loginUser);

            String oldShipStreet = CommonUtil.fromNullToEmpty(originalAccount
                    .getShip_street());
            String newShipStreet = CommonUtil.fromNullToEmpty(account
                    .getShip_street());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.shipping_street.label", oldShipStreet,
                    newShipStreet, loginUser);

            String oldShipState = CommonUtil.fromNullToEmpty(originalAccount
                    .getShip_state());
            String newShipState = CommonUtil.fromNullToEmpty(account
                    .getShip_state());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.shipping_state.label", oldShipState, newShipState,
                    loginUser);

            String oldShipPostalCode = CommonUtil
                    .fromNullToEmpty(originalAccount.getShip_postal_code());
            String newShipPostalCode = CommonUtil.fromNullToEmpty(account
                    .getShip_postal_code());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.shipping_postal_code.label", oldShipPostalCode,
                    newShipPostalCode, loginUser);

            String oldShipCountry = CommonUtil.fromNullToEmpty(originalAccount
                    .getShip_country());
            String newShipCountry = CommonUtil.fromNullToEmpty(account
                    .getShip_country());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.shipping_country.label", oldShipCountry,
                    newShipCountry, loginUser);

            String oldEmail = CommonUtil.fromNullToEmpty(originalAccount
                    .getEmail());
            String newEmail = CommonUtil.fromNullToEmpty(account.getEmail());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.email.label", oldEmail, newEmail, loginUser);

            String oldAccounNature = getOptionValue(originalAccount
                    .getAccount_nature());
            String newAccounNature = getOptionValue(account.getAccount_nature());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.account_nature.label", oldAccounNature,
                    newAccounNature, loginUser);

            String oldLegalRepresentative = CommonUtil
                    .fromNullToEmpty(originalAccount.getLegal_representative());
            String newLegalRepresentative = CommonUtil.fromNullToEmpty(account
                    .getLegal_representative());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.legal_representative.label",
                    oldLegalRepresentative, newLegalRepresentative, loginUser);

            String oldBusinessScope = CommonUtil
                    .fromNullToEmpty(originalAccount.getBusiness_scope());
            String newBusinessScope = CommonUtil.fromNullToEmpty(account
                    .getBusiness_scope());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.business_scope.label", oldBusinessScope,
                    newBusinessScope, loginUser);

            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    Constant.DATE_FORMAT);
            String oldCreateDateValue = "";
            Date oldCreateDate = originalAccount.getCreate_date();
            if (oldCreateDate != null) {
                oldCreateDateValue = dateFormat.format(oldCreateDate);
            }
            String newCreateDateValue = "";
            Date newCreateDate = account.getCreate_date();
            if (newCreateDate != null) {
                newCreateDateValue = dateFormat.format(newCreateDate);
            }
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.create_date.label", oldCreateDateValue,
                    newCreateDateValue, loginUser);

            String oldCredit = CommonUtil.fromNullToEmpty(originalAccount
                    .getCredit());
            String newCredit = CommonUtil.fromNullToEmpty(account.getCredit());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.credit.label", oldCredit, newCredit, loginUser);

            String oldReputation = CommonUtil.fromNullToEmpty(originalAccount
                    .getReputation());
            String newReputation = CommonUtil.fromNullToEmpty(account
                    .getReputation());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.reputation.label", oldReputation, newReputation,
                    loginUser);

            String oldMarketPosition = CommonUtil
                    .fromNullToEmpty(originalAccount.getMarket_position());
            String newMarketPosition = CommonUtil.fromNullToEmpty(account
                    .getMarket_position());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.market_position.label", oldMarketPosition,
                    newMarketPosition, loginUser);

            String oldDevelopmentPotential = CommonUtil
                    .fromNullToEmpty(originalAccount.getDevelopment_potential());
            String newDevelopmentPotential = CommonUtil.fromNullToEmpty(account
                    .getDevelopment_potential());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.development_potential.label",
                    oldDevelopmentPotential, newDevelopmentPotential, loginUser);

            String oldOperationalCharacteristics = CommonUtil
                    .fromNullToEmpty(originalAccount
                            .getOperational_characteristics());
            String newOperationalCharacteristics = CommonUtil
                    .fromNullToEmpty(account.getOperational_characteristics());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.operational_characteristics.label",
                    oldOperationalCharacteristics,
                    newOperationalCharacteristics, loginUser);

            String oldOperationalDirection = CommonUtil
                    .fromNullToEmpty(originalAccount.getOperational_direction());
            String newOperationalDirection = CommonUtil.fromNullToEmpty(account
                    .getOperational_direction());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.operational_direction.label",
                    oldOperationalDirection, newOperationalDirection, loginUser);

            String oldNotes = CommonUtil.fromNullToEmpty(originalAccount
                    .getNotes());
            String newNotes = CommonUtil.fromNullToEmpty(account.getNotes());
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.notes.label", oldNotes, newNotes, loginUser);

            String oldSicCode = CommonUtil.fromNullToEmpty(originalAccount
                    .getSic_code());
            String newSicCode = CommonUtil.fromNullToEmpty(account
                    .getSic_code());
            createChangeLog(changeLogs, entityName, recordID,
                    "account.sic_code.label", oldSicCode, newSicCode, loginUser);

            String oldTicketSymbol = CommonUtil.fromNullToEmpty(originalAccount
                    .getTicket_symbol());
            String newTicketSymbol = CommonUtil.fromNullToEmpty(account
                    .getTicket_symbol());
            createChangeLog(changeLogs, entityName, recordID,
                    "account.ticket_symbol.label", oldTicketSymbol,
                    newTicketSymbol, loginUser);

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
            createChangeLog(changeLogs, entityName, recordID,
                    "account.manager.label", oldManagerName, newManagerName,
                    loginUser);

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
            createChangeLog(changeLogs, entityName, recordID,
                    "entity.assigned_to.label",
                    CommonUtil.fromNullToEmpty(oldAssignedToName),
                    CommonUtil.fromNullToEmpty(newAssignedToName), loginUser);
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
            AccountLevel accountLevel = account.getAccount_level();
            if (accountLevel != null) {
                accountLevelID = accountLevel.getId();
            }
            AccountType type = account.getAccount_type();
            if (type != null) {
                typeID = type.getId();
            }
            Currency currency = account.getCurrency();
            if (currency != null) {
                currencyID = currency.getId();
            }
            AnnualRevenue annualRevenue = account.getAnnual_revenue();
            if (annualRevenue != null) {
                annualRevenueID = annualRevenue.getId();
            }
            Capital capital = account.getCapital();
            if (capital != null) {
                capitalID = capital.getId();
            }
            CompanySize companySize = account.getCompany_size();
            if (companySize != null) {
                companySizeID = companySize.getId();
            }
            AccountNature accountNature = account.getAccount_nature();
            if (accountNature != null) {
                accountNatureID = accountNature.getId();
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

            Date create_date = account.getCreate_date();
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    Constant.DATE_FORMAT);
            if (create_date != null) {
                createDate = dateFormat.format(create_date);
            }

            this.getBaseInfo(account, Account.class.getSimpleName(),
                    Constant.CRM_NAMESPACE);

        } else {
            this.initBaseInfo();
        }
        return SUCCESS;
    }

    /**
     * Gets Account Relation Counts
     * 
     * @return null
     */
    public String getAccountRelationCounts() throws Exception {
        long contactNumber = this.baseService
                .countsByParams(
                        "select count(contact.id) from Contact contact where account.id = ?",
                        new Integer[] { this.getId() });
        long opportunityNumber = this.baseService
                .countsByParams(
                        "select count(opportunity.id) from Opportunity opportunity where account.id = ?",
                        new Integer[] { this.getId() });
        long leadNumber = this.baseService.countsByParams(
                "select count(lead.id) from Lead lead where account.id = ?",
                new Integer[] { this.getId() });
        long accountNumber = this.baseService
                .countsByParams(
                        "select count(account.id) from Account account where manager.id = ?",
                        new Integer[] { this.getId() });
        long documentNumber = this.baseService
                .countsByParams(
                        "select count(*) from Account account join account.documents where account.id = ?",
                        new Integer[] { this.getId() });
        long caseNumber = this.baseService
                .countsByParams(
                        "select count(caseInstance.id) from CaseInstance caseInstance where account.id = ?",
                        new Integer[] { this.getId() });
        long taskNumber = this.baseService
                .countsByParams(
                        "select count(task.id) from Task task where related_object='Account' and related_record = ?",
                        new Integer[] { this.getId() });

        StringBuilder jsonBuilder = new StringBuilder("");
        jsonBuilder.append("{\"contactNumber\":\"").append(contactNumber)
                .append("\",\"opportunityNumber\":\"")
                .append(opportunityNumber).append("\",\"leadNumber\":\"")
                .append(leadNumber).append("\",\"accountNumber\":\"")
                .append(accountNumber).append("\",\"documentNumber\":\"")
                .append(documentNumber).append("\",\"caseNumber\":\"")
                .append(caseNumber).append("\",\"taskNumber\":\"")
                .append(taskNumber).append("\"}");
        // Returns JSON data back to page
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(jsonBuilder.toString());
        return null;
    }

    /**
     * Prepares the list
     * 
     */
    public void prepare() throws Exception {
        ActionContext context = ActionContext.getContext();
        Map<String, Object> session = context.getSession();
        String local = (String) session.get("locale");
        this.accountLevels = accountLevelService.getOptions(
                AccountLevel.class.getSimpleName(), local);
        this.types = accountTypeService.getOptions(
                AccountType.class.getSimpleName(), local);
        this.annualRevenues = annualRevenueService.getOptions(
                AnnualRevenue.class.getSimpleName(), local);
        this.capitals = capitalService.getOptions(
                Capital.class.getSimpleName(), local);
        this.companySizes = companySizeService.getOptions(
                CompanySize.class.getSimpleName(), local);
        this.accountNatures = accountNatureService.getOptions(
                AccountNature.class.getSimpleName(), local);
        this.industries = industryService.getOptions(
                Industry.class.getSimpleName(), local);
        this.currencies = currencyService.getAllObjects(Currency.class
                .getSimpleName());
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

    /**
     * @return the accountLevelService
     */
    public IOptionService<AccountLevel> getAccountLevelService() {
        return accountLevelService;
    }

    /**
     * @param accountLevelService
     *            the accountLevelService to set
     */
    public void setAccountLevelService(
            IOptionService<AccountLevel> accountLevelService) {
        this.accountLevelService = accountLevelService;
    }

    /**
     * @return the capitalService
     */
    public IOptionService<Capital> getCapitalService() {
        return capitalService;
    }

    /**
     * @param capitalService
     *            the capitalService to set
     */
    public void setCapitalService(IOptionService<Capital> capitalService) {
        this.capitalService = capitalService;
    }

    /**
     * @return the annualRevenueService
     */
    public IOptionService<AnnualRevenue> getAnnualRevenueService() {
        return annualRevenueService;
    }

    /**
     * @param annualRevenueService
     *            the annualRevenueService to set
     */
    public void setAnnualRevenueService(
            IOptionService<AnnualRevenue> annualRevenueService) {
        this.annualRevenueService = annualRevenueService;
    }

    /**
     * @return the companySizeService
     */
    public IOptionService<CompanySize> getCompanySizeService() {
        return companySizeService;
    }

    /**
     * @param companySizeService
     *            the companySizeService to set
     */
    public void setCompanySizeService(
            IOptionService<CompanySize> companySizeService) {
        this.companySizeService = companySizeService;
    }

    /**
     * @return the accountNatureService
     */
    public IOptionService<AccountNature> getAccountNatureService() {
        return accountNatureService;
    }

    /**
     * @param accountNatureService
     *            the accountNatureService to set
     */
    public void setAccountNatureService(
            IOptionService<AccountNature> accountNatureService) {
        this.accountNatureService = accountNatureService;
    }

    /**
     * @return the accountLevels
     */
    public List<AccountLevel> getAccountLevels() {
        return accountLevels;
    }

    /**
     * @param accountLevels
     *            the accountLevels to set
     */
    public void setAccountLevels(List<AccountLevel> accountLevels) {
        this.accountLevels = accountLevels;
    }

    /**
     * @return the capitals
     */
    public List<Capital> getCapitals() {
        return capitals;
    }

    /**
     * @param capitals
     *            the capitals to set
     */
    public void setCapitals(List<Capital> capitals) {
        this.capitals = capitals;
    }

    /**
     * @return the annualRevenues
     */
    public List<AnnualRevenue> getAnnualRevenues() {
        return annualRevenues;
    }

    /**
     * @param annualRevenues
     *            the annualRevenues to set
     */
    public void setAnnualRevenues(List<AnnualRevenue> annualRevenues) {
        this.annualRevenues = annualRevenues;
    }

    /**
     * @return the companySizes
     */
    public List<CompanySize> getCompanySizes() {
        return companySizes;
    }

    /**
     * @param companySizes
     *            the companySizes to set
     */
    public void setCompanySizes(List<CompanySize> companySizes) {
        this.companySizes = companySizes;
    }

    /**
     * @return the accountNatures
     */
    public List<AccountNature> getAccountNatures() {
        return accountNatures;
    }

    /**
     * @param accountNatures
     *            the accountNatures to set
     */
    public void setAccountNatures(List<AccountNature> accountNatures) {
        this.accountNatures = accountNatures;
    }

    /**
     * @return the accountLevelID
     */
    public Integer getAccountLevelID() {
        return accountLevelID;
    }

    /**
     * @param accountLevelID
     *            the accountLevelID to set
     */
    public void setAccountLevelID(Integer accountLevelID) {
        this.accountLevelID = accountLevelID;
    }

    /**
     * @return the capitalID
     */
    public Integer getCapitalID() {
        return capitalID;
    }

    /**
     * @param capitalID
     *            the capitalID to set
     */
    public void setCapitalID(Integer capitalID) {
        this.capitalID = capitalID;
    }

    /**
     * @return the annualRevenueID
     */
    public Integer getAnnualRevenueID() {
        return annualRevenueID;
    }

    /**
     * @param annualRevenueID
     *            the annualRevenueID to set
     */
    public void setAnnualRevenueID(Integer annualRevenueID) {
        this.annualRevenueID = annualRevenueID;
    }

    /**
     * @return the companySizeID
     */
    public Integer getCompanySizeID() {
        return companySizeID;
    }

    /**
     * @param companySizeID
     *            the companySizeID to set
     */
    public void setCompanySizeID(Integer companySizeID) {
        this.companySizeID = companySizeID;
    }

    /**
     * @return the accountNatureID
     */
    public Integer getAccountNatureID() {
        return accountNatureID;
    }

    /**
     * @param accountNatureID
     *            the accountNatureID to set
     */
    public void setAccountNatureID(Integer accountNatureID) {
        this.accountNatureID = accountNatureID;
    }

    /**
     * @return the currencies
     */
    public List<Currency> getCurrencies() {
        return currencies;
    }

    /**
     * @param currencies
     *            the currencies to set
     */
    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
    }

    /**
     * @return the currencyService
     */
    public IBaseService<Currency> getCurrencyService() {
        return currencyService;
    }

    /**
     * @param currencyService
     *            the currencyService to set
     */
    public void setCurrencyService(IBaseService<Currency> currencyService) {
        this.currencyService = currencyService;
    }

    /**
     * @return the currencyID
     */
    public Integer getCurrencyID() {
        return currencyID;
    }

    /**
     * @param currencyID
     *            the currencyID to set
     */
    public void setCurrencyID(Integer currencyID) {
        this.currencyID = currencyID;
    }

    /**
     * @return the createDate
     */
    public String getCreateDate() {
        return createDate;
    }

    /**
     * @param createDate
     *            the createDate to set
     */
    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
