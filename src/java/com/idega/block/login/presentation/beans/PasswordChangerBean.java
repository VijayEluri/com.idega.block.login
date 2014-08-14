/**
 * @(#)PasswordChangerBean.java    1.0.0 2:27:15 PM
 *
 * Idega Software hf. Source Code Licence Agreement x
 *
 * This agreement, made this 10th of February 2006 by and between 
 * Idega Software hf., a business formed and operating under laws 
 * of Iceland, having its principal place of business in Reykjavik, 
 * Iceland, hereinafter after referred to as "Manufacturer" and Agura 
 * IT hereinafter referred to as "Licensee".
 * 1.  License Grant: Upon completion of this agreement, the source 
 *     code that may be made available according to the documentation for 
 *     a particular software product (Software) from Manufacturer 
 *     (Source Code) shall be provided to Licensee, provided that 
 *     (1) funds have been received for payment of the License for Software and 
 *     (2) the appropriate License has been purchased as stated in the 
 *     documentation for Software. As used in this License Agreement, 
 *     Licensee shall also mean the individual using or installing 
 *     the source code together with any individual or entity, including 
 *     but not limited to your employer, on whose behalf you are acting 
 *     in using or installing the Source Code. By completing this agreement, 
 *     Licensee agrees to be bound by the terms and conditions of this Source 
 *     Code License Agreement. This Source Code License Agreement shall 
 *     be an extension of the Software License Agreement for the associated 
 *     product. No additional amendment or modification shall be made 
 *     to this Agreement except in writing signed by Licensee and 
 *     Manufacturer. This Agreement is effective indefinitely and once
 *     completed, cannot be terminated. Manufacturer hereby grants to 
 *     Licensee a non-transferable, worldwide license during the term of 
 *     this Agreement to use the Source Code for the associated product 
 *     purchased. In the event the Software License Agreement to the 
 *     associated product is terminated; (1) Licensee's rights to use 
 *     the Source Code are revoked and (2) Licensee shall destroy all 
 *     copies of the Source Code including any Source Code used in 
 *     Licensee's applications.
 * 2.  License Limitations
 *     2.1 Licensee may not resell, rent, lease or distribute the 
 *         Source Code alone, it shall only be distributed as a 
 *         compiled component of an application.
 *     2.2 Licensee shall protect and keep secure all Source Code 
 *         provided by this this Source Code License Agreement. 
 *         All Source Code provided by this Agreement that is used 
 *         with an application that is distributed or accessible outside
 *         Licensee's organization (including use from the Internet), 
 *         must be protected to the extent that it cannot be easily 
 *         extracted or decompiled.
 *     2.3 The Licensee shall not resell, rent, lease or distribute 
 *         the products created from the Source Code in any way that 
 *         would compete with Idega Software.
 *     2.4 Manufacturer's copyright notices may not be removed from 
 *         the Source Code.
 *     2.5 All modifications on the source code by Licencee must 
 *         be submitted to or provided to Manufacturer.
 * 3.  Copyright: Manufacturer's source code is copyrighted and contains 
 *     proprietary information. Licensee shall not distribute or 
 *     reveal the Source Code to anyone other than the software 
 *     developers of Licensee's organization. Licensee may be held 
 *     legally responsible for any infringement of intellectual property 
 *     rights that is caused or encouraged by Licensee's failure to abide 
 *     by the terms of this Agreement. Licensee may make copies of the 
 *     Source Code provided the copyright and trademark notices are 
 *     reproduced in their entirety on the copy. Manufacturer reserves 
 *     all rights not specifically granted to Licensee.
 *
 * 4.  Warranty & Risks: Although efforts have been made to assure that the 
 *     Source Code is correct, reliable, date compliant, and technically 
 *     accurate, the Source Code is licensed to Licensee as is and without 
 *     warranties as to performance of merchantability, fitness for a 
 *     particular purpose or use, or any other warranties whether 
 *     expressed or implied. Licensee's organization and all users 
 *     of the source code assume all risks when using it. The manufacturers, 
 *     distributors and resellers of the Source Code shall not be liable 
 *     for any consequential, incidental, punitive or special damages 
 *     arising out of the use of or inability to use the source code or 
 *     the provision of or failure to provide support services, even if we 
 *     have been advised of the possibility of such damages. In any case, 
 *     the entire liability under any provision of this agreement shall be 
 *     limited to the greater of the amount actually paid by Licensee for the 
 *     Software or 5.00 USD. No returns will be provided for the associated 
 *     License that was purchased to become eligible to receive the Source 
 *     Code after Licensee receives the source code. 
 */
package com.idega.block.login.presentation.beans;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hsqldb.lib.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.login.business.LoginServices;
import com.idega.block.login.business.PasswordTokenBusiness;
import com.idega.block.login.business.PasswordValidator;
import com.idega.block.login.presentation.PasswordTokenCreator;
import com.idega.business.IBOLookup;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.builder.data.ICPage;
import com.idega.presentation.IWContext;
import com.idega.user.business.UserBusiness;
import com.idega.user.dao.UserDAO;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.expression.ELUtil;

/**
 * <p>JSF managed bean for changing a password</p>
 * <p>You can report about problems to: 
 * <a href="mailto:martynas@idega.is">Martynas Stakė</a></p>
 *
 * @version 1.0.0 Jan 17, 2014
 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
 */
public class PasswordChangerBean {

	private static final String PARAMETER_PASSWORD_CHANGED = "prm_password_changed";

	private String newPassword = null;

	private String retypedPassword = null;

	private String token = null;

	private Boolean changed = null;
	
	private boolean redirectToHomepage;
	private String validationError;
	
	@Autowired
	private LoginServices loginServices;

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getRetypedPassword() {
		return retypedPassword;
	}

	public void setRetypedPassword(String retypedPassword) {
		this.retypedPassword = retypedPassword;
	}

	public void setToken(String token) {
		if (!StringUtil.isEmpty(token)) {
			this.token = token;
		}
	}
	
	public String getToken() {
		String newToken = getIWContext().getParameter(
				PasswordTokenCreator.PARAMETER_TOKEN);
		if (!StringUtil.isEmpty(newToken)) {
			setToken(newToken);
		}

		return this.token;
	}

	public boolean isTokenValid() {
		return getPasswordTokenBusiness().isTokenValid(getToken());
	}

	public boolean isChanged() {
		if (this.changed == null) {
			String value = getIWContext().getParameter(PARAMETER_PASSWORD_CHANGED);
			if (Boolean.TRUE.toString().equals(value)) {
				this.changed = Boolean.TRUE;
			} else {
				this.changed = Boolean.FALSE;
			}
		}

		return this.changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public boolean isValidPassword(String password,String token){
		Collection<PasswordValidator> validators = getLoginServices().getPasswordValidators();
		for(PasswordValidator validator : validators){
			validationError = validator.getPasswordError(password, getIWContext(), getPasswordTokenBusiness().getUserByToken(token));
			if(validationError != null){
				return false;
			}
		}
		return true;
	}
	@SuppressWarnings("deprecation")
	public void submit() {
		String newPassword = getNewPassword();
		String token = getToken();
		if (!newPassword.equals(getRetypedPassword()) || !isValidPassword(newPassword,token)) {
			setChanged(Boolean.FALSE);
			return;
		}
		User user = getPasswordTokenBusiness().completePasswordReset(
				token, newPassword);
		boolean changed = user != null;
		setChanged(changed);
		if(changed && redirectToHomepage){
			try {
				UserDAO userDAO = ELUtil.getInstance().getBean("userDAO");
				com.idega.user.data.bean.User usr = userDAO.getUser(Integer.valueOf(user.getId()));
				LoginBusinessBean loginBean = LoginBusinessBean.getLoginBusinessBean(iwc);
				loginBean.logInUser(iwc, usr);
				UserBusiness userBusiness = IBOLookup.getServiceInstance(iwc, UserBusiness.class);
				
				String url;
				try{
					ICPage page = userBusiness.getHomePageForUser(user);
					url = CoreConstants.PAGES_URI_PREFIX + page.getDefaultPageURI();
				}catch (Exception e) {
					url = "/pages/";
				}
				getIWContext().getExternalContext().redirect(url);
			} catch (Exception e) {
				getLogger().log(Level.WARNING, "Failed redirecting user to homepage", e);
			}
			return;
		}
	}

	private Logger getLogger(){
		return Logger.getLogger(PasswordChangerBean.class.getName());
	}
	
	private IWContext iwc = null;

	protected IWContext getIWContext() {
		if (this.iwc == null) {
			this.iwc = CoreUtil.getIWContext();
		}

		return this.iwc;
	}

	@Autowired
	private PasswordTokenBusiness passwordTokenBusiness;

	protected PasswordTokenBusiness getPasswordTokenBusiness() {
		if (this.passwordTokenBusiness == null) {
			ELUtil.getInstance().autowire(this);
		}

		return this.passwordTokenBusiness;
	}

	public boolean isRedirectToHomepage() {
		return redirectToHomepage;
	}

	public void setRedirectToHomepage(boolean redirectToHomepage) {
		this.redirectToHomepage = redirectToHomepage;
	}

	private LoginServices getLoginServices() {
		if(loginServices != null){
			return loginServices;
		}
		ELUtil.getInstance().autowire(this);
		return loginServices;
	}

	public String getMainError() {
		return validationError;
	}

	public void setMainError(String mainError) {
		this.validationError = mainError;
	}
}
