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
import com.gcrm.domain.Call;
import com.gcrm.domain.Campaign;
import com.gcrm.domain.CaseInstance;
import com.gcrm.domain.ChangeLog;
import com.gcrm.domain.Contact;
import com.gcrm.domain.Document;
import com.gcrm.domain.LeadSource;
import com.gcrm.domain.Meeting;
import com.gcrm.domain.Opportunity;
import com.gcrm.domain.Salutation;
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
 * Edits Contact
 * 
 */
public class EditContactAction extends BaseEditAction implements Preparable {

    private static final long serialVersionUID = -2404576552417042445L;

    private IBaseService<Contact> baseService;
    private IBaseService<Account> accountService;
    private IOptionService<LeadSource> leadSourceService;
    private IOptionService<Salutation> salutationService;
    private IBaseService<User> userService;
    private IBaseService<Campaign> campaignService;
    private IBaseService<Opportunity> opportunityService;
    private IBaseService<TargetList> targetListService;
    private IBaseService<Document> documentService;
    private IBaseService<CaseInstance> caseService;
    private IBaseService<Call> callService;
    private IBaseService<Meeting> meetingService;
    private IBaseService<ChangeLog> changeLogService;
    private TaskExecutor taskExecutor;
    private Contact contact;
    private List<LeadSource> leadSources;
    private List<Salutation> salutations;
    private Integer accountID = null;
    private String accountText = null;
    private Integer reportToID = null;
    private String reportToText = null;
    private Integer leadSourceID = null;
    private Integer salutationID = null;
    private Integer campaignID = null;
    private String campaignText = null;

    /**
     * Saves the entity.
     * 
     * @return the SUCCESS result
     */
    public String save() throws Exception {
        Contact originalContact = saveEntity();
        final Collection<ChangeLog> changeLogs = changeLog(originalContact,
                contact);
        contact = getBaseService().makePersistent(contact);
        this.setId(contact.getId());
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

    /**
     * Gets the entity.
     * 
     * @return the SUCCESS result
     */
    public String get() throws Exception {
        if (this.getId() != null) {
            contact = baseService.getEntityById(Contact.class, this.getId());
            Account account = contact.getAccount();
            if (account != null) {
                accountID = account.getId();
                accountText = account.getName();
            }

            Contact report_to = contact.getReport_to();
            if (report_to != null) {
                reportToID = report_to.getId();
                reportToText = report_to.getName();
            }

            LeadSource leadSource = contact.getLeadSource();
            if (leadSource != null) {
                leadSourceID = leadSource.getId();
            }

            Salutation salutation = contact.getSalutation();
            if (salutation != null) {
                salutationID = salutation.getId();
            }

            Campaign campaign = contact.getCampaign();
            if (campaign != null) {
                campaignID = campaign.getId();
                campaignText = campaign.getName();
            }
            User assignedTo = contact.getAssigned_to();
            if (assignedTo != null) {
                this.setAssignedToID(assignedTo.getId());
                this.setAssignedToText(assignedTo.getName());
            }
            this.getBaseInfo(contact, Contact.class.getSimpleName(),
                    Constant.CRM_NAMESPACE);
        } else {
            this.initBaseInfo();
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
            Collection<Contact> contacts = new ArrayList<Contact>();
            User loginUser = this.getLoginUser();
            User user = userService
                    .getEntityById(User.class, loginUser.getId());
            Collection<ChangeLog> allChangeLogs = new ArrayList<ChangeLog>();
            for (String IDString : selectIDArray) {
                int id = Integer.parseInt(IDString);
                Contact contactInstance = this.baseService.getEntityById(
                        Contact.class, id);
                Contact originalContact = contactInstance.clone();
                for (String fieldName : fieldNames) {
                    Object value = BeanUtil.getFieldValue(contact, fieldName);
                    BeanUtil.setFieldValue(contactInstance, fieldName, value);
                }
                contactInstance.setUpdated_by(user);
                contactInstance.setUpdated_on(new Date());
                Collection<ChangeLog> changeLogs = changeLog(originalContact,
                        contactInstance);
                allChangeLogs.addAll(changeLogs);
                contacts.add(contactInstance);
            }
            final Collection<ChangeLog> changeLogsForSave = allChangeLogs;
            if (contacts.size() > 0) {
                this.baseService.batchUpdate(contacts);
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
     * @return original contact record
     * @throws Exception
     */
    private Contact saveEntity() throws Exception {
        Contact originalContact = null;
        if (contact.getId() == null) {
            UserUtil.permissionCheck("create_contact");
        } else {
            UserUtil.permissionCheck("update_contact");
            originalContact = baseService.getEntityById(Contact.class,
                    contact.getId());
            contact.setOpportunities(originalContact.getOpportunities());
            contact.setCalls(originalContact.getCalls());
            contact.setCases(originalContact.getCases());
            contact.setDocuments(originalContact.getDocuments());
            contact.setMeetings(originalContact.getMeetings());
            contact.setLeads(originalContact.getLeads());
            contact.setTargetLists(originalContact.getTargetLists());
        }

        Account account = null;
        if (accountID != null) {
            account = accountService.getEntityById(Account.class, accountID);
        }
        contact.setAccount(account);

        Contact reportTo = null;
        if (reportToID != null) {
            reportTo = baseService.getEntityById(Contact.class, reportToID);
        }
        contact.setReport_to(reportTo);

        LeadSource leadSource = null;
        if (leadSourceID != null) {
            leadSource = leadSourceService.getEntityById(LeadSource.class,
                    leadSourceID);
        }
        contact.setLeadSource(leadSource);

        Salutation salutation = null;
        if (salutationID != null) {
            salutation = salutationService.getEntityById(Salutation.class,
                    salutationID);
        }
        contact.setSalutation(salutation);

        User user = null;
        if (this.getAssignedToID() != null) {
            user = userService
                    .getEntityById(User.class, this.getAssignedToID());
        }
        contact.setAssigned_to(user);

        User owner = null;
        if (this.getOwnerID() != null) {
            owner = userService.getEntityById(User.class, this.getOwnerID());
        }
        contact.setOwner(owner);

        Campaign campaign = null;
        if (campaignID != null) {
            campaign = campaignService
                    .getEntityById(Campaign.class, campaignID);
        }
        contact.setCampaign(campaign);

        if ("Opportunity".equals(this.getRelationKey())) {
            Opportunity opportunity = opportunityService
                    .getEntityById(Opportunity.class,
                            Integer.valueOf(this.getRelationValue()));
            Set<Opportunity> opportunities = contact.getOpportunities();
            if (opportunities == null) {
                opportunities = new HashSet<Opportunity>();
            }
            opportunities.add(opportunity);
        } else if ("TargetList".equals(this.getRelationKey())) {
            TargetList targetList = targetListService.getEntityById(
                    TargetList.class, Integer.valueOf(this.getRelationValue()));
            Set<TargetList> targetLists = contact.getTargetLists();
            if (targetLists == null) {
                targetLists = new HashSet<TargetList>();
            }
            targetLists.add(targetList);
        } else if ("Call".equals(this.getRelationKey())) {
            Call call = callService.getEntityById(Call.class,
                    Integer.valueOf(this.getRelationValue()));
            Set<Call> calls = contact.getCalls();
            if (calls == null) {
                calls = new HashSet<Call>();
            }
            calls.add(call);
        } else if ("Meeting".equals(this.getRelationKey())) {
            Meeting meeting = meetingService.getEntityById(Meeting.class,
                    Integer.valueOf(this.getRelationValue()));
            Set<Meeting> meetings = contact.getMeetings();
            if (meetings == null) {
                meetings = new HashSet<Meeting>();
            }
            meetings.add(meeting);
        } else if ("Document".equals(this.getRelationKey())) {
            Document document = documentService.getEntityById(Document.class,
                    Integer.valueOf(this.getRelationValue()));
            Set<Document> documents = contact.getDocuments();
            if (documents == null) {
                documents = new HashSet<Document>();
            }
            documents.add(document);
        } else if ("CaseInstance".equals(this.getRelationKey())) {
            CaseInstance caseInstance = caseService.getEntityById(
                    CaseInstance.class,
                    Integer.valueOf(this.getRelationValue()));
            Set<CaseInstance> cases = contact.getCases();
            if (cases == null) {
                cases = new HashSet<CaseInstance>();
            }
            cases.add(caseInstance);
        }
        super.updateBaseInfo(contact);
        return originalContact;
    }

    /**
     * Creates change log
     * 
     * @param originalContact
     *            original contact record
     * @param contact
     *            current contact record
     * @return change log collections
     */
    private Collection<ChangeLog> changeLog(Contact originalContact,
            Contact contact) {
        Collection<ChangeLog> changeLogs = null;
        if (originalContact != null) {
            ActionContext context = ActionContext.getContext();
            Map<String, Object> session = context.getSession();
            String entityName = Contact.class.getSimpleName();
            Integer recordID = contact.getId();
            User loginUser = (User) session
                    .get(AuthenticationSuccessListener.LOGIN_USER);
            changeLogs = new ArrayList<ChangeLog>();

            String oldSalutation = getOptionValue(originalContact
                    .getSalutation());
            String newSalutation = getOptionValue(contact.getSalutation());
            if (!oldSalutation.equals(newSalutation)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "menu.salutation.title", oldSalutation, newSalutation,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldFirstName = CommonUtil.fromNullToEmpty(originalContact
                    .getFirst_name());
            String newFirstName = CommonUtil.fromNullToEmpty(contact
                    .getFirst_name());
            if (!oldFirstName.equals(newFirstName)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.first_name.label", oldFirstName, newFirstName,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldLastName = CommonUtil.fromNullToEmpty(originalContact
                    .getLast_name());
            String newLastName = CommonUtil.fromNullToEmpty(contact
                    .getLast_name());
            if (!oldLastName.equals(newLastName)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.last_name.label", oldLastName, newLastName,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldOfficePhone = CommonUtil.fromNullToEmpty(originalContact
                    .getOffice_phone());
            String newOfficePhone = CommonUtil.fromNullToEmpty(contact
                    .getOffice_phone());
            if (!oldOfficePhone.equals(newOfficePhone)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.office_phone.label", oldOfficePhone,
                        newOfficePhone, loginUser);
                changeLogs.add(changeLog);
            }

            String oldTitle = CommonUtil.fromNullToEmpty(originalContact
                    .getTitle());
            String newTitle = CommonUtil.fromNullToEmpty(contact.getTitle());
            if (!oldTitle.equals(newTitle)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.title.label", oldTitle, newTitle, loginUser);
                changeLogs.add(changeLog);
            }

            String oldMobile = CommonUtil.fromNullToEmpty(originalContact
                    .getMobile());
            String newMobile = CommonUtil.fromNullToEmpty(contact.getMobile());
            if (!oldMobile.equals(newMobile)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.mobile.label", oldMobile, newMobile, loginUser);
                changeLogs.add(changeLog);
            }

            String oldDepartment = CommonUtil.fromNullToEmpty(originalContact
                    .getDepartment());
            String newDepartment = CommonUtil.fromNullToEmpty(contact
                    .getDepartment());
            if (!oldDepartment.equals(newDepartment)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.department.label", oldDepartment,
                        newDepartment, loginUser);
                changeLogs.add(changeLog);
            }

            String oldFax = CommonUtil
                    .fromNullToEmpty(originalContact.getFax());
            String newWFax = CommonUtil.fromNullToEmpty(contact.getFax());
            if (!oldFax.equals(newWFax)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.fax.label", oldFax, newWFax, loginUser);
                changeLogs.add(changeLog);
            }

            String oldSkypeId = CommonUtil.fromNullToEmpty(originalContact
                    .getSkype_id());
            String newSkypeId = CommonUtil.fromNullToEmpty(contact
                    .getSkype_id());
            if (!oldSkypeId.equals(newSkypeId)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "contact.skype_id.label", oldSkypeId, newSkypeId,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldWebsite = CommonUtil.fromNullToEmpty(originalContact
                    .getWebsite());
            String newWebsite = CommonUtil
                    .fromNullToEmpty(contact.getWebsite());
            if (!oldWebsite.equals(newWebsite)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.website.label", oldWebsite, newWebsite,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldPrimaryStreet = CommonUtil
                    .fromNullToEmpty(originalContact.getPrimary_street());
            String newPrimaryStreet = CommonUtil.fromNullToEmpty(contact
                    .getPrimary_street());
            if (!oldPrimaryStreet.equals(newPrimaryStreet)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.primary_street.label", oldPrimaryStreet,
                        newPrimaryStreet, loginUser);
                changeLogs.add(changeLog);
            }

            String oldPrimaryState = CommonUtil.fromNullToEmpty(originalContact
                    .getPrimary_state());
            String newPrimaryState = CommonUtil.fromNullToEmpty(contact
                    .getPrimary_state());
            if (!oldPrimaryState.equals(newPrimaryState)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.primary_state.label", oldPrimaryState,
                        newPrimaryState, loginUser);
                changeLogs.add(changeLog);
            }

            String oldPrimaryPostalCode = CommonUtil
                    .fromNullToEmpty(originalContact.getPrimary_postal_code());
            String newPrimaryPostalCode = CommonUtil.fromNullToEmpty(contact
                    .getPrimary_postal_code());
            if (!oldPrimaryPostalCode.equals(newPrimaryPostalCode)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.primary_postal_code.label",
                        oldPrimaryPostalCode, newPrimaryPostalCode, loginUser);
                changeLogs.add(changeLog);
            }

            String oldPrimaryCountry = CommonUtil
                    .fromNullToEmpty(originalContact.getPrimary_country());
            String newPrimaryCountry = CommonUtil.fromNullToEmpty(contact
                    .getPrimary_country());
            if (!oldPrimaryCountry.equals(newPrimaryCountry)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.primary_country.label", oldPrimaryCountry,
                        newPrimaryCountry, loginUser);
                changeLogs.add(changeLog);
            }

            String oldOtherStreet = CommonUtil.fromNullToEmpty(originalContact
                    .getOther_street());
            String newOtherStreet = CommonUtil.fromNullToEmpty(contact
                    .getOther_street());
            if (!oldOtherStreet.equals(newOtherStreet)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.other_street.label", oldOtherStreet,
                        newOtherStreet, loginUser);
                changeLogs.add(changeLog);
            }

            String oldOtherState = CommonUtil.fromNullToEmpty(originalContact
                    .getOther_state());
            String newOtherState = CommonUtil.fromNullToEmpty(contact
                    .getOther_state());
            if (!oldOtherState.equals(newOtherState)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.other_state.label", oldOtherState,
                        newOtherState, loginUser);
                changeLogs.add(changeLog);
            }

            String oldOtherPostalCode = CommonUtil
                    .fromNullToEmpty(originalContact.getOther_postal_code());
            String newOtherPostalCode = CommonUtil.fromNullToEmpty(contact
                    .getOther_postal_code());
            if (!oldOtherPostalCode.equals(newOtherPostalCode)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.other_postal_code.label", oldOtherPostalCode,
                        newOtherPostalCode, loginUser);
                changeLogs.add(changeLog);
            }

            String oldOtherCountry = CommonUtil.fromNullToEmpty(originalContact
                    .getOther_country());
            String newOtherCountry = CommonUtil.fromNullToEmpty(contact
                    .getOther_country());
            if (!oldOtherCountry.equals(newOtherCountry)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.other_country.label", oldOtherCountry,
                        newOtherCountry, loginUser);
                changeLogs.add(changeLog);
            }

            String oldEmail = CommonUtil.fromNullToEmpty(originalContact
                    .getEmail());
            String newEmail = CommonUtil.fromNullToEmpty(contact.getEmail());
            if (!oldEmail.equals(newEmail)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.email.label", oldEmail, newEmail, loginUser);
                changeLogs.add(changeLog);
            }

            String oldDescription = CommonUtil.fromNullToEmpty(originalContact
                    .getDescription());
            String newDescription = CommonUtil.fromNullToEmpty(contact
                    .getDescription());
            if (!oldDescription.equals(newDescription)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.description.label", oldDescription,
                        newDescription, loginUser);
                changeLogs.add(changeLog);
            }

            String oldNotes = CommonUtil.fromNullToEmpty(originalContact
                    .getNotes());
            String newNotes = CommonUtil.fromNullToEmpty(contact.getNotes());
            if (!oldNotes.equals(newNotes)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.notes.label", oldNotes, newNotes, loginUser);
                changeLogs.add(changeLog);
            }

            String oldReportToName = "";
            Contact oldReportTo = originalContact.getReport_to();
            if (oldReportTo != null) {
                oldReportToName = CommonUtil.fromNullToEmpty(oldReportTo
                        .getName());
            }
            String newReportToName = "";
            Contact newReportTo = contact.getReport_to();
            if (newReportTo != null) {
                newReportToName = CommonUtil.fromNullToEmpty(newReportTo
                        .getName());
            }
            if (oldReportToName != newReportToName) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "contact.report_to.label", oldReportToName,
                        newReportToName, loginUser);
                changeLogs.add(changeLog);
            }

            String oldAccountName = "";
            Account oldAccount = originalContact.getAccount();
            if (oldAccount != null) {
                oldAccountName = CommonUtil.fromNullToEmpty(oldAccount
                        .getName());
            }
            String newAccountName = "";
            Account newAccount = contact.getAccount();
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

            boolean oldNotCall = originalContact.isNot_call();

            boolean newNotCall = contact.isNot_call();
            if (oldNotCall != newNotCall) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.not_call.label", String.valueOf(oldNotCall),
                        String.valueOf(newNotCall), loginUser);
                changeLogs.add(changeLog);
            }

            String oldLeadSource = getOptionValue(originalContact
                    .getLeadSource());
            String newLeadSource = getOptionValue(contact.getLeadSource());
            if (!oldLeadSource.equals(newLeadSource)) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "menu.leadSource.title", oldLeadSource, newLeadSource,
                        loginUser);
                changeLogs.add(changeLog);
            }

            String oldCampaignName = "";
            Campaign oldCampaign = originalContact.getCampaign();
            if (oldCampaign != null) {
                oldCampaignName = oldCampaign.getName();
            }
            String newCampaignName = "";
            Campaign newCampaign = contact.getCampaign();
            if (newCampaign != null) {
                newCampaignName = newCampaign.getName();
            }
            if (oldCampaignName != newCampaignName) {
                ChangeLog changeLog = saveChangeLog(entityName, recordID,
                        "entity.campaign.label",
                        CommonUtil.fromNullToEmpty(oldCampaignName),
                        CommonUtil.fromNullToEmpty(newCampaignName), loginUser);
                changeLogs.add(changeLog);
            }

            String oldAssignedToName = "";
            User oldAssignedTo = originalContact.getAssigned_to();
            if (oldAssignedTo != null) {
                oldAssignedToName = oldAssignedTo.getName();
            }
            String newAssignedToName = "";
            User newAssignedTo = contact.getAssigned_to();
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
     * Gets Contact Relation Counts
     * 
     * @return null
     */
    public String getContactRelationCounts() throws Exception {
        long opportunityNumber = this.baseService
                .countsByParams(
                        "select count(*) from Contact contact join contact.opportunities where contact.id = ?",
                        new Integer[] { this.getId() });
        long leadNumber = this.baseService
                .countsByParams(
                        "select count(*) from Contact contact join contact.leads where contact.id = ?",
                        new Integer[] { this.getId() });
        long documentNumber = this.baseService
                .countsByParams(
                        "select count(*) from Contact contact join contact.documents where contact.id = ?",
                        new Integer[] { this.getId() });
        long taskNumber = this.baseService
                .countsByParams(
                        "select count(task.id) from Task task where related_object='Contact' and related_record = ?",
                        new Integer[] { this.getId() });
        long caseNumber = this.baseService
                .countsByParams(
                        "select count(*) from Contact contact join contact.cases where contact.id = ?",
                        new Integer[] { this.getId() });

        StringBuilder jsonBuilder = new StringBuilder("");
        jsonBuilder.append("{\"opportunityNumber\":\"")
                .append(opportunityNumber).append("\",\"leadNumber\":\"")
                .append(leadNumber).append("\",\"documentNumber\":\"")
                .append(documentNumber).append("\",\"taskNumber\":\"")
                .append(taskNumber).append("\",\"caseNumber\":\"")
                .append(caseNumber).append("\"}");
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
        this.leadSources = leadSourceService.getOptions(
                LeadSource.class.getSimpleName(), local);
        this.salutations = salutationService.getOptions(
                Salutation.class.getSimpleName(), local);
    }

    /**
     * @return the baseService
     */
    public IBaseService<Contact> getBaseService() {
        return baseService;
    }

    /**
     * @param baseService
     *            the baseService to set
     */
    public void setBaseService(IBaseService<Contact> baseService) {
        this.baseService = baseService;
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
     * @return the contact
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * @param contact
     *            the contact to set
     */
    public void setContact(Contact contact) {
        this.contact = contact;
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
     * @return the leadSourceID
     */
    public Integer getLeadSourceID() {
        return leadSourceID;
    }

    /**
     * @param leadSourceID
     *            the leadSourceID to set
     */
    public void setLeadSourceID(Integer leadSourceID) {
        this.leadSourceID = leadSourceID;
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
     * @return the leadSource
     */
    public List<LeadSource> getLeadSources() {
        return leadSources;
    }

    /**
     * @param leadSource
     *            the leadSource to set
     */
    public void setLeadSource(List<LeadSource> leadSources) {
        this.leadSources = leadSources;
    }

    /**
     * @return the reportToID
     */
    public Integer getReportToID() {
        return reportToID;
    }

    /**
     * @param reportToID
     *            the reportToID to set
     */
    public void setReportToID(Integer reportToID) {
        this.reportToID = reportToID;
    }

    /**
     * @return the opportunityService
     */
    public IBaseService<Opportunity> getOpportunityService() {
        return opportunityService;
    }

    /**
     * @param opportunityService
     *            the opportunityService to set
     */
    public void setOpportunityService(
            IBaseService<Opportunity> opportunityService) {
        this.opportunityService = opportunityService;
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
     * @return the callService
     */
    public IBaseService<Call> getCallService() {
        return callService;
    }

    /**
     * @param callService
     *            the callService to set
     */
    public void setCallService(IBaseService<Call> callService) {
        this.callService = callService;
    }

    /**
     * @return the meetingService
     */
    public IBaseService<Meeting> getMeetingService() {
        return meetingService;
    }

    /**
     * @param meetingService
     *            the meetingService to set
     */
    public void setMeetingService(IBaseService<Meeting> meetingService) {
        this.meetingService = meetingService;
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
     * @return the caseService
     */
    public IBaseService<CaseInstance> getCaseService() {
        return caseService;
    }

    /**
     * @param caseService
     *            the caseService to set
     */
    public void setCaseService(IBaseService<CaseInstance> caseService) {
        this.caseService = caseService;
    }

    /**
     * @return the accountText
     */
    public String getAccountText() {
        return accountText;
    }

    /**
     * @param accountText
     *            the accountText to set
     */
    public void setAccountText(String accountText) {
        this.accountText = accountText;
    }

    /**
     * @return the reportToText
     */
    public String getReportToText() {
        return reportToText;
    }

    /**
     * @return the campaignText
     */
    public String getCampaignText() {
        return campaignText;
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
     * @return the leadSourceService
     */
    public IOptionService<LeadSource> getLeadSourceService() {
        return leadSourceService;
    }

    /**
     * @param leadSourceService
     *            the leadSourceService to set
     */
    public void setLeadSourceService(
            IOptionService<LeadSource> leadSourceService) {
        this.leadSourceService = leadSourceService;
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

    /**
     * @param leadSources
     *            the leadSources to set
     */
    public void setLeadSources(List<LeadSource> leadSources) {
        this.leadSources = leadSources;
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
