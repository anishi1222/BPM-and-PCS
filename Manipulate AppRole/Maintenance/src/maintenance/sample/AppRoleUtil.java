package maintenance.sample;

import java.io.FileNotFoundException;
import java.io.IOException;

import oracle.bpel.services.workflow.WorkflowException;

import oracle.bpm.services.common.exception.BPMException;


public class AppRoleUtil {

    private static final String CREATE = "-create";
    private static final String DELETE = "-delete";
    private static final String OPEROLE = "-operole";
    private static final String LSTROLE = "-lstrole";
    private static final String MEMADD = "-add";
    private static final String MEMREM = "-remove";
    private static final String MEMLST = "-list";

    /**
     * @param args
     */
    public static void main(String[] args) {

        // Branching based on argument
        for (int i = 0; i < args.length; i++) {
            try {
                String opeKey = args[i];
                if (CREATE.equals(opeKey)) {
                    createAppRole(args[++i]);
                } else if (DELETE.equals(opeKey)) {
                    deleteAppRole(args[++i]);
                } else if (LSTROLE.equals(opeKey)) {
                    listAppRoles();
                } else if (OPEROLE.equals(opeKey)) {
                    String AppRoleName = args[++i];
                    String subOpeKey = args[++i];
                    if (MEMADD.equals(subOpeKey)) {
                        addMemberToAppRole(AppRoleName, args[++i]);
                    } else if (MEMREM.equals(subOpeKey)) {
                        removeMemberFromAppRole(AppRoleName, args[++i]);
                    } else if (MEMLST.equals(subOpeKey)) {
                        listMembersInAppRole(AppRoleName);
                    }
                } else {
                    System.err.printf("maintenance.sample.AppRoleUtil {%s|%s|%s|%s} {AppRoleName} [%s|%s|%s] {RoleMember}\n", CREATE,
                                      DELETE, OPEROLE, LSTROLE, MEMADD, MEMREM, MEMLST);
                }
            } catch (Exception e) {
                System.err.printf("maintenance.sample.AppRoleUtil {%s|%s|%s|%s} {AppRoleName} [%s|%s|%s] {RoleMember}\n", CREATE, DELETE,
                                  OPEROLE, LSTROLE, MEMADD, MEMREM, MEMLST);
            }
        }
    }
    /**
     * Create application role.
     * @param pAppRoleName Application Role Name
     */
    public static void createAppRole(String pAppRoleName) {
        try {
            // 認証、サービスインスタンスを取得
            AppRoles ar = new AppRoles();
            ar.createAppRole(pAppRoleName);
        } catch (BPMException | FileNotFoundException | WorkflowException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 引数で指定したアプリケーション・ロールを削除します。.
     * @param pAppRoleName アプリケーションロール名
     */
    public static void deleteAppRole(String pAppRoleName) {
        try {
            // 認証、サービスインスタンスを取得
            AppRoles ar = new AppRoles();
            ar.deleteAppRole(pAppRoleName);
        } catch (BPMException | FileNotFoundException | WorkflowException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * すべてのアプリケーション・ロールとそのメンバーを表示します。.
     */
    public static void listAppRoles() {
        try {
            // 認証、サービスインスタンスを取得
            AppRoles ar = new AppRoles();
            ar.listAppRoles();
        } catch (BPMException | FileNotFoundException | WorkflowException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 引数で指定したアプリケーション・ロールに指定されたメンバーを追加します。.
     * @param pAppRoleName アプリケーションロール名
     * @param pMember 追加したいメンバー（このサンプルではユーザーのみ）
     */
    public static void addMemberToAppRole(String pAppRoleName, String pMember) {
        try {
            AppRoles ar = new AppRoles();
            ar.addMemberToAppRole(pAppRoleName, pMember);
        } catch (BPMException | FileNotFoundException | WorkflowException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 引数で指定したアプリケーション・ロールから指定されたメンバーを削除します。.
     * @param pAppRoleName アプリケーションロール名
     * @param pMember 追加したいメンバー（このサンプルではユーザーのみ）
     */
    public static void removeMemberFromAppRole(String pAppRoleName, String pMember) {
        try {
            AppRoles ar = new AppRoles();
            ar.removeMemberFromAppRole(pAppRoleName, pMember);
        } catch (BPMException | FileNotFoundException | WorkflowException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 引数で指定したアプリケーション・ロールに属するメンバーを表示します。.
     * @param pAppRoleName アプリケーションロール名
     */
    public static void listMembersInAppRole(String pAppRoleName) {
        try {
            // 認証、サービスインスタンスを取得
            AppRoles ar = new AppRoles();
            ar.listMembersInAppRole(pAppRoleName);
        } catch (BPMException | FileNotFoundException | WorkflowException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
