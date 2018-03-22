package nc.bs.hrss.rmLogin.rmRegist.ctrl;

import java.util.HashMap;

import nc.bs.dao.BaseDAO;
import nc.bs.hrss.pub.ServiceLocator;
import nc.bs.hrss.pub.cmd.AddCmd;
import nc.bs.hrss.pub.exception.HrssException;
import nc.bs.hrss.pub.tool.CommonUtil;
import nc.bs.hrss.pub.tool.DatasetUtil;
import nc.bs.hrss.resume.schlResume.cmd.RmWebResumeSaveCmd;
import nc.bs.hrss.resume.schlResume.lsnr.SchlResumeAddProcessor;
import nc.bs.hrss.rmLogin.rmLogin.ctrl.RMLoginViewMain;
import nc.hr.utils.ResHelper;
import nc.itf.hrss.rmweb.rmlogin.IRMLoginService;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.WebContext;
import nc.uap.lfw.core.cmd.CmdInvoker;
import nc.uap.lfw.core.comp.ButtonComp;
import nc.uap.lfw.core.ctrl.IController;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.ctx.ApplicationContext;
import nc.uap.lfw.core.data.Dataset;
import nc.uap.lfw.core.data.Row;
import nc.uap.lfw.core.event.DataLoadEvent;
import nc.uap.lfw.core.event.MouseEvent;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.LfwWindow;
import nc.uap.lfw.core.page.ViewModels;
import nc.uap.lfw.core.serializer.impl.Dataset2SuperVOSerializer;
import nc.vo.hrss.pub.rmweb.RmUserVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDouble;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMPsndocVO;

public class RMRegistViewMain implements IController {
	private static final long serialVersionUID = 1L;
	public static final String PAGE_REGIST_WIDGET = "regist";
	public static final String DS_USER = "dsUser";
	public static final String FD_EMAIL = "email";
	public static final String FD_NAME = "name";
	public static final String FD_IDCARD = "idcard";
	public static final String FD_PASSWORD = "password";
	public static final String FD_PASSWORD2 = "password2";

	public void onBack(MouseEvent<ButtonComp> mouseEvent) {
		String returnAppId = LfwRuntimeEnvironment.getWebContext()
				.getOriginalParameter("returnAppId");
		String appId = "/app/" + returnAppId;
		ApplicationContext appCtx = AppLifeCycleContext.current()
				.getApplicationContext();
		String url = LfwRuntimeEnvironment.getRootPath() + appId;
		appCtx.sendRedirect(url);
	}

	public void onCancel(MouseEvent mouseEvent) {
	}

	public void onSubmit(MouseEvent mouseEvent) {
		LfwView wdtMain = LfwRuntimeEnvironment.getWebContext().getPageMeta()
				.getView("main");
		Dataset dsRegist = wdtMain.getViewModels().getDataset("dsUser");
		Row row = dsRegist.getSelectedRow();

		dsRegist.setVoMeta(RmUserVO.class.getName());
		SuperVO[] vos = new Dataset2SuperVOSerializer()
				.serialize(dsRegist, row);
		RmUserVO vo = (RmUserVO) vos[0];
		HashMap value = DatasetUtil.getValueMap(dsRegist);
		String password2 = (String) value.get("password2");
		String mobile = (String)value.get("mobile");
		vo.setPassword2(password2);
		checkRmUserVO(vo,mobile);
		try {
			IRMLoginService irmLoginService = (IRMLoginService) ServiceLocator
					.lookup(IRMLoginService.class);
			irmLoginService.insertVO(vo);
			RMLoginViewMain.afterLogin(vo);		
		} catch (BusinessException e) {
			new HrssException(e).deal();
		} catch (HrssException e) {
			e.alert();
		}
	}

	public void checkRmUserVO(RmUserVO vo,String mobile) {
		if (StringUtil.isEmptyWithTrim(vo.getEmail())) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0048"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0015"));
		}

		String regexEmail = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";

		if (!vo.getEmail().matches(regexEmail)) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0048"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0016"));
		}

		if (StringUtil.isEmptyWithTrim(vo.getName())) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0048"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0017"));
		}

		if (StringUtil.isEmptyWithTrim(vo.getIdcard())) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0048"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0018"));
		}
		// 增加身份证的18位的正则表达式校验
		String regexIdcard = "^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";

		if (!vo.getIdcard().matches(regexIdcard)) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0048"),
					"不符合18身份证规则");
		}
		//增加电话校验
		if (StringUtil.isEmptyWithTrim(mobile)) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0048"),
					"电话号码不能为空");
		}
		
		String regexMoblie = "^0?(13[0-9]|15[012356789]|17[013678]|18[0-9]|14[57])[0-9]{8}$";
		if (!mobile.matches(regexMoblie)) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0048"),
					"不符合11手机号规则");
		}		
		try {
			IRMLoginService irmLoginService = (IRMLoginService) ServiceLocator
					.lookup(IRMLoginService.class);
			if (!UFDouble.ZERO_DBL.equals(irmLoginService.checkEMail(vo
					.getEmail()))) {
				CommonUtil.showErrorDialog(
						ResHelper.getString("c_hrss-res", "0c_hrss-res0048"),
						NCLangRes4VoTransl.getNCLangRes().getStrByID(
								"c_hrss-res", "0c_hrss-res0020"));
			}

		} catch (BusinessException e) {
			new HrssException(e).deal();
		} catch (HrssException e) {
			e.alert();
		}

		// 增加身份证的重复校验
		try {
			IRMLoginService irmLoginService = (IRMLoginService) ServiceLocator
					.lookup(IRMLoginService.class);
			if (!UFDouble.ZERO_DBL.equals(irmLoginService.checkIdCard(vo
					.getIdcard()))) {
				CommonUtil.showErrorDialog(
						ResHelper.getString("c_hrss-res", "0c_hrss-res0048"),
						"此身份证已注册");
			}

		} catch (BusinessException e) {
			new HrssException(e).deal();
		} catch (HrssException e) {
			e.alert();
		}

		if (StringUtil.isEmptyWithTrim(vo.getPassword())) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0048"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0012"));
		}

		if (StringUtil.isEmptyWithTrim(vo.getPassword2())) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0048"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0022"));
		}

		if (vo.getPassword().length() < 6) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0048"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0023"));
		}

		if (!vo.getPassword().equals(vo.getPassword2()))
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0048"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0024"));
	}

	public void onDatasetLoad_dsRegist(DataLoadEvent dataLoadEvent) {
		Dataset ds = (Dataset) dataLoadEvent.getSource();
		DatasetUtil.initWithEmptyRow(ds, 0);
	}
}