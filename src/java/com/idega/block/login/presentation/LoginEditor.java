package com.idega.block.login.presentation;

/**
 * 
 * Title:
 * 
 * Description:
 * 
 * Copyright: Copyright (c) 2001
 * 
 * Company: idega multimedia
 * 
 * @author <a href="mailto:aron@idega.is">aron@idega.is</a>
 * 
 * @version 1.0
 * 
 */
import java.awt.Color;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.ejb.FinderException;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.accesscontrol.data.LoginTable;
import com.idega.core.user.data.User;
import com.idega.core.user.data.UserHome;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.PresentationObjectContainer;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.PasswordInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.util.IWColor;

public class LoginEditor extends PresentationObjectContainer {

	private User eUser = null;
	private String customMsg = "";
	private String errorMsg = "";
	public static String prmUserId = "user_id";
	protected String MiddleColor, LightColor, DarkColor, WhiteColor, TextFontColor, HeaderFontColor, IndexFontColor;
	protected int fontSize = 2;
	protected boolean fontBold = true;
	private boolean changeNextTime = false;
	protected String styleAttribute = "font-size: 8pt";
	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.login";
	protected IWResourceBundle iwrb;
	protected IWBundle iwb;

	private static final String BUNDEL_PRPERTY_NAME_USERNAME_CONSTANT = "cannot_change_username";

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	public LoginEditor() {
		this.LightColor = "#D7DADF";
		this.MiddleColor = "#9fA9B3";
		this.DarkColor = "#27334B";
		this.WhiteColor = "#FFFFFF";
		this.TextFontColor = "#000000";
		this.HeaderFontColor = this.DarkColor;
		this.IndexFontColor = "#000000";
	}

	public LoginEditor(int iUserId) {
		this();
		try {
			this.eUser = ((com.idega.core.user.data.UserHome) com.idega.data.IDOLookup.getHomeLegacy(User.class)).findByPrimaryKeyLegacy(iUserId);
		}
		catch (SQLException ex) {
			this.eUser = null;
		}
	}

	protected void control(IWContext iwc) {
		String sUserId = iwc.getParameter(prmUserId);
		// if(eUser == null)
		// eUser = LoginBusiness.getUser(iwc);
		/** Gimmi 04.11.2002 */
		if (sUserId == null) {
			this.eUser = iwc.getCurrentUser();
		}
		else {
			try {
				UserHome uHome = (UserHome) IDOLookup.getHome(User.class);
				this.eUser = uHome.findByPrimaryKey(new Integer(sUserId));
			}
			catch (RemoteException e) {
				e.printStackTrace(System.err);
			}
			catch (FinderException e) {
				e.printStackTrace(System.err);
			}
		}
		if (this.eUser != null) {
			String userlogin = null;
			if (iwc.getParameter("ok") != null || iwc.getParameter("ok.x") != null) {
				doAddTo(iwc, this.eUser.getID());
			}
			userlogin = getUsrLogin(this.eUser.getID());
			// add((iwrb.getLocalizedString("login","Login")));
			add(doView(this.eUser, userlogin));
		}
		else {
			this.errorMsg = this.iwrb.getLocalizedString("non_user", "Non-user");
		}
		add(getMsgText(this.errorMsg));
	}

	private Text getMsgText(String msg) {
		Text t = formatText(msg);
		t.setFontColor("#FF0000");
		return t;
	}

	protected PresentationObject makeLinkTable(int menuNr) {
		return new Text("");
	}

	private String getUsrLogin(int mbid) {
		String userLogin = getUserLogin(mbid);
		if (userLogin != null) {
			return userLogin;
		}
		else {
			return this.iwrb.getLocalizedString("has_no_login", "Has no login");
		}
	}

	private boolean doAddTo(IWContext iwc, int iUserId) {
		String sLogin = iwc.getParameter("ml.usrlgn");
		String sPasswd = iwc.getParameter("ml.psw1");
		String sConfirm = iwc.getParameter("ml.psw2");
		boolean register = false;
		if (sLogin != null && sPasswd != null && sConfirm != null) {
			if (sLogin.length() > 0 && sPasswd.length() > 0 && sConfirm.length() > 0) {
				try {
					register = registerMemberLogin(iUserId, sLogin, sPasswd, sConfirm);
				}
				catch (SQLException sql) {
					sql.printStackTrace();
					register = false;
					this.errorMsg = this.iwrb.getLocalizedString("database_trouble", "database_trouble");
				}
			}
			else {
				this.errorMsg = this.iwrb.getLocalizedString("empty_fields", "Empty fields");
			}
		}
		return register;
	}

	private PresentationObject doView(User user, String sUserLogin) {
		boolean allowChangingUsername = true;
		try {
			String prop = getPropertyValue(this.iwb, BUNDEL_PRPERTY_NAME_USERNAME_CONSTANT, Boolean.FALSE.toString());
			allowChangingUsername = !prop.trim().equalsIgnoreCase("true");
		}
		catch (NullPointerException e) {
			// no property exists, use default.
			// e.printStackTrace();
			allowChangingUsername = true;
		}

		// System.out.println("Creating view for changin password,
		// allowChangingUsername=" + allowChangingUsername);

		Form myForm = new Form();

		Table T = new Table();
		Text msgText = formatText(this.customMsg);
		msgText.setFontColor(IWColor.getHexColorString(Color.blue));
		T.add(msgText, 1, 1);
		T.add(formatText(user.getName()), 1, 2);
		PresentationObject tUsrLgn;
		if (allowChangingUsername) {
			tUsrLgn = new TextInput("ml.usrlgn", sUserLogin);
		}
		else {
			tUsrLgn = new Text(sUserLogin);
			HiddenInput hInput = new HiddenInput("ml.usrlgn", sUserLogin);
			myForm.add(hInput);
		}
		this.setStyle(tUsrLgn);
		PasswordInput psw1 = new PasswordInput("ml.psw1");
		this.setStyle(psw1);
		PasswordInput psw2 = new PasswordInput("ml.psw2");
		this.setStyle(psw2);
		T.add(formatText(this.iwrb.getLocalizedString("login", "Login") + ":"), 1, 3);
		T.add(tUsrLgn, 1, 4);
		T.add(formatText(this.iwrb.getLocalizedString("passwd", "Passwd") + ":"), 1, 5);
		T.add(psw1, 1, 6);
		T.add(formatText(this.iwrb.getLocalizedString("confirm", "Confirm") + ":"), 1, 7);
		T.add(psw2, 1, 8);
		SubmitButton ok = new SubmitButton(this.iwrb.getLocalizedImageButton("save", "Save"), "ok");
		CloseButton close = new CloseButton(this.iwrb.getLocalizedImageButton("close", "Close"));
		T.add(ok, 1, 9);
		T.add(Text.NON_BREAKING_SPACE, 1, 9);
		T.add(close, 1, 9);
		T.add(new HiddenInput(prmUserId, String.valueOf(user.getID())));
		if (!"".equals(this.customMsg)) {
			T.add(new HiddenInput("msg", this.customMsg));
		}
		if (this.changeNextTime) {
			T.add(new HiddenInput("chg", "true"));
		}
		myForm.add(T);
		return myForm;
	}

	public boolean registerMemberLogin(int iUserId, String sUserLogin, String sPasswd, String sConfirm) throws SQLException {
		boolean returner = false;
		if (sPasswd.equals(sConfirm)) {
			LoginTable logTable = LoginDBHandler.getUserLogin(iUserId);
			if (logTable == null) {
				try {
					if (sPasswd.equals(sConfirm)) {
						LoginDBHandler.createLogin(iUserId, sUserLogin, sPasswd);
						returner = true;
						this.errorMsg = this.iwrb.getLocalizedString("login_created", "Login created");
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
					returner = false;
					// errorMsg = iwrb.getLocalizedString("creation_failed","Failed");
					this.errorMsg = ex.getMessage();
				}
			}
			else if (logTable != null) {
				try {
					if (sPasswd.equals(sConfirm)) {
						LoginDBHandler.updateLogin(iUserId, sUserLogin, sPasswd);
						if (this.changeNextTime) {
							LoginDBHandler.changeNextTime(logTable, false);
						}

						returner = true;
						this.errorMsg = this.iwrb.getLocalizedString("updated", "Login updated");
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
					returner = false;
					// errorMsg = iwrb.getLocalizedString("update_failed","Update
					// Failed");
					this.errorMsg = ex.getMessage();
				}
			}
		}
		else {
			this.errorMsg = this.iwrb.getLocalizedString("wrong_confirm", "Confirm failed");
		}

		return returner;
	}

	public String getUserLogin(int iUserId) {
		LoginTable L = LoginDBHandler.getUserLogin(iUserId);
		if (L != null) {
			return L.getUserLogin();
		}
		else {
			return null;
		}
	}

	public Text formatText(String s) {
		Text T = new Text();
		if (s != null) {
			T = new Text(s);
			if (this.fontBold) {
				T.setBold();
			}
			T.setFontColor(this.TextFontColor);
			T.setFontSize(this.fontSize);
		}
		return T;
	}

	public Text formatText(int i) {
		return formatText(String.valueOf(i));
	}

	protected void setStyle(PresentationObject O) {
		O.setMarkupAttribute("style", this.styleAttribute);
	}

	public void main(IWContext iwc) {
		this.iwrb = getResourceBundle(iwc);
		this.iwb = getBundle(iwc);
		if (LoginBusinessBean.isLoggedOn(iwc)) {
			control(iwc);
		}
		else {
			add(this.iwrb.getLocalizedString("not logged on", "Not logged on"));
		}
	}

	public void setMessage(String msg) {
		this.customMsg = msg;
	}

	public void setChangeLoginNextTime(boolean change) {
		this.changeNextTime = change;
	}

	/**
	 * Gets the value for a property name ... replaces the bundle properties that
	 * were used previously
	 * 
	 * @param propertyName
	 * @return
	 */
	private String getPropertyValue(IWBundle iwb, String propertyName, String defaultValue) {
		IWMainApplicationSettings settings = getIWApplicationContext().getApplicationSettings();
		String value = settings.getProperty(propertyName);
		if (value != null) {
			return value;
		}
		value = iwb.getProperty(propertyName);
		value = value != null ? value : defaultValue;
		settings.setProperty(propertyName, value);
		return value;
	}

}