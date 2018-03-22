/*     */package nc.bs.hrss.wa.util;

/*     */
/*     */import java.util.List;
/*     */
import java.util.Map;
/*     */
import nc.bs.framework.common.NCLocator;
/*     */
import nc.bs.hrss.pub.Logger;
/*     */
import nc.bs.hrss.pub.ServiceLocator;
/*     */
import nc.bs.hrss.pub.exception.HrssException;
/*     */
import nc.bs.hrss.pub.tool.CommonUtil;
/*     */
import nc.bs.hrss.pub.tool.SessionUtil;
import nc.bs.pub.mobile.WirelessManager;
/*     */
import nc.hr.utils.ResHelper;
/*     */
import nc.itf.hi.IPsndocQryService;
/*     */
import nc.itf.hrss.pub.profile.IProfileService;
/*     */
import nc.itf.uap.pf.IPFConfig;
/*     */
import nc.mail.MailSender;
/*     */
import nc.message.config.MailConfigAccessor;
/*     */
import nc.vo.hi.psndoc.PsndocAggVO;
/*     */
import nc.vo.hi.psndoc.PsndocVO;
/*     */
import nc.vo.hr.notice.MailconfigVO;
/*     */
import nc.vo.jcom.lang.StringUtil;
/*     */
import nc.vo.ml.AbstractNCLangRes;
/*     */
import nc.vo.ml.NCLangRes4VoTransl;
/*     */
import nc.vo.pub.BusinessException;
/*     */
import nc.vo.pub.msg.DefaultSMTP;
/*     */
import nc.vo.pub.msg.SysMessageParam;
/*     */
import nc.vo.wa.pub.PeriodStateVO;
/*     */
import nc.vo.wa.pub.WaLoginContext;
/*     */
import nc.vo.wa.pub.WaLoginVO;
/*     */
import org.apache.commons.lang.StringUtils;

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */public class WaUtil
/*     */{
	/*     */public WaUtil() {
	}

	/*     */
	/*     */public static final WaLoginContext createContext(String pk_group,
			String pk_org, String pk_wa_class, String cyear, String cperiod)
	/*     */{
		/* 47 */WaLoginContext context = new WaLoginContext();
		/* 48 */WaLoginVO waLoginVO = new WaLoginVO();
		/* 49 */waLoginVO.setPk_group(pk_group);
		/* 50 */waLoginVO.setPk_org(pk_org);
		/* 51 */waLoginVO.setPk_wa_class(pk_wa_class);
		/* 52 */waLoginVO.setCyear(cyear);
		/* 53 */waLoginVO.setCperiod(cperiod);
		/* 54 */PeriodStateVO periodStateVO = new PeriodStateVO();
		/* 55 */periodStateVO.setCyear(cyear);
		/* 56 */periodStateVO.setCperiod(cperiod);
		/* 57 */waLoginVO.setPeriodVO(periodStateVO);
		/* 58 */context.setWaLoginVO(waLoginVO);
		/* 59 */return context;
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */public static final String getPsnEmail(String pk_psnjob)
	/*     */throws BusinessException
	/*     */{
		/* 71 */String email = null;
		/* 72 */IPsndocQryService service = (IPsndocQryService) NCLocator
				.getInstance().lookup(IPsndocQryService.class);
		/* 73 */PsndocAggVO[] psnVos = service
				.queryPsndocVOByPks(new String[] { pk_psnjob });
		/* 74 */if ((psnVos != null) && (psnVos.length > 0)) {
			/* 75 */email = psnVos[0].getParentVO().getSecret_email();
			/* 76 */if (StringUtil.isEmptyWithTrim(email))
				/* 77 */email = psnVos[0].getParentVO().getEmail();
			/*     */}
		/* 79 */if (StringUtil.isEmptyWithTrim(email)) {
			/* 80 */throw new BusinessException(NCLangRes4VoTransl
					.getNCLangRes().getStrByID("c_wa-res", "0c_wa-res0101"));
			/*     */}
		/*     */
		/*     */
		/*     */
		/* 85 */return email;
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */public static final String getPsnEmail4Mobile(String pk_psnjob)
	/*     */throws BusinessException
	/*     */{
		/* 97 */String email = null;
		/* 98 */IPsndocQryService service = (IPsndocQryService) NCLocator
				.getInstance().lookup(IPsndocQryService.class);
		/* 99 */PsndocAggVO[] psnVos = service
				.queryPsndocVOByPks(new String[] { pk_psnjob });
		/* 100 */if ((psnVos != null) && (psnVos.length > 0)) {
			/* 101 */email = psnVos[0].getParentVO().getSecret_email();
			/* 102 */if (StringUtil.isEmptyWithTrim(email)) {
				/* 103 */email = psnVos[0].getParentVO().getEmail();
				/*     */}
			/*     */}
		/* 106 */return email;
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */public static final String sendMail(String title, String msg,
			String email)
	/*     */throws BusinessException
	/*     */{
		/* 121 */String strHost = MailConfigAccessor.getSMTPServer();
		/* 122 */String strUser = MailConfigAccessor.getUser();
		/* 123 */String strPass = MailConfigAccessor.getPassword();
		/* 124 */String from = MailConfigAccessor.getMailFrom();
		/* 125 */boolean bAuth = MailConfigAccessor.isAuthen();
		/* 126 */if (StringUtils.isEmpty(strHost)) {
			/* 127 */throw new BusinessException(ResHelper.getString(
					"6001msgtmp", "16001msgtmp0005"));
			/*     */}
		/*     */
		/* 130 */if (StringUtils.isEmpty(strUser))
		/*     */{
			/*     */
			/* 133 */CommonUtil.showMessageDialog("∑¢ÀÕ ß∞‹",
					ResHelper.getString("6001msgtmp", "16001msgtmp0006"));
			/*     */}
		/* 135 */MailSender sender = new MailSender(strHost, strUser, strPass,
				bAuth);
		/*     */try {
			/* 137 */sender.sendMail(from, new String[] { email }, null, title,
					msg, null);
			/* 138 */return "SENDMAILSUCCESS!";
			/*     */}
		/*     */catch (Exception e2) {
			/* 141 */Logger.error(e2.getMessage(), e2);
			/*     */}
		/* 143 */return null;
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */public static final void sendMailUseUAP(String title, String msg,
			String email)
	/*     */throws Exception
	/*     */{
		/* 157 */MailconfigVO mailConfigVO = getMailConfig(null);
		/* 158 */getMailSender().sendMail(mailConfigVO.getVsourceaddress(),
				new String[] { email }, null, title, msg, null);
		/*     */}

	/*     */
	/*     */private static MailSender getMailSender() throws Exception {
		/* 162 */String strHost = MailConfigAccessor.getSMTPServer();
		/* 163 */String strUser = MailConfigAccessor.getUser();
		/* 164 */String strPass = MailConfigAccessor.getPassword();
		/* 165 */boolean bAuth = MailConfigAccessor.isAuthen();
		/* 166 */if (StringUtils.isEmpty(strHost)) {
			/* 167 */throw new BusinessException(ResHelper.getString(
					"6001msgtmp", "16001msgtmp0005"));
			/*     */}
		/*     */
		/* 170 */if (StringUtils.isEmpty(strUser)) {
			/* 171 */throw new BusinessException(ResHelper.getString(
					"6001msgtmp", "16001msgtmp0006"));
			/*     */}
		/*     */
		/* 174 */return new MailSender(strHost, strUser, strPass, bAuth);
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */public static final MailconfigVO getMailConfig(String senderEmail)
	/*     */throws BusinessException
	/*     */{
		/* 187 */IPFConfig iPfConfig = (IPFConfig) NCLocator.getInstance()
				.lookup(IPFConfig.class);
		/*     */
		WirelessManager wm = new WirelessManager();

		/* 189 */SysMessageParam smp = iPfConfig.getSysMsgParam();
		/*     */
		/* 191 */String vsourceaddress = StringUtil
				.isEmptyWithTrim(senderEmail) ? smp.getSmtp().getSender()
				: senderEmail;
		/*     */
		/* 193 */MailconfigVO mailConfigVO = new MailconfigVO();
		/*     */
		/* 195 */mailConfigVO.setVsmtp(smp.getSmtp().getSmtp());
		/*     */
		/* 197 */mailConfigVO.setCuser(smp.getSmtp().getUser());
		/*     */
		/* 199 */mailConfigVO.setCpassword(smp.getSmtp().getPassword());
		/*     */
		/* 201 */mailConfigVO.setVsourceaddress(vsourceaddress);
		/* 202 */return mailConfigVO;
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */public static boolean resetWageQueryPassword(String userid)
	/*     */{
		/* 211 */boolean isSuccess = false;
		/*     */try {
			/* 213 */IProfileService service = (IProfileService) ServiceLocator
					.lookup(IProfileService.class);
			/* 214 */List<Map<String, String>> result = service
					.resetSalaryPswd(SessionUtil.getPk_group(), userid);
			/* 215 */String flag = (String) ((Map) result.get(0)).get("flag");
			/* 216 */String desc = (String) ((Map) result.get(0)).get("des");
			/* 217 */isSuccess = "0".equals(flag);
			/* 218 */if (isSuccess) {
				/* 219 */CommonUtil.showMessageDialog(desc);
				/*     */} else {
				/* 221 */CommonUtil.showErrorDialog(
						ResHelper.getString("c_pub-res", "0c_pub-res0193"),
						desc);
				/*     */}
			/*     */
			/*     */}
		/*     */catch (BusinessException e)
		/*     */{
			/* 227 */new HrssException(e).deal();
			/*     */} catch (HrssException e) {
			/* 229 */e.deal();
			/*     */}
		/* 231 */return isSuccess;
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */public static void modifyWageQueryPassword(String userid,
			String oldPwd, String newPwd)
	/*     */{
		/*     */try
		/*     */{
			/* 243 */IProfileService service = (IProfileService) ServiceLocator
					.lookup(IProfileService.class);
			/* 244 */List<Map<String, String>> result = service
					.modifySalaryPswd(SessionUtil.getPk_group(), userid,
							oldPwd, newPwd);
			/*     */
			/* 246 */String flag = (String) ((Map) result.get(0)).get("flag");
			/* 247 */String desc = (String) ((Map) result.get(0)).get("des");
			/* 248 */boolean isSuccess = "0".equals(flag);
			/* 249 */if (isSuccess) {
				/* 250 */CommonUtil.showMessageDialog(desc);
				/*     */} else {
				/* 252 */CommonUtil.showErrorDialog(
						ResHelper.getString("c_pub-res", "0c_pub-res0192"),
						desc);
				/*     */}
			/*     */
			/*     */}
		/*     */catch (BusinessException e)
		/*     */{
			/* 258 */new HrssException(e).deal();
			/*     */} catch (HrssException e) {
			/* 260 */e.deal();
			/*     */}
		/*     */}
	/*     */
}

/*
 * Location: D:\NC\Metro\NC633GOLD_metro\modules\hrss\classes Qualified Name:
 * nc.bs.hrss.wa.util.WaUtil Java Class Version: 6 (50.0) JD-Core Version:
 * 0.7.0.1
 */