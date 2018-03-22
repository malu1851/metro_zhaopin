package nc.bs.hrss.society.loginindex.ctrl;

import nc.bs.dao.BaseDAO;
import nc.bs.hrss.pub.ServiceLocator;
import nc.bs.hrss.pub.cmd.CloseViewCmd;
import nc.bs.hrss.pub.exception.HrssException;
import nc.bs.hrss.pub.tool.CommonUtil;
import nc.bs.hrss.pub.tool.SessionUtil;
import nc.hr.utils.ResHelper;
import nc.itf.hrss.rmweb.rmlogin.IRMLoginService;
import nc.uap.lfw.core.AppInteractionUtil;
import nc.uap.lfw.core.cmd.CmdInvoker;
import nc.uap.lfw.core.ctrl.IController;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.ctx.ViewContext;
import nc.uap.lfw.core.data.Dataset;
import nc.uap.lfw.core.data.Row;
import nc.uap.lfw.core.event.DataLoadEvent;
import nc.uap.lfw.core.event.DialogEvent;
import nc.uap.lfw.core.event.MouseEvent;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.ViewModels;
import nc.uap.lfw.core.util.AppDynamicCompUtil;
import nc.vo.hrss.pub.SessionBean;
import nc.vo.hrss.pub.rmweb.RmUserVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;

public class LoginSociPasswordView implements IController {
	private static final long serialVersionUID = 1L;

	public void onSubmit(MouseEvent mouseEvent) {
		Dataset ds = AppLifeCycleContext.current().getViewContext().getView()
				.getViewModels().getDataset("password");
		Row row = ds.getSelectedRow();
		String oldpassword = (String) row.getValue(ds
				.nameToIndex("oldpassword"));
		String newpassword = (String) row.getValue(ds
				.nameToIndex("newpassword"));
		String newpassword2 = (String) row.getValue(ds
				.nameToIndex("newpassword2"));
		if (StringUtil.isEmptyWithTrim(oldpassword)) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_pub-res", "0c_pub-res0169"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0027"));
		}

		if (StringUtil.isEmptyWithTrim(newpassword)) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_pub-res", "0c_pub-res0169"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0012"));
		}

		if (newpassword.length() < 6) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_pub-res", "0c_pub-res0169"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0023"));
		}

		if (!newpassword.equals(newpassword2)) {
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_pub-res", "0c_pub-res0169"),
					NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res",
							"0c_hrss-res0024"));
		}

		SessionBean session = SessionUtil.getRMWebSessionBean();
		String idcard = session.getRmUserVO().getIdcard();
		RmUserVO rmUserVO = null;
		try {
			rmUserVO = ((IRMLoginService) ServiceLocator
					.lookup(IRMLoginService.class)).loginRMWeb(idcard,
					oldpassword);

			if (rmUserVO == null) {
				CommonUtil.showErrorDialog(
						ResHelper.getString("c_pub-res", "0c_pub-res0169"),
						NCLangRes4VoTransl.getNCLangRes().getStrByID(
								"c_hrss-res", "0c_hrss-res0028"));
			} else {
				rmUserVO.setPassword(newpassword);

				((IRMLoginService) ServiceLocator.lookup(IRMLoginService.class))
						.updateVO(rmUserVO);

			}
		} catch (BusinessException e) {
			new HrssException(e).deal();
		} catch (HrssException e) {
			new HrssException(e).alert();
		}
		AppInteractionUtil.showShortMessage(NCLangRes4VoTransl.getNCLangRes()
				.getStrByID("c_hrss-res", "0c_hrss-res0029"));
		CmdInvoker.invoke(new CloseViewCmd("password"));
	}

	public void onCancel(MouseEvent mouseEvent) {
		CmdInvoker.invoke(new CloseViewCmd("password"));
	}

	public void onDataLoad(DataLoadEvent dataLoadEvent) {
		Dataset ds = (Dataset) dataLoadEvent.getSource();
		Row row = ds.getEmptyRow();
		ds.setCurrentKey("MASTER_KEY");
		ds.addRow(row);
		ds.setRowSelectIndex(Integer.valueOf(0));
		ds.setEnabled(true);
	}

	public void beforeShow(DialogEvent dialogEvent) {
		LfwView viewMain = AppLifeCycleContext.current().getViewContext()
				.getView();
		Dataset ds = viewMain.getViewModels().getDataset("password");
		String currentKey = ds.getCurrentKey();

		if (!StringUtil.isEmptyWithTrim(currentKey)) {
			new AppDynamicCompUtil(AppLifeCycleContext.current()
					.getApplicationContext(), AppLifeCycleContext.current()
					.getViewContext()).refreshDataset(ds);
		}
	}
}