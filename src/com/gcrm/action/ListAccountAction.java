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
package com.gcrm.action;

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
import com.gcrm.domain.AccountType;
import com.gcrm.domain.Campaign;
import com.gcrm.domain.Document;
import com.gcrm.domain.Industry;
import com.gcrm.domain.TargetList;
import com.gcrm.domain.User;
import com.gcrm.exception.ServiceException;
import com.gcrm.service.IBaseService;
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
    private IBaseService<AccountType> accountTypeService;
    private IBaseService<Industry> industryService;
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

            if (isList) {
                jsonBuilder.append("{\"cell\":[\"").append(id).append("\",\"")
                        .append(name).append("\",\"").append(officePhone)
                        .append("\",\"").append(email).append("\"");
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
                String annualRevenue = CommonUtil.fromNullToEmpty(instance
                        .getAnnual_revenue());
                String employees = CommonUtil.fromNullToEmpty(instance
                        .getEmployees());
                String sicCode = CommonUtil.fromNullToEmpty(instance
                        .getSic_code());
                String ticketSymbol = CommonUtil.fromNullToEmpty(instance
                        .getTicket_symbol());
                Account manager = instance.getManager();
                String managerName = "";
                if (manager != null) {
                    managerName = CommonUtil.fromNullToEmpty(manager.getName());
                }
                String ownship = CommonUtil.fromNullToEmpty(instance
                        .getOwnship());
                String rating = CommonUtil
                        .fromNullToEmpty(instance.getRating());
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
                        .append(shipPostalCode).append("\",\"")
                        .append(annualRevenue).append("\",\"")
                        .append(employees).append("\",\"").append(sicCode)
                        .append("\",\"").append(ticketSymbol).append("\",\"")
                        .append(managerName).append("\",\"").append(ownship)
                        .append("\",\"").append(rating).append("\",\"")
                        .append(createdByName).append("\",\"")
                        .append(updatedByName).append("\",\"")
                        .append(createdOnName).append("\",\"")
                        .append(updatedOnName).append("\"]}");
            } else {
                jsonBuilder.append("{\"id\":\"").append(id)
                        .append("\",\"name\":\"").append(name)
                        .append("\",\"office_phone\":\"").append(officePhone)
                        .append("\",\"email\":\"").append(email)
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
                    getText("entity.email.label"),
                    getText("entity.description.label"),
                    getText("account.type_id.label"),
                    getText("account.type_name.label"),
                    getText("entity.industry_id.label"),
                    getText("entity.industry_name.label"),
                    getText("account.annual_revenue.label"),
                    getText("account.market_value.label"),
                    getText("account.employees.label"),
                    getText("account.sic_code.label"),
                    getText("account.ticket_symbol.label"),
                    getText("account.manager_id.label"),
                    getText("account.manager_name.label"),
                    getText("account.ownship.label"),
                    getText("account.rating.label"),
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
                    data1.put(header[2], CommonUtil.fromNullToEmpty(account
                            .getOffice_phone()));
                    data1.put(header[3],
                            CommonUtil.fromNullToEmpty(account.getWebsite()));
                    data1.put(header[4],
                            CommonUtil.fromNullToEmpty(account.getFax()));
                    data1.put(header[5], CommonUtil.fromNullToEmpty(account
                            .getBill_street()));
                    data1.put(header[6],
                            CommonUtil.fromNullToEmpty(account.getBill_city()));
                    data1.put(header[7],
                            CommonUtil.fromNullToEmpty(account.getBill_state()));
                    data1.put(header[8], CommonUtil.fromNullToEmpty(account
                            .getBill_postal_code()));
                    data1.put(header[9], CommonUtil.fromNullToEmpty(account
                            .getBill_country()));
                    data1.put(header[10], CommonUtil.fromNullToEmpty(account
                            .getShip_street()));
                    data1.put(header[11],
                            CommonUtil.fromNullToEmpty(account.getShip_city()));
                    data1.put(header[12],
                            CommonUtil.fromNullToEmpty(account.getShip_state()));
                    data1.put(header[13], CommonUtil.fromNullToEmpty(account
                            .getShip_postal_code()));
                    data1.put(header[14], CommonUtil.fromNullToEmpty(account
                            .getShip_country()));
                    data1.put(header[15],
                            CommonUtil.fromNullToEmpty(account.getEmail()));
                    data1.put(header[16], CommonUtil.fromNullToEmpty(account
                            .getDescription()));
                    AccountType accountType = account.getAccount_type();
                    if (accountType != null) {
                        data1.put(header[17], accountType.getId());
                    } else {
                        data1.put(header[17], "");
                    }
                    data1.put(header[18],
                            CommonUtil.getOptionLabel(accountType));
                    Industry industry = account.getIndustry();
                    if (industry != null) {
                        data1.put(header[19], industry.getId());
                    } else {
                        data1.put(header[19], "");
                    }
                    data1.put(header[20], CommonUtil.getOptionLabel(industry));
                    data1.put(header[21], CommonUtil.fromNullToEmpty(account
                            .getAnnual_revenue()));
                    data1.put(header[22], CommonUtil.fromNullToEmpty(account
                            .getMarket_value()));
                    data1.put(header[23],
                            CommonUtil.fromNullToEmpty(account.getEmployees()));
                    data1.put(header[24],
                            CommonUtil.fromNullToEmpty(account.getSic_code()));
                    data1.put(header[25], CommonUtil.fromNullToEmpty(account
                            .getTicket_symbol()));
                    if (account.getManager() != null) {
                        data1.put(header[26], account.getManager().getId());
                        data1.put(header[27], account.getManager().getName());
                    } else {
                        data1.put(header[26], "");
                        data1.put(header[27], "");
                    }
                    data1.put(header[28],
                            CommonUtil.fromNullToEmpty(account.getOwnship()));
                    data1.put(header[29],
                            CommonUtil.fromNullToEmpty(account.getRating()));
                    if (account.getAssigned_to() != null) {
                        data1.put(header[30], account.getAssigned_to().getId());
                        data1.put(header[31], account.getAssigned_to()
                                .getName());
                    } else {
                        data1.put(header[30], "");
                        data1.put(header[31], "");
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
                    account.setEmail(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.email.label"))));
                    account.setDescription(CommonUtil.fromNullToEmpty(row
                            .get(getText("entity.description.label"))));
                    String typeID = row.get(getText("account.type_id.label"));
                    if (CommonUtil.isNullOrEmpty(typeID)) {
                        account.setAccount_type(null);
                    } else {
                        AccountType accountType = accountTypeService
                                .getEntityById(AccountType.class,
                                        Integer.parseInt(typeID));
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
                    account.setAnnual_revenue(CommonUtil.fromNullToEmpty(row
                            .get(getText("account.annual_revenue.label"))));
                    account.setMarket_value(CommonUtil.fromNullToEmpty(row
                            .get(getText("account.market_value.label"))));
                    account.setEmployees(CommonUtil.fromNullToEmpty(row
                            .get(getText("account.employees.label"))));
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
                    account.setOwnship(CommonUtil.fromNullToEmpty(row
                            .get(getText("account.ownship.label"))));
                    account.setRating(CommonUtil.fromNullToEmpty(row
                            .get(getText("account.rating.label"))));
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
     * @return the accountTypeService
     */
    public IBaseService<AccountType> getAccountTypeService() {
        return accountTypeService;
    }

    /**
     * @param accountTypeService
     *            the accountTypeService to set
     */
    public void setAccountTypeService(
            IBaseService<AccountType> accountTypeService) {
        this.accountTypeService = accountTypeService;
    }

    /**
     * @return the industryService
     */
    public IBaseService<Industry> getIndustryService() {
        return industryService;
    }

    /**
     * @param industryService
     *            the industryService to set
     */
    public void setIndustryService(IBaseService<Industry> industryService) {
        this.industryService = industryService;
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

}
