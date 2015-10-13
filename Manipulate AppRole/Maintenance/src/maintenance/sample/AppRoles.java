package maintenance.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import oracle.bpel.services.bpm.common.IBPMContext;
import oracle.bpel.services.workflow.WorkflowException;
import oracle.bpel.services.workflow.client.IWorkflowServiceClient;
import oracle.bpel.services.workflow.client.IWorkflowServiceClientConstants;
import oracle.bpel.services.workflow.client.WorkflowServiceClientFactory;
import oracle.bpel.services.workflow.metadata.ITaskMetadataService;
import oracle.bpel.services.workflow.query.ITaskQueryService;
import oracle.bpel.services.workflow.task.ITaskService;
import oracle.bpel.services.workflow.verification.IWorkflowContext;

import oracle.bpm.client.BPMServiceClientFactory;
import oracle.bpm.services.client.IBPMServiceClient;
import oracle.bpm.services.common.exception.BPMException;
import oracle.bpm.services.instancemanagement.IInstanceManagementService;
import oracle.bpm.services.organization.BPMOrganizationException;
import oracle.bpm.services.organization.IBPMOrganizationService;
import oracle.bpm.services.organization.model.AppRoleRefType;
import oracle.bpm.services.organization.model.ApplicationContext;
import oracle.bpm.services.organization.model.ApplicationContextTypeEnum;
import oracle.bpm.services.organization.model.ApplicationRoleType;
import oracle.bpm.services.organization.model.ApplicationRoles;
import oracle.bpm.services.organization.model.Organization;
import oracle.bpm.services.organization.model.Participant;
import oracle.bpm.services.organization.model.ParticipantTypeEnum;
import oracle.bpm.services.organization.model.PrincipleRefType;

public class AppRoles {
    private IWorkflowServiceClient wfSvcClient;
    private ITaskQueryService querySvc;
    private ITaskService taskSvc;
    private ITaskMetadataService taskMetaSvc;
    private IInstanceManagementService instanceManageSvc;
    private IWorkflowContext ctx;
    private IBPMContext bpmCtx;
    private ApplicationRoles appRoles;
    private IBPMServiceClient bpmSvcClient;
    private IBPMOrganizationService bpmOrgSvc;
    private Organization bpmOrg;

    AppRole() {
        // create instance　with connected and authenticated.
        init();
    }

    /**
     * Conenct to BPM server for authentication and get several service instances.
     * @throws WorkflowException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws BPMException
     */
    private void init() throws WorkflowException, FileNotFoundException, IOException, BPMException {
        // get connection information from property file.
        Properties conf = new Properties();
        InputStream is = new FileInputStream(new File("connection.properties"));
        conf.load(is);
        String adminUser = conf.getProperty("user");
        String adminPasswd = conf.getProperty("passwd");
        String t3URL = conf.getProperty("url");

        // Connection information is set to hashmap
        Map<IWorkflowServiceClientConstants.CONNECTION_PROPERTY, String> conProp =
            new HashMap<IWorkflowServiceClientConstants.CONNECTION_PROPERTY, String>();

        conProp.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.CLIENT_TYPE,
                    WorkflowServiceClientFactory.REMOTE_CLIENT);
        conProp.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_PROVIDER_URL, t3URL);
        conProp.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_INITIAL_CONTEXT_FACTORY,
                    "weblogic.jndi.WLInitialContextFactory");

        // Create BPMServiceClientFactory instance to get BPMServiceClient and BPMOrganizationService
        BPMServiceClientFactory bpmServiceClientFactory = BPMServiceClientFactory.getInstance(conProp, null, null);
        bpmSvcClient = bpmServiceClientFactory.getBPMServiceClient();
        bpmOrgSvc = bpmSvcClient.getBPMOrganizationService();

        // Create WorkflowService Client
        wfSvcClient = WorkflowServiceClientFactory.getWorkflowServiceClient(conProp, null, null);

        // get TaskQueryService
        querySvc = wfSvcClient.getTaskQueryService();

        // get TaskService
        taskSvc = wfSvcClient.getTaskService();

        // get TaskMetadataService
        taskMetaSvc = wfSvcClient.getTaskMetadataService();

        // get BPMInstanceManagementService
        instanceManageSvc = bpmServiceClientFactory.getBPMServiceClient().getInstanceManagementService();

        // 認証・認可
        // Authentication against TaskQueryService
        ctx = querySvc.authenticate(adminUser, adminPasswd.toCharArray(), null);

        // Authentication against BPMUserAuthenticationService
        bpmCtx =
            bpmServiceClientFactory.getBPMUserAuthenticationService().authenticate(adminUser, adminPasswd.toCharArray(),
                                                                                   null);

        // get BPM Organization and Application Roles
        bpmOrg = bpmOrgSvc.exportOrganization(bpmCtx);
        appRoles = bpmOrg.getApplicationRoles();
    }

    /**
     * Create an application role specified argument as its name
     * @param pAppRoleName
     * @throws BPMOrganizationException
     * @throws WorkflowException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws BPMException
     */
    public void createAppRole(String pAppRoleName) throws BPMOrganizationException, WorkflowException, FileNotFoundException,
                                                   IOException, BPMException {

        // Get Application Role list
        List<ApplicationRoleType> roleList = appRoles.getApplicationRole();
        ApplicationContext appCtx = new ApplicationContext();

        // Check application role whose name is same as argument
        for (ApplicationRoleType role : roleList) {
            if (role.getName().equals(pAppRoleName)) {
                // If application role whose name is same as argument exists, unable to create new application role
                System.err.printf( "Unable to create application role [%s] as application role whose name is [%s] already exists.\n", pAppRoleName, pAppRoleName);
                return;
            }
        }
        appCtx.setApplicationType(ApplicationContextTypeEnum.ORACLE_BPM_PROCESS_ROLES_APP);
        bpmOrgSvc.createAppRole(bpmCtx, appCtx, pAppRoleName, null, null);
        // Application Role is successfully created.
        System.err.printf("Application Role[%s] was successfully created.\n", pAppRoleName);
    }

    /**
     * Remove application role specified by argument as its name.
     * @param pAppRoleName
     * @throws BPMOrganizationException
     * @throws WorkflowException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws BPMException
     */
    public void deleteAppRole(String pAppRoleName) throws BPMOrganizationException, WorkflowException, FileNotFoundException,
                                                   IOException, BPMException {

        // Get Application Role list
        List<ApplicationRoleType> roleList = appRoles.getApplicationRole();
        ApplicationContext appCtx = new ApplicationContext();

        // Check application role whose name is same as argument
        for (ApplicationRoleType role : roleList) {
            if (role.getName().equals(pAppRoleName)) {
                // If application role whose name is same as argument exists, remove the application role
                appCtx.setApplicationType(ApplicationContextTypeEnum.ORACLE_BPM_PROCESS_ROLES_APP);

                // To remove application role forcibly even if member exits in the role, set true to 4th argument.
                bpmOrgSvc.removeAppRole(bpmCtx, appCtx, pAppRoleName, true);

                // Application role is successfully removed.
                System.out.printf("Application Role [%s] was successfully removed.\n", pAppRoleName);
                return;
            }
        }
        // Such application role does not exist.
        System.err.printf("Application role [%s] does not exist.\n", pAppRoleName);
    }

    /**
     * Add member to application role.
     * @param pAppRoleName
     * @param pMember
     * @throws BPMOrganizationException
     * @throws WorkflowException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws BPMException
     */
    public void addMemberToAppRole(String pAppRoleName, String pMember) throws BPMOrganizationException, WorkflowException,
                                                               FileNotFoundException, IOException, BPMException {

        // Get Application Role list
        ApplicationContext appCtx = new ApplicationContext();
        List<ApplicationRoleType> roleList = appRoles.getApplicationRole();

        // Check application role whose name is same as argument
        for (ApplicationRoleType role : roleList) {
            if (role.getName().equals(pAppRoleName)) {
                // Add members to the application role identified by argument.
                appCtx.setApplicationType(ApplicationContextTypeEnum.ORACLE_BPM_PROCESS_ROLES_APP);

                // In this case, choose USER out of USER、GROUP、APPLICATION ROLE
                AppRoleRefType appRoleRef = new AppRoleRefType();
                appRoleRef.setType(ParticipantTypeEnum.USER);
                appRoleRef.setName(pMember);
                bpmOrgSvc.grantAppRoleToPrincipal(bpmCtx, appCtx, pAppRoleName, new Participant(appRoleRef));
                System.out.printf("Member[%s] was successfully added to application role[%s].\n", pMember, pAppRoleName);
                return;
            }
        }
        // No application role whose name is same as argument
        System.err.printf("Application Role [%s] does not exist.\n", pAppRoleName);
    }

    /**
     * Remove member to application role.
     * @param pAppRoleName
     * @param pMember
     * @throws BPMOrganizationException
     * @throws WorkflowException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws BPMException
     */
    public void removeMemberFromAppRole(String pAppRoleName, String pMember) throws BPMOrganizationException, WorkflowException,
                                                                  FileNotFoundException, IOException, BPMException {
        // Get Application Role list
        ApplicationContext appCtx = new ApplicationContext();
        List<ApplicationRoleType> roleList = appRoles.getApplicationRole();

        // Check application role whose name is same as argument
        for (ApplicationRoleType role : roleList) {
            if (role.getName().equals(pAppRoleName)) {
                List<PrincipleRefType> memberList = role.getMember();
                // Check a member whose name is same as argument
                for (PrincipleRefType member : memberList) {
                    // Remove members from the application role identified by argument.
                    if (member.getName().equals(pMember)) {
                        appCtx.setApplicationType(ApplicationContextTypeEnum.ORACLE_BPM_PROCESS_ROLES_APP);

                        // In this case, choose USER out of USER、GROUP、APPLICATION ROLE
                        AppRoleRefType appRoleRef = new AppRoleRefType();
                        appRoleRef.setType(ParticipantTypeEnum.USER);
                        appRoleRef.setName(pMember);
                        bpmOrgSvc.revokeAppRoleFromPrincipal(bpmCtx, appCtx, pAppRoleName, new Participant(appRoleRef));
                        System.out.printf("Member[%s] was successfully removed  from application role[%s].\n", pMember, pAppRoleName);
                        return;
                    }
                }
                // No member exists in any application roles.
                System.out.printf("Member [%s] does not exist in the application role[%s].\n", pMember, pAppRoleName);
                return;
            }
        }
        // No such application role
        System.err.printf("Application role [%s] does not exist.\n", pAppRoleName);
    }

    /**
     * Show all members in an application roles.
     * @param pAppRoleName
     * @throws WorkflowException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws BPMException
     */
    public void listMembersInAppRole(String pAppRoleName) throws WorkflowException, FileNotFoundException, IOException, BPMException {

        // Get Application Role list
        List<ApplicationRoleType> roleList = appRoles.getApplicationRole();
        for (ApplicationRoleType role : roleList) {
            if (role.getName().equals(pAppRoleName)) {
                // Show members who belong to application role whose name is same as specified argument.
                System.out.printf("Application Role [%s]\n", pAppRoleName);
                System.out.println("  Member: ");
                List<PrincipleRefType> memberList = role.getMember();
                for (PrincipleRefType member : memberList) {
                    System.out.printf("  [%s]\n", member.getName());
                }
                return;
            }
        }
        // No such application role
        System.err.printf("Application Role [%s] does not exist.\n", pAppRoleName);
    }

    /**
     * Show all members in any application roles.
     * @throws WorkflowException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws BPMException
     */
    public void listAppRoles() throws WorkflowException, FileNotFoundException, IOException, BPMException {
        // Get Application Role list
        List<ApplicationRoleType> roleList = appRoles.getApplicationRole();
        for (ApplicationRoleType role : roleList) {
            System.out.printf("Application Role [%s]\n", role.getName());
            System.out.println("  Member: ");
            List<PrincipleRefType> memberList = role.getMember();
            for (PrincipleRefType member : memberList) {
                System.out.printf("  [%s]\n", member.getName());
            }
        }
    }
}
