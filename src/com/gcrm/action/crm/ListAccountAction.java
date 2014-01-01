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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.gcrm.domain.Account;
import com.gcrm.domain.AccountLevel;
import com.gcrm.domain.AccountNature;
import com.gcrm.domain.AccountType;
import com.gcrm.domain.AnnualRevenue;
import com.gcrm.domain.Campaign;
import com.gcrm.domain.Capital;
import com.gcrm.domain.CompanySize;
import com.gcrm.domain.Currency;
import com.gcrm.domain.Document;
import com.gcrm.domain.Industry;
import com.gcrm.domain.TargetList;
import com.gcrm.domain.User;
import com.gcrm.exception.ServiceException;
import com.gcrm.service.IBaseService;
import com.gcrm.service.IOptionService;
import com.gcrm.util.CommonUtil;
import com.gcrm.util.Constant;
import com.gcrm.util.security.UserUtil;
import com.gcrm.vo.SearchCondition;
import com.gcrm.vo.SearchResult;

/**
 * Lists Account
 * 
 */
public class ListAccountAction extends BaseListAction {

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
    private Account account;

    private static final String CLAZZ = Account.class.getSimpleName();

    /**
     * Gets the list data.
     * 
     * @return null
     */
    @Override
    public String list() throws Exception {

        SearchCondition searchCondition = getSearchCondition();
        SearchResult<Account> result = baseService.getPaginationObjects(CLAZZ,
                searchCondition);
        Iterator<Account> accounts = result.getResult().iterator();
        long totalRecords = result.getTotalRecords();
        getListJson(accounts, totalRecords, null, false);
        return null;
    }

    /**
     * Gets the list data.
     * 
     * @return null
     */
    public String listFull() throws Exception {
        UserUtil.permissionCheck("view_account");

        Map<String, String> fieldTypeMap = new HashMap<String, String>();
        fieldTypeMap.put("created_on", Constant.DATA_TYPE_DATETIME);
        fieldTypeMap.put("updated_on", Constant.DATA_TYPE_DATETIME);
        User loginUser = UserUtil.getLoginUser();
        SearchCondition searchCondition = getSearchCondition(fieldTypeMap,
                loginUser.getScope_account(), loginUser);
        SearchResult<Account> result = baseService.getPaginationObjects(CLAZZ,
                searchCondition);
        Iterator<Account> accounts = result.getResult().iterator();
        long totalRecords = result.getTotalRecords();
        getListJson(accounts, totalRecords, searchCondition, true);
        return null;
    }

    /**
     * Gets the list JSON data.
     * 
     * @return list JSON data
     */
    public static void getListJson(Iterator<Account> accounts,
            long totalRecords, SearchCondition searchCondition, boolean isList)
            throws Exception {

        StringBuilder jsonBuilder = new StringBuilder("");
        String assignedTo = null;
        jsonBuilder
                .append(getJsonHeader(totalRecords, searchCondition, isList));
        while (accounts.hasNext()) {
            Account instance = accounts.next();
            int id = instance.getId();
            String name = CommonUtil.fromNullToEmpty(instance.getName());
            String officePhone = CommonUtil.fromNullToEmpty(instance
                    .getOffice_phone());
            String email = CommonUtil.fromNullToEmpty(instance.getEmail());
            User user = instance.getAssigned_to();
            if (user != null) {
                assignedTo = CommonUtil.fromNullToEmpty(user.getName());
            } else {
                assignedTo = "";
            }

            String billStreet = CommonUtil.fromNullToEmpty(instance
                    .getBill_street());
            String billCity = CommonUtil.fromNullToEmpty(instance
                    .getBill_city());
            String billCountry = CommonUtil.fromNullToEmpty(instance
                    .getBill_country());
            String billState = CommonUtil.fromNullToEmpty(instance
                    .getBill_state());
            String billPostalCode = CommonUtil.fromNullToEmpty(instance
                    .getBill_postal_code());
            if (isList) {
                jsonBuilder.append("{\"cell\":[\"").append(id).append("\",\"")
                        .append(name).append("\",\"").append(officePhone)
                        .append("\",\"").append(email).append("\"");
                String website = CommonUtil.fromNullToEmpty(instance
                        .getWebsite());
                String fax = CommonUtil.fromNullToEmpty(instance.getFax());
                String shipStreet = CommonUtil.fromNullToEmpty(instance
                        .getShip_street());
                String shipCity = CommonUtil.fromNullToEmpty(instance
                        .getBill_city());
                String shipState = CommonUtil.fromNullToEmpty(instance
                        .getBill_state());
                String shipPostalCode = CommonUtil.fromNullToEmpty(instance
                        .getShip_postal_code());
                String shipCountry = CommonUtil.fromNullToEmpty(instance
                        .getShip_country());
                String sicCode = CommonUtil.fromNullToEmpty(instance
                        .getSic_code());
                String ticketSymbol = CommonUtil.fromNullToEmpty(instance
                        .getTicket_symbol());
                Account manager = instance.getManager();
                String managerName = "";
                if (manager != null) {
                    managerName = CommonUtil.fromNullToEmpty(manager.getName());
                }
                User createdBy = instance.getCreated_by();
                String createdByName = "";
                if (createdBy != null) {
                    createdByName = CommonUtil.fromNullToEmpty(createdBy
                            .getName());
                }
                User updatedBy = instance.getUpdated_by();
                String updatedByName = "";
                if (updatedBy != null) {
                    updatedByName = CommonUtil.fromNullToEmpty(updatedBy
                            .getName());
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        Constant.DATE_TIME_FORMAT);
                Date createdOn = instance.getCreated_on();
                String createdOnName = "";
                if (createdOn != null) {
                    createdOnName = dateFormat.format(createdOn);
                }
                Date updatedOn = instance.getUpdated_on();
                String updatedOnName = "";
                if (updatedOn != null) {
                    updatedOnName = dateFormat.format(updatedOn);
                }
                jsonBuilder.append(",\"").append(billStreet).append("\",\"")
                        .append(billCity).append("\",\"").append(billState)
                        .append("\",\"").append(billCountry).append("\",\"")
                        .append(billPostalCode).append("\",\"")
                        .append(assignedTo).append("\",\"").append(website)
                        .append("\",\"").append(fax).append("\",\"")
                        .append(shipStreet).append("\",\"").append(shipCity)
                        .append("\",\"").append(shipState).append("\",\"")
                        .append(shipCountry).append("\",\"")
                        .append(shipPostalCode).append("\",\"").append(sicCode)
                        .append("\",\"").append(ticketSymbol).append("\",\"")
                        .append(managerName).append("\",\"")
                        .append(createdByName).append("\",\"")
                        .append(updatedByName).append("\",\"")
                        .append(createdOnName).append("\",\"")
                        .append(updatedOnName).append("\"]}");
            } else {
                jsonBuilder.append("{\"id\":\"").append(id)
                        .append("\",\"name\":\"").append(name)
                        .append("\",\"office_phone\":\"").append(officePhone)
                        .append("\",\"email\":\"").append(email)
                        .append("\",\"bill_street\":\"").append(billStreet)
                        .append("\",\"bill_city\":\"").append(billCity)
                        .append("\",\"bill_state\":\"").append(billState)
                        .append("\",\"bill_country\":\"").append(billCountry)
                        .append("\",\"bill_postal_code\":\"")
                        .append(billPostalCode)
                        .append("\",\"assigned_to.name\":\"")
                        .append(assignedTo).append("\"}");
            }
            if (accounts.hasNext()) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]}");

        // Returns JSON data back to page
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(jsonBuilder.toString());
    }

    /**
     * Selects the entities
     * 
     * @return the SUCCESS result
     */
    public String select() throws ServiceException {
        TargetList targetList = null;
        Document document = null;
        Set<Account> accounts = null;

        if ("TargetList".equals(this.getRelationKey())) {
            targetList = targetListService.getEntityById(TargetList.class,
                    Integer.valueOf(this.getRelationValue()));
            accounts = targetList.getAccounts();
        } else if ("Document".equals(this.getRelationKey())) {
            document = documentService.getEntityById(Document.class,
                    Integer.valueOf(this.getRelationValue()));
            accounts = document.getAccounts();
        }

        if (this.getSeleteIDs() != null) {
            String[] ids = seleteIDs.split(",");
            for (int i = 0; i < ids.length; i++) {
                String selectId = ids[i];
                account = baseService.getEntityById(Account.class,
                        Integer.valueOf(selectId));
                accounts.add(account);
            }
        }

        if ("TargetList".equals(this.getRelationKey())) {
            targetListService.makePersistent(targetList);
        } else if ("Document".equals(this.getRelationKey())) {
            documentService.makePersistent(document);
        }
        return SUCCESS;
    }

    /**
     * Unselects the entities
     * 
     * @return the SUCCESS result
     */
    public String unselect() throws ServiceException {
        TargetList targetList = null;
        Document document = null;
        Set<Account> accounts = null;

        if ("TargetList".equals(this.getRelationKey())) {
            targetList = targetListService.getEntityById(TargetList.class,
                    Integer.valueOf(this.getRelationValue()));
            accounts = targetList.getAccounts();
        } else if ("Document".equals(this.getRelationKey())) {
            document = documentService.getEntityById(Document.class,
                    Integer.valueOf(this.getRelationValue()));
            accounts = document.getAccounts();
        }

        if (this.getSeleteIDs() != null) {
            String[] ids = seleteIDs.split(",");
            Collection<Account> selectedAccounts = new ArrayList<Account>();
            for (int i = 0; i < ids.length; i++) {
                Integer selectId = Integer.valueOf(ids[i]);
                A: for (Account account : accounts) {
                    if (account.getId().intValue() == selectId.intValue()) {
                        selectedAccounts.add(account);
                        break A;
                    }
                }
            }
            accounts.removeAll(selectedAccounts);
        }

        if ("TargetList".equals(this.getRelationKey())) {
            targetListService.makePersistent(targetList);
        } else if ("Document".equals(this.getRelationKey())) {
            documentService.makePersistent(document);
        }
        return SUCCESS;
    }

    /**
     * Gets the related documents.
     * 
     * @return null
     */
    public String relateAccountDocument() throws Exception {
        account = baseService.getEntityById(Account.class, id);
        Set<Document> documents = account.getDocuments();
        Iterator<Document> documentIterator = documents.iterator();
        long totalRecords = documents.size();
        ListDocumentAction.getListJson(documentIterator, totalRecords, null,
                false);
        return null;
    }

    /**
     * Deletes the selected entities.
     * 
     * @return the SUCCESS result
     */
    public String delete() throws Exception {
        UserUtil.permissionCheck("delete_account");
        baseService.batchDeleteEntity(Account.class, this.getSeleteIDs());
        return SUCCESS;
    }

    /**
     * Removes the related entities
     * 
     * @return the SUCCESS result
     */
    public String remove() throws ServiceException {
        if (this.getSeleteIDs() != null) {
            String[] ids = seleteIDs.split(",");
            for (int i = 0; i < ids.length; i++) {
                String removeId = ids[i];
                account = baseService.getEntityById(Account.class,
                        Integer.valueOf(removeId));
                if ("Account".endsWith(super.getRemoveKey())) {
                    account.setManager(null);
                }
                this.baseService.makePersistent(account);
            }
        }
        return SUCCESS;
    }

    /**
     * Copies the selected entities
     * 
     * @return the SUCCESS result
     */
    public String copy() throws Exception {
        UserUtil.permissionCheck("create_account");
        if (this.getSeleteIDs() != null) {
            String[] ids = seleteIDs.split(",");
            for (int i = 0; i < ids.length; i++) {
                String copyid = ids[i];
                Account oriRecord = baseService.getEntityById(Account.class,
                        Integer.valueOf(copyid));
                Account targetRecord = oriRecord.clone();
                targetRecord.setId(null);
                this.getbaseService().makePersistent(targetRecord);
            }
        }
        return SUCCESS;
    }

    /**
     * Exports the entities
     * 
     * @return the exported entities inputStream
     */
    public InputStream getInputStream() throws Exception {
        return getDownloadContent(false);
    }

    /**
     * Exports the template
     * 
     * @return the exported template inputStream
     */
    public InputStream getTemplateStream() throws Exception {
        return getDownloadContent(true);
    }

    private InputStream getDownloadContent(boolean isTemplate) throws Exception {
        UserUtil.permissionCheck("view_account");

        String fileName = getText("entity.account.label") + ".csv";
        fileName = new String(fileName.getBytes(), "ISO8859-1");
        File file = new File(fileName);
        ICsvMapWriter writer = new CsvMapWriter(new FileWriter(file),
                CsvPreference.EXCEL_PREFERENCE);
        try {
            final String[] header = new String[] { getText("entity.id.label"),
                    getText("entity.name.label"),
                    getText("entity.account_level_id.label"),
                    getText("entity.account_level_name.label"),
                    getText("entity.currency_id.label"),
                    getText("entity.currency_name.label"),
                    getText("entity.capital_id.label"),
                    getText("entity.capital_name.label"),
                    getText("entity.annual_revenue_id.label"),
                    getText("entity.annual_revenue_name.label"),
                    getText("entity.company_size_id.label"),
                    getText("entity.company_size_name.label"),
                    getText("entity.account_type_id.label"),
                    getText("entity.account_type_name.label"),
                    getText("entity.industry_id.label"),
                    getText("entity.industry_name.label"),
                    getText("entity.email.label"),
                    getText("entity.office_phone.label"),
                    getText("entity.website.label"),
                    getText("entity.fax.label"),
                    getText("entity.billing_street.label"),
                    getText("entity.billing_city.label"),
                    getText("entity.billing_state.label"),
                    getText("entity.billing_postal_code.label"),
                    getText("entity.billing_country.label"),
                    getText("entity.shipping_street.label"),
                    getText("entity.shipping_city.label"),
                    getText("entity.shipping_state.label"),
                    getText("entity.shipping_postal_code.label"),
                    getText("entity.shipping_country.label"),
                    getText("entity.account_nature_id.label"),
                    getText("entity.account_nature_name.label"),
                    getText("entity.legal_representative.label"),
                    getText("entity.business_scope.label"),
                    getText("entity.create_date.label"),
                    getText("entity.credit.label"),
                    getText("entity.reputation.label"),
                    getText("entity.market_position.label"),
                    getText("entity.development_potential.label"),
                    getText("entity.operational_characteristics.label"),
                    getText("entity.operational_direction.label"),
                    getText("account.sic_code.label"),
                    getText("account.ticket_symbol.label"),
                    getText("account.manager_id.label"),
                    getText("account.manager_name.label"),
                    getText("entity.assigned_to_id.label"),
                    getText("entity.assigned_to_name.label") };
            writer.writeHeader(header);
            if (!isTemplate) {
                String[] ids = seleteIDs.split(",");
                for (int i = 0; i < ids.length; i++) {
                    String id = ids[i];
                    Account account = baseService.getEntityById(Account.class,
                            Integer.parseInt(id));
                    final HashMap<String, ? super Object> data1 = new HashMap<String, Object>();
                    data1.put(header[0], account.getId());
                    data1.put(header[1],
                            CommonUtil.fromNullToEmpty(account.getName()));
                    AccountLevel accountLevel = account.getAccount_level();
                    if (accountLevel != null) {
                        data1.put(header[2], accountLevel.getId());
                    } else {
                        data1.put(header[2], "");
                    }
                    data1.put(header[3],
                            CommonUtil.getOptionLabel(accountLevel));
                    if (account.getCurrency() != null) {
                        data1.put(header[4], account.getCurrency().getId());
                        data1.put(header[5], account.getCurrency().getName());
                    } else {
                        data1.put(header[4], "");
                        data1.put(header[5], "");
                    }
                    Capital capital = account.getCapital();
                    if (capital != null) {
                        data1.put(header[6], capital.getId());
                    } else {
                        data1.put(header[6], "");
                    }
                    data1.put(header[7], CommonUtil.getOptionLabel(capital));
                    AnnualRevenue annualRevenue = account.getAnnual_revenue();
                    if (annualRevenue != null) {
                        data1.put(header[8], annualRevenue.getId());
                    } else {
                        data1.put(header[8], "");
                    }
                    data1.put(header[9],
                            CommonUtil.getOptionLabel(annualRevenue));
                    CompanySize companySize = account.getCompany_size();
                    if (companySize != null) {
                        data1.put(header[10], companySize.getId());
                    } else {
                        data1.put(header[10], "");
                    }
                    data1.put(header[11],
                            CommonUtil.getOptionLabel(companySize));
                    AccountType accountType = account.getAccount_type();
                    if (accountType != null) {
                        data1.put(header[12], accountType.getId());
                    } else {
                        data1.put(header[12], "");
                    }
                    data1.put(header[13],
                            CommonUtil.getOptionLabel(accountType));
                    Industry industry = account.getIndustry();
                    if (industry != null) {
                        data1.put(header[14], industry.getId());
                    } else {
                        data1.put(header[14], "");
                    }
                    data1.put(header[15], CommonUtil.getOptionLabel(industry));
                    data1.put(header[16],
                            CommonUtil.fromNullToEmpty(account.getEmail()));
                    data1.put(header[17], CommonUtil.fromNullToEmpty(account
                            .getOffice_phone()));
                    data1.put(header[18],
                            CommonUtil.fromNullToEmpty(account.getWebsite()));
                    data1.put(header[19],
                            CommonUtil.fromNullToEmpty(account.getFax()));
                    data1.put(header[20], CommonUtil.fromNullToEmpty(account
                            .getBill_street()));
                    data1.put(header[21],
                            CommonUtil.fromNullToEmpty(account.getBill_city()));
                    data1.put(header[22],
                            CommonUtil.fromNullToEmpty(account.getBill_state()));
                    data1.put(header[23], CommonUtil.fromNullToEmpty(account
                            .getBill_postal_code()));
                    data1.put(header[24], CommonUtil.fromNullToEmpty(account
                            .getBill_country()));
                    data1.put(header[25], CommonUtil.fromNullToEmpty(account
                            .getShip_street()));
                    data1.put(header[26],
                            CommonUtil.fromNullToEmpty(account.getShip_city()));
                    data1.put(header[27],
                            CommonUtil.fromNullToEmpty(account.getShip_state()));
                    data1.put(header[28], CommonUtil.fromNullToEmpty(account
                            .getShip_postal_code()));
                    data1.put(header[29], CommonUtil.fromNullToEmpty(account
                            .getShip_country()));
                    AccountNature accountNature = account.getAccount_nature();
                    if (accountNature != null) {
                        data1.put(header[30], accountNature.getId());
                    } else {
                        data1.put(header[30], "");
                    }
                    data1.put(header[31],
                            CommonUtil.getOptionLabel(accountNature));
                    data1.put(header[32], CommonUtil.fromNullToEmpty(account
                            .getLegal_representative()));
                    data1.put(header[33], CommonUtil.fromNullToEmpty(account
                            .getBusiness_scope()));
                    Date createDate = account.getCreate_date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                            Constant.DATE_EDIT_FORMAT);
                    if (createDate != null) {
                        data1.put(header[34], dateFormat.format(createDate));
                    } else {
                        data1.put(header[34], "");
                    }
                    data1.put(header[35],
                            CommonUtil.fromNullToEmpty(account.getCredit()));
                    data1.put(header[36],
                            CommonUtil.fromNullToEmpty(account.getReputation()));
                    data1.put(header[37], CommonUtil.fromNullToEmpty(account
                            .getMarket_position()));
                    data1.put(header[38], CommonUtil.fromNullToEmpty(account
                            .getDevelopment_potential()));
                    data1.put(header[39], CommonUtil.fromNullToEmpty(account
                            .getOperational_characteristics()));
                    data1.put(header[40], CommonUtil.fromNullToEmpty(account
                            .getOperational_direction()));
                    data1.put(header[41],
                            CommonUtil.fromNullToEmpty(account.getSic_code()));
                    data1.put(header[42], CommonUtil.fromNullToEmpty(account
                            .getTicket_symbol()));
                    if (account.getManager() != null) {
                        data1.put(header[43], account.getManager().getId());
                        data1.put(header[44], account.getManager().getName());
                    } else {
                        data1.put(header[43], "");
                        data1.put(header[44], "");
                    }
                    if (account.getAssigned_to() != null) {
                        data1.put(header[45], account.getAssigned_to().getId());
                        data1.put(header[46], account.getAssigned_to()
                                .getName());
                    } else {
                        data1.put(header[45], "");
                        data1.put(header[46], "");
                    }
                    writer.write(data1, header);
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            writer.close();
        }

        InputStream in = new FileInputStream(file);
        this.setFileName(fileName);
        return in;
    }

    /**
     * Imports the entities
     * 
     * @return the SUCCESS result
     */
    public String importCSV() throws Exception {
        File file = this.getUpload();
        CsvListReader reader = new CsvListReader(new FileReader(file),
                CsvPreference.EXCEL_PREFERENCE);
        int failedNum = 0;
        int successfulNum = 0;
        try {
            final String[] header = reader.getCSVHeader(true);

            List<String> line = new ArrayList<String>();
            Map<String, String> failedMsg = new HashMap<String, String>();
            while ((line = reader.read()) != null) {

                Map<String, String> row = new HashMap<String, String>();
                for (int i = 0; i < line.size(); i++) {
                    row.put(header[i], line.get(i));
                }

                Account account = new Account();
                try {
                    String id = row.get(getText("entity.id.label"));
                    if (!CommonUtil.isNullOrEmpty(id)) {
                        account.setId(Integer.parseInt(id));
                        UserUtil.permissionCheck("update_account");
                    } else {
                        UserUtil.permissionCheck("create_account");
                    }
                    account.setName(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.name.label"))));
                    String typeID = row.get(getText("account.type_id.label"));
                    if (CommonUtil.isNullOrEmpty(typeID)) {
                        account.setAccount_type(null);
                    } else {
                        AccountType accountType = accountTypeService
                                .getEntityById(AccountType.class,
                                        Integer.parseInt(typeID));
                        account.setAccount_type(accountType);
                    }
                    String accountLevelID = row
                            .get(getText("entity.account_level_id.label"));
                    if (CommonUtil.isNullOrEmpty(accountLevelID)) {
                        account.setAccount_type(null);
                    } else {
                        AccountLevel accountLevel = accountLevelService
                                .getEntityById(AccountLevel.class,
                                        Integer.parseInt(accountLevelID));
                        account.setAccount_level(accountLevel);
                    }
                    String currencyID = row
                            .get(getText("entity.currency_id.label"));
                    if (CommonUtil.isNullOrEmpty(currencyID)) {
                        account.setCurrency(null);
                    } else {
                        Currency currency = currencyService.getEntityById(
                                Currency.class, Integer.parseInt(currencyID));
                        account.setCurrency(currency);
                    }
                    String capitalID = row
                            .get(getText("entity.capital_id.label"));
                    if (CommonUtil.isNullOrEmpty(capitalID)) {
                        account.setCapital(null);
                    } else {
                        Capital capital = capitalService.getEntityById(
                                Capital.class, Integer.parseInt(capitalID));
                        account.setCapital(capital);
                    }
                    String annualRevenueID = row
                            .get(getText("entity.annual_revenue_id.label"));
                    if (CommonUtil.isNullOrEmpty(annualRevenueID)) {
                        account.setAnnual_revenue(null);
                    } else {
                        AnnualRevenue annualRevenue = annualRevenueService
                                .getEntityById(AnnualRevenue.class,
                                        Integer.parseInt(annualRevenueID));
                        account.setAnnual_revenue(annualRevenue);
                    }
                    String companySizeID = row
                            .get(getText("entity.company_size_id.label"));
                    if (CommonUtil.isNullOrEmpty(companySizeID)) {
                        account.setCompany_size(null);
                    } else {
                        CompanySize companySize = companySizeService
                                .getEntityById(CompanySize.class,
                                        Integer.parseInt(companySizeID));
                        account.setCompany_size(companySize);
                    }
                    String accountTypeID = row
                            .get(getText("entity.account_type_id.label"));
                    if (CommonUtil.isNullOrEmpty(accountTypeID)) {
                        account.setAccount_type(null);
                    } else {
                        AccountType accountType = accountTypeService
                                .getEntityById(AccountType.class,
                                        Integer.parseInt(accountTypeID));
                        account.setAccount_type(accountType);
                    }
                    String industryID = row
                            .get(getText("entity.industry_id.label"));
                    if (CommonUtil.isNullOrEmpty(industryID)) {
                        account.setIndustry(null);
                    } else {
                        Industry industry = industryService.getEntityById(
                                Industry.class, Integer.parseInt(industryID));
                        account.setIndustry(industry);
                    }
                    account.setEmail(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.email.label"))));
                    account.setOffice_phone(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.office_phone.label"))));
                    account.setWebsite(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.website.label"))));
                    account.setFax(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.fax.label"))));
                    account.setBill_street(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.billing_street.label"))));
                    account.setBill_city(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.billing_city.label"))));
                    account.setBill_state(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.billing_state.label"))));
                    account.setBill_postal_code(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.billing_postal_code.label"))));
                    account.setBill_country(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.billing_country.label"))));
                    account.setShip_street(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.shipping_street.label"))));
                    account.setShip_city(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.shipping_city.label"))));
                    account.setShip_state(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.shipping_state.label"))));
                    account.setShip_postal_code(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.shipping_postal_code.label"))));
                    account.setShip_country(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.shipping_country.label"))));
                    String accountNatureID = row
                            .get(getText("entity.account_nature_id.label"));
                    if (CommonUtil.isNullOrEmpty(accountNatureID)) {
                        account.setAccount_nature(null);
                    } else {
                        AccountNature accountNature = accountNatureService
                                .getEntityById(AccountNature.class,
                                        Integer.parseInt(accountNatureID));
                        account.setAccount_nature(accountNature);
                    }
                    account.setLegal_representative(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.legal_representative.label"))));
                    account.setBusiness_scope(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.business_scope.label"))));
                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                            Constant.DATE_EDIT_FORMAT);
                    String createDateS = row
                            .get(getText("entity.create_date.label"));
                    if (createDateS != null) {
                        Date createDate = dateFormat.parse(createDateS);
                        account.setCreate_date(createDate);
                    } else {
                        account.setCreate_date(null);
                    }
                    account.setCredit(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.credit.label"))));
                    account.setMarket_position(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.market_position.label"))));
                    account.setDevelopment_potential(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.development_potential.label"))));
                    account.setOperational_characteristics(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.operational_characteristics.label"))));
                    account.setOperational_direction(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.operational_direction.label"))));
                    account.setSic_code(CommonUtil.fromNullToEmpty(row
                            .get(getText("account.sic_code.label"))));
                    account.setTicket_symbol(CommonUtil.fromNullToEmpty(row
                            .get(getText("account.ticket_symbol.label"))));
                    String managerID = row
                            .get(getText("account.manager_id.label"));
                    if (CommonUtil.isNullOrEmpty(managerID)) {
                        account.setManager(null);
                    } else {
                        Account manager = baseService.getEntityById(
                                Account.class, Integer.parseInt(managerID));
                        account.setManager(manager);
                    }
                    String assignedToID = row
                            .get(getText("entity.assigned_to_id.label"));
                    if (CommonUtil.isNullOrEmpty(assignedToID)) {
                        account.setAssigned_to(null);
                    } else {
                        User assignedTo = userService.getEntityById(User.class,
                                Integer.parseInt(assignedToID));
                        account.setAssigned_to(assignedTo);
                    }
                    baseService.makePersistent(account);
                    successfulNum++;
                } catch (Exception e) {
                    failedNum++;
                    failedMsg.put(account.getName(), e.getMessage());
                }

            }

            this.setFailedMsg(failedMsg);
            this.setFailedNum(failedNum);
            this.setSuccessfulNum(successfulNum);
            this.setTotalNum(successfulNum + failedNum);
        } finally {
            reader.close();
        }
        return SUCCESS;
    }

    @Override
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Override
    protected String getEntityName() {
        return Account.class.getSimpleName();
    }

    public IBaseService<Account> getbaseService() {
        return baseService;
    }

    public void setbaseService(IBaseService<Account> baseService) {
        this.baseService = baseService;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * @return the id
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
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
     * @param accountTypeService
     *            the accountTypeService to set
     */
    public void setAccountTypeService(
            IOptionService<AccountType> accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    /**
     * @return the accountTypeService
     */
    public IOptionService<AccountType> getAccountTypeService() {
        return accountTypeService;
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

}
