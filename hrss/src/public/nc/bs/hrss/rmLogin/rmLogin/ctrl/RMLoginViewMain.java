package nc.bs.hrss.rmLogin.rmLogin.ctrl;

import java.util.Random;
import javax.mail.AuthenticationFailedException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;

import nc.bs.framework.common.NCLocator;
import nc.bs.hrss.postApply.schl.ctrl.SchlRmPostViewList;
import nc.bs.hrss.pub.ServiceLocator;
import nc.bs.hrss.pub.exception.HrssException;
import nc.bs.hrss.pub.tool.CommonUtil;
import nc.bs.hrss.pub.tool.DatasetUtil;
import nc.bs.hrss.pub.tool.SessionUtil;
import nc.bs.hrss.wa.util.WaUtil;
import nc.hr.utils.ResHelper;
import nc.impl.hrss.rmweb.rmlogin.RMLoginServiceImpl;
import nc.itf.hrss.rmweb.rmlogin.IRMLoginService;
import nc.itf.rm.IPublishQueryService;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.WebContext;
import nc.uap.lfw.core.comp.ButtonComp;
import nc.uap.lfw.core.comp.LabelComp;
import nc.uap.lfw.core.comp.text.TextComp;
import nc.uap.lfw.core.ctrl.IController;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.ctx.ApplicationContext;
import nc.uap.lfw.core.data.Dataset;
import nc.uap.lfw.core.event.DataLoadEvent;
import nc.uap.lfw.core.event.LinkEvent;
import nc.uap.lfw.core.event.MouseEvent;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.LfwWindow;
import nc.uap.lfw.core.page.ViewComponents;
import nc.vo.hrss.pub.SessionBean;
import nc.vo.hrss.pub.rmweb.RmUserVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
import nc.vo.rm.publish.AggPublishVO;
import nc.vo.rm.publish.PublishJobVO;
import org.apache.commons.lang.StringUtils;

import uap.web.bd.pub.AppUtil;

public class RMLoginViewMain implements IController {
	private static final long serialVersionUID = 1L;

	public void onBack(MouseEvent<ButtonComp> mouseEvent) {
		String returnAppId = LfwRuntimeEnvironment.getWebContext()
				.getOriginalParameter("returnAppId");
		String appId = "/app/" + returnAppId;
		ApplicationContext appCtx = AppLifeCycleContext.current()
				.getApplicationContext();
		String url = LfwRuntimeEnvironment.getRootPath() + appId;
		appCtx.sendRedirect(url);
	}
	
	/***
	 * 登录界面注册按钮跳转
	 * 添加appid
	 * 马鹏鹏
	 * 
	 */
	public void onRegist(LinkEvent linkEvent) {
//		String pk_job = (String) LfwRuntimeEnvironment.getWebContext()
//				.getPageMeta().getExtendAttributeValue("pk_job");
//		sendRedirectLogin("/app/RMWebRegistApp?pk_job=" + pk_job);
		
		String appId = "/app/RMWebRegistApp?returnAppId=" + AppUtil.getCntAppCtx().getAppId();
		ApplicationContext appCtx = AppLifeCycleContext.current().getApplicationContext();
	    String url = LfwRuntimeEnvironment.getRootPath() + appId;
	    appCtx.sendRedirect(url);
	}

	public void onForgetPassword(MouseEvent<?> mouseEvent) {
		String str = "密码已重置，请到注册邮箱获取！";
		LfwView widget = LfwRuntimeEnvironment.getWebContext().getPageMeta()
				.getView("main");

		LabelComp labelcomp = (LabelComp) widget.getViewComponents()
				.getComponent("lblInfo");
		labelcomp.setText("");

		TextComp useridComp = (TextComp) widget.getViewComponents()
				.getComponent("userid");
		String userid = useridComp.getValue();

		if (StringUtil.isEmptyWithTrim(userid)) {
			labelcomp.setColor("red");
			labelcomp.setText("身份证不能为空！");
			return;
		}
		UFDouble checkidcard = null;
		try {
			checkidcard = ((IRMLoginService) ServiceLocator
					.lookup(IRMLoginService.class)).checkIdCard(userid);

		} catch (BusinessException e) {
			new HrssException(e).deal();
		} catch (HrssException e) {
			e.alert();
		}

		if ((checkidcard != null) && (checkidcard.intValue() == 0)) {
			labelcomp.setColor("red");
			labelcomp.setText("您输入的用户名不存在，请重新输入");
			return;
		}

		String passwdInfo = "亲爱的用户:" + userid + ",您好！您的密码已重置为：";
		String passwd = getRandomPwd(6, "0123456789");
		passwdInfo = passwdInfo + passwd;

		RmUserVO rmUserVO = null;

		try {
			IRMLoginService isw =(IRMLoginService)NCLocator.getInstance().lookup(IRMLoginService.class.getName());
			//IRMLoginService isw = new RMLoginServiceImpl();
			rmUserVO = isw.qryRmUserVOByIdcard(userid);

		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			WaUtil.sendMailUseUAP("密码重置", passwdInfo, rmUserVO.getEmail());
			labelcomp.setColor("red");

			labelcomp.setText(str);

			try {

				rmUserVO.setAttributeValue("password", passwd);

				((IRMLoginService) ServiceLocator.lookup(IRMLoginService.class))
						.updateVO(rmUserVO);
			} catch (HrssException e) {
				e.alert();
			}
		} catch (SendFailedException e1) {
			labelcomp.setColor("red");
			labelcomp.setText("您输入的邮箱格式不正确");
		} catch (NullPointerException e2) {
			labelcomp.setColor("red");
			labelcomp.setText("未配置邮件发送服务器");
		} catch (AddressException e) {
			labelcomp.setColor("red");
			labelcomp.setText("您输入的邮箱格式不正确");
		} catch (AuthenticationFailedException e) {
			labelcomp.setColor("red");
			labelcomp.setText("邮件发送服务器配置错误");
		} catch (Exception e) {
			new HrssException(e).deal();
		}
	}

	public void doSubmit(MouseEvent<?> mouseEvent) {
		LfwView widget = LfwRuntimeEnvironment.getWebContext().getPageMeta()
				.getView("main");
		TextComp useridComp = (TextComp) widget.getViewComponents()
				.getComponent("userid");
		String userid = useridComp.getValue();
		TextComp passwordComp = (TextComp) widget.getViewComponents()
				.getComponent("password");
		String password = passwordComp.getValue();
		if (StringUtil.isEmptyWithTrim(userid)) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0047"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0011"));
		}

		if (StringUtil.isEmptyWithTrim(password)) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0047"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0012"));
		}

		RmUserVO rmUserVO = null;
		try {
			rmUserVO = ((IRMLoginService) ServiceLocator
					.lookup(IRMLoginService.class))
					.loginRMWeb(userid, password);
		} catch (HrssException e) {
			e.alert();
		} catch (BusinessException e) {
			new HrssException(e).deal();
		}
		if (rmUserVO == null) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0047"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0013"));
		} else {
			afterLogin(rmUserVO);
		}
	}

	public static void afterLogin(RmUserVO rmUserVO) {
		SessionBean bean = new SessionBean();
		bean.setRmUserVO(rmUserVO);
		SessionUtil.setRMWebSessionBean(bean);
		String pk_job = (String) LfwRuntimeEnvironment.getWebContext()
				.getPageMeta().getExtendAttributeValue("pk_job");
		if (StringUtil.isEmptyWithTrim(rmUserVO.getHrrmpsndoc())) {
			sendRedirectLogin("/app/RMWebLoginSociIndexApp?pk_job=" + pk_job);
		} else {
			if (StringUtils.isNotEmpty(pk_job)) {
				insertRMPsnJobVO(rmUserVO.getHrrmpsndoc(), pk_job);
				LfwRuntimeEnvironment.getWebContext().getPageMeta()
						.removeExtendAttribute("pk_job");
			}
			sendRedirectLogin("/app/RMWebLoginSociIndexApp");
		}
	}

	public static void insertRMPsnJobVO(String pk_psndoc, String pk_reg_job) {
		PublishJobVO publishJobVO = null;
		try {
			publishJobVO = (PublishJobVO) ((IPublishQueryService) ServiceLocator
					.lookup(IPublishQueryService.class)).queryByPk(pk_reg_job)
					.getParentVO();
		} catch (HrssException e) {
			e.alert();
		} catch (BusinessException e) {
			new HrssException(e).deal();
		}
		SchlRmPostViewList.doApprovePost(pk_reg_job,
				publishJobVO.getPk_group(), publishJobVO.getPk_org(),
				publishJobVO.getPk_rmorg(), publishJobVO.getPk_rmdept());
	}

	private static void sendRedirectLogin(String app) {
		ApplicationContext appCtx = AppLifeCycleContext.current()
				.getApplicationContext();
		String url = LfwRuntimeEnvironment.getRootPath() + app;
		appCtx.sendRedirect(url);
	}

	public void onDatasetLoad_dsLogin(DataLoadEvent dataLoadEvent) {
		Dataset ds = (Dataset) dataLoadEvent.getSource();
		DatasetUtil.initWithEmptyRow(ds, 0);
	}

	public String getRandomPwd(int len, String availableChars) {
		char[] ac = availableChars.toCharArray();
		char[] pwd = new char[len];
		Random random = new Random();
		for (int i = 0; i < pwd.length; i++) {
			pwd[i] = ac[random.nextInt(ac.length)];
		}
		return new String(pwd);
	}
}