package com.idega.block.login.business;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import com.idega.builder.business.BuilderLogic;
import com.idega.business.IWEventListener;
import com.idega.core.accesscontrol.business.LoggedOnInfo;
import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.data.GenericGroup;
import com.idega.core.user.business.UserBusiness;
import com.idega.core.user.data.User;
import com.idega.core.user.data.UserGroupRepresentative;
import com.idega.idegaweb.IWException;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.util.Encrypter;
import com.idega.util.IWTimestamp;
/**
 * Title:        LoginBusiness The default login business handler for the Login presentation module
 * Description:
 * Copyright:    Copyright (c) 2000-2002 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:gummi@idega.is">Gu�mundur �g�st S�mundsson</a>,<a href="mailto:tryggvi@idega.is">Tryggvi Larusson</a>
 * @version 1.1

 */

public class LoginBusiness implements IWEventListener
{
	public static String UserAttributeParameter = "user_login";
	public static String PermissionGroupParameter = "user_permission_groups";
	public static String LoginStateParameter = "login_state";
	public static String LoginRedirectPageParameter = "login_redirect_page";
	private static String LoginAttributeParameter = "login_attributes";
	private static String UserGroupRepresentativeParameter = "ic_user_representative_group";
	private static String PrimaryGroupsParameter = "ic_user_primarygroups";
	private static String PrimaryGroupParameter = "ic_user_primarygroup";
	private static final String _APPADDRESS_LOGGED_ON_LIST = "ic_loggedon_list";
	private static final String _LOGGINADDRESS_LOGGED_ON_INFO = "ic_loggedon_info";
	public LoginBusiness()
	{}
	public static boolean isLoggedOn(IWUserContext iwc)
	{
		if (iwc.getSessionAttribute(LoginAttributeParameter) == null)
		{
			return false;
		}
		return true;
	}
	public static void internalSetState(IWContext iwc, String state)
	{
		iwc.setSessionAttribute(LoginStateParameter, state);
	}
	public static String internalGetState(IWContext iwc)
	{
		return (String) iwc.getSessionAttribute(LoginStateParameter);
	}
	/**
	 * To get the userame of the current log-in attempt
	 * @return The username the current user is trying to log in with. Returns null if no log-in attemt is going on.
	 */
	protected String getLoginUserName(IWContext iwc)
	{
		return iwc.getParameter("login");
	}
	/**
	 * To get the password of the current log-in attempt
	 * @return The password the current user is trying to log in with. Returns null if no log-in attemt is going on.
	 */
	protected String getLoginPassword(IWContext iwc)
	{
		return iwc.getParameter("password");
	}
	/**
	 * @return True if logIn was succesful, false if it failed
	 */
	protected boolean logInUser(IWContext iwc, String username, String password)
	{
		try
		{
			boolean didLogin = verifyPasswordAndLogin(iwc, username, password);
			if (didLogin)
			{
				internalSetState(iwc, "loggedon");
			}
			return didLogin;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	 * @return True if logOut was succesful, false if it failed
	 */
	protected boolean logOutUser(IWContext iwc)
	{
		try
		{
			logOut(iwc);
			internalSetState(iwc, "loggedoff");
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	 * Invoked when the login failed
	 * Can be overrided in subclasses to alter behaviour
	 * By default this sets the state to "login failed" and does not log in a user
	 */
	protected void onLoginFailed(IWContext iwc)
	{
		logOutUser(iwc);
		internalSetState(iwc, "loginfailed");
	}
	/**
	 * Invoked when the login was succesful
	 * Can be overrided in subclasses to alter behaviour
	 * By default this sets the state to "logged on"
	 */
	protected void onLoginSuccessful(IWContext iwc)
	{
		internalSetState(iwc, "loggedon");
	}

	 protected static boolean isLogOnAction(IWContext iwc){
	  	return "login".equals(getControlActionValue(iwc));
	  }

	  protected static boolean isLogOffAction(IWContext iwc){
	  	return "logoff".equals(getControlActionValue(iwc));
	  }

	  protected static boolean isTryAgainAction(IWContext iwc){
	  	return "tryagain".equals(getControlActionValue(iwc));
	  }

	  private static String getControlActionValue(IWContext iwc){
	  	return iwc.getParameter(LoginBusiness.LoginStateParameter);
	  }

	/**
	 * The method invoked when the login presentation module sends a login to this class
	 */
	public boolean actionPerformed(IWContext iwc) throws IWException
	{
		//System.out.println("LoginBusiness.actionPerformed");
		try
		{
			if (isLoggedOn(iwc))
			{
				if (isLogOffAction(iwc))
				{
					//logOut(iwc);
					//internalSetState(iwc,"loggedoff");
					logOutUser(iwc);
				}
			}
			else
			{

					if (isLogOnAction(iwc))
					{
						boolean canLogin = false;
						String username = getLoginUserName(iwc);
						String password = getLoginPassword(iwc);
						if ((username != null) && (password != null))
						{
							canLogin = verifyPasswordAndLogin(iwc, username, password);
							if (canLogin)
							{
								//isLoggedOn(iwc);
								//internalSetState(iwc,"loggedon");
								// addon
								if (iwc.isParameterSet(LoginRedirectPageParameter))
								{
									//System.err.println("redirect parameter is set");
									BuilderLogic.getInstance().setCurrentPriorityPageID(
										iwc,
										iwc.getParameter(LoginRedirectPageParameter));
								}
								onLoginSuccessful(iwc);
							}
							else
							{
								//logOut(iwc);
								//internalSetState(iwc,"loginfailed");
								onLoginFailed(iwc);
							}
						}
					}
					else if (isTryAgainAction(iwc))
					{
						internalSetState(iwc, "loggedoff");
					}

			}
		}
		catch (Exception ex)
		{
			try
			{
				logOut(iwc);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			ex.printStackTrace(System.err);
			//throw (IdegaWebException)ex.fillInStackTrace();
		}
		return true;
	}
	/*

	  public boolean isAdmin(IWContext iwc)throws Exception{

	    return iwc.isAdmin();

	  }

	*/
	public static void setLoginAttribute(String key, Object value, IWUserContext iwc) throws NotLoggedOnException
	{
		if (isLoggedOn(iwc))
		{
			Object obj = iwc.getSessionAttribute(LoginAttributeParameter);
			((Hashtable) obj).put(key, value);
		}
		else
		{
			throw new NotLoggedOnException();
		}
	}
	public static Object getLoginAttribute(String key, IWUserContext iwc) throws NotLoggedOnException
	{
		if (isLoggedOn(iwc))
		{
			Object obj = iwc.getSessionAttribute(LoginAttributeParameter);
			if (obj == null)
			{
				return null;
			}
			else
			{
				return ((Hashtable) obj).get(key);
			}
		}
		else
		{
			throw new NotLoggedOnException();
		}
	}
	public static void removeLoginAttribute(String key, IWContext iwc)
	{
		if (isLoggedOn(iwc))
		{
			Object obj = iwc.getSessionAttribute(LoginAttributeParameter);
			if (obj != null)
			{
				((Hashtable) obj).remove(key);
			}
		}
		else if (iwc.getSessionAttribute(LoginAttributeParameter) != null)
		{
			iwc.removeSessionAttribute(LoginAttributeParameter);
		}
	}
	public static User getUser(IWUserContext iwc) /* throws NotLoggedOnException */
	{
		try
		{
			return (User) LoginBusiness.getLoginAttribute(UserAttributeParameter, iwc);
		}
		catch (NotLoggedOnException ex)
		{
			return null;
		}
		/*Object obj = iwc.getSessionAttribute(UserAttributeParameter);

		if (obj != null){

		  return (User)obj;

		}else{

		  throw new NotLoggedOnException();

		}

		*/
	}
	public static List getPermissionGroups(IWUserContext iwc) throws NotLoggedOnException
	{
		return (List) LoginBusiness.getLoginAttribute(PermissionGroupParameter, iwc);
	}
	public static UserGroupRepresentative getUserRepresentativeGroup(IWUserContext iwc) throws NotLoggedOnException
	{
		return (UserGroupRepresentative) LoginBusiness.getLoginAttribute(UserGroupRepresentativeParameter, iwc);
	}
	public static GenericGroup getPrimaryGroup(IWUserContext iwc) throws NotLoggedOnException
	{
		return (GenericGroup) LoginBusiness.getLoginAttribute(PrimaryGroupParameter, iwc);
	}
	protected static void setUser(IWUserContext iwc, User user)
	{
		LoginBusiness.setLoginAttribute(UserAttributeParameter, user, iwc);
	}
	protected static void setPermissionGroups(IWUserContext iwc, List value)
	{
		LoginBusiness.setLoginAttribute(PermissionGroupParameter, value, iwc);
	}
	protected static void setUserRepresentativeGroup(IWUserContext iwc, UserGroupRepresentative value)
	{
		LoginBusiness.setLoginAttribute(UserGroupRepresentativeParameter, value, iwc);
	}
	protected static void setPrimaryGroup(IWUserContext iwc, GenericGroup value)
	{
		LoginBusiness.setLoginAttribute(PrimaryGroupParameter, value, iwc);
	}
	private boolean logIn(IWContext iwc, LoginTable loginTable, String login) throws Exception
	{
		User user =
			(
				(com.idega.core.user.data.UserHome) com.idega.data.IDOLookup.getHomeLegacy(
					User.class)).findByPrimaryKeyLegacy(
				loginTable.getUserId());
		iwc.setSessionAttribute(LoginAttributeParameter, new Hashtable());
		LoginBusiness.setUser(iwc, user);
		//List groups = AccessControl.getPermissionGroups(user);
		List groups = UserBusiness.getUserGroups(user);
		if (groups != null)
		{
			LoginBusiness.setPermissionGroups(iwc, groups);
		}
		int userGroupId = user.getGroupID();
		if (userGroupId != -1)
		{
			LoginBusiness.setUserRepresentativeGroup(
				iwc,
				(
					(com.idega.core.user.data.UserGroupRepresentativeHome) com.idega.data.IDOLookup.getHomeLegacy(
						UserGroupRepresentative.class)).findByPrimaryKeyLegacy(
					userGroupId));
		}
		if (user.getPrimaryGroupID() != -1)
		{
			GenericGroup primaryGroup =
				(
					(com.idega.core.data.GenericGroupHome) com.idega.data.IDOLookup.getHomeLegacy(
						GenericGroup.class)).findByPrimaryKeyLegacy(
					user.getPrimaryGroupID());
			LoginBusiness.setPrimaryGroup(iwc, primaryGroup);
		}
		int loginRecordId = LoginDBHandler.recordLogin(loginTable.getID(), iwc.getRemoteIpAddress());
		LoggedOnInfo lInfo = new LoggedOnInfo();
		lInfo.setLogin(login);
		lInfo.setSession(iwc.getSession());
		lInfo.setTimeOfLogon(IWTimestamp.RightNow());
		lInfo.setUser(user);
		lInfo.setLoginRecordId(loginRecordId);
		getLoggedOnInfoList(iwc).add(lInfo);
		setLoggedOnInfo(lInfo, iwc);
		return true;
	}
	private boolean verifyPasswordAndLogin(IWContext iwc, String login, String password) throws Exception
	{
		boolean returner = false;
		LoginTable[] login_table =
			(LoginTable[]) (com.idega.core.accesscontrol.data.LoginTableBMPBean.getStaticInstance()).findAllByColumn(
				com.idega.core.accesscontrol.data.LoginTableBMPBean.getUserLoginColumnName(),
				login);
		if (login_table != null && login_table.length > 0)
		{
			if (Encrypter.verifyOneWayEncrypted(login_table[0].getUserPassword(), password))
			{
				returner = logIn(iwc, login_table[0], login);
			}
		}
		return returner;
	}
	public static boolean verifyPassword(User user, String login, String password) throws IOException, SQLException
	{
		boolean returner = false;
		LoginTable[] login_table =
			(LoginTable[]) (com.idega.core.accesscontrol.data.LoginTableBMPBean.getStaticInstance()).findAllByColumn(
				com.idega.core.accesscontrol.data.LoginTableBMPBean.getUserIDColumnName(),
				Integer.toString(user.getID()),
				com.idega.core.accesscontrol.data.LoginTableBMPBean.getUserLoginColumnName(),
				login);
		if (login_table != null && login_table.length > 0)
		{
			if (Encrypter.verifyOneWayEncrypted(login_table[0].getUserPassword(), password))
			{
				returner = true;
			}
		}
		return returner;
	}
	private void logOut(IWContext iwc) throws Exception
	{
		if (iwc.getSessionAttribute(LoginAttributeParameter) != null)
		{
			// this.getLoggedOnInfoList(iwc).remove(this.getLoggedOnInfo(iwc));
			List ll = this.getLoggedOnInfoList(iwc);
			int indexOfLoggedOfInfo = ll.indexOf(getLoggedOnInfo(iwc));
			if (indexOfLoggedOfInfo > 0)
			{
				LoggedOnInfo _logOnInfo = (LoggedOnInfo) ll.remove(indexOfLoggedOfInfo);
				LoginDBHandler.recordLogout(_logOnInfo.getLoginRecordId());
			}
			iwc.removeSessionAttribute(LoginAttributeParameter);
		}
	}
	/**

	 * returns empty List if no one is logged on

	 */
	public static List getLoggedOnInfoList(IWContext iwc)
	{
		List loggedOnList = (List) iwc.getApplicationAttribute(_APPADDRESS_LOGGED_ON_LIST);
		if (loggedOnList == null)
		{
			loggedOnList = new Vector();
			iwc.setApplicationAttribute(_APPADDRESS_LOGGED_ON_LIST, loggedOnList);
		}
		return loggedOnList;
	}
	public static LoggedOnInfo getLoggedOnInfo(IWUserContext iwc) throws NotLoggedOnException
	{
		return (LoggedOnInfo) getLoginAttribute(_LOGGINADDRESS_LOGGED_ON_INFO, iwc);
	}
	public static void setLoggedOnInfo(LoggedOnInfo lInfo, IWContext iwc) throws NotLoggedOnException
	{
		setLoginAttribute(_LOGGINADDRESS_LOGGED_ON_INFO, lInfo, iwc);
	}
	public static LoginContext changeUserPassword(User user, String password) throws Exception
	{
		LoginTable login = LoginDBHandler.getUserLogin(user.getID());
		LoginDBHandler.changePassword(login, password);
		LoginContext loginContext = new LoginContext(user, login.getUserLogin(), password);
		return loginContext;
	}
	public static LoginContext createNewUser(
		String fullName,
		String email,
		String preferredUserName,
		String preferredPassword)
	{
		UserBusiness ub = new UserBusiness();
		StringTokenizer tok = new StringTokenizer(fullName);
		String first = "";
		String middle = "";
		String last = "";
		if (tok.hasMoreTokens())
			first = tok.nextToken();
		if (tok.hasMoreTokens())
			middle = tok.nextToken();
		if (tok.hasMoreTokens())
			last = tok.nextToken();
		else
		{
			last = middle;
			middle = "";
		}
		LoginContext loginContext = null;
		try
		{
			User user = ub.insertUser(first, middle, last, "", null, null, null, null);
			String login = preferredUserName;
			String pass = preferredPassword;
			if (user != null)
			{
				if (email != null && email.length() > 0)
					ub.addNewUserEmail(user.getID(), email);
				if (login == null)
					login = LoginCreator.createLogin(user.getName());
				if (pass == null)
					pass = LoginCreator.createPasswd(8);
				LoginDBHandler.createLogin(user.getID(), login, pass);
				loginContext = new LoginContext(user, login, pass);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return loginContext;
	}

	// added for cookie login maybe unsafe ( Aron )
	protected boolean logInUnVerified(IWContext iwc,String login) throws Exception{
    boolean returner = false;
    LoginTable[] login_table = (LoginTable[]) (com.idega.core.accesscontrol.data.LoginTableBMPBean.getStaticInstance()).findAllByColumn(com.idega.core.accesscontrol.data.LoginTableBMPBean.getUserLoginColumnName(),login);
    if(login_table != null && login_table.length > 0){
        returner = logIn(iwc,login_table[0],login);
    }
    return returner;
  }

}
